package dev.arne.smartfiles.app;

import dev.arne.smartfiles.core.model.ArchiveEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationModelTest {

    private ApplicationModel model;

    @BeforeEach
    void setUp() {
        model = new ApplicationModel();
    }

    @Test
    void setSelectedDocument_setsDescriptionProperty() {
        var entry = createTestEntry("test.pdf", "Initial description");
        model.getDocumentsList().add(entry);

        model.setSelectedDocument(entry);

        assertEquals("Initial description", model.getDescriptionProperty().get());
    }

    @Test
    void updateDescription_updatesDescriptionProperty() {
        var entry = createTestEntry("test.pdf", "Initial description");
        model.getDocumentsList().add(entry);
        model.setSelectedDocument(entry);

        model.updateDescription("Updated description");

        assertEquals("Updated description", model.getDescriptionProperty().get());
    }

    @Test
    void updateDescription_updatesEntryInDocumentsList() {
        var entry = createTestEntry("test.pdf", "Initial description");
        model.getDocumentsList().add(entry);
        model.setSelectedDocument(entry);

        model.updateDescription("Updated description");

        var updatedEntry = model.getDocumentsList().getFirst();
        assertEquals("Updated description", updatedEntry.getSummary());
    }

    @Test
    void updateDescription_refreshesDocumentInList() {
        var entry = createTestEntry("test.pdf", "Initial description");
        model.getDocumentsList().add(entry);
        model.setSelectedDocument(entry);

        var listChanged = new boolean[]{false};
        model.getDocumentsList().addListener((javafx.collections.ListChangeListener<ArchiveEntry>) c -> {
            listChanged[0] = true;
        });

        model.updateDescription("Updated description");

        assertTrue(listChanged[0], "Document list should have been updated");
    }

    @Test
    void updateDescription_whenNoDocumentSelected_doesNotThrow() {
        assertDoesNotThrow(() -> model.updateDescription("Some description"));
    }

    private ArchiveEntry createTestEntry(String name, String summary) {
        var entry = new ArchiveEntry();
        entry.setId(UUID.randomUUID());
        entry.setName(name);
        entry.setSummary(summary);
        entry.setPath("/tmp/" + name);
        entry.setAbsolutePath("/tmp/" + name);
        entry.setOriginalPath("/orig/" + name);
        entry.setTags(new HashSet<>());
        entry.setDateCreated(LocalDateTime.now());
        entry.setDateLastModified(LocalDateTime.now());
        return entry;
    }
}
