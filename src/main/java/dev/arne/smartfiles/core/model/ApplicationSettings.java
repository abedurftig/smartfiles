package dev.arne.smartfiles.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public final class ApplicationSettings implements AggregateRoot {

    private String applicationVersion;
    private LocalDateTime dateCreated;
    private LocalDateTime dateLastModified;
    private boolean lightThemeActive;

    public static ApplicationSettings empty() {
        var now = LocalDateTime.now();
        return new ApplicationSettings(CURRENT_APP_VERSION, now, now, false);
    }

    @Override
    public void updateLastModified() {
        this.dateLastModified = LocalDateTime.now();
    }
}
