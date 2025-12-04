package dev.arne.smartfiles.app;

import dev.arne.smartfiles.core.model.ArchiveEntry;
import dev.arne.smartfiles.core.model.Tag;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ApplicationModel {

    private final StringProperty selectedDocumentNameProperty = new SimpleStringProperty(null);
    private final BooleanProperty lightModeActivated = new SimpleBooleanProperty(false);
    private final SimpleListProperty<ArchiveEntry> documentsProperty =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final SimpleListProperty<Tag> tagsProperty =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ObjectProperty<ArchiveEntry> selectedDocumentProperty = new SimpleObjectProperty<>();

    public void setSelectedDocument(ArchiveEntry selectedDocument) {
        selectedDocumentProperty.setValue(selectedDocument);
        selectedDocumentNameProperty.setValue(selectedDocument.getName());
        updateDocumentTags();
    }

    public void updateDocumentTags() {
        tagsProperty.removeIf(_ -> true);
        tagsProperty.addAll(selectedDocumentProperty.get().getTags());
    }

    public UUID getSelectedDocumentId() {
        return selectedDocumentProperty.get().getId();
    }


    public boolean isLightModeActivated() {
        return lightModeActivated.get();
    }

    public void setLightModeActivated(boolean lightModeActivated) {
        this.lightModeActivated.set(lightModeActivated);
    }

    public void setDocumentsFromArchiveEntries(List<ArchiveEntry> archiveEntries) {
        documentsProperty.addAll(archiveEntries);
    }

    public void addDocumentFromArchiveEntry(ArchiveEntry archiveEntry) {
        documentsProperty.addAll(archiveEntry);
    }
}
