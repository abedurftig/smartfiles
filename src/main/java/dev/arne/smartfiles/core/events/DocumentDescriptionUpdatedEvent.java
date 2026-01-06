package dev.arne.smartfiles.core.events;

import lombok.Getter;

import java.util.UUID;

@Getter
public final class DocumentDescriptionUpdatedEvent extends SmartFilesEvent {

    private final UUID documentId;
    private final String description;

    public DocumentDescriptionUpdatedEvent(UUID documentId, String description) {
        super(documentId);
        this.documentId = documentId;
        this.description = description;
    }
}
