package dev.arne.smartfiles.app;

import dev.arne.smartfiles.app.components.DocumentListCell;
import dev.arne.smartfiles.core.model.ArchiveEntry;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApplicationModel {

    private final StringProperty selectedDocumentNameProperty = new SimpleStringProperty(null);
    private final BooleanProperty lightModeActivated = new SimpleBooleanProperty(false);
    private final SimpleListProperty<DocumentListCell.ListItem> documentsProperty =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    public String getSelectedDocumentName() {
        return selectedDocumentNameProperty.get();
    }

    public void setSelectedDocumentName(String selectedDocumentName) {
        selectedDocumentNameProperty.setValue(selectedDocumentName);
    }

    public boolean isLightModeActivated() {
        return lightModeActivated.get();
    }

    public void setLightModeActivated(boolean lightModeActivated) {
        this.lightModeActivated.set(lightModeActivated);
    }

    public void toggleTheme() {
        setLightModeActivated(!isLightModeActivated());
    }

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
}
