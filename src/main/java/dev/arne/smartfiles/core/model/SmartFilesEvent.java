package dev.arne.smartfiles.core.model;

import org.springframework.context.ApplicationEvent;

public abstract sealed class SmartFilesEvent
        extends ApplicationEvent
        permits ArchiveEntryAddedEvent, LightThemeActivatedSettingChangedEvent {

    public SmartFilesEvent(Object source) {
        super(source);
    }
}
