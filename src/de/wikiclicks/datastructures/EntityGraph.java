package de.wikiclicks.datastructures;

import de.wikiclicks.utils.Style;
import de.wikiclicks.views.ViewSmallMultiples;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class EntityGraph extends Graph{
    private String entity;

    private Rectangle2D bounds;
    private Rectangle2D namedEntityArea;

    private List<Integer> hotnessValues;
    private List<Integer> clickValues;

    private int hotnessMax;
    private int clicksMax;

    private int hotnessTotal;
    private int clicksTotal;

    private Color hotnessColor = Color.RED;
    private Color clicksColor = Color.BLUE;

    private boolean drawValues;

    public EntityGraph(String entity){
        this.entity = entity;

        hotnessValues = new ArrayList<>(24);
        clickValues = new ArrayList<>(24);

        hotnessTotal = 0;
        clicksTotal = 0;
    }

    @Override
    public void paint(Graphics2D g2D){
        if(hotnessMax == 0){
            hotnessMax = 1;
        }

        if(clicksMax == 0){
            clicksMax = 1;
        }

        g2D.setColor(new Color(250, 250, 250));
        g2D.fill(bounds);
        g2D.setColor(Color.BLACK);
        g2D.draw(bounds);

        g2D.setColor(Color.WHITE);
        g2D.fill(namedEntityArea);
        g2D.setColor(Color.BLACK);

        g2D.draw(namedEntityArea);

        g2D.setFont(g2D.getFont().deriveFont(Font.BOLD));

        int stringOffsetX = g2D.getFontMetrics().stringWidth(entity);
        int stringOffsetY = g2D.getFontMetrics().getHeight();

        g2D.drawString(entity,
                (int) namedEntityArea.getCenterX() - stringOffsetX / 2,
                (int) namedEntityArea.getCenterY());

        g2D.setFont(g2D.getFont().deriveFont(Font.PLAIN));

        String totalHotness = "hotness: "+String.valueOf(hotnessTotal);
        g2D.setColor(hotnessColor);
        g2D.drawString(totalHotness,
                (int) (namedEntityArea.getX() + 5),
                (int) (namedEntityArea.getMaxY() - namedEntityArea.getHeight() * 0.05));

        stringOffsetX = g2D.getFontMetrics().stringWidth(String.valueOf(hotnessMax));
        g2D.drawString(String.valueOf(hotnessMax),
                (int)graphArea.getX() - stringOffsetX - 5,
                (int) graphArea.getY() + stringOffsetY + 5);

        g2D.setColor(clicksColor);
        g2D.drawString(String.valueOf(clicksMax),
                (int) (graphArea.getMaxX() + 5),
                (int)graphArea.getY() + stringOffsetY + 5);

        String totalClicks = "clicks: "+String.valueOf(clicksTotal);
        g2D.drawString(totalClicks,
                (int) namedEntityArea.getX() + 5,
                (int) (namedEntityArea.getMaxY() - namedEntityArea.getHeight() * 0.05 - stringOffsetY));


        double scalingX = graphArea.getWidth() / 24.0;
        double hotnessScaling = graphArea.getHeight() / hotnessMax;
        double clicksScaling = graphArea.getHeight() / clicksMax;

        int numHelpLinesHot = hotnessMax / 100;
        int numHelpLinesClicks = clicksMax / 1000;

        g2D.setStroke(Style.STROKE_HELPLINES);
        g2D.setColor(hotnessColor);

        for(int i = 1; i <= numHelpLinesHot; i++){
            g2D.drawLine(
                    (int) graphArea.getX(),
                    (int) (graphArea.getMaxY() - i * 100.0 * hotnessScaling),
                    (int) graphArea.getMaxX(),
                    (int) (graphArea.getMaxY() - i * 100.0 * hotnessScaling));
        }

        g2D.setColor(clicksColor);

        for(int i = 1; i <= numHelpLinesClicks; i++){
            g2D.drawLine(
                    (int) graphArea.getX(),
                    (int) (graphArea.getMaxY() - i * 1000.0 * clicksScaling),
                    (int) graphArea.getMaxX(),
                    (int) (graphArea.getMaxY() - i * 1000.0 * clicksScaling));
        }

        for(int i = 0; i < 23; i++){
            DataPoint thisPoint = getDataPoint(i, scalingX, clicksScaling, clickValues);
            DataPoint nextPoint = getDataPoint(i + 1, scalingX, clicksScaling, clickValues);

            g2D.setColor(Color.GRAY);
            g2D.setStroke(Style.STROKE_HELPLINES);

            g2D.drawLine((int)(thisPoint.getX() + scalingX / 2.0),
                    (int)graphArea.getY(),
                    (int)(thisPoint.getX() + scalingX / 2.0),
                    (int)graphArea.getMaxY());

            g2D.setStroke(Style.STROKE_GRAPH);
            g2D.setColor(clicksColor);
            g2D.drawLine((int)thisPoint.getX(), (int)thisPoint.getY(), (int) nextPoint.getX(), (int) nextPoint.getY());

            thisPoint = getDataPoint(i, scalingX, hotnessScaling, hotnessValues);
            nextPoint = getDataPoint(i + 1, scalingX, hotnessScaling, hotnessValues);

            g2D.setColor(hotnessColor);
            g2D.drawLine((int)thisPoint.getX(), (int)thisPoint.getY(), (int)nextPoint.getX(), (int) nextPoint.getY());


        }

        g2D.setStroke(new BasicStroke(1.5f));
        g2D.setColor(Color.BLACK);

        if(ViewSmallMultiples.drawCrosshair){
            int crosshairY = (int) (ViewSmallMultiples.crosshairY + bounds.getY());
            g2D.drawLine(
                    ViewSmallMultiples.crosshairX, (int)graphArea.getY(), ViewSmallMultiples.crosshairX, (int)graphArea.getMaxY());
            g2D.drawLine((int) graphArea.getX(), crosshairY, (int)graphArea.getMaxX(), crosshairY);

            int hotness = hotnessMax - (int) ((double)ViewSmallMultiples.crosshairY / hotnessScaling);
            int clicks = clicksMax - (int)((double)ViewSmallMultiples.crosshairY / clicksScaling);

            /*g2D.setFont(g2D.getFont().deriveFont(8.f));
            stringOffsetX = g2D.getFontMetrics().stringWidth(String.valueOf(hotness));
            g2D.setColor(hotnessColor);
            g2D.drawString(String.valueOf(hotness), ViewSmallMultiples.crosshairX - stringOffsetX - 10, crosshairY);
            g2D.setColor(clicksColor);
            g2D.drawString(String.valueOf(clicks), ViewSmallMultiples.crosshairX + 10, crosshairY);*/

            int index = (int) ((ViewSmallMultiples.crosshairX - getGraphArea().getX()) / scalingX);

            g2D.setFont(g2D.getFont().deriveFont(12.f));

            if(index > 0 && index < hotnessValues.size()){
                int hotnessValue = hotnessValues.get(index);
                int clicksValue = clickValues.get(index);

                int hotnessY = (int) (getGraphArea().getMaxY() -  hotnessValue * hotnessScaling);
                int clicksY = (int)(getGraphArea().getMaxY() - clicksValue * clicksScaling);

                stringOffsetX = g2D.getFontMetrics().stringWidth(String.valueOf(hotnessValue));
                g2D.setColor(hotnessColor);
                g2D.drawString(String.valueOf(hotnessValue), ViewSmallMultiples.crosshairX - stringOffsetX - 3, hotnessY);

                g2D.setColor(clicksColor);
                g2D.drawString(String.valueOf(clicksValue), ViewSmallMultiples.crosshairX + 3, clicksY);

            }
        }

        g2D.setStroke(Style.STROKE_DEFAULT);
    }

    public void addHotnessValue(int value){
        hotnessValues.add(value);
        hotnessTotal += value;
    }

    public void addClickValue(int value){
        clickValues.add(value);
        clicksTotal += value;
    }

    public void setBounds(double x, double y, double width, double height){
        if(bounds == null)
            this.bounds = new Rectangle2D.Double(x, y, width, height);
        else
            this.bounds.setRect(x, y, width, height);

        if(namedEntityArea == null)
            namedEntityArea = new Rectangle2D.Double(bounds.getX(),  bounds.getY(), 0.1 * bounds.getWidth(), bounds.getHeight());
        else
            namedEntityArea.setRect(bounds.getX(),  bounds.getY(), 0.1 * bounds.getWidth(), bounds.getHeight());

        if(graphArea == null)
            graphArea = new Rectangle2D.Double(
                    bounds.getX() + namedEntityArea.getWidth(),
                    bounds.getY(),
                    bounds.getWidth() - namedEntityArea.getWidth(),
                    bounds.getHeight());
        else
            graphArea.setRect(bounds.getX() + namedEntityArea.getWidth(),
                    bounds.getY(),
                    bounds.getWidth() - namedEntityArea.getWidth(),
                    bounds.getHeight());

    }

    public int getHotnessMax(){
        return calcMaxGraph(hotnessValues);
    }

    public int getClicksMax(){
        return calcMaxGraph(clickValues);
    }

    public void setHotnessMax(int hotnessMax){
        this.hotnessMax = hotnessMax;
    }

    public void setClicksMax(int clicksMax){
        this.clicksMax = clicksMax;
    }

    public List<Integer> getHotnessValues() {
        return hotnessValues;
    }

    public List<Integer> getClickValues() {
        return clickValues;
    }

    public String getEntity() {
        return entity;
    }

    public Rectangle2D getBounds() {
        return bounds;
    }

    public Rectangle2D getGraphArea() {
        return graphArea;
    }
}
