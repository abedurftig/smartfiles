package dev.arne.smartfiles.core.service;

import dev.arne.smartfiles.core.FileService;
import dev.arne.smartfiles.core.events.ArchiveEntryAddedEvent;
import dev.arne.smartfiles.core.events.DocumentTagAddedEvent;
import dev.arne.smartfiles.core.model.Archive;
import dev.arne.smartfiles.core.model.ArchiveEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArchiveServiceImplTest {

    @TempDir
    Path tempDir;

    @Mock
    private FileService fileService;

    @Mock
    private ApplicationEventPublisher publisher;

    private Archive archive;
    private ArchiveServiceImpl archiveService;

    @BeforeEach
    void setUp() {
        archive = Archive.empty();
        when(fileService.loadArchive()).thenReturn(archive);
        lenient().when(fileService.getTenantDirectory()).thenReturn(tempDir.toString());

        archiveService = new ArchiveServiceImpl(fileService, publisher);
    }

    @Test
    void constructor_loadsArchiveFromFileService() {
        verify(fileService).loadArchive();
    }

    @Test
    void getAll_returnsAllEntries() {
        archive.addArchiveEntryFromFile(new File("/tmp/doc1.pdf"), "/orig/doc1.pdf");
        archive.addArchiveEntryFromFile(new File("/tmp/doc2.pdf"), "/orig/doc2.pdf");

        var entries = archiveService.getAll();

        assertEquals(2, entries.size());
    }

    @Test
    void getAll_whenEmpty_returnsEmptyList() {
        var entries = archiveService.getAll();

        assertTrue(entries.isEmpty());
    }

    @Test
    void retrieveFileDetails_returnsCorrectEntry() {
        var entry = archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/orig/test.pdf");

        var retrieved = archiveService.retrieveFileDetails(entry.getId());

        assertEquals(entry.getId(), retrieved.getId());
        assertEquals(entry.getName(), retrieved.getName());
    }

    @Test
    void retrieveFileDetails_whenNotFound_returnsNull() {
        var result = archiveService.retrieveFileDetails(UUID.randomUUID());

        assertNull(result);
    }

    @Test
    void getFile_returnsFileForEntry() {
        var entry = archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/orig/test.pdf");

        var file = archiveService.getFile(entry.getId());

        assertNotNull(file);
        assertEquals("/tmp/test.pdf", file.getAbsolutePath());
    }

    @Test
    void getFile_whenNotFound_returnsNull() {
        var file = archiveService.getFile(UUID.randomUUID());

        assertNull(file);
    }

    @Test
    void manageFiles_copiesFilesAndAddsEntries() throws IOException {
        var sourceFile = tempDir.resolve("source.pdf");
        Files.writeString(sourceFile, "PDF content");
        Files.createDirectories(tempDir.resolve("files"));

        var entries = archiveService.manageFiles(List.of(sourceFile.toFile()));

        assertEquals(1, entries.size());
        assertEquals("source.pdf", entries.getFirst().getName());
        verify(fileService).saveArchive(archive);
    }

    @Test
    void manageFiles_publishesEventForEachEntry() throws IOException {
        var sourceFile = tempDir.resolve("source.pdf");
        Files.writeString(sourceFile, "PDF content");
        Files.createDirectories(tempDir.resolve("files"));

        archiveService.manageFiles(List.of(sourceFile.toFile()));

        var captor = ArgumentCaptor.forClass(ArchiveEntryAddedEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertEquals("source.pdf", captor.getValue().getArchiveEntry().getName());
    }

    @Test
    void addTag_addsTagToEntry() {
        var entry = archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/orig/test.pdf");
        entry.setTags(new HashSet<>());

        archiveService.addTag(entry.getId(), "invoice");

        assertEquals(1, entry.getTags().size());
        assertTrue(entry.getTags().stream().anyMatch(t -> t.label().equals("invoice")));
    }

    @Test
    void addTag_publishesDocumentTagAddedEvent() {
        var entry = archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/orig/test.pdf");
        entry.setTags(new HashSet<>());

        archiveService.addTag(entry.getId(), "receipt");

        var captor = ArgumentCaptor.forClass(DocumentTagAddedEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertEquals("receipt", captor.getValue().getNewTag().label());
        assertEquals(entry.getId(), captor.getValue().getSelectedDocumentId());
    }
}
