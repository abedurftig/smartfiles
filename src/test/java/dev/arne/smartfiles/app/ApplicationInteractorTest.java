package dev.arne.smartfiles.app;

import dev.arne.smartfiles.core.events.*;
import dev.arne.smartfiles.core.model.ArchiveEntry;
import dev.arne.smartfiles.core.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationInteractorTest {

    private ApplicationModel model;
    private ApplicationInteractor interactor;

    @BeforeEach
    void setUp() {
        model = new ApplicationModel();
        // Use direct scheduler to execute synchronously without requiring JavaFX runtime
        interactor = new ApplicationInteractor(model, FxScheduler.direct());
    }

    @Test
    void handleArchiveEntryAddedEvent_addsDocumentToModel() {
        var entry = createTestEntry("test.pdf");

        interactor.onApplicationEvent(new ArchiveEntryAddedEvent(entry));

        assertEquals(1, model.getDocumentsList().size());
        assertEquals("test.pdf", model.getDocumentsList().getFirst().getName());
    }

    @Test
    void handleAllTagsUpdatedEvent_updatesModelTags() {
        var tags = Set.of(new Tag("invoice"), new Tag("receipt"));

        interactor.onApplicationEvent(new AllTagsUpdatedEvent(tags));

        assertEquals(2, model.getAllTagsProperty().size());
        assertTrue(model.getAllTagsProperty().stream().anyMatch(t -> t.label().equals("invoice")));
        assertTrue(model.getAllTagsProperty().stream().anyMatch(t -> t.label().equals("receipt")));
    }

    @Test
    void handleAllTagsUpdatedEvent_removesInvalidSelectedFilterTags() {
        var initialTags = Set.of(new Tag("invoice"), new Tag("receipt"));
        model.setAllTags(initialTags);
        model.toggleFilterTag(new Tag("invoice"));
        assertTrue(model.isFilterTagSelected(new Tag("invoice")));

        // Update tags without "invoice"
        var newTags = Set.of(new Tag("receipt"));
        interactor.onApplicationEvent(new AllTagsUpdatedEvent(newTags));

        assertFalse(model.isFilterTagSelected(new Tag("invoice")));
    }

    @Test
    void handleArchiveLastModifiedUpdatedEvent_formatsTimestamp() {
        var timestamp = LocalDateTime.of(2024, 6, 15, 14, 30);

        interactor.onApplicationEvent(new ArchiveLastModifiedUpdatedEvent(timestamp));

        assertEquals("Jun 15, 2024 at 14:30", model.getArchiveDateLastModifiedProperty().get());
    }

    @Test
    void handleDocumentDeletedEvent_removesDocumentFromModel() {
        var entry = createTestEntry("test.pdf");
        model.getDocumentsList().add(entry);
        assertEquals(1, model.getDocumentsList().size());

        interactor.onApplicationEvent(new DocumentDeletedEvent(entry.getId()));

        assertTrue(model.getDocumentsList().isEmpty());
    }

    @Test
    void handleDocumentDeletedEvent_clearsSelectionIfDeletedDocumentWasSelected() {
        var entry = createTestEntry("test.pdf");
        model.getDocumentsList().add(entry);
        model.setSelectedDocument(entry);
        assertEquals("test.pdf", model.getSelectedDocumentNameProperty().get());

        interactor.onApplicationEvent(new DocumentDeletedEvent(entry.getId()));

        assertNull(model.getSelectedDocumentProperty().get());
        assertNull(model.getSelectedDocumentNameProperty().get());
    }

    @Test
    void handleDocumentDescriptionUpdatedEvent_updatesDescription() {
        var entry = createTestEntry("test.pdf");
        model.getDocumentsList().add(entry);
        model.setSelectedDocument(entry);

        interactor.onApplicationEvent(new DocumentDescriptionUpdatedEvent(entry.getId(), "New description"));

        assertEquals("New description", model.getDescriptionProperty().get());
    }

    @Test
    void handleDocumentTagAddedEvent_updatesTags() {
        var entry = createTestEntry("test.pdf");
        entry.getTags().add(new Tag("invoice"));
        model.getDocumentsList().add(entry);
        model.setSelectedDocument(entry);

        // Add another tag to the entry (simulating what the service does)
        entry.getTags().add(new Tag("receipt"));

        interactor.onApplicationEvent(new DocumentTagAddedEvent(new Tag("receipt"), entry.getId()));

        assertEquals(2, model.getTagsProperty().size());
    }

    @Test
    void handleLightThemeActivatedSettingsChangedEvent_setsModeActivated_true() {
        assertFalse(model.isLightModeActivated());

        interactor.onApplicationEvent(new LightThemeActivatedSettingChangedEvent(true));

        assertTrue(model.isLightModeActivated());
    }

    @Test
    void handleLightThemeActivatedSettingsChangedEvent_setsModeActivated_false() {
        model.setLightModeActivated(true);
        assertTrue(model.isLightModeActivated());

        interactor.onApplicationEvent(new LightThemeActivatedSettingChangedEvent(false));

        assertFalse(model.isLightModeActivated());
    }

    @Test
    void handleTagAddedEvent_completesWithoutError() {
        // TagAddedEvent handler is intentionally empty (no-op)
        assertDoesNotThrow(() -> interactor.onApplicationEvent(new TagAddedEvent(new Tag("test"))));
    }

    @Test
    void onApplicationEvent_dispatchesToAllEventTypes() {
        var entry = createTestEntry("test.pdf");
        var tag = new Tag("test");
        var timestamp = LocalDateTime.now();

        // Set up a selected document for DocumentTagAddedEvent
        model.getDocumentsList().add(entry);
        model.setSelectedDocument(entry);

        // Test all event types dispatch without error
        assertDoesNotThrow(() -> {
            interactor.onApplicationEvent(new ArchiveEntryAddedEvent(createTestEntry("another.pdf")));
            interactor.onApplicationEvent(new AllTagsUpdatedEvent(Set.of(tag)));
            interactor.onApplicationEvent(new ArchiveLastModifiedUpdatedEvent(timestamp));
            interactor.onApplicationEvent(new DocumentDeletedEvent(UUID.randomUUID()));
            interactor.onApplicationEvent(new DocumentDescriptionUpdatedEvent(entry.getId(), "desc"));
            interactor.onApplicationEvent(new DocumentTagAddedEvent(tag, entry.getId()));
            interactor.onApplicationEvent(new LightThemeActivatedSettingChangedEvent(true));
            interactor.onApplicationEvent(new TagAddedEvent(tag));
        });
    }

    private ArchiveEntry createTestEntry(String name) {
        var entry = new ArchiveEntry();
        entry.setId(UUID.randomUUID());
        entry.setName(name);
        entry.setSummary("");
        entry.setPath("/tmp/" + name);
        entry.setAbsolutePath("/tmp/" + name);
        entry.setOriginalPath("/orig/" + name);
        entry.setTags(new HashSet<>());
        entry.setDateCreated(LocalDateTime.now());
        entry.setDateLastModified(LocalDateTime.now());
        return entry;
    }
}
