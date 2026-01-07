package dev.arne.smartfiles.core.service;

import dev.arne.smartfiles.core.ArchiveService;
import dev.arne.smartfiles.core.FileService;
import dev.arne.smartfiles.core.events.AllTagsUpdatedEvent;
import dev.arne.smartfiles.core.events.ArchiveLastModifiedUpdatedEvent;
import dev.arne.smartfiles.core.events.DocumentDeletedEvent;
import dev.arne.smartfiles.core.events.DocumentDescriptionUpdatedEvent;
import dev.arne.smartfiles.core.events.DocumentTagAddedEvent;
import dev.arne.smartfiles.core.model.Archive;
import dev.arne.smartfiles.core.model.ArchiveEntry;
import dev.arne.smartfiles.core.events.ArchiveEntryAddedEvent;
import dev.arne.smartfiles.core.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ArchiveServiceImpl implements ArchiveService, ApplicationListener<ContextClosedEvent> {

    private final Logger logger = LoggerFactory.getLogger(ArchiveServiceImpl.class);

    private final FileService fileService;
    private final ApplicationEventPublisher publisher;

    private final Archive archive;

    public ArchiveServiceImpl(FileService fileService, ApplicationEventPublisher publisher) {
        this.fileService = fileService;
        this.publisher = publisher;
        this.archive = fileService.loadArchive();
    }

    @Override
    public List<ArchiveEntry> manageFiles(List<File> files) {
        var newEntries = files.stream().map(file -> {
            try {
                var copy = Files.copy(file.toPath(), copyPath(file)).toFile();
                logger.info("Created copy of file '{}' at: {}", file.getName(), copy.getAbsolutePath());
                return archive.addArchiveEntryFromFile(copy, file.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();
        for (var newEntry : newEntries) {
            publisher.publishEvent(new ArchiveEntryAddedEvent(newEntry));
        }
        saveArchiveAndPublishUpdate();
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
    public void addTag(UUID selectedDocumentId, String text) {
        var entry = archive.getArchiveEntries().get(selectedDocumentId);
        var newTag = new Tag(text);
        entry.getTags().add(newTag);
        publisher.publishEvent(new DocumentTagAddedEvent(newTag, selectedDocumentId));
        publisher.publishEvent(new AllTagsUpdatedEvent(getAllUniqueTags()));
        saveArchiveAndPublishUpdate();
    }

    @Override
    public void updateDescription(UUID documentId, String description) {
        var entry = archive.getArchiveEntries().get(documentId);
        entry.setSummary(description);
        entry.updateLastModified();
        publisher.publishEvent(new DocumentDescriptionUpdatedEvent(documentId, description));
        saveArchiveAndPublishUpdate();
    }

    @Override
    public Set<Tag> getAllUniqueTags() {
        Set<Tag> allTags = new HashSet<>();
        for (ArchiveEntry entry : archive.getArchiveEntries().values()) {
            allTags.addAll(entry.getTags());
        }
        return allTags;
    }

    @Override
    public void deleteDocument(UUID documentId) {
        var entry = archive.getArchiveEntries().get(documentId);
        if (entry == null) {
            logger.warn("Document with id {} not found", documentId);
            return;
        }
        try {
            var filePath = Path.of(entry.getAbsolutePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("Deleted file: {}", filePath);
            }
        } catch (IOException e) {
            logger.error("Failed to delete file for document {}", documentId, e);
        }
        archive.getArchiveEntries().remove(documentId);
        publisher.publishEvent(new DocumentDeletedEvent(documentId));
        publisher.publishEvent(new AllTagsUpdatedEvent(getAllUniqueTags()));
        saveArchiveAndPublishUpdate();
    }

    @Override
    public LocalDateTime getArchiveDateCreated() {
        return archive.getDateCreated();
    }

    @Override
    public LocalDateTime getArchiveDateLastModified() {
        return archive.getDateLastModified();
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        fileService.saveArchive(archive);
    }

    private Path copyPath(File file) {
        var path = fileService.getTenantDirectory() +
                FileSystems.getDefault().getSeparator() +
                "files" +
                FileSystems.getDefault().getSeparator() +
                file.getName();
        return Path.of(path);
    }

    private void saveArchiveAndPublishUpdate() {
        archive.updateLastModified();
        publisher.publishEvent(new ArchiveLastModifiedUpdatedEvent(archive.getDateLastModified()));
        fileService.saveArchive(archive);
    }
}
