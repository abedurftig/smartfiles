package dev.arne.smartfiles.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationSettingsTest {

    @Test
    void empty_createsSettingsWithDefaults() {
        var before = LocalDateTime.now();
        var settings = ApplicationSettings.empty();
        var after = LocalDateTime.now();

        assertEquals(AggregateRoot.CURRENT_APP_VERSION, settings.getApplicationVersion());
        assertNotNull(settings.getDateCreated());
        assertNotNull(settings.getDateLastModified());
        assertTrue(settings.getDateCreated().isAfter(before.minusSeconds(1)));
        assertTrue(settings.getDateCreated().isBefore(after.plusSeconds(1)));
        assertFalse(settings.isLightThemeActive());
    }

    @Test
    void setLightThemeActive_changesValue() {
        var settings = ApplicationSettings.empty();

        settings.setLightThemeActive(true);

        assertTrue(settings.isLightThemeActive());
    }

    @Test
    void updateLastModified_updatesTimestamp() throws InterruptedException {
        var settings = ApplicationSettings.empty();
        var originalModified = settings.getDateLastModified();

        Thread.sleep(10);
        settings.updateLastModified();

        assertTrue(settings.getDateLastModified().isAfter(originalModified));
    }
}
