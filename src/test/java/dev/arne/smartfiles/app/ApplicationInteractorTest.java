package dev.arne.smartfiles.app;

import dev.arne.smartfiles.core.events.*;
import dev.arne.smartfiles.core.model.ArchiveEntry;
import dev.arne.smartfiles.core.model.Tag;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationInteractorTest {

    private ApplicationModel model;
    private ApplicationInteractor interactor;

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        // Initialize JavaFX toolkit for Platform.runLater() calls
        // Set properties for headless CI environments without a display
        System.setProperty("java.awt.headless", "true");
        System.setProperty("prism.useSWRender", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("testfx.headless", "true");

        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            // Platform already initialized
            latch.countDown();
        } catch (UnsupportedOperationException e) {
            // In headless environments without a display, we get UnsupportedOperationException
            // We still need Platform.runLater to work, so we create a dummy thread that handles it
            // JavaFX may initialize partially even after this exception
            latch.countDown();
        } catch (Exception e) {
            // In headless environments, Platform.startup may fail with graphics-related exceptions
            // This is expected and we can proceed
            latch.countDown();
        }
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Failed to initialize JavaFX Platform");
        }
    }

    @BeforeEach
    void setUp() {
        model = new ApplicationModel();
        interactor = new ApplicationInteractor(model);
    }

    @Test
    void handleArchiveEntryAddedEvent_addsDocumentToModel() throws InterruptedException {
        var entry = createTestEntry("test.pdf");

        interactor.onApplicationEvent(new ArchiveEntryAddedEvent(entry));

        waitForPlatform();
        assertEquals(1, model.getDocumentsList().size());
        assertEquals("test.pdf", model.getDocumentsList().getFirst().getName());
    }

    @Test
    void handleAllTagsUpdatedEvent_updatesModelTags() throws InterruptedException {
        var tags = Set.of(new Tag("invoice"), new Tag("receipt"));

        interactor.onApplicationEvent(new AllTagsUpdatedEvent(tags));

        waitForPlatform();
        assertEquals(2, model.getAllTagsProperty().size());
        assertTrue(model.getAllTagsProperty().stream().anyMatch(t -> t.label().equals("invoice")));
        assertTrue(model.getAllTagsProperty().stream().anyMatch(t -> t.label().equals("receipt")));
    }

    @Test
    void handleAllTagsUpdatedEvent_removesInvalidSelectedFilterTags() throws InterruptedException {
        var initialTags = Set.of(new Tag("invoice"), new Tag("receipt"));
        model.setAllTags(initialTags);
        model.toggleFilterTag(new Tag("invoice"));
        assertTrue(model.isFilterTagSelected(new Tag("invoice")));

        // Update tags without "invoice"
        var newTags = Set.of(new Tag("receipt"));
        interactor.onApplicationEvent(new AllTagsUpdatedEvent(newTags));

        waitForPlatform();
        assertFalse(model.isFilterTagSelected(new Tag("invoice")));
    }

    @Test
    void handleArchiveLastModifiedUpdatedEvent_formatsTimestamp() throws InterruptedException {
        var timestamp = LocalDateTime.of(2024, 6, 15, 14, 30);

        interactor.onApplicationEvent(new ArchiveLastModifiedUpdatedEvent(timestamp));

        waitForPlatform();
        assertEquals("Jun 15, 2024 at 14:30", model.getArchiveDateLastModifiedProperty().get());
    }

    @Test
    void handleDocumentDeletedEvent_removesDocumentFromModel() throws InterruptedException {
        var entry = createTestEntry("test.pdf");
        model.getDocumentsList().add(entry);
        assertEquals(1, model.getDocumentsList().size());

        interactor.onApplicationEvent(new DocumentDeletedEvent(entry.getId()));

        waitForPlatform();
        assertTrue(model.getDocumentsList().isEmpty());
    }

    @Test
    void handleDocumentDeletedEvent_clearsSelectionIfDeletedDocumentWasSelected() throws InterruptedException {
        var entry = createTestEntry("test.pdf");
        model.getDocumentsList().add(entry);
        model.setSelectedDocument(entry);
        assertEquals("test.pdf", model.getSelectedDocumentNameProperty().get());

        interactor.onApplicationEvent(new DocumentDeletedEvent(entry.getId()));

        waitForPlatform();
        assertNull(model.getSelectedDocumentProperty().get());
        assertNull(model.getSelectedDocumentNameProperty().get());
    }

    @Test
    void handleDocumentDescriptionUpdatedEvent_updatesDescription() throws InterruptedException {
        var entry = createTestEntry("test.pdf");
        model.getDocumentsList().add(entry);
        model.setSelectedDocument(entry);

        interactor.onApplicationEvent(new DocumentDescriptionUpdatedEvent(entry.getId(), "New description"));

        waitForPlatform();
        assertEquals("New description", model.getDescriptionProperty().get());
    }

    @Test
    void handleDocumentTagAddedEvent_updatesTags() throws InterruptedException {
        var entry = createTestEntry("test.pdf");
        entry.getTags().add(new Tag("invoice"));
        model.getDocumentsList().add(entry);

        // Select the document on the JavaFX thread to ensure proper state
        CountDownLatch selectLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            model.setSelectedDocument(entry);
            selectLatch.countDown();
        });
        selectLatch.await(5, TimeUnit.SECONDS);

        // Add another tag to the entry (simulating what the service does)
        entry.getTags().add(new Tag("receipt"));

        interactor.onApplicationEvent(new DocumentTagAddedEvent(new Tag("receipt"), entry.getId()));

        waitForPlatform();
        assertEquals(2, model.getTagsProperty().size());
    }

    @Test
    void handleLightThemeActivatedSettingsChangedEvent_setsModeActivated_true() throws InterruptedException {
        assertFalse(model.isLightModeActivated());

        interactor.onApplicationEvent(new LightThemeActivatedSettingChangedEvent(true));

        waitForPlatform();
        assertTrue(model.isLightModeActivated());
    }

    @Test
    void handleLightThemeActivatedSettingsChangedEvent_setsModeActivated_false() throws InterruptedException {
        model.setLightModeActivated(true);
        assertTrue(model.isLightModeActivated());

        interactor.onApplicationEvent(new LightThemeActivatedSettingChangedEvent(false));

        waitForPlatform();
        assertFalse(model.isLightModeActivated());
    }

    @Test
    void handleTagAddedEvent_completesWithoutError() throws InterruptedException {
        // TagAddedEvent handler is intentionally empty (no-op)
        assertDoesNotThrow(() -> interactor.onApplicationEvent(new TagAddedEvent(new Tag("test"))));
        waitForPlatform();
        // Just verify it doesn't throw
    }

    @Test
    void onApplicationEvent_dispatchesToAllEventTypes() throws InterruptedException {
        var entry = createTestEntry("test.pdf");
        var tag = new Tag("test");
        var timestamp = LocalDateTime.now();

        // Set up a selected document for DocumentTagAddedEvent
        model.getDocumentsList().add(entry);
        CountDownLatch selectLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            model.setSelectedDocument(entry);
            selectLatch.countDown();
        });
        selectLatch.await(5, TimeUnit.SECONDS);

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

        waitForPlatform();
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

    private void waitForPlatform() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Platform.runLater did not complete in time");
    }
}
