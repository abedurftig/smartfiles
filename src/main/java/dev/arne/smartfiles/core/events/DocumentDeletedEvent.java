package dev.arne.smartfiles.core.events;

import lombok.Getter;

import java.util.UUID;

@Getter
public final class DocumentDeletedEvent extends SmartFilesEvent {

    private final UUID documentId;

    public DocumentDeletedEvent(UUID documentId) {
        super(documentId);
        this.documentId = documentId;
    }
}
