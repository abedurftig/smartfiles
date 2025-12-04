package dev.arne.smartfiles.core.events;

import org.springframework.context.ApplicationEvent;

public abstract sealed class SmartFilesEvent
        extends ApplicationEvent
        permits ArchiveEntryAddedEvent, DocumentTagAddedEvent, LightThemeActivatedSettingChangedEvent, TagAddedEvent {

    public SmartFilesEvent(Object source) {
        super(source);
    }
}
