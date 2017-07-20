package de.wikiclicks.gui;

import de.wikiclicks.gui.renderer.VisSelectRenderer;
import de.wikiclicks.views.View;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public class GUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public GUI(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("WikiClicks-Vis");
        setSize(800, 800);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        add(cardPanel);

        initGlobalButtons();
    }

    private void initGlobalButtons(){
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(200, getHeight()));

        String[] items = {"main", "test"};
        JList list = new JList(items);

        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                displayView(items[list.getSelectedIndex()]);
            }
        });

        list.setCellRenderer(new VisSelectRenderer());

        list.setFixedCellHeight(50);
        list.setFixedCellWidth(200);

        buttonPanel.add(list);

        buttonPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        add(buttonPanel, BorderLayout.WEST);
    }

    public void addView(String identifier, View view){
        cardPanel.add(view, identifier);
    }

    public void displayView(String identifier){
        cardLayout.show(cardPanel, identifier);
    }

    public void start(){
        setVisible(true);
    }
}
