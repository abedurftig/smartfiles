package dev.arne.smartfiles.core.events;

import dev.arne.smartfiles.core.model.Tag;

import java.util.UUID;

public final class DocumentTagAddedEvent extends SmartFilesEvent {

    private final UUID selectedDocumentId;

    public DocumentTagAddedEvent(Tag newTag, UUID selectedDocumentId) {
        super(newTag);
        this.selectedDocumentId = selectedDocumentId;
    }

    public Tag getNewTag() {
        return (Tag) getSource();
    }

    public UUID getSelectedDocumentId() {
        return selectedDocumentId;
    }
}
