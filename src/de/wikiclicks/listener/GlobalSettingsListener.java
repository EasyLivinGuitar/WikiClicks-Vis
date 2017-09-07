package de.wikiclicks.listener;

import java.util.EventListener;

public abstract class GlobalSettingsListener implements EventListener {
    public abstract void entitySelectionChanged();
    public abstract void splitToEntitiesChanged();
}
