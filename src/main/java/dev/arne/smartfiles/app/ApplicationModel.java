package dev.arne.smartfiles.app;

import dev.arne.smartfiles.core.model.ArchiveEntry;
import dev.arne.smartfiles.core.model.Tag;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.FilteredList;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;
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
    private final StringProperty descriptionProperty = new SimpleStringProperty("");
    private final ObjectProperty<ArchiveEntry> selectedDocumentProperty = new SimpleObjectProperty<>();

    private final SimpleListProperty<Tag> allTagsProperty =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObservableSet<Tag> selectedFilterTags = FXCollections.observableSet();

    private final StringProperty archiveDateCreatedProperty = new SimpleStringProperty("");
    private final StringProperty archiveDateLastModifiedProperty = new SimpleStringProperty("");

    public ApplicationModel() {
        searchTextProperty.addListener((_, _, _) -> updateFilterPredicate());
        selectedFilterTags.addListener((SetChangeListener<Tag>) _ -> updateFilterPredicate());
    }

    private void updateFilterPredicate() {
        String searchText = searchTextProperty.get();
        boolean hasSearchText = searchText != null && !searchText.isBlank();
        String lowerCaseSearch = hasSearchText ? searchText.toLowerCase() : "";
        boolean hasSelectedTags = !selectedFilterTags.isEmpty();

        filteredDocuments.setPredicate(entry -> {
            boolean matchesSearch = !hasSearchText ||
                    entry.getName().toLowerCase().contains(lowerCaseSearch);
            boolean matchesTags = !hasSelectedTags ||
                    entry.getTags().stream().anyMatch(selectedFilterTags::contains);
            return matchesSearch && matchesTags;
        });
    }

    public void toggleFilterTag(Tag tag) {
        if (selectedFilterTags.contains(tag)) {
            selectedFilterTags.remove(tag);
        } else {
            selectedFilterTags.add(tag);
        }
    }

    public boolean isFilterTagSelected(Tag tag) {
        return selectedFilterTags.contains(tag);
    }

    public void setAllTags(Set<Tag> tags) {
        allTagsProperty.clear();
        allTagsProperty.addAll(tags);
    }

    public void setSelectedDocument(ArchiveEntry selectedDocument) {
        selectedDocumentProperty.setValue(selectedDocument);
        selectedDocumentNameProperty.setValue(selectedDocument.getName());
        descriptionProperty.setValue(selectedDocument.getSummary());
        updateDocumentTags();
    }

    public void updateDocumentTags() {
        tagsProperty.removeIf(_ -> true);
        tagsProperty.addAll(selectedDocumentProperty.get().getTags());
    }

    public void updateDescription(String description) {
        descriptionProperty.setValue(description);
        var selectedDocument = selectedDocumentProperty.get();
        if (selectedDocument != null) {
            selectedDocument.setSummary(description);
            refreshDocumentInList(selectedDocument);
        }
    }

    private void refreshDocumentInList(ArchiveEntry entry) {
        int index = documentsList.indexOf(entry);
        if (index >= 0) {
            documentsList.set(index, entry);
        }
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

    public void removeDocument(UUID documentId) {
        documentsList.removeIf(entry -> entry.getId().equals(documentId));
        if (selectedDocumentProperty.get() != null && selectedDocumentProperty.get().getId().equals(documentId)) {
            clearSelectedDocument();
        }
    }

    public void clearSelectedDocument() {
        selectedDocumentProperty.setValue(null);
        selectedDocumentNameProperty.setValue(null);
        descriptionProperty.setValue("");
        tagsProperty.clear();
    }
}
