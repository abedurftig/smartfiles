package dev.arne.smartfiles.core;

import dev.arne.smartfiles.core.model.ArchiveEntry;
import dev.arne.smartfiles.core.model.Tag;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ArchiveService {

    List<ArchiveEntry> manageFiles(List<File> files);

    List<ArchiveEntry> getAll();

    ArchiveEntry retrieveFileDetails(UUID id);

    File getFile(UUID id);

    void addTag(UUID selectedDocumentId, String text);

    void updateDescription(UUID documentId, String description);

    Set<Tag> getAllUniqueTags();
}
