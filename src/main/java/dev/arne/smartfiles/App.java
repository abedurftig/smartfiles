package dev.arne.smartfiles;

import dev.arne.smartfiles.ui.StageManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;


/**
 * JavaFX + Spring Boot App
 */
@SpringBootApplication
@EnableConfigurationProperties
public class App extends Application {

    private final Logger logger = LoggerFactory.getLogger(App.class);
    private ConfigurableApplicationContext context;
    private final StageManager stageManager = new StageManager();

    @Override
    public void init() throws Exception {
        logger.info("Initializing application...");
        super.init();
        context = SpringApplication.run(App.class);
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping application...");
        context.close();
        super.stop();
    }

    @Override
    public void start(Stage stage) throws IOException {
        stageManager.showMainStage(stage, context::getBean);
    }

    public static void main(String[] args) {
        launch(App.class);
    }
}