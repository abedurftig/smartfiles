package dev.arne.smartfiles.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Archive {

    private String applicationVersion;
    private LocalDateTime dateCreated;
    private LocalDateTime dateLastModified;
    private Map<UUID, ArchiveEntry> archiveEntries;
    private Set<Tag> tags;

    public static Archive empty() {
        var timeStamp = LocalDateTime.now();
        return new Archive("0.0.1", timeStamp, timeStamp, new HashMap<>(), Set.of());
    }

    public ArchiveEntry addArchiveEntryFromFile(File file, String originalPath) {
        var newEntry = ArchiveEntry.of(file.getName(), file.getAbsolutePath(), originalPath);
        archiveEntries.put(newEntry.getId(), newEntry);
        return newEntry;
    }

    public void updateLastModified() {
        this.dateLastModified = LocalDateTime.now();
    }
}
