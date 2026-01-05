package dev.arne.smartfiles.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveEntryTest {

    @Test
    void of_createsEntryWithCorrectValues() {
        var before = LocalDateTime.now();
        var entry = ArchiveEntry.of("document.pdf", "/archive/files/document.pdf", "/downloads/document.pdf");
        var after = LocalDateTime.now();

        assertNotNull(entry.getId());
        assertEquals("document.pdf", entry.getName());
        assertEquals("Not available yet", entry.getSummary());
        assertEquals("document.pdf", entry.getPath());
        assertEquals("/archive/files/document.pdf", entry.getAbsolutePath());
        assertEquals("/downloads/document.pdf", entry.getOriginalPath());
        assertNotNull(entry.getTags());
        assertTrue(entry.getTags().isEmpty());
        assertTrue(entry.getDateCreated().isAfter(before.minusSeconds(1)));
        assertTrue(entry.getDateCreated().isBefore(after.plusSeconds(1)));
        assertEquals(entry.getDateCreated(), entry.getDateLastModified());
    }

    @Test
    void of_extractsPathFromAbsolutePath() {
        var entry = ArchiveEntry.of("test.pdf", "/some/long/path/to/file.pdf", "/original/file.pdf");

        assertEquals("file.pdf", entry.getPath());
    }

    @Test
    void of_generatesUniqueIds() {
        var entry1 = ArchiveEntry.of("doc1.pdf", "/path/doc1.pdf", "/orig/doc1.pdf");
        var entry2 = ArchiveEntry.of("doc2.pdf", "/path/doc2.pdf", "/orig/doc2.pdf");

        assertNotEquals(entry1.getId(), entry2.getId());
    }

    @Test
    void updateLastModified_updatesTimestamp() throws InterruptedException {
        var entry = ArchiveEntry.of("doc.pdf", "/path/doc.pdf", "/orig/doc.pdf");
        var originalModified = entry.getDateLastModified();

        Thread.sleep(10);
        entry.updateLastModified();

        assertTrue(entry.getDateLastModified().isAfter(originalModified));
    }
}
