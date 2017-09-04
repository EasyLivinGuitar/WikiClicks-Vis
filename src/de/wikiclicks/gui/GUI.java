package de.wikiclicks.gui;

import de.wikiclicks.gui.renderer.VisSelectRenderer;
import de.wikiclicks.views.View;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;
import java.util.Set;

public class GUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private ArticleSelectGUI articleSelectGUI;

    private Set<String> viewIdentifier;

    public GUI(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("WikiClicks-Vis");
        setSize(1440, 900);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        getContentPane().add(cardPanel);

        articleSelectGUI = new ArticleSelectGUI();

        viewIdentifier = new LinkedHashSet<>();
    }

    public void initComponents(){
        initUIComponents();
    }

    private void initUIComponents(){
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.01;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.insets = new Insets(10, 5, 0, 5);

        buttonPanel.setPreferredSize(new Dimension(300, getHeight()));

        JButton articleSelectButton = new JButton("Select article");
        articleSelectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                articleSelectGUI.setVisible(true);
                articleSelectGUI.repaint();
            }
        });

        articleSelectButton.setPreferredSize(new Dimension(150, 30));

        buttonPanel.add(articleSelectButton, gridBagConstraints);

        setGlassPane(articleSelectGUI);

        String[] items = viewIdentifier.toArray(new String[viewIdentifier.size()]);
        JList list = new JList(items);

        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                displayView(items[list.getSelectedIndex()]);
            }
        });

        list.setCellRenderer(new VisSelectRenderer());

        list.setFixedCellHeight(50);
//        list.setFixedCellWidth(200);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        gridBagConstraints.weighty = 0.99;

        buttonPanel.add(list, gridBagConstraints);

        buttonPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

//        add(buttonPanel, BorderLayout.WEST);

        splitPane.setLeftComponent(buttonPanel);
        splitPane.setRightComponent(cardPanel);

        add(splitPane);
    }

    public void addView(View view){
        viewIdentifier.add(view.getIdentifier());

        for(JComponent component: view.getUIComponents()){
            add(component);
        }

        cardPanel.add(view, view.getIdentifier());
//        add(view);
    }

    @Override
    public void paintComponents(Graphics g){
        super.paintComponents(g);

        System.out.println("Test");
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
