package dev.arne.smartfiles.core.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveEntry {

    private UUID id;
    private String name;
    private String summary;
    private String path;
    private String absolutePath;
    private String originalPath;
    private Set<Tag> tags;
    private LocalDateTime dateCreated;
    private LocalDateTime dateLastModified;

    public static ArchiveEntry of(String name, String absolutePath, String originalPath) {
        var path = absolutePath.substring(absolutePath.lastIndexOf("/") + 1);
        var timeStamp = LocalDateTime.now();
        return new ArchiveEntry(UUID.randomUUID(), name, "Not available yet", path, absolutePath, originalPath, new HashSet<>(), timeStamp, timeStamp);
    }

    public void updateLastModified() {
        this.dateLastModified = LocalDateTime.now();
    }
}
