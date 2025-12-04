package dev.arne.smartfiles.core.events;

import dev.arne.smartfiles.core.model.Tag;

public final class TagAddedEvent extends SmartFilesEvent {

    public TagAddedEvent(Tag newTag) {
        super(newTag);
    }

    public Tag getNewTag() {
        return (Tag) getSource();
    }
}
