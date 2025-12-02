package dev.arne.smartfiles.app.components;

import dev.arne.smartfiles.core.model.ArchiveEntry;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

public class DocumentListCell extends ListCell<ArchiveEntry> {

    private final Label title = new Label();
    private final Label detail = new Label();
    private final VBox layout = new VBox(title, detail);

    private DocumentListCell() {
        super();
        this.getStyleClass().add("sf-document-list-item");
        title.setStyle("-fx-font-size: 14px;");
        detail.setStyle("-fx-font-size: 11px;");
        layout.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(ArchiveEntry item, boolean empty) {
        super.updateItem(item, empty);

        setText(null);

        if (empty || item == null || item.getName() == null) {
            title.setText(null);
            detail.setText(null);
            setGraphic(null);
        } else {
            title.setText(item.getName());
            detail.setText(item.getSummary());
            setGraphic(layout);
        }
    }

    public static DocumentListCell createDocumentListCell() {
        return new DocumentListCell();
    }
}