package de.wikiclicks.datastructures;

import de.wikiclicks.listener.EntitySelectionListener;

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

    private List<EntitySelectionListener> settingsChangedListener;

    public GlobalSettings(){
        settingsChangedListener = new ArrayList<>();
    }

    public void selectNamedEntity(String namedEntity){
        selectedNamedEntities.add(namedEntity);

        for(EntitySelectionListener listener: settingsChangedListener){
            listener.entitySelectionChanged();
        }
    }

    public void unselectNamedEntity(String namedEntity){
        selectedNamedEntities.remove(namedEntity);

        for(EntitySelectionListener lister: settingsChangedListener){
            lister.entitySelectionChanged();
        }
    }

    public Set<String> getSelectedNamedEntities(){
        return selectedNamedEntities;
    }

    public void addListener(EntitySelectionListener listener){
        settingsChangedListener.add(listener);
    }
}
