package de.wikiclicks.datastructures;

import de.wikiclicks.listener.GlobalSettingsListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GlobalSettings {
    public WikiArticle currentArticle;
    public boolean articleChangeSuccess;

    public SimpleDateFormat hourFormat = new SimpleDateFormat("yyyyMMddHHmm");
    public SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
    public SimpleDateFormat monthFormat = new SimpleDateFormat("yyyyMM");

    private Set<String> selectedNamedEntities = new LinkedHashSet<>();
    private boolean splitToEntities = true;

    private List<GlobalSettingsListener> settingsChangedListener;

    public GlobalSettings(){
        settingsChangedListener = new ArrayList<>();
    }

    public void selectNamedEntity(String namedEntity){
        selectedNamedEntities.add(namedEntity);

        for(GlobalSettingsListener listener: settingsChangedListener){
            listener.entitySelectionChanged();
        }
    }

    public void unselectNamedEntity(String namedEntity){
        selectedNamedEntities.remove(namedEntity);

        for(GlobalSettingsListener lister: settingsChangedListener){
            lister.entitySelectionChanged();
        }
    }

    public int getNumSelectedNamedEntities(){
        return selectedNamedEntities.size();
    }

    public Set<String> getSelectedNamedEntities(){
        return selectedNamedEntities;
    }

    public void addListener(GlobalSettingsListener listener){
        settingsChangedListener.add(listener);
    }

    public boolean isSplitToEntities() {
        return splitToEntities;
    }

    public void setSplitToEntities(boolean splitToEntities) {
        this.splitToEntities = splitToEntities;

        for(GlobalSettingsListener listener: settingsChangedListener){
            listener.splitToEntitiesChanged();
        }
    }
}
