package dev.arne.smartfiles.core.model;

public sealed interface AggregateRoot permits ApplicationSettings, Archive {

    String CURRENT_APP_VERSION = "0.0.1";

    void updateLastModified();
}
