package dev.arne.smartfiles.app.components;

import dev.arne.smartfiles.core.model.Tag;
import javafx.beans.property.ListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TagFilterView extends FlowPane {

    private final ListProperty<Tag> allTags;
    private final ObservableSet<Tag> selectedTags;
    private final Consumer<Tag> onTagToggle;
    private final Map<Tag, ToggleButton> tagButtons = new HashMap<>();

    public TagFilterView(ListProperty<Tag> allTags, ObservableSet<Tag> selectedTags, Consumer<Tag> onTagToggle) {
        setPadding(new Insets(5));
        setHgap(6);
        setVgap(6);
        setPrefWrapLength(Region.USE_COMPUTED_SIZE);

        this.allTags = allTags;
        this.selectedTags = selectedTags;
        this.onTagToggle = onTagToggle;

        renderTags(allTags.get());

        allTags.addListener((ListChangeListener<Tag>) _ -> renderTags(allTags.get()));
        selectedTags.addListener((SetChangeListener<Tag>) change -> {
            if (change.wasAdded()) {
                updateButtonState(change.getElementAdded(), true);
            }
            if (change.wasRemoved()) {
                updateButtonState(change.getElementRemoved(), false);
            }
        });
    }

    private void renderTags(List<Tag> tags) {
        getChildren().clear();
        tagButtons.clear();
        tags.forEach(this::renderTag);
    }

    private void renderTag(Tag tag) {
        ToggleButton button = new ToggleButton(tag.label());
        button.setSelected(selectedTags.contains(tag));
        button.getStyleClass().add("sf-tag-filter-button");
        button.setOnAction(_ -> onTagToggle.accept(tag));
        tagButtons.put(tag, button);
        getChildren().add(button);
    }

    private void updateButtonState(Tag tag, boolean selected) {
        ToggleButton button = tagButtons.get(tag);
        if (button != null) {
            button.setSelected(selected);
        }
    }
}
