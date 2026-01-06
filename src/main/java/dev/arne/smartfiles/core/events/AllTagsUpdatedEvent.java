package dev.arne.smartfiles.core.events;

import dev.arne.smartfiles.core.model.Tag;

import java.util.Set;

public final class AllTagsUpdatedEvent extends SmartFilesEvent {

    public AllTagsUpdatedEvent(Set<Tag> allTags) {
        super(allTags);
    }

    @SuppressWarnings("unchecked")
    public Set<Tag> getAllTags() {
        return (Set<Tag>) getSource();
    }
}
