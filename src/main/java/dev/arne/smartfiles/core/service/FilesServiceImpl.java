package dev.arne.smartfiles.core.service;

import dev.arne.smartfiles.core.FileService;
import dev.arne.smartfiles.core.configuration.SmartFilesConfiguration;
import dev.arne.smartfiles.core.model.AggregateRoot;
import dev.arne.smartfiles.core.model.ApplicationSettings;
import dev.arne.smartfiles.core.model.Archive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

@Service
public class FilesServiceImpl implements FileService {

    private final Logger logger = LoggerFactory.getLogger(FilesServiceImpl.class);

    private static final String SMARTFILES_STORE_FILE = "archive.json";
    private static final FilenameFilter STORE_FILTER = (_, name) -> SMARTFILES_STORE_FILE.equals(name);

    private static final String SMARTFILES_SETTINGS_FILE = "settings.json";
    private static final FilenameFilter SETTINGS_FILTER = (_, name) -> SMARTFILES_SETTINGS_FILE.equals(name);

    private final SmartFilesConfiguration configuration;
    private final ObjectMapper objectMapper;

    public FilesServiceImpl(SmartFilesConfiguration configuration, ObjectMapper objectMapper) {
        this.configuration = configuration;
        this.objectMapper = objectMapper;
        makeSureRootDirectoryExists();
    }

    @Override
    public String getTenantDirectory() {
        return configuration.getTenantDirectory();
    }

    @Override
    public ApplicationSettings loadApplicationSettings() {
        return loadFromDisk(SETTINGS_FILTER, ApplicationSettings.class, ApplicationSettings::empty);
    }

    @Override
    public void saveApplicationSettings(ApplicationSettings settings) {
        settings.updateLastModified();
        writeToDisk(SMARTFILES_SETTINGS_FILE, settings);
    }

    @Override
    public Archive loadArchive() {
        return loadFromDisk(STORE_FILTER, Archive.class, Archive::empty);
    }

    @Override
    public void saveArchive(Archive archive) {
        archive.updateLastModified();
        writeToDisk(SMARTFILES_STORE_FILE, archive);
    }

    private void makeSureRootDirectoryExists() {

        var dir = new File(configuration.getTenantDirectory());
        if (dir.exists() && dir.isDirectory()) {
            logger.info("Root directory exists: {}", dir.getAbsolutePath());
        } else {
            logger.info("Creating new root directory: {}", dir.getAbsolutePath());
            try {
                Files.createDirectories(Path.of(configuration.getTenantDirectory(), "files"));
            } catch (IOException e) {
                throw new RuntimeException("Cannot create root directory: " + dir.getAbsolutePath());
            }
        }
    }

    private void writeToDisk(String fileName, AggregateRoot root) {

        var content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        var dir = new File(configuration.getTenantDirectory());
        var file = new File(dir, fileName);
        try {
            Files.writeString(file.toPath(), content);
            logger.info("Wrote to disk: {}", file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Cannot write " + fileName + " to disk: " + dir.getAbsolutePath());
        }
    }

    private <E extends AggregateRoot> E loadFromDisk(FilenameFilter filter, Class<E> clazz, Supplier<E> factory) {
        var dir = new File(configuration.getTenantDirectory());
        var maybeFile = Arrays.stream(Objects.requireNonNull(dir.listFiles(filter))).findFirst();
        if (maybeFile.isEmpty()) {
            logger.info("No file found");
            return factory.get();
        } else {
            logger.info("Loaded from disk: {}", maybeFile.get().getName());
            return objectMapper.readValue(maybeFile.get(), clazz);
        }
    }
}
