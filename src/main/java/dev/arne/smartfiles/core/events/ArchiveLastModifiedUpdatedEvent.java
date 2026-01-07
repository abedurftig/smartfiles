package dev.arne.smartfiles.core.events;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public final class ArchiveLastModifiedUpdatedEvent extends SmartFilesEvent {

    private final LocalDateTime lastModified;

    public ArchiveLastModifiedUpdatedEvent(LocalDateTime lastModified) {
        super(lastModified);
        this.lastModified = lastModified;
    }
}
