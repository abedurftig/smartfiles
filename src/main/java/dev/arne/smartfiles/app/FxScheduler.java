package dev.arne.smartfiles.app;

import javafx.application.Platform;

/**
 * Functional interface for scheduling work on the JavaFX Application Thread.
 * Allows for easy testing by substituting direct execution in test environments.
 */
@FunctionalInterface
public interface FxScheduler {

    void runLater(Runnable runnable);

    /**
     * Returns a scheduler that uses Platform.runLater() for JavaFX Application Thread execution.
     */
    static FxScheduler platform() {
        return Platform::runLater;
    }

    /**
     * Returns a scheduler that executes runnables directly on the calling thread.
     * Useful for testing without a functioning JavaFX runtime.
     */
    static FxScheduler direct() {
        return Runnable::run;
    }
}
