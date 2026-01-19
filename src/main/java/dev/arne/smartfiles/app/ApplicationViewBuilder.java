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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.util.Builder;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
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
        vBox.getChildren().add(createFooter());
        return vBox;
    }

    private HBox createFooter() {
        var footer = new HBox(20);
        footer.getStyleClass().add("sf-footer");
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(8, 12, 8, 12));

        var createdLabel = new Label();
        createdLabel.textProperty().bind(model.getArchiveDateCreatedProperty().map(date -> "Created: " + date));
        createdLabel.getStyleClass().add("sf-footer-label");

        var modifiedLabel = new Label();
        modifiedLabel.textProperty().bind(model.getArchiveDateLastModifiedProperty().map(date -> "Last modified: " + date));
        modifiedLabel.getStyleClass().add("sf-footer-label");

        footer.getChildren().addAll(createdLabel, modifiedLabel);
        return footer;
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
        model.clearSelectedDocument();
        documentView.clear();
    }

    private void showDeleteConfirmation(UUID documentId) {
        var entry = archiveService.retrieveFileDetails(documentId);
        if (entry == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Document");
        alert.setHeaderText("Delete \"" + entry.getName() + "\"?");
        alert.setContentText("This action cannot be undone. The document will be permanently removed from the archive.");

        alert.initModality(Modality.APPLICATION_MODAL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            archiveService.deleteDocument(documentId);
        }
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

        var dateCreatedLabel = new Label();
        dateCreatedLabel.textProperty().bind(model.getDocumentDateCreatedProperty().map(d -> d.isEmpty() ? "" : "Created: " + d));
        dateCreatedLabel.getStyleClass().add("sf-footer-label");
        vBox.getChildren().add(dateCreatedLabel);

        var dateModifiedLabel = new Label();
        dateModifiedLabel.textProperty().bind(model.getDocumentDateLastModifiedProperty().map(d -> d.isEmpty() ? "" : "Modified: " + d));
        dateModifiedLabel.getStyleClass().add("sf-footer-label");
        vBox.getChildren().add(dateModifiedLabel);

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

        var spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        vBox.getChildren().add(spacer);

        var deleteButton = new Button("Delete Document", new FontIcon("ci-trash-can"));
        deleteButton.getStyleClass().add("sf-delete-button");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setOnAction(_ -> showDeleteConfirmation(model.getSelectedDocumentId()));
        vBox.getChildren().add(deleteButton);

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

        var content = new VBox(10);
        content.setPadding(new Insets(10));

        var inboxLabel = new Label("Inbox folder:");
        var inboxPathField = new TextField(settingsService.getInboxFolderPath());
        inboxPathField.setEditable(false);
        inboxPathField.setPrefWidth(300);

        var browseButton = new Button("Browse...");
        browseButton.setOnAction(_ -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Inbox Folder");
            File currentDir = new File(inboxPathField.getText());
            if (currentDir.exists() && currentDir.isDirectory()) {
                chooser.setInitialDirectory(currentDir);
            }
            File selected = chooser.showDialog(dialog.getOwner());
            if (selected != null) {
                inboxPathField.setText(selected.getAbsolutePath());
            }
        });

        var inboxRow = new HBox(10, inboxPathField, browseButton);
        inboxRow.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(inboxLabel, inboxRow);

        dialog.getDialogPane().setContent(content);

        ButtonType okType = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, cancelType);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == okType) {
                return inboxPathField.getText();
            }
            return null;
        });

        dialog.setOnHidden(_ -> {
            String result = dialog.getResult();
            if (result != null && !result.equals(settingsService.getInboxFolderPath())) {
                settingsService.setInboxFolderPath(result);
                logger.info("Inbox folder updated to: {}", result);
            }
            inboxPathField.setText(settingsService.getInboxFolderPath());
        });
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
