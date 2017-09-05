package de.wikiclicks.controller;

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
        view.setDayGraph(e.getX(), e.getY());
        view.changeDate(e.getX(), e.getY());
    }

    @Override
    public void mousePressed(MouseEvent e) {
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
        view.setHighlightedUnit(e.getX(), e.getY());
    }
}
