package dev.arne.smartfiles.core.service;

import dev.arne.smartfiles.core.FileService;
import dev.arne.smartfiles.core.events.LightThemeActivatedSettingChangedEvent;
import dev.arne.smartfiles.core.model.ApplicationSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingsServiceImplTest {

    @Mock
    private FileService fileService;

    @Mock
    private ApplicationEventPublisher publisher;

    private ApplicationSettings settings;
    private SettingsServiceImpl settingsService;

    @BeforeEach
    void setUp() {
        settings = ApplicationSettings.empty();
        when(fileService.loadApplicationSettings()).thenReturn(settings);

        settingsService = new SettingsServiceImpl(fileService, publisher);
    }

    @Test
    void constructor_loadsSettingsFromFileService() {
        verify(fileService).loadApplicationSettings();
    }

    @Test
    void isLightThemeActive_returnsFalseByDefault() {
        assertFalse(settingsService.isLightThemeActive());
    }

    @Test
    void isLightThemeActive_returnsCurrentState() {
        settings.setLightThemeActive(true);

        assertTrue(settingsService.isLightThemeActive());
    }

    @Test
    void toggleLightThemeActive_togglesFromFalseToTrue() {
        settingsService.toggleLightThemeActive();

        assertTrue(settings.isLightThemeActive());
    }

    @Test
    void toggleLightThemeActive_togglesFromTrueToFalse() {
        settings.setLightThemeActive(true);

        settingsService.toggleLightThemeActive();

        assertFalse(settings.isLightThemeActive());
    }

    @Test
    void toggleLightThemeActive_publishesEvent() {
        settingsService.toggleLightThemeActive();

        var captor = ArgumentCaptor.forClass(LightThemeActivatedSettingChangedEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertTrue(captor.getValue().isLightThemeActive());
    }

    @Test
    void toggleLightThemeActive_eventReflectsNewState() {
        settings.setLightThemeActive(true);

        settingsService.toggleLightThemeActive();

        var captor = ArgumentCaptor.forClass(LightThemeActivatedSettingChangedEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertFalse(captor.getValue().isLightThemeActive());
    }
}
