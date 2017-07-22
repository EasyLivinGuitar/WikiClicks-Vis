package de.wikiclicks.controller;

import de.wikiclicks.datastructures.DataPoint;
import de.wikiclicks.views.ViewClicksGraph;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class ClicksGraphMouseController implements MouseListener, MouseMotionListener {
    private ViewClicksGraph view;

    public ClicksGraphMouseController(ViewClicksGraph view){
        this.view = view;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON3){
            view.triggerPopup(e.getXOnScreen(), e.getYOnScreen());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        DataPoint point = view.getRelevantDataPoint(e.getX());

        if(point != null){
            if(point.listenForHighlighting(e.getX(), e.getY())){
                view.repaint();
            }
        }

    }
}
