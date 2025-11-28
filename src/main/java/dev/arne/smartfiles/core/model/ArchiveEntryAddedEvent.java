package dev.arne.smartfiles.core.model;

import java.time.Clock;

public final class ArchiveEntryAddedEvent extends SmartFilesEvent {
    public ArchiveEntryAddedEvent(ArchiveEntry archiveEntry) {
        super(archiveEntry);
    }

    public ArchiveEntry getArchiveEntry() {
        return (ArchiveEntry) getSource();
    }
}
