package dev.arne.smartfiles.app;

import dev.arne.smartfiles.core.events.*;
import javafx.application.Platform;
import org.springframework.context.ApplicationListener;

import java.time.format.DateTimeFormatter;

public class ApplicationInteractor implements ApplicationListener<SmartFilesEvent> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm");

    private final ApplicationModel model;

    public ApplicationInteractor(ApplicationModel model) {
        this.model = model;
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
        Platform.runLater(() -> model.getArchiveDateLastModifiedProperty().set(e.getLastModified().format(DATE_FORMATTER)));
    }

    private void handleDocumentDeletedEvent(DocumentDeletedEvent e) {
        Platform.runLater(() -> model.removeDocument(e.getDocumentId()));
    }

    private void handleAllTagsUpdatedEvent(AllTagsUpdatedEvent e) {
        Platform.runLater(() -> model.setAllTags(e.getAllTags()));
    }

    private void handleTagAddedEvent(TagAddedEvent e) {
    }

    private void handleDocumentTagAddedEvent(DocumentTagAddedEvent e) {
        model.updateDocumentTags();
    }

    private void handleDocumentDescriptionUpdatedEvent(DocumentDescriptionUpdatedEvent e) {
        Platform.runLater(() -> model.updateDescription(e.getDescription()));
    }

    private void handleLightThemeActivatedSettingsChangedEvent(LightThemeActivatedSettingChangedEvent lightThemeActivatedSettingChangedEvent) {
        model.setLightModeActivated(lightThemeActivatedSettingChangedEvent.isLightThemeActive());
    }

    private void handleArchiveEntryAddedEvent(ArchiveEntryAddedEvent event) {
        var entry = event.getArchiveEntry();
        model.addDocumentFromArchiveEntry(entry);
    }
}
