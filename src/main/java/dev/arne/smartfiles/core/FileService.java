package dev.arne.smartfiles.core;

import dev.arne.smartfiles.core.model.ApplicationSettings;
import dev.arne.smartfiles.core.model.Archive;

public interface FileService {

    String getTenantDirectory();

    ApplicationSettings loadApplicationSettings();

    void saveApplicationSettings(ApplicationSettings settings);

    Archive loadArchive();

    void saveArchive(Archive archive);
}
