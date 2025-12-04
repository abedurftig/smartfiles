package dev.arne.smartfiles.core.events;

import dev.arne.smartfiles.core.model.ArchiveEntry;

public final class ArchiveEntryAddedEvent extends SmartFilesEvent {
    public ArchiveEntryAddedEvent(ArchiveEntry archiveEntry) {
        super(archiveEntry);
    }

    public ArchiveEntry getArchiveEntry() {
        return (ArchiveEntry) getSource();
    }
}
