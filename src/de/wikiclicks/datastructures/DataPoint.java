package de.wikiclicks.datastructures;

import java.awt.geom.Ellipse2D;

public class DataPoint {
    private Ellipse2D drawGeom;
    private double x, y;
    private Long value;

    public DataPoint(){
        drawGeom = new Ellipse2D.Double();
    }

    public void setCoord(double x, double y){
        this.x = x;
        this.y = y;

        double movedX = x - 5.0 / 2.0;
        double movedY = y - 5.0 / 2.0;

        drawGeom.setFrame(movedX, movedY, 5.0, 5.0);
    }

    public void setValue(Long value){
        if(value == null){
            this.value = 0L;
        }
        else{
            this.value = value;
        }
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
}
