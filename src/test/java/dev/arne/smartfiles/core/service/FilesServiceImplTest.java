package dev.arne.smartfiles.core.service;

import dev.arne.smartfiles.core.configuration.SmartFilesConfiguration;
import dev.arne.smartfiles.core.model.ApplicationSettings;
import dev.arne.smartfiles.core.model.Archive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FilesServiceImplTest {

    @TempDir
    Path tempDir;

    private SmartFilesConfiguration configuration;
    private ObjectMapper objectMapper;
    private FilesServiceImpl filesService;

    @BeforeEach
    void setUp() {
        configuration = new SmartFilesConfiguration();
        configuration.setRootDirectory(tempDir.toString());
        configuration.setTenantId("test-tenant");

        objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();

        filesService = new FilesServiceImpl(configuration, objectMapper);
    }

    @Test
    void constructor_createsRootDirectory() {
        var tenantDir = new File(configuration.getTenantDirectory());
        var filesDir = new File(tenantDir, "files");

        assertTrue(tenantDir.exists());
        assertTrue(tenantDir.isDirectory());
        assertTrue(filesDir.exists());
        assertTrue(filesDir.isDirectory());
    }

    @Test
    void getTenantDirectory_returnsCorrectPath() {
        var expected = tempDir + File.separator + "test-tenant";

        assertEquals(expected, filesService.getTenantDirectory());
    }

    @Test
    void loadArchive_whenNoFile_returnsEmptyArchive() {
        var archive = filesService.loadArchive();

        assertNotNull(archive);
        assertTrue(archive.getArchiveEntries().isEmpty());
    }

    @Test
    void saveArchive_thenLoadArchive_roundTrip() {
        var archive = Archive.empty();
        archive.addArchiveEntryFromFile(new File("/tmp/test.pdf"), "/original/test.pdf");

        filesService.saveArchive(archive);
        var loaded = filesService.loadArchive();

        assertEquals(1, loaded.getArchiveEntries().size());
        var entry = loaded.getArchiveEntries().values().iterator().next();
        assertEquals("test.pdf", entry.getName());
        assertEquals("/original/test.pdf", entry.getOriginalPath());
    }

    @Test
    void saveArchive_createsJsonFile() {
        var archive = Archive.empty();

        filesService.saveArchive(archive);

        var archiveFile = new File(configuration.getTenantDirectory(), "archive.json");
        assertTrue(archiveFile.exists());
    }

    @Test
    void loadApplicationSettings_whenNoFile_returnsEmptySettings() {
        var settings = filesService.loadApplicationSettings();

        assertNotNull(settings);
        assertFalse(settings.isLightThemeActive());
    }

    @Test
    void saveApplicationSettings_thenLoadApplicationSettings_roundTrip() {
        var settings = ApplicationSettings.empty();
        settings.setLightThemeActive(true);

        filesService.saveApplicationSettings(settings);
        var loaded = filesService.loadApplicationSettings();

        assertTrue(loaded.isLightThemeActive());
    }

    @Test
    void saveApplicationSettings_createsJsonFile() {
        var settings = ApplicationSettings.empty();

        filesService.saveApplicationSettings(settings);

        var settingsFile = new File(configuration.getTenantDirectory(), "settings.json");
        assertTrue(settingsFile.exists());
    }

    @Test
    void getTenantDirectory_usesPlatformSeparator() {
        var tenantDir = filesService.getTenantDirectory();
        var separator = FileSystems.getDefault().getSeparator();

        // Verify the path uses the platform's path separator
        assertTrue(tenantDir.contains(separator),
                "Tenant directory should use platform separator: " + separator);

        // Verify the path is constructed correctly with root + separator + tenantId
        var expectedEnd = separator + "test-tenant";
        assertTrue(tenantDir.endsWith(expectedEnd),
                "Tenant directory should end with separator + tenantId");
    }

    @Test
    void getTenantDirectory_doesNotContainHardcodedSlash_whenOnWindows() {
        var tenantDir = filesService.getTenantDirectory();
        var separator = FileSystems.getDefault().getSeparator();

        // If we're on a system where separator is not "/", there should be no "/" in the path
        // This verifies the bug fix for hardcoded "/" characters
        if (!separator.equals("/")) {
            assertFalse(tenantDir.contains("/"),
                    "Tenant directory should not contain hardcoded '/' when platform separator is: " + separator);
        }
    }

    @Test
    void constructor_createsFilesSubdirectory() {
        var tenantDir = new File(configuration.getTenantDirectory());
        var filesDir = new File(tenantDir, "files");

        assertTrue(filesDir.exists(), "Files subdirectory should exist");
        assertTrue(filesDir.isDirectory(), "Files subdirectory should be a directory");
    }
}
