package de.wikiclicks.views;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class ViewClicksGraph extends View {
    @Override
    public void paint(Graphics g){
        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.clearRect(0, 0, getWidth(), getHeight());

        int days = 30;

        double marginX = getWidth() * 0.05;
        double marginY = getHeight() * 0.2;

        Rectangle2D graphBackground = new Rectangle2D.Double(
                marginX,
                marginY,
                getWidth() -  2.0 * marginX,
                getHeight() - 2.0 * marginY
        );

        g2D.setColor(Color.WHITE);
        g2D.fill(graphBackground);
        g2D.setColor(Color.BLACK);
        g2D.draw(graphBackground);

        double marginGraphX = graphBackground.getWidth() * 0.05;
        double marginGraphY = graphBackground.getHeight() * 0.08;

        double backgroundX = graphBackground.getX();
        double backgroundY = graphBackground.getY();

        Line2D xAxis = new Line2D.Double(
                backgroundX + marginGraphX,
                backgroundY + graphBackground.getHeight() - marginGraphY,
                backgroundX + graphBackground.getWidth() -  marginGraphX,
                backgroundY + graphBackground.getHeight() - marginGraphY
        );

        Line2D yAxis = new Line2D.Double(
            backgroundX + marginGraphX,
            backgroundY + graphBackground.getHeight() - marginGraphY,
            backgroundX + marginGraphX,
            backgroundY + marginGraphY
        );

        g2D.setStroke(new BasicStroke(0.5f));

        g2D.draw(xAxis);
        g2D.draw(yAxis);

        double dayLength = (xAxis.getX2() - xAxis.getX1()) / (double)(days);
        double dayStartX = xAxis.getX1();
        double dayStartY = xAxis.getY1();

        for(int i = 1; i <= days; i++){
            double currentX = dayStartX + (i - 1) * dayLength;
            g2D.draw(new Rectangle2D.Double(currentX, dayStartY, dayLength, graphBackground.getHeight() * 0.03));

            float stringWidth = g2D.getFontMetrics().stringWidth(String.valueOf(i));
            float stringHeight = g2D.getFont().getSize();

            g2D.drawString(
                    String.valueOf(i),
                    (float)(currentX + dayLength / 2.0 - stringWidth / 2.0),
                    (float) (dayStartY + graphBackground.getHeight() * 0.015 + stringHeight / 2.0)
            );
        }
    }

    @Override
    public void triggerPopup(int x, int y) {}

    @Override
    public String getIdentifier() {
        return "clicks-graph";
    }
}
