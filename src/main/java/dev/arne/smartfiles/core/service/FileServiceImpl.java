package dev.arne.smartfiles.core.service;

import dev.arne.smartfiles.core.FileService;
import dev.arne.smartfiles.core.configuration.SmartFilesConfiguration;
import dev.arne.smartfiles.core.model.Archive;
import dev.arne.smartfiles.core.model.ArchiveEntry;
import dev.arne.smartfiles.core.model.ArchiveEntryAddedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService, ApplicationListener<ApplicationEvent> {

    private static final String SMARTFILES_FILE = "smartfiles.json";
    private static final FilenameFilter FILTER = (dir1, name) -> SMARTFILES_FILE.equals(name);

    private final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    private final SmartFilesConfiguration configuration;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher publisher;

    private final Archive archive;

    public FileServiceImpl(SmartFilesConfiguration configuration, ObjectMapper objectMapper, ApplicationEventPublisher publisher) {
        this.configuration = configuration;
        this.objectMapper = objectMapper;
        this.publisher = publisher;
        makeSureRootDirectoryExists();
        this.archive = readFromDisk();
    }

    @Override
    public List<ArchiveEntry> manageFiles(List<File> files) {
        var newEntries = files.stream().map(file -> {
            try {
                var copy = Files.copy(file.toPath(), copyPath(file)).toFile();
                logger.info("created copy of file '{}' at: {}", file.getName(), copy.getAbsolutePath());
                return archive.addArchiveEntryFromFile(copy, file.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();
        for (var newEntry : newEntries) {
            publisher.publishEvent(new ArchiveEntryAddedEvent(newEntry));
        }
        writeToDisk(archive);
        return newEntries;
    }

    @Override
    public List<ArchiveEntry> getAll() {
        return archive.getArchiveEntries().values().stream().toList();
    }

    @Override
    public ArchiveEntry retrieveFileDetails(UUID id) {
        return archive.getArchiveEntries().get(id);
    }

    @Override
    public File getFile(UUID id) {
        var entry = retrieveFileDetails(id);
        if (entry != null) {
            return new File(entry.getAbsolutePath());
        }
        return null;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {

        if ("ApplicationStartedEvent".equals(event.getClass().getSimpleName())) {
//            makeSureRootDirectoryExists();
//            archive = readFromDisk();
        } else if ("ContextClosedEvent".equals(event.getClass().getSimpleName())) {
            writeToDisk(archive);
        }
    }

    private void makeSureRootDirectoryExists() {

        var dir = new File(configuration.getCurrentDirectory());
        if (dir.exists() && dir.isDirectory()) {
            logger.info("root directory exists: {}", dir.getAbsolutePath());
        } else {
            logger.info("creating new root directory: {}", dir.getAbsolutePath());
            try {
                Files.createDirectories(Path.of(configuration.getCurrentDirectory(), "files"));
            } catch (IOException e) {
                throw new RuntimeException("Cannot create root directory: " + dir.getAbsolutePath());
            }
        }
    }

    private Archive readFromDisk() {
        var dir = new File(configuration.getCurrentDirectory());
        var maybeFile = Arrays.stream(dir.listFiles(FILTER)).findFirst();
        if (maybeFile.isEmpty()) {
            logger.info("No smartfiles.json found");
            return Archive.empty();
        } else {
            logger.info("Loaded archive from smartfiles.json");
            return objectMapper.readValue(maybeFile.get(), Archive.class);
        }
    }

    private void writeToDisk(Archive archive) {

        archive.updateLastModified();
        var content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(archive);
        var dir = new File(configuration.getCurrentDirectory());
        var file = new File(dir, "smartfiles.json");
        try {
            Files.writeString(file.toPath(), content);
            logger.info("Wrote archive to disk: {}", file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Cannot write archive to disk: " + file.getAbsolutePath());
        }
    }

    private Path copyPath(File file) {
        var path = configuration.getCurrentDirectory() +
                FileSystems.getDefault().getSeparator() +
                "files" +
                FileSystems.getDefault().getSeparator() +
                file.getName();
        return Path.of(path);
    }
}
