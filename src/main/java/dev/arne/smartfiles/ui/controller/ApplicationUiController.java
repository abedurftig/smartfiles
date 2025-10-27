package dev.arne.smartfiles.ui.controller;

import dev.arne.smartfiles.core.FileService;
import dev.arne.smartfiles.core.model.ArchiveEntryAddedEvent;
import dev.arne.smartfiles.ui.components.DocumentListItem;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class ApplicationUiController implements Initializable, ApplicationListener<ArchiveEntryAddedEvent> {

    private final Logger logger = LoggerFactory.getLogger(ApplicationUiController.class);

    private final FileService fileService;

    @FXML
    private ListView<DocumentListItem.ListItem> documentList;

    @FXML
    private Pane documentPane;

    @FXML
    private TextField searchTextField;

    @FXML
    private ListView tagFilterList;

    @FXML
    private Label documentName;

    public ApplicationUiController(FileService fileService) {
        this.fileService = fileService;
    }

    public void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            /* allow for both copying and moving, whatever user chooses */
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
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
        /* let the source know whether the string was successfully
         * transferred and used */
        event.setDropCompleted(success);
        event.consume();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        documentList.setCellFactory(listView -> DocumentListItem.createDocumentListItem());
        documentList.selectionModelProperty().get().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                documentName.setText(newValue.text());
            }
        });
        var items = fileService.getAll().stream().map(entry ->
                new DocumentListItem.ListItem(entry.getName(), entry.getSummary(), entry.getId().toString())).toList();
        documentList.getItems().addAll(items);

    }

    @Override
    public void onApplicationEvent(ArchiveEntryAddedEvent event) {
        var entry = event.getArchiveEntry();
        documentList.getItems().add(
                new DocumentListItem.ListItem(entry.getName(), entry.getSummary(), entry.getId().toString())
        );
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
