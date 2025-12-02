package dev.arne.smartfiles.app;

import atlantafx.base.theme.Styles;
import dev.arne.smartfiles.SmartFilesApp;
import dev.arne.smartfiles.app.components.DocumentListCell;
import dev.arne.smartfiles.app.pdf.PdfImageRenderer;
import dev.arne.smartfiles.core.ArchiveService;
import dev.arne.smartfiles.core.SettingsService;
import dev.arne.smartfiles.core.model.ArchiveEntry;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
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

import java.io.IOException;
import java.util.UUID;

public class ApplicationViewBuilder implements Builder<Region> {

    private final Logger logger = LoggerFactory.getLogger(ApplicationViewBuilder.class);

    private final static int VBOX_SPACING = 3;

    private final ApplicationModel model;
    private final SettingsService settingsService;
    private final ArchiveService fileService;

    private Label documentName;
    private TextField searchTextField;
    private TextField newTagTextField;
    private ListView<ArchiveEntry> documentList;
    private ScrollPane documentPane;
    private ImageView imageView;
    private Dialog<String> dialog;

    public ApplicationViewBuilder(ApplicationModel model, SettingsService settingsService, ArchiveService archiveService) {
        this.model = model;
        this.settingsService = settingsService;
        this.fileService = archiveService;
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
        themeToggleButton.setOnAction(e -> {
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

        documentList = new ListView<>();
        alwaysVGrow(documentList);
        documentList.getStyleClass().add("sf-document-list");
        documentList.setCellFactory(listView -> DocumentListCell.createDocumentListCell());
        documentList.selectionModelProperty().get().selectedItemProperty()
                .addListener((_, _, newValue) -> selectDocumentFromListItem(newValue));
        documentList.itemsProperty().bind(model.getDocumentsProperty());

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
            var file = fileService.getFile(selectedItem.getId());
            try {
                var renderer = new PdfImageRenderer(file);
                imageView.setImage(renderer.renderPage(0));
                imageView.setFitWidth(documentPane.getViewportBounds().getWidth());
            } catch (IOException e) {
                logger.error("Cannot create renderer: {}", e.getMessage(), e);
            }
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
        documentName = createAreaLabel("Document name");
        documentName.textProperty().bind(model.getSelectedDocumentNameProperty());

        imageView = new ImageView();
        imageView.setPreserveRatio(true);

        documentPane = new ScrollPane();
        documentPane.setFitToWidth(true);
        documentPane.setFitToHeight(true);
        documentPane.setContent(imageView);

        vBox.getChildren().add(documentName);
        vBox.getChildren().add(documentPane);
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
        searchTextField = new TextField();
        searchTextField.setPromptText("Search");
        vBox.getChildren().add(createAreaLabel("Find document"));
        vBox.getChildren().add(searchTextField);
        return vBox;
    }

    private VBox createRightBottom() {
        var vBox = createVBoxColumn();
        vBox.disableProperty().bind(model.getSelectedDocumentNameProperty().isNull());
        newTagTextField = new TextField();
        newTagTextField.setPromptText("Add tag");
        vBox.getChildren().add(createAreaLabel("Document details"));
        vBox.getChildren().add(newTagTextField);
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
        settingsButton.setOnAction(e -> {
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
            fileService.manageFiles(db.getFiles());
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }
}
