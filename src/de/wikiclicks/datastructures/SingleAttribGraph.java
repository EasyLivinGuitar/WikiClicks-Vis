package de.wikiclicks.datastructures;

import de.wikiclicks.utils.ColorFactory;
import de.wikiclicks.utils.Style;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SingleAttribGraph extends Graph {
    private String attrib;

    private Map<String, List<Integer>> attribValues;
    public static Map<String, Color> attribColors;

    private int maxGraph;

    public SingleAttribGraph(String attrib){
        attribValues = new LinkedHashMap<>();
        attribColors = new HashMap<>();

        graphArea = new Rectangle2D.Double();
        maxGraph = 0;

        this.attrib = attrib;
    }

    public void addAttribValues(String namedEntity, List<Integer> attribValues){
        this.attribValues.put(namedEntity, attribValues);
        int max = calcMaxGraph(attribValues);

        if(max > maxGraph){
            maxGraph = max;
        }

        if(!attribColors.containsKey(namedEntity)){
            attribColors.put(namedEntity, ColorFactory.getColor());
        }
    }

    public void removeAttribValues(String namedEntity){
        attribValues.remove(namedEntity);

        ColorFactory.clearColor(attribColors.get(namedEntity));
        attribColors.remove(namedEntity);

        maxGraph = 0;
        for(Map.Entry<String, List<Integer>> attribValue: attribValues.entrySet()){
            int max = calcMaxGraph(attribValue.getValue());

            if(max > maxGraph){
                maxGraph = max;
            }
        }
    }

    public void resetMax(){
        maxGraph = 0;
    }

    @Override
    public void paint(Graphics2D g2D) {
        if(maxGraph == 0){
            maxGraph = 1;
        }

        g2D.setColor(Color.WHITE);
        g2D.fill(graphArea);
        g2D.setColor(Color.BLACK);
        g2D.draw(graphArea);

        boolean drawnHelpLines = false;

        double scalingX = graphArea.getWidth() / 24.0;
        double scalingY = graphArea.getHeight() / maxGraph;

        int numHelpLines = 0;

        if(attrib.equals("hotness")){
            numHelpLines = (int) (maxGraph / 100.0);
        }else if(attrib.equals("clicks")){
            numHelpLines = (int) (maxGraph / 1000.0);
        }

        g2D.setStroke(Style.STROKE_HELPLINES);
        for(int i = 1; i <= numHelpLines; i++){
            int y = 0;

            if(attrib.equals("hotness")){
                y = (int) (graphArea.getMaxY() - i * 100 * scalingY);
            }
            else if(attrib.equals("clicks")){
                y = (int) (graphArea.getMaxY() - i * 1000 * scalingY);
            }

            g2D.drawLine((int)getGraphArea().getX(), y, (int) getGraphArea().getMaxX(), y);
        }
        g2D.setStroke(Style.STROKE_DEFAULT);

        for(Map.Entry<String, List<Integer>> entry: attribValues.entrySet()){
            for(int i = 0; i < entry.getValue().size() - 1; i++){
                DataPoint thisDataPoint = getDataPoint(i, scalingX, scalingY, entry.getValue());
                DataPoint nextDataPoint = getDataPoint(i + 1, scalingX, scalingY, entry.getValue());

                if(!drawnHelpLines){
                    g2D.setColor(Color.GRAY);
                    g2D.setStroke(Style.STROKE_HELPLINES);

                    g2D.drawLine(
                            (int) (thisDataPoint.getX() + scalingX / 2.0),
                            (int) graphArea.getY(),
                            (int) (thisDataPoint.getX() + scalingX/ 2.0),
                            (int) graphArea.getMaxY());
                }


                g2D.setStroke(Style.STROKE_GRAPH);
                g2D.setColor(attribColors.getOrDefault(entry.getKey(), Color.BLACK));
                g2D.drawLine(
                        (int) thisDataPoint.getX(),
                        (int) thisDataPoint.getY(),
                        (int) nextDataPoint.getX(),
                        (int) nextDataPoint.getY());
            }

            drawnHelpLines = true;
        }

        g2D.setStroke(Style.STROKE_DEFAULT);

        String scaleMax = String.valueOf(maxGraph);
        int stringOffsetX = g2D.getFontMetrics().stringWidth(scaleMax);

        g2D.setColor(Color.BLACK);
        g2D.drawString(scaleMax,
                (int) graphArea.getX() - stringOffsetX - 5,
                (int) (graphArea.getY() + g2D.getFontMetrics().getHeight() + 5)
        );
    }

    public void setBounds(double x, double y, double width, double height){
        graphArea.setRect(x, y, width, height);
    }
}
