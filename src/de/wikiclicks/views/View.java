package de.wikiclicks.views;

import de.wikiclicks.datastructures.WikiArticle;

import javax.swing.*;

public abstract class View extends JPanel {
    public abstract void triggerPopup(int x, int y);

    public abstract String getIdentifier();

    public abstract void changeArticle(WikiArticle newArticle);
}
