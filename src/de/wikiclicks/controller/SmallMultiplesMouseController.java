package de.wikiclicks.controller;

import de.wikiclicks.views.ViewSmallMultiples;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class SmallMultiplesMouseController implements MouseListener, MouseMotionListener {
    private ViewSmallMultiples view;

    public SmallMultiplesMouseController(ViewSmallMultiples view){
        this.view = view;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //Check for date change
        view.changeDay(e.getX(), e.getY());
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
        //Move crosshairs
        view.updateCrosshair(e.getX(), e.getY());
    }
}
