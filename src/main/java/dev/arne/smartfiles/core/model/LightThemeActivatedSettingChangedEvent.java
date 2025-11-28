package dev.arne.smartfiles.core.model;

import java.time.Clock;

public final class LightThemeActivatedSettingChangedEvent extends SmartFilesEvent {

    public LightThemeActivatedSettingChangedEvent(boolean lightThemeActive) {
        super(lightThemeActive);
    }

    public boolean isLightThemeActive() {
        return (boolean) getSource();
    }
}
