package dev.arne.smartfiles.core.events;

import org.springframework.context.ApplicationEvent;

public abstract sealed class SmartFilesEvent
        extends ApplicationEvent
        permits AllTagsUpdatedEvent, ArchiveEntryAddedEvent, DocumentDescriptionUpdatedEvent, DocumentTagAddedEvent, LightThemeActivatedSettingChangedEvent, TagAddedEvent {

    public SmartFilesEvent(Object source) {
        super(source);
    }
}
