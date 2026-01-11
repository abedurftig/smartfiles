package dev.arne.smartfiles.core;

public interface SettingsService {

    boolean isLightThemeActive();

    void toggleLightThemeActive();

    String getInboxFolderPath();

    void setInboxFolderPath(String path);
}
