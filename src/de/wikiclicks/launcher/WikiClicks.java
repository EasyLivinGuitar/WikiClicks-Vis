package de.wikiclicks.launcher;

import de.wikiclicks.controller.MouseController;
import de.wikiclicks.gui.GUI;
import de.wikiclicks.views.View;
import de.wikiclicks.views.ViewTest;

import javax.swing.*;

public class WikiClicks {
    private JPanel initTestView(){
        JPanel testView = new ViewTest();
        MouseController controller = new MouseController((View) testView);

        testView.addMouseListener(controller);
        testView.addMouseMotionListener(controller);

        return testView;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GUI gui = new GUI();
                WikiClicks wikiClicks = new WikiClicks();

                gui.addView("main", new JPanel());
                gui.addView("test", wikiClicks.initTestView());

                gui.start();
            }
        });
    }
}
