package dev.arne.smartfiles.app.components;

import dev.arne.smartfiles.core.model.Tag;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

public class TagListCell extends ListCell<Tag> {

    private final Label label = new Label();
    private final VBox layout = new VBox(label);

    private TagListCell() {
        super();
        this.getStyleClass().add("sf-document-list-item");
        label.setStyle("-fx-font-size: 11px;");
        layout.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(Tag tag, boolean empty) {
        super.updateItem(tag, empty);
        setText(null);

        if (empty || tag == null || tag.label() == null) {
            label.setText(null);
            setGraphic(null);
        } else {
            label.setText(tag.label());
            setGraphic(layout);
        }
    }

    public static TagListCell createTagListCell() {
        return new TagListCell();
    }
}
