package dev.arne.smartfiles.app;

import javafx.scene.layout.Region;

public class ApplicationController {

    private final ApplicationViewBuilder viewBuilder;

    public ApplicationController(ApplicationViewBuilder viewBuilder) {
        this.viewBuilder = viewBuilder;
    }

    public Region getView() {
        return viewBuilder.build();
    }
}
