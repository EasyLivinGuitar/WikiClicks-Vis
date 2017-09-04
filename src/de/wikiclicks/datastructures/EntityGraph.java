package de.wikiclicks.datastructures;

import org.apache.commons.math.util.MathUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EntityGraph{
    private String date;
    private NamedEntity entity;

    private Rectangle2D bounds;
    private Rectangle2D namedEntityArea;
    private Rectangle2D graphArea;

    private List<Integer> hotnessValues;
    private List<Integer> clickValues;

    private int hotnessMax;
    private int clicksMax;

    private int hotnessTotal;
    private int clicksTotal;

    private Color hotnessColor = Color.RED;
    private Color clicksColor = Color.BLUE;

    public EntityGraph(NamedEntity entity, String date){
        this.entity = entity;
        this.date = date;

        hotnessValues = new ArrayList<>(24);
        clickValues = new ArrayList<>(24);

        hotnessTotal = 0;
        clicksTotal = 0;
    }

    public void paint(Graphics2D g2D){
        g2D.setColor(new Color(250, 250, 250));
        g2D.fill(bounds);
        g2D.setColor(Color.BLACK);
        g2D.draw(bounds);

        g2D.setColor(Color.WHITE);
        g2D.fill(namedEntityArea);
        g2D.setColor(Color.BLACK);

        g2D.draw(namedEntityArea);

        g2D.setFont(g2D.getFont().deriveFont(Font.BOLD));

        int stringOffsetX = g2D.getFontMetrics().stringWidth(entity.getNamedEntity());
        int stringOffsetY = g2D.getFontMetrics().getHeight();

        g2D.drawString(entity.getNamedEntity(),
                (int) namedEntityArea.getCenterX() - stringOffsetX / 2,
                (int) namedEntityArea.getCenterY());

        g2D.setFont(g2D.getFont().deriveFont(Font.PLAIN));

        String totalHotness = "hotness: "+String.valueOf(hotnessTotal);
        g2D.setColor(hotnessColor);
        g2D.drawString(totalHotness,
                (int) (namedEntityArea.getX() + 5),
                (int)namedEntityArea.getMaxY() - 5);

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
                (int) namedEntityArea.getMaxY() - 10 - stringOffsetY);


        double scalingX = graphArea.getWidth() / 24.0;
        double hotnessScaling = graphArea.getHeight() / hotnessMax;
        double clicksScaling = graphArea.getHeight() / clicksMax;

        for(int i = 0; i < 23; i++){
            DataPoint thisPoint = getDataPoint(i, scalingX, clicksScaling, clickValues);
            DataPoint nextPoint = getDataPoint(i + 1, scalingX, clicksScaling, clickValues);

            g2D.setStroke(new BasicStroke(2.0f));
            g2D.setColor(clicksColor);
            g2D.drawLine((int)thisPoint.getX(), (int)thisPoint.getY(), (int) nextPoint.getX(), (int) nextPoint.getY());

            /*g2D.setColor(clicksColor);
            g2D.setStroke(new BasicStroke(25.0f));
            g2D.drawLine((int)thisPoint.getX(), (int)thisPoint.getY(), (int)thisPoint.getX(), (int) bounds.getMaxY());*/

            /*DataPoint*/ thisPoint = getDataPoint(i, scalingX, hotnessScaling, hotnessValues);
            /*DataPoint */nextPoint = getDataPoint(i + 1, scalingX, hotnessScaling, hotnessValues);


            g2D.setColor(hotnessColor);
            g2D.drawLine((int)thisPoint.getX(), (int)thisPoint.getY(), (int)nextPoint.getX(), (int) nextPoint.getY());


        }

        g2D.setStroke(new BasicStroke());
    }

    private DataPoint getDataPoint(int index , double scalingX, double scalingY, List<Integer> values){
        DataPoint point = new DataPoint();
        point.setValue(Long.valueOf(values.get(index)));

        double dataX = scalingX * (index + 1) - scalingX / 2 + graphArea.getX();
        double dataY = graphArea.getMaxY() - point.getValue() * scalingY;

        point.setCoord(dataX, dataY);

        return point;
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

    private int calcMaxGraph(Collection<Integer> values){
        int max = Collections.max(values);
        int roundPrecision = String.valueOf(max).length() - 2;
        return (int) MathUtils.round(max, -roundPrecision, BigDecimal.ROUND_UP);
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
}
