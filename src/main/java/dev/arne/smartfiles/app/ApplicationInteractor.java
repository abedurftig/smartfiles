package dev.arne.smartfiles.app;

import dev.arne.smartfiles.core.model.ArchiveEntryAddedEvent;
import dev.arne.smartfiles.core.model.LightThemeActivatedSettingChangedEvent;
import dev.arne.smartfiles.core.model.SmartFilesEvent;
import org.springframework.context.ApplicationListener;

public class ApplicationInteractor implements ApplicationListener<SmartFilesEvent> {

    private final ApplicationModel model;

    public ApplicationInteractor(ApplicationModel model) {
        this.model = model;
    }

    @Override
    public void onApplicationEvent(SmartFilesEvent event) {

        switch (event) {
            case ArchiveEntryAddedEvent archiveEntryAddedEvent -> handleArchiveEntryAddedEvent(archiveEntryAddedEvent);
            case LightThemeActivatedSettingChangedEvent lightThemeActivatedSettingChangedEvent ->
                    handleLightThemeActivatedSettingsChangedEvent(lightThemeActivatedSettingChangedEvent);
        }
    }

    private void handleLightThemeActivatedSettingsChangedEvent(LightThemeActivatedSettingChangedEvent lightThemeActivatedSettingChangedEvent) {
        model.setLightModeActivated(lightThemeActivatedSettingChangedEvent.isLightThemeActive());
    }

    private void handleArchiveEntryAddedEvent(ArchiveEntryAddedEvent event) {
        var entry = event.getArchiveEntry();
        model.addDocumentFromArchiveEntry(entry);
    }
}
