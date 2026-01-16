package dev.arne.smartfiles.core.service;

import dev.arne.smartfiles.core.FileService;
import dev.arne.smartfiles.core.events.AllTagsUpdatedEvent;
import dev.arne.smartfiles.core.events.ArchiveEntryAddedEvent;
import dev.arne.smartfiles.core.events.ArchiveLastModifiedUpdatedEvent;
import dev.arne.smartfiles.core.events.DocumentDeletedEvent;
import dev.arne.smartfiles.core.events.DocumentDescriptionUpdatedEvent;
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
        verify(publisher, atLeastOnce()).publishEvent(captor.capture());
        var documentTagEvent = captor.getAllValues().stream()
                .filter(e -> e instanceof DocumentTagAddedEvent)
                .map(e -> (DocumentTagAddedEvent) e)
                .findFirst()
                .orElseThrow();
        assertEquals("receipt", documentTagEvent.getNewTag().label());
        assertEquals(entry.getId(), documentTagEvent.getSelectedDocumentId());
    }

    @Test
    void addTag_publishesAllTagsUpdatedEvent() {
        var entry = archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/orig/test.pdf");
        entry.setTags(new HashSet<>());

        archiveService.addTag(entry.getId(), "invoice");

        var captor = ArgumentCaptor.forClass(AllTagsUpdatedEvent.class);
        verify(publisher, atLeastOnce()).publishEvent(captor.capture());
        var allTagsEvent = captor.getAllValues().stream()
                .filter(e -> e instanceof AllTagsUpdatedEvent)
                .map(e -> (AllTagsUpdatedEvent) e)
                .findFirst()
                .orElseThrow();
        assertEquals(1, allTagsEvent.getAllTags().size());
        assertTrue(allTagsEvent.getAllTags().stream().anyMatch(t -> t.label().equals("invoice")));
    }

    @Test
    void getAllUniqueTags_whenNoDocuments_returnsEmptySet() {
        var tags = archiveService.getAllUniqueTags();

        assertTrue(tags.isEmpty());
    }

    @Test
    void getAllUniqueTags_whenDocumentsHaveNoTags_returnsEmptySet() {
        archive.addArchiveEntryFromFile(new File("/tmp/doc1.pdf"), "/orig/doc1.pdf");
        archive.addArchiveEntryFromFile(new File("/tmp/doc2.pdf"), "/orig/doc2.pdf");

        var tags = archiveService.getAllUniqueTags();

        assertTrue(tags.isEmpty());
    }

    @Test
    void getAllUniqueTags_returnsAllUniqueTags() {
        var entry1 = archive.addArchiveEntryFromFile(new File("/tmp/doc1.pdf"), "/orig/doc1.pdf");
        var entry2 = archive.addArchiveEntryFromFile(new File("/tmp/doc2.pdf"), "/orig/doc2.pdf");
        entry1.setTags(new HashSet<>());
        entry2.setTags(new HashSet<>());

        archiveService.addTag(entry1.getId(), "invoice");
        archiveService.addTag(entry1.getId(), "important");
        archiveService.addTag(entry2.getId(), "receipt");

        var tags = archiveService.getAllUniqueTags();

        assertEquals(3, tags.size());
        assertTrue(tags.stream().anyMatch(t -> t.label().equals("invoice")));
        assertTrue(tags.stream().anyMatch(t -> t.label().equals("important")));
        assertTrue(tags.stream().anyMatch(t -> t.label().equals("receipt")));
    }

    @Test
    void getAllUniqueTags_deduplicatesTagsAcrossDocuments() {
        var entry1 = archive.addArchiveEntryFromFile(new File("/tmp/doc1.pdf"), "/orig/doc1.pdf");
        var entry2 = archive.addArchiveEntryFromFile(new File("/tmp/doc2.pdf"), "/orig/doc2.pdf");
        entry1.setTags(new HashSet<>());
        entry2.setTags(new HashSet<>());

        archiveService.addTag(entry1.getId(), "invoice");
        archiveService.addTag(entry2.getId(), "invoice");

        var tags = archiveService.getAllUniqueTags();

        assertEquals(1, tags.size());
        assertTrue(tags.stream().anyMatch(t -> t.label().equals("invoice")));
    }

    @Test
    void updateDescription_updatesEntrySummary() {
        var entry = archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/orig/test.pdf");

        archiveService.updateDescription(entry.getId(), "New description");

        assertEquals("New description", entry.getSummary());
    }

    @Test
    void updateDescription_publishesDocumentDescriptionUpdatedEvent() {
        var entry = archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/orig/test.pdf");

        archiveService.updateDescription(entry.getId(), "Updated description");

        var captor = ArgumentCaptor.forClass(DocumentDescriptionUpdatedEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertEquals(entry.getId(), captor.getValue().getDocumentId());
        assertEquals("Updated description", captor.getValue().getDescription());
    }

    @Test
    void updateDescription_savesArchive() {
        var entry = archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/orig/test.pdf");

        archiveService.updateDescription(entry.getId(), "Description");

        verify(fileService).saveArchive(archive);
    }

    @Test
    void updateDescription_updatesLastModified() {
        var entry = archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/orig/test.pdf");
        var originalLastModified = entry.getDateLastModified();

        archiveService.updateDescription(entry.getId(), "Description");

        assertNotEquals(originalLastModified, entry.getDateLastModified());
    }

    @Test
    void deleteDocument_removesEntryFromArchive() {
        var entry = archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/orig/test.pdf");
        assertEquals(1, archive.getArchiveEntries().size());

        archiveService.deleteDocument(entry.getId());

        assertTrue(archive.getArchiveEntries().isEmpty());
    }

    @Test
    void deleteDocument_deletesFileFromDisk() throws IOException {
        var sourceFile = tempDir.resolve("test.pdf");
        Files.writeString(sourceFile, "PDF content");
        var entry = archive.addArchiveEntryFromFile(sourceFile.toFile(), "/orig/test.pdf");
        assertTrue(Files.exists(sourceFile));

        archiveService.deleteDocument(entry.getId());

        assertFalse(Files.exists(sourceFile));
    }

    @Test
    void deleteDocument_publishesDocumentDeletedEvent() {
        var entry = archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/orig/test.pdf");

        archiveService.deleteDocument(entry.getId());

        var captor = ArgumentCaptor.forClass(DocumentDeletedEvent.class);
        verify(publisher, atLeastOnce()).publishEvent(captor.capture());
        var deletedEvent = captor.getAllValues().stream()
                .filter(e -> e instanceof DocumentDeletedEvent)
                .map(e -> (DocumentDeletedEvent) e)
                .findFirst()
                .orElseThrow();
        assertEquals(entry.getId(), deletedEvent.getDocumentId());
    }

    @Test
    void deleteDocument_publishesAllTagsUpdatedEvent() {
        var entry = archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/orig/test.pdf");
        entry.setTags(new HashSet<>());
        archiveService.addTag(entry.getId(), "invoice");
        reset(publisher);

        archiveService.deleteDocument(entry.getId());

        var captor = ArgumentCaptor.forClass(AllTagsUpdatedEvent.class);
        verify(publisher, atLeastOnce()).publishEvent(captor.capture());
        var allTagsEvent = captor.getAllValues().stream()
                .filter(e -> e instanceof AllTagsUpdatedEvent)
                .map(e -> (AllTagsUpdatedEvent) e)
                .findFirst()
                .orElseThrow();
        assertTrue(allTagsEvent.getAllTags().isEmpty());
    }

    @Test
    void deleteDocument_publishesArchiveLastModifiedUpdatedEvent() {
        var entry = archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/orig/test.pdf");

        archiveService.deleteDocument(entry.getId());

        var captor = ArgumentCaptor.forClass(ArchiveLastModifiedUpdatedEvent.class);
        verify(publisher, atLeastOnce()).publishEvent(captor.capture());
        assertNotNull(captor.getValue().getLastModified());
    }

    @Test
    void deleteDocument_whenEntryNotFound_doesNotPublishEvents() {
        var nonExistentId = UUID.randomUUID();
        reset(publisher);

        archiveService.deleteDocument(nonExistentId);

        verify(publisher, never()).publishEvent(any(DocumentDeletedEvent.class));
        verify(publisher, never()).publishEvent(any(AllTagsUpdatedEvent.class));
    }

    @Test
    void deleteDocument_savesArchive() {
        var entry = archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/orig/test.pdf");
        reset(fileService);

        archiveService.deleteDocument(entry.getId());

        verify(fileService).saveArchive(archive);
    }

    @Test
    void deleteDocument_whenFileNotOnDisk_stillRemovesEntry() {
        var nonExistentFile = new File(tempDir.resolve("nonexistent.pdf").toString());
        var entry = archive.addArchiveEntryFromFile(nonExistentFile, "/orig/nonexistent.pdf");
        assertEquals(1, archive.getArchiveEntries().size());

        archiveService.deleteDocument(entry.getId());

        assertTrue(archive.getArchiveEntries().isEmpty());
    }
}
