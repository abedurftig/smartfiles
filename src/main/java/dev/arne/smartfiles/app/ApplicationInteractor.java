package dev.arne.smartfiles.app;

import dev.arne.smartfiles.core.model.ArchiveEntryAddedEvent;
import org.springframework.context.ApplicationListener;

public class ApplicationInteractor implements ApplicationListener<ArchiveEntryAddedEvent> {

    private final ApplicationModel model;

    public ApplicationInteractor(ApplicationModel model) {
        this.model = model;
    }

    @Override
    public void onApplicationEvent(ArchiveEntryAddedEvent event) {
        var entry = event.getArchiveEntry();
        model.addDocumentFromArchiveEntry(entry);

        model.getDocumentsProperty().forEach(item -> {
            System.out.println(item.text());
        });

    }
}
