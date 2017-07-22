package de.wikiclicks.datastructures;

import java.awt.geom.Ellipse2D;

public class DataPoint {
    private Ellipse2D drawGeom;
    private double x, y;
    private Long value;

    private boolean highlighted;

    public DataPoint(){
        drawGeom = new Ellipse2D.Double();
        highlighted = false;
    }

    public void setCoord(double x, double y){
        this.x = x;
        this.y = y;

        double movedX = x - 5.0 / 2.0;
        double movedY = y - 5.0 / 2.0;

        drawGeom.setFrame(movedX, movedY, 5.0, 5.0);
    }

    public void setValue(Long value){
        this.value = value;
    }

    public Long getValue(){
        return value;
    }

    public Ellipse2D getDrawGeom(){
        return drawGeom;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public void setHighlighted(boolean highlighted){
        this.highlighted = highlighted;
    }

    public boolean isHighlighted(){
        return highlighted;
    }

    public boolean listenForHighlighting(double mouseX, double mouseY){
        double distance = Math.sqrt(Math.pow(x - mouseX, 2) + Math.pow(y - mouseY, 2));
        boolean changed = false;

        if(distance < 5.0){
            if(!highlighted){
                changed = true;
            }

            highlighted = true;
        }
        else{
            if(highlighted){
                changed = true;
            }

            highlighted = false;
        }

        return changed;
    }
}
