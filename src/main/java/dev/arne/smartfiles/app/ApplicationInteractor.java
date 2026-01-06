package dev.arne.smartfiles.app;

import dev.arne.smartfiles.core.events.*;
import javafx.application.Platform;
import org.springframework.context.ApplicationListener;

public class ApplicationInteractor implements ApplicationListener<SmartFilesEvent> {

    private final ApplicationModel model;

    public ApplicationInteractor(ApplicationModel model) {
        this.model = model;
    }

    @Override
    public void onApplicationEvent(SmartFilesEvent event) {

        switch (event) {
            case AllTagsUpdatedEvent e -> handleAllTagsUpdatedEvent(e);
            case ArchiveEntryAddedEvent e -> handleArchiveEntryAddedEvent(e);
            case DocumentDescriptionUpdatedEvent e -> handleDocumentDescriptionUpdatedEvent(e);
            case LightThemeActivatedSettingChangedEvent e -> handleLightThemeActivatedSettingsChangedEvent(e);
            case DocumentTagAddedEvent e -> handleDocumentTagAddedEvent(e);
            case TagAddedEvent e -> handleTagAddedEvent(e);
        }
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
