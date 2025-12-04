package dev.arne.smartfiles.core.events;

public final class LightThemeActivatedSettingChangedEvent extends SmartFilesEvent {

    public LightThemeActivatedSettingChangedEvent(boolean lightThemeActive) {
        super(lightThemeActive);
    }

    public boolean isLightThemeActive() {
        return (boolean) getSource();
    }
}
