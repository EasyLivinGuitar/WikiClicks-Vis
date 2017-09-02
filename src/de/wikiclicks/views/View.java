package de.wikiclicks.views;

import de.wikiclicks.datastructures.WikiArticle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public abstract class View extends JPanel {
    protected List<JComponent> components;

    public View(){
        components = new ArrayList<>();
    }

    public abstract String getIdentifier();

    public List<JComponent> getUIComponents(){
        return components;
    }

    public abstract void changeArticle(WikiArticle newArticle);
}
