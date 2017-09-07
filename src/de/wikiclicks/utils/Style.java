package de.wikiclicks.utils;

import java.awt.*;

public class Style {
    public static Stroke STROKE_DEFAULT = new BasicStroke();

    public static Stroke STROKE_GRAPH = new BasicStroke(
            2.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND);

    public static Stroke STROKE_HELPLINES = new BasicStroke(
            0.3f
    );

    public static Stroke STROKE_HIGHLIGHT = new BasicStroke(
            0.5f,
            BasicStroke.CAP_SQUARE,
            BasicStroke.JOIN_MITER,
            1,
            new float[]{4.0f},
            0);

    public static Stroke STROKE_GRAPH_OUTLINE = new BasicStroke(0.5f);
}
