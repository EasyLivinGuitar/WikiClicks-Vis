package de.wikiclicks.views;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Created by vanya on 20.07.17.
 */
public class ViewPieNews extends View {
    @Override
    public void triggerPopup(int x, int y) {

    }

    @Override
    public String getIdentifier() {
        return "pie";
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.clearRect(0,0, getWidth(), getHeight());
    }
}
