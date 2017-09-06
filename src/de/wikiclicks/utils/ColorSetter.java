package de.wikiclicks.utils;

import java.awt.*;
import java.util.*;

public class ColorSetter {

    private static ArrayList<Color> colors = new ArrayList<>();
    private static ArrayList<Color> usedColors = new ArrayList<>();

    public ColorSetter() {
        colors.add(new Color(255, 16, 14));
        colors.add(new Color(21, 49, 255));
        colors.add(new Color(255, 221, 25));
        colors.add(new Color(11, 145, 27));
        colors.add(new Color(105, 12, 255));
        colors.add(new Color(239, 77, 84));
        colors.add(new Color(50, 255, 166));
        colors.add(new Color(241, 21, 255));
        colors.add(new Color(255, 114, 12));
        colors.add(new Color(27, 12, 255));

    }

    public static Color getColor(){
        for (Color col: colors){
            if (!usedColors.contains(col)) {
                usedColors.add(col);
                return col;
            }
        }
        return null;
    }

    public static void clearColor(Color col) {
        if (usedColors.contains(col)) {
            usedColors.remove(col);
        }
    }
}
