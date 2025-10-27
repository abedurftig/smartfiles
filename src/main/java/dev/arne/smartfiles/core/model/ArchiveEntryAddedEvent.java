package dev.arne.smartfiles.core.model;

import org.springframework.context.ApplicationEvent;

import java.time.Clock;

public class ArchiveEntryAddedEvent extends ApplicationEvent {
    public ArchiveEntryAddedEvent(ArchiveEntry archiveEntry) {
        super(archiveEntry);
    }

    public ArchiveEntryAddedEvent(ArchiveEntry archiveEntry, Clock clock) {
        super(archiveEntry, clock);
    }

    public ArchiveEntry getArchiveEntry() {
        return (ArchiveEntry) getSource();
    }
}
