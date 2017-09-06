package de.wikiclicks.datastructures;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SingleAttribGraph extends Graph {
    private String attrib;

    private Map<String, List<Integer>> attribValues;
    private Map<String, Color> attribColors;

    private int maxGraph;

    public SingleAttribGraph(){
        attribValues = new LinkedHashMap<>();

        graphArea = new Rectangle2D.Double();
    }

    public void addAttribValues(String namedEntity, List<Integer> attribValues){
        this.attribValues.put(namedEntity, attribValues);
        int max = Collections.max(attribValues);

        if(max > maxGraph){
            maxGraph = max;
        }
    }

    @Override
    public void paint(Graphics2D g2D) {
        g2D.setColor(Color.BLACK);
        g2D.draw(graphArea);

        double scalingX = graphArea.getWidth() / 24.0;
        double scalingY = graphArea.getHeight() / maxGraph;

        g2D.setStroke(new BasicStroke(2.0f));

        for(Map.Entry<String, List<Integer>> entry: attribValues.entrySet()){
            for(int i = 0; i < entry.getValue().size() - 1; i++){
                DataPoint thisDataPoint = getDataPoint(i, scalingX, scalingY, entry.getValue());
                DataPoint nextDataPoint = getDataPoint(i + 1, scalingX, scalingY, entry.getValue());

                g2D.setColor(attribColors.getOrDefault(entry.getKey(), Color.BLACK));
                g2D.drawLine(
                        (int) thisDataPoint.getX(),
                        (int) thisDataPoint.getY(),
                        (int) nextDataPoint.getX(),
                        (int) nextDataPoint.getY());
            }
        }

        g2D.setStroke(new BasicStroke());
    }

    public void setBounds(double x, double y, double width, double height){
        graphArea.setRect(x, y, width, height);
    }
}
