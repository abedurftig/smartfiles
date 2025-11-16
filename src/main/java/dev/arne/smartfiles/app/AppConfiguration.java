package dev.arne.smartfiles.app;

import dev.arne.smartfiles.core.FileService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public ApplicationModel applicationModel(FileService fileService) {
        var model = new ApplicationModel();
        model.setDocumentsFromArchiveEntries(fileService.getAll());
        return model;
    }

    @Bean
    public ApplicationInteractor applicationInteractor(ApplicationModel model) {
        return new ApplicationInteractor(model);
    }

    @Bean
    public ApplicationViewBuilder applicationViewBuilder(
            ApplicationModel model,
            ApplicationInteractor interactor,
            FileService fileService
    ) {
        return new ApplicationViewBuilder(model, interactor, fileService);
    }

    @Bean
    public ApplicationController applicationController(ApplicationViewBuilder viewBuilder) {
        return new ApplicationController(viewBuilder);
    }
}
