package de.wikiclicks.views;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class ViewTest extends View {
    public ViewTest(){}

    @Override
    public void paint(Graphics g){
        Graphics2D g2D = (Graphics2D) g;
        g2D.setBackground(Color.WHITE);

        Rectangle2D rect = new Rectangle2D.Double(10, 10, 200, 200);

        g2D.setColor(Color.YELLOW);
        g2D.fill(rect);
        g2D.setColor(Color.BLACK);

        g2D.draw(rect);
    }

    @Override
    public void triggerPopup(int x, int y) {
        /*JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add("Test 1");
        popupMenu.add("Test 2");
        popupMenu.setLocation(x, y);
        popupMenu.setVisible(true);*/
    }

    @Override
    public String getIdentifier() {
        return "test";
    }
}
