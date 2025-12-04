package dev.arne.smartfiles.app.components;

import dev.arne.smartfiles.core.model.Tag;
import javafx.beans.property.ListProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;

import java.util.List;

public class WrappingListView extends FlowPane {

    private final ListProperty<Tag> items;

    public WrappingListView(ListProperty<Tag> items) {
        setPadding(new Insets(5));
        setHgap(8);
        setVgap(8);
        setPrefWrapLength(Region.USE_COMPUTED_SIZE);

        this.items = items;
        renderItems(items.get());
        items.addListener((ListChangeListener<Tag>) change -> {
            renderItems(items.get());
        });
    }

    private void renderItems(List<Tag> items) {
        getChildren().clear();
        items.forEach(this::renderItem);
    }

    private void renderItem(Tag tag) {
        getChildren().add(new Label(tag.label()));
    }
}
