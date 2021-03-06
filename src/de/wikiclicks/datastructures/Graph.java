package de.wikiclicks.datastructures;

import org.apache.commons.math.util.MathUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class Graph {
    protected Rectangle2D graphArea;

    public abstract void paint(Graphics2D g2D);

    protected DataPoint getDataPoint(int index , double scalingX, double scalingY, List<Integer> values){
        DataPoint point = new DataPoint();
        point.setValue(Long.valueOf(values.get(index)));

        double dataX = scalingX * (index + 1) - scalingX / 2 + graphArea.getX();
        double dataY = graphArea.getMaxY() - point.getValue() * scalingY;

        point.setCoord(dataX, dataY);

        return point;
    }

    protected int calcMaxGraph(Collection<Integer> values){
        int max = Collections.max(values);
        int roundPrecision = String.valueOf(max).length() - 2;
        return (int) MathUtils.round(max, -roundPrecision, BigDecimal.ROUND_UP);
    }

    public Rectangle2D getGraphArea() {
        return graphArea;
    }
}
