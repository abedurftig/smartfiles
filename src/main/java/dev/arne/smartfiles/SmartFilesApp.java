package dev.arne.smartfiles;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import dev.arne.smartfiles.app.ApplicationController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;

@SpringBootApplication
@EnableConfigurationProperties
public class SmartFilesApp extends Application {

    private final static int INITIAL_APP_WIDTH = 1024;
    private final static int INITIAL_APP_HEIGHT = 768;

    private final Logger logger = LoggerFactory.getLogger(SmartFilesApp.class);
    private ConfigurableApplicationContext context;

    @Override
    public void init() throws Exception {
        logger.info("Initializing application...");
        super.init();
        context = SpringApplication.run(SmartFilesApp.class);
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping application...");
        context.close();
        super.stop();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        var controller = context.getBean(ApplicationController.class);
        primaryStage.setScene(createScene(controller));
        primaryStage.show();
    }

    private Scene createScene(ApplicationController controller) {
        var scene = new Scene(controller.getView(), INITIAL_APP_WIDTH, INITIAL_APP_HEIGHT);
        scene.getStylesheets().add(Objects.requireNonNull(this.getClass().getResource("/css/application.css")).toExternalForm());
        return scene;
    }

//    public static void setLightTheme() {
//        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
//    }
//
//    public static void setDarkTheme() {
//        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
//    }

    private static void setThemeWithContext(Runnable themeOperation) {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(SmartFilesApp.class.getClassLoader());
            themeOperation.run();
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    public static void setLightTheme() {
        setThemeWithContext(() -> Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet()));
    }

    public static void setDarkTheme() {
        setThemeWithContext(() -> Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet()));
    }


    public static void main(String[] args) {
        launch(args);
    }
}
