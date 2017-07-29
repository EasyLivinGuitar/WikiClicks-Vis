package de.wikiclicks.listener;

import java.util.EventListener;

public abstract class ArticleListener implements EventListener {
    public abstract void articleChanged(String title);
}
