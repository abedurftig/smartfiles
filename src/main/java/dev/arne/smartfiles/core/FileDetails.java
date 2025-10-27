package dev.arne.smartfiles.core;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

public record FileDetails(
        UUID id,
        File file,
        String name,
        LocalDateTime dateCreated,
        LocalDateTime dateLastModified
) { }
