package de.wikiclicks.views;

import javax.swing.*;

public abstract class View extends JPanel {
    public abstract void triggerPopup(int x, int y);
}
