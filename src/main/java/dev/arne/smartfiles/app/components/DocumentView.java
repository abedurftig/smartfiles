package dev.arne.smartfiles.app.components;

import dev.arne.smartfiles.app.pdf.PdfImageRenderer;
import javafx.concurrent.Task;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.arne.smartfiles.app.ApplicationViewBuilder.APP_EXECUTOR;

public class DocumentView extends ScrollPane {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(DocumentView.class);

    private final VBox content = new VBox();
    private final List<Task<?>> activeTasks = new ArrayList<>();
    private final Map<Integer, ImageView> currentImageViews = new HashMap<>();
    private PdfImageRenderer currentRenderer;
    private String currentFilePath;

    public DocumentView() {
        super();
//        content.getStyleClass().add("sf-document-view");
        this.setContent(content);
        content.setFillWidth(true);
    }

    public void clear() {
        for (Task<?> task : activeTasks) {
            if (task != null && task.isRunning()) {
                task.cancel();
            }
        }
        activeTasks.clear();
        currentImageViews.clear();
        content.getChildren().clear();

        if (currentRenderer != null) {
            try {
                currentRenderer.close();
            } catch (IOException e) {
                logger.warn("Failed to close PDF renderer", e);
            }
            currentRenderer = null;
            currentFilePath = null;
        }
    }

    public void viewFile(File file) {

        if (!file.isFile() || !file.getName().contains(".pdf")) return;

        for (Task<?> task : activeTasks) {
            if (task != null && task.isRunning()) {
                logger.info("Cancelling task: {}", task);
                task.cancel();
            }
        }
        activeTasks.clear();
        currentImageViews.clear();

        this.setVvalue(0.0);

        String filePath = file.getAbsolutePath();

        // Close previous renderer if switching to a different file
        if (currentRenderer != null && !filePath.equals(currentFilePath)) {
            try {
                currentRenderer.close();
            } catch (IOException e) {
                logger.warn("Failed to close previous PDF renderer", e);
            }
            currentRenderer = null;
            currentFilePath = null;
        }

        // Create new renderer if needed
        if (currentRenderer == null) {
            try {
                currentRenderer = new PdfImageRenderer(file);
                currentFilePath = filePath;
            } catch (Exception e) {
                logger.error("Failed to open PDF file: {}", filePath, e);
                return;
            }
        }

        content.getChildren().clear();
        var number = currentRenderer.getNumberOfPages();
        for (int i = 0; i < number; i++) {
            ImageView imageView = createPlaceholder();
            currentImageViews.put(i, imageView);

            // Create and submit the rendering task
            Task<Image> renderTask = createRenderTask(currentRenderer, i);
            APP_EXECUTOR.submit(renderTask);
            activeTasks.add(renderTask); // Track the new task
        }

    }

    private ImageView createPlaceholder() {
        var imageView = new ImageView();
        imageView.setPreserveRatio(true);
        // Bind fitWidth to the VBox width for responsive resizing
        imageView.fitWidthProperty().bind(content.widthProperty());
        content.getChildren().add(imageView);
        return imageView;
    }


    private Task<Image> createRenderTask(PdfImageRenderer renderer, int pageIndex) {

        Task<Image> renderTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                if (isCancelled()) {
                    logger.info("Page " + pageIndex + " rendering skipped before starting.");
                    return null;
                }

                try {
                    // The actual rendering call that might throw the Interrupt Exception
                    return renderer.renderPage(pageIndex);
                } catch (ClosedByInterruptException e) {
                    logger.info("Rendering interrupted for page {}.", pageIndex);
                } catch (ClosedChannelException e) {
                    logger.info("Channel closed during rendering for page {}.", pageIndex);
                }
                return null;
            }
        };

        renderTask.setOnSucceeded(_ -> {
            // JavaFX Thread work: Check if the task result is still relevant

            // Get the specific ImageView placeholder associated with this page index
            ImageView targetView = currentImageViews.get(pageIndex);

            // ONLY proceed if the ImageView is still tracked (i.e., VBox hasn't been cleared for a new document)
            if (targetView != null) {
                targetView.setImage(renderTask.getValue());
            } else {
                logger.info("Target view is null for page {}. Task result discarded.", pageIndex);
            }
            // If targetView is null, the result is discarded, avoiding errors.
        });

        renderTask.setOnFailed(_ -> {
            // Handle rendering errors gracefully
            System.err.println("Rendering failed for page " + pageIndex);
//            renderTask.getException().printStackTrace();
        });

        return renderTask;
    }
}
