package dev.arne.smartfiles.ui;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StageManager {

    private final Logger logger = LoggerFactory.getLogger(StageManager.class);

    public void showMainStage(Stage stage, Callback<Class<?>, Object> controllerFactory) throws IOException {
        logger.info("Starting application...");

        var loader = new FXMLLoader();
        loader.setControllerFactory(controllerFactory);
        loader.setLocation(getClass().getResource("/fxml/layout.fxml"));

        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        Parent root = loader.load();
        var scene = new Scene(root, 1024, 768);
        stage.setScene(scene);
        stage.show();
    }
}
