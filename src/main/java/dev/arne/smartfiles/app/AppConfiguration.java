package dev.arne.smartfiles.app;

import dev.arne.smartfiles.core.ArchiveService;
import dev.arne.smartfiles.core.SettingsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public ApplicationModel applicationModel(SettingsService settingsService, ArchiveService archiveService) {
        var model = new ApplicationModel();
        model.setLightModeActivated(settingsService.isLightThemeActive());
        model.setDocumentsFromArchiveEntries(archiveService.getAll());
        return model;
    }

    @Bean
    public ApplicationInteractor applicationInteractor(ApplicationModel model) {
        return new ApplicationInteractor(model);
    }

    @Bean
    public ApplicationViewBuilder applicationViewBuilder(
            ApplicationModel model,
            SettingsService settingsService,
            ArchiveService archiveService
    ) {
        return new ApplicationViewBuilder(model, settingsService, archiveService);
    }

    @Bean
    public ApplicationController applicationController(ApplicationViewBuilder viewBuilder) {
        return new ApplicationController(viewBuilder);
    }
}
