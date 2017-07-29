package de.wikiclicks.gui;

import de.wikiclicks.gui.renderer.VisSelectRenderer;
import de.wikiclicks.views.View;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private ArticleSelectGUI articleSelectGUI;

    public GUI(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("WikiClicks-Vis");
        setSize(1440, 900);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        add(cardPanel);

        articleSelectGUI = new ArticleSelectGUI();

        initUIComponents();
    }

    private void initUIComponents(){
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setPreferredSize(new Dimension(200, getHeight()));

        JButton articleSelectButton = new JButton("Select article");
        articleSelectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                articleSelectGUI.setVisible(true);
                articleSelectGUI.repaint();
            }
        });

        buttonPanel.add(articleSelectButton);

        setGlassPane(articleSelectGUI);

        String[] items = {"clicks-graph", "test", "pie"};
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

    public void addView(View view){
        cardPanel.add(view, view.getIdentifier());
    }

    public void displayView(String identifier){
        cardLayout.show(cardPanel, identifier);
    }

    public void start(){
        setVisible(true);
    }

    public void openArticleSelect(){
        articleSelectGUI.setVisible(true);
    }

    public ArticleSelectGUI getArticleSelectGUI() {
        return articleSelectGUI;
    }
}
