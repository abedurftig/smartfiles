package dev.arne.smartfiles.ui.components;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

public class DocumentListItem extends ListCell<DocumentListItem.ListItem> {

    private final Label title = new Label();
    private final Label detail = new Label();
    private final VBox layout = new VBox(title, detail);

    private DocumentListItem() {
        super();
        title.setStyle("-fx-font-size: 11px;");
    }

    @Override
    protected void updateItem(ListItem item, boolean empty) {
        super.updateItem(item, empty);

        setText(null);

        if (empty || item == null || item.text() == null) {
            title.setText(null);
            detail.setText(null);
            setGraphic(null);
        } else {
            title.setText(item.text());
            detail.setText(
                    item.detail() != null
                            ? item.detail()
                            : "Undefined"
            );
            setGraphic(layout);
        }
    }

    public static DocumentListItem createDocumentListItem() {
        return new DocumentListItem();
    }

    public record ListItem(String text, String detail, String value) {}
}