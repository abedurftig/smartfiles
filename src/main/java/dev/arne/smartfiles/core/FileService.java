package dev.arne.smartfiles.core;

import dev.arne.smartfiles.core.model.ArchiveEntry;

import java.io.File;
import java.util.List;
import java.util.UUID;

public interface FileService {

    List<ArchiveEntry> manageFiles(List<File> files);

    List<ArchiveEntry> getAll();

    ArchiveEntry retrieveFileDetails(UUID id);
}
