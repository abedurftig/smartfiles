package dev.arne.smartfiles.core.service;

import dev.arne.smartfiles.core.FileService;
import dev.arne.smartfiles.core.SettingsService;
import dev.arne.smartfiles.core.model.ApplicationSettings;
import dev.arne.smartfiles.core.events.LightThemeActivatedSettingChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

@Service
public class SettingsServiceImpl implements SettingsService, ApplicationListener<ContextClosedEvent> {

    private final ApplicationSettings settings;
    private final FileService fileService;
    private final ApplicationEventPublisher publisher;

    public SettingsServiceImpl(FileService fileService, ApplicationEventPublisher publisher) {
        this.fileService = fileService;
        this.settings = fileService.loadApplicationSettings();
        this.publisher = publisher;
    }

    @Override
    public boolean isLightThemeActive() {
        return settings.isLightThemeActive();
    }

    @Override
    public void toggleLightThemeActive() {
        settings.setLightThemeActive(!settings.isLightThemeActive());
        publisher.publishEvent(new LightThemeActivatedSettingChangedEvent(settings.isLightThemeActive()));
    }

    @Override
    public String getInboxFolderPath() {
        return settings.getInboxFolderPath();
    }

    @Override
    public void setInboxFolderPath(String path) {
        settings.setInboxFolderPath(path);
        settings.updateLastModified();
        fileService.saveApplicationSettings(settings);
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        fileService.saveApplicationSettings(settings);
    }
}
