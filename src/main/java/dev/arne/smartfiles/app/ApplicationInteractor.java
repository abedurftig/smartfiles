package dev.arne.smartfiles.app;

import dev.arne.smartfiles.core.events.*;
import org.springframework.context.ApplicationListener;

import java.time.format.DateTimeFormatter;

public class ApplicationInteractor implements ApplicationListener<SmartFilesEvent> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm");

    private final ApplicationModel model;
    private final FxScheduler scheduler;

    public ApplicationInteractor(ApplicationModel model) {
        this(model, FxScheduler.platform());
    }

    public ApplicationInteractor(ApplicationModel model, FxScheduler scheduler) {
        this.model = model;
        this.scheduler = scheduler;
    }

    @Override
    public void onApplicationEvent(SmartFilesEvent event) {

        switch (event) {
            case AllTagsUpdatedEvent e -> handleAllTagsUpdatedEvent(e);
            case ArchiveEntryAddedEvent e -> handleArchiveEntryAddedEvent(e);
            case ArchiveLastModifiedUpdatedEvent e -> handleArchiveLastModifiedUpdatedEvent(e);
            case DocumentDeletedEvent e -> handleDocumentDeletedEvent(e);
            case DocumentDescriptionUpdatedEvent e -> handleDocumentDescriptionUpdatedEvent(e);
            case LightThemeActivatedSettingChangedEvent e -> handleLightThemeActivatedSettingsChangedEvent(e);
            case DocumentTagAddedEvent e -> handleDocumentTagAddedEvent(e);
            case TagAddedEvent e -> handleTagAddedEvent(e);
        }
    }

    private void handleArchiveLastModifiedUpdatedEvent(ArchiveLastModifiedUpdatedEvent e) {
        scheduler.runLater(() -> model.getArchiveDateLastModifiedProperty().set(e.getLastModified().format(DATE_FORMATTER)));
    }

    private void handleDocumentDeletedEvent(DocumentDeletedEvent e) {
        scheduler.runLater(() -> model.removeDocument(e.getDocumentId()));
    }

    private void handleAllTagsUpdatedEvent(AllTagsUpdatedEvent e) {
        scheduler.runLater(() -> model.setAllTags(e.getAllTags()));
    }

    private void handleTagAddedEvent(TagAddedEvent e) {
    }

    private void handleDocumentTagAddedEvent(DocumentTagAddedEvent e) {
        scheduler.runLater(() -> model.updateDocumentTags());
    }

    private void handleDocumentDescriptionUpdatedEvent(DocumentDescriptionUpdatedEvent e) {
        scheduler.runLater(() -> model.updateDescription(e.getDescription()));
    }

    private void handleLightThemeActivatedSettingsChangedEvent(LightThemeActivatedSettingChangedEvent e) {
        scheduler.runLater(() -> model.setLightModeActivated(e.isLightThemeActive()));
    }

    private void handleArchiveEntryAddedEvent(ArchiveEntryAddedEvent e) {
        scheduler.runLater(() -> model.addDocumentFromArchiveEntry(e.getArchiveEntry()));
    }
}
