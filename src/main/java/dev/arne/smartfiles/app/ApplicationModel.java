package dev.arne.smartfiles.app;

import dev.arne.smartfiles.core.model.ArchiveEntry;
import dev.arne.smartfiles.core.model.Tag;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ApplicationModel {

    private final StringProperty selectedDocumentNameProperty = new SimpleStringProperty(null);
    private final BooleanProperty lightModeActivated = new SimpleBooleanProperty(false);
    private final ObservableList<ArchiveEntry> documentsList = FXCollections.observableArrayList();
    private final FilteredList<ArchiveEntry> filteredDocuments = new FilteredList<>(documentsList, _ -> true);
    private final SimpleListProperty<Tag> tagsProperty =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final StringProperty searchTextProperty = new SimpleStringProperty("");
    private final ObjectProperty<ArchiveEntry> selectedDocumentProperty = new SimpleObjectProperty<>();

    public ApplicationModel() {
        searchTextProperty.addListener((_, _, newValue) -> {
            if (newValue == null || newValue.isBlank()) {
                filteredDocuments.setPredicate(_ -> true);
            } else {
                String lowerCaseSearch = newValue.toLowerCase();
                filteredDocuments.setPredicate(entry ->
                        entry.getName().toLowerCase().contains(lowerCaseSearch));
            }
        });
    }

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
        documentsList.addAll(archiveEntries);
    }

    public void addDocumentFromArchiveEntry(ArchiveEntry archiveEntry) {
        documentsList.add(archiveEntry);
    }
}
