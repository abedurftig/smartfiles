package dev.arne.smartfiles.app;

import atlantafx.base.theme.Styles;
import dev.arne.smartfiles.SmartFilesApp;
import dev.arne.smartfiles.app.components.DocumentListCell;
import dev.arne.smartfiles.app.components.DocumentView;
import dev.arne.smartfiles.app.components.TagFilterView;
import dev.arne.smartfiles.app.components.WrappingListView;
import dev.arne.smartfiles.core.ArchiveService;
import dev.arne.smartfiles.core.SettingsService;
import dev.arne.smartfiles.core.model.ArchiveEntry;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Builder;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApplicationViewBuilder implements Builder<Region> {

    public static final ExecutorService APP_EXECUTOR = Executors.newCachedThreadPool();

    private final Logger logger = LoggerFactory.getLogger(ApplicationViewBuilder.class);

    private final static int VBOX_SPACING = 3;

    private final ApplicationModel model;
    private final SettingsService settingsService;
    private final ArchiveService archiveService;

    private TextField newTagTextField;
    private TextField descriptionField;
    private Dialog<String> dialog;
    private DocumentView documentView;

    public ApplicationViewBuilder(ApplicationModel model, SettingsService settingsService, ArchiveService archiveService) {
        this.model = model;
        this.settingsService = settingsService;
        this.archiveService = archiveService;
    }

    @Override
    public Region build() {
        initDialog();
        activeTheme();
        return createRoot();
    }

    private VBox createRoot() {
        VBox vBox = new VBox();
        alwaysVGrow(vBox);
        vBox.getChildren().add(createToolBar());
        vBox.getChildren().add(createLeft());
        return vBox;
    }

    private ToolBar createToolBar() {
        var toolBar = new ToolBar();
        toolBar.getStyleClass().add("sf-tool-bar");
        toolBar.getItems().add(createThemeToggleButton());
        toolBar.getItems().add(createSettingsDialogAndButton());
        return toolBar;
    }

    private Button createThemeToggleButton() {
        logger.info("lightModeActivated: {}", model.isLightModeActivated());
        var iconCode = model.isLightModeActivated() ? "ci-asleep" : "ci-awake";
        var themeToggleButton = new Button("", new FontIcon(iconCode));
        themeToggleButton.setOnAction(_ -> {
            settingsService.toggleLightThemeActive();
            if (model.isLightModeActivated()) {
                themeToggleButton.setGraphic(new FontIcon("ci-asleep"));
                SmartFilesApp.setLightTheme();
            } else {
                themeToggleButton.setGraphic(new FontIcon("ci-awake"));
                SmartFilesApp.setDarkTheme();
            }
        });
        return themeToggleButton;
    }

    private SplitPane createLeft() {
        SplitPane left = new SplitPane();
        alwaysVGrow(left);
        left.getItems().add(createLeftContent());
        left.getItems().add(createCenter());
        left.setDividerPositions(0.25);
        return left;
    }

    private VBox createLeftContent() {
        var vBox = createVBoxColumn();
        vBox.setAlignment(Pos.TOP_LEFT);

        ListView<ArchiveEntry> documentList = new ListView<>();
        alwaysVGrow(documentList);
        documentList.getStyleClass().add("sf-document-list");
        documentList.setCellFactory(_ -> DocumentListCell.createDocumentListCell());
        documentList.selectionModelProperty().get().selectedItemProperty()
                .addListener((_, _, newValue) -> selectDocumentFromListItem(newValue));
        documentList.setItems(model.getFilteredDocuments());

        vBox.getChildren().add(createAreaLabel("Documents"));
        vBox.getChildren().add(documentList);

        var separator = new Separator(Orientation.HORIZONTAL);
        separator.getStyleClass().add(Styles.SMALL);
        vBox.getChildren().add(separator);
        vBox.getChildren().add(createAreaLabel("Add documents"));

        var dropBox = new VBox();
        dropBox.setFillWidth(true);
        dropBox.setMinHeight(150);
        dropBox.setAlignment(Pos.CENTER);
        dropBox.setOnDragOver(this::handleDragOver);
        dropBox.setOnDragDropped(this::handleDragDropped);
        dropBox.getChildren().add(createAreaLabel("Drop files here"));

        vBox.getChildren().add(dropBox);

        return vBox;
    }

    private void selectDocumentFromListItem(ArchiveEntry selectedItem) {

        if (selectedItem == null) {
            clearSelectedDocument();
        } else {
            model.setSelectedDocument(selectedItem);
            var file = archiveService.getFile(selectedItem.getId());
            documentView.viewFile(file);
        }
    }

    private void clearSelectedDocument() {

    }

    private SplitPane createCenter() {
        SplitPane right = new SplitPane();
        alwaysVGrow(right);
        right.getItems().add(createCenterContent());
        right.getItems().add(createRight());
        right.setDividerPositions(0.75);
        return right;
    }

    private VBox createCenterContent() {
        var vBox = createVBoxColumn();
        Label documentName = createAreaLabel("Document name");
        documentName.textProperty().bind(model.getSelectedDocumentNameProperty());

        documentView = new DocumentView();
        documentView.setFitToWidth(true);
        documentView.setFitToHeight(true);

        vBox.getChildren().add(documentName);
        vBox.getChildren().add(documentView);
        return vBox;
    }

    private SplitPane createRight() {
        SplitPane right = new SplitPane();
        right.setOrientation(Orientation.VERTICAL);
        right.getItems().add(createRightTop());
        right.getItems().add(createRightBottom());
        right.setDividerPositions(0.5);
        return right;
    }

    private VBox createRightTop() {
        var vBox = createVBoxColumn();
        vBox.setFocusTraversable(true);
        vBox.setOnMousePressed(_ -> vBox.requestFocus());

        TextField searchTextField = new TextField();
        searchTextField.setPromptText("Search");
        searchTextField.textProperty().bindBidirectional(model.getSearchTextProperty());
        vBox.getChildren().add(createAreaLabel("Find document"));
        vBox.getChildren().add(searchTextField);

        vBox.getChildren().add(createAreaLabel("Filter by tags"));
        var tagFilterView = new TagFilterView(
                model.getAllTagsProperty(),
                model.getSelectedFilterTags(),
                model::toggleFilterTag
        );
        vBox.getChildren().add(tagFilterView);

        return vBox;
    }

    private VBox createRightBottom() {
        var vBox = createVBoxColumn();
        vBox.setFocusTraversable(true);
        vBox.setOnMousePressed(_ -> vBox.requestFocus());
        vBox.disableProperty().bind(model.getSelectedDocumentNameProperty().isNull());

        vBox.getChildren().add(createAreaLabel("Document details"));

        descriptionField = new TextField();
        descriptionField.setPromptText("Click to add description");
        descriptionField.textProperty().bindBidirectional(model.getDescriptionProperty());
        descriptionField.setEditable(false);
        descriptionField.getStyleClass().add("sf-description-field");
        descriptionField.setOnMouseClicked(_ -> {
            descriptionField.setEditable(true);
            descriptionField.requestFocus();
            descriptionField.selectAll();
        });
        descriptionField.setOnAction(_ -> {
            logger.info("Updating description: {}", descriptionField.getText());
            archiveService.updateDescription(model.getSelectedDocumentId(), descriptionField.getText());
            descriptionField.setEditable(false);
            vBox.requestFocus();
        });
        descriptionField.focusedProperty().addListener((_, _, focused) -> {
            if (!focused && descriptionField.isEditable()) {
                descriptionField.setEditable(false);
                descriptionField.setText(model.getDescriptionProperty().get());
            }
        });
        vBox.getChildren().add(descriptionField);

        newTagTextField = new TextField();
        newTagTextField.setPromptText("Add tag");
        newTagTextField.setOnAction(_ -> {
            logger.info("Adding tag: {}", newTagTextField.getText());
            archiveService.addTag(model.getSelectedDocumentId(), newTagTextField.getText());
            newTagTextField.clear();
        });
        vBox.getChildren().add(newTagTextField);

        vBox.getChildren().add(createAreaLabel("Tags"));
        vBox.getChildren().add(new WrappingListView(model.getTagsProperty()));
        return vBox;
    }

    private Label createAreaLabel(String text) {
        var label = new Label(text);
        label.getStyleClass().add("sf-area-label");
        label.setPadding(new Insets(0));
        return label;
    }

    private VBox createVBoxColumn() {
        var vBox = new VBox(VBOX_SPACING);
        vBox.setPadding(new Insets(5));
        alwaysVGrow(vBox);
        return vBox;
    }

    private void alwaysVGrow(Node node) {
        VBox.setVgrow(node, Priority.ALWAYS);
    }

    private Button createSettingsDialogAndButton() {
        var settingsButton = new Button("", new FontIcon("ci-settings"));
        settingsButton.setOnAction(_ -> {
            if (!dialog.isShowing()) {
                dialog.showAndWait();
            }
        });
        return settingsButton;
    }

    private void activeTheme() {
        if (model.isLightModeActivated()) {
            SmartFilesApp.setLightTheme();
        } else {
            SmartFilesApp.setDarkTheme();
        }
    }

    private void initDialog() {
        dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Settings");
        ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialog.setContentText("This is a sample dialog");
        dialog.getDialogPane().getButtonTypes().add(type);
    }

    public void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    public void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            archiveService.manageFiles(db.getFiles());
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }
}
