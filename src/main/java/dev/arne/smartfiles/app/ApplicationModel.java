package dev.arne.smartfiles.app;

import dev.arne.smartfiles.app.components.DocumentListCell;
import dev.arne.smartfiles.core.model.ArchiveEntry;
import javafx.beans.property.*;
import javafx.collections.FXCollections;

import java.util.List;

public class ApplicationModel {

    private final StringProperty selectedDocumentName = new SimpleStringProperty(null);

    public String getSelectedDocumentName() {
        return selectedDocumentName.get();
    }

    public StringProperty selectedDocumentNameProperty() {
        return selectedDocumentName;
    }

    public void setSelectedDocumentName(String selectedDocumentName) {
        this.selectedDocumentName.set(selectedDocumentName);
    }

    private final BooleanProperty lightModeActivated = new SimpleBooleanProperty(false);

    public BooleanProperty lightModeActivatedProperty() {
        return lightModeActivated;
    }

    public boolean isLightModeActivated() {
        return lightModeActivated.get();
    }

    public void setLightModeActivated(boolean lightModeActivated) {
        this.lightModeActivated.set(lightModeActivated);
    }

    public void toggleTheme() {
        setLightModeActivated(!isLightModeActivated());
        isLightModeActivated();
    }

    private final SimpleListProperty<DocumentListCell.ListItem> documentsProperty =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    public void setDocumentsFromArchiveEntries(List<ArchiveEntry> archiveEntries) {
        var items = archiveEntries.stream().map(this::archiveEntryToListItem).toList();
        documentsProperty.addAll(items);
    }

    public void addDocumentFromArchiveEntry(ArchiveEntry archiveEntry) {
        documentsProperty.addAll(archiveEntryToListItem(archiveEntry));
    }

    private DocumentListCell.ListItem archiveEntryToListItem(ArchiveEntry entry) {
        return new DocumentListCell.ListItem(entry.getName(), entry.getSummary(), entry.getId().toString());
    }

    public SimpleListProperty<DocumentListCell.ListItem> getDocumentsProperty() {
        return documentsProperty;
    }
}
