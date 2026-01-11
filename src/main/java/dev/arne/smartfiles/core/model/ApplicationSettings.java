package dev.arne.smartfiles.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public final class ApplicationSettings implements AggregateRoot {

    private static final String DEFAULT_INBOX_FOLDER = System.getProperty("user.home") + "/Downloads";

    private String applicationVersion;
    private LocalDateTime dateCreated;
    private LocalDateTime dateLastModified;
    private boolean lightThemeActive;
    private String inboxFolderPath;

    public static ApplicationSettings empty() {
        var now = LocalDateTime.now();
        return new ApplicationSettings(CURRENT_APP_VERSION, now, now, false, DEFAULT_INBOX_FOLDER);
    }

    public String getInboxFolderPath() {
        return inboxFolderPath != null ? inboxFolderPath : DEFAULT_INBOX_FOLDER;
    }

    @Override
    public void updateLastModified() {
        this.dateLastModified = LocalDateTime.now();
    }
}
