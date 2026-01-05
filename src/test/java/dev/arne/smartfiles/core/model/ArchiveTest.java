package dev.arne.smartfiles.core.model;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveTest {

    @Test
    void empty_createsArchiveWithDefaults() {
        var before = LocalDateTime.now();
        var archive = Archive.empty();
        var after = LocalDateTime.now();

        assertEquals(AggregateRoot.CURRENT_APP_VERSION, archive.getApplicationVersion());
        assertNotNull(archive.getDateCreated());
        assertNotNull(archive.getDateLastModified());
        assertTrue(archive.getDateCreated().isAfter(before.minusSeconds(1)));
        assertTrue(archive.getDateCreated().isBefore(after.plusSeconds(1)));
        assertNotNull(archive.getArchiveEntries());
        assertTrue(archive.getArchiveEntries().isEmpty());
        assertNotNull(archive.getTags());
        assertTrue(archive.getTags().isEmpty());
    }

    @Test
    void addArchiveEntryFromFile_addsEntryToArchive() {
        var archive = Archive.empty();
        var file = new File("/tmp/test/document.pdf");

        var entry = archive.addArchiveEntryFromFile(file, "/original/path/document.pdf");

        assertNotNull(entry);
        assertNotNull(entry.getId());
        assertEquals("document.pdf", entry.getName());
        assertEquals("/tmp/test/document.pdf", entry.getAbsolutePath());
        assertEquals("/original/path/document.pdf", entry.getOriginalPath());
        assertEquals(1, archive.getArchiveEntries().size());
        assertTrue(archive.getArchiveEntries().containsKey(entry.getId()));
    }

    @Test
    void addArchiveEntryFromFile_multipleFiles_addsAllEntries() {
        var archive = Archive.empty();

        archive.addArchiveEntryFromFile(new File("/tmp/doc1.pdf"), "/orig/doc1.pdf");
        archive.addArchiveEntryFromFile(new File("/tmp/doc2.pdf"), "/orig/doc2.pdf");
        archive.addArchiveEntryFromFile(new File("/tmp/doc3.pdf"), "/orig/doc3.pdf");

        assertEquals(3, archive.getArchiveEntries().size());
    }

    @Test
    void updateLastModified_updatesTimestamp() throws InterruptedException {
        var archive = Archive.empty();
        var originalModified = archive.getDateLastModified();

        Thread.sleep(10);
        archive.updateLastModified();

        assertTrue(archive.getDateLastModified().isAfter(originalModified));
    }
}
