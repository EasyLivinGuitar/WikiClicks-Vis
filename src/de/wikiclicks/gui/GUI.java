package de.wikiclicks.gui;

import de.wikiclicks.datastructures.SingleAttribGraph;
import de.wikiclicks.gui.renderer.VisSelectRenderer;
import de.wikiclicks.launcher.WikiClicks;
import de.wikiclicks.views.View;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class GUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private ArticleSelectGUI articleSelectGUI;

    private Set<String> viewIdentifier;

    private JList<String> namedEntityChoose;
    private JList<String> namedEntitySelected;

    private DefaultListModel<String> chooseModel;
    private DefaultListModel<String> selectedModel;

    private JScrollPane namedEntityPanel;
    private JCheckBox splitGraphByEntity;

    private JLabel selectedView;
    private JButton articleSelectButton;

    public GUI(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("WikiClicks-Vis");
        setSize(1440, 900);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        getContentPane().add(cardPanel);

        articleSelectGUI = new ArticleSelectGUI();

        viewIdentifier = new LinkedHashSet<>();
        selectedView = new JLabel();
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
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.insets = new Insets(10, 5, 5, 5);

        buttonPanel.setPreferredSize(new Dimension(300, getHeight()));

        articleSelectButton = new JButton("Select article");
        articleSelectButton.setBackground(Color.LIGHT_GRAY);
        articleSelectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                articleSelectGUI.setVisible(true);
                articleSelectGUI.repaint();
            }
        });

        setGlassPane(articleSelectGUI);

        JLabel views = new JLabel("Views");
        views.setFont(views.getFont().deriveFont(20.0f));
        views.setOpaque(true);

        buttonPanel.add(views, gridBagConstraints);

        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);

        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);

        buttonPanel.add(separator, gridBagConstraints);

        String[] items = viewIdentifier.toArray(new String[viewIdentifier.size()]);
        JList<String> list = new JList<>(items);

        list.setSelectedIndex(0);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                displayView(items[list.getSelectedIndex()]);
            }
        });

        list.setCellRenderer(new VisSelectRenderer());


        list.setFixedCellHeight(50);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        gridBagConstraints.weighty = 1;

        buttonPanel.add(list, gridBagConstraints);

        buttonPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        splitPane.setLeftComponent(buttonPanel);
        splitPane.setRightComponent(cardPanel);

        add(splitPane);

        selectedView.setFont(views.getFont());

        gridBagConstraints.gridy = 3;
        gridBagConstraints.weighty = 0;
        buttonPanel.add(selectedView, gridBagConstraints);

        JSeparator buttonSeparator = new JSeparator(JSeparator.HORIZONTAL);

        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        buttonPanel.add(buttonSeparator, gridBagConstraints);

        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(0, 5, 420, 5);

        buttonPanel.add(articleSelectButton, gridBagConstraints);


        namedEntityChoose = new JList<>();
        namedEntityChoose.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        namedEntityChoose.setModel(chooseModel);

        namedEntityChoose.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String selected = namedEntityChoose.getSelectedValue();
                if(selected != null){
                    if(WikiClicks.globalSettings.getNumSelectedNamedEntities() < 10){
                        selectedModel.addElement(selected);
                        chooseModel.removeElement(selected);

                        WikiClicks.globalSettings.selectNamedEntity(
                                selected.substring(selected.indexOf(" "), selected.length()).trim());
                    }
                }
            }
        });

        namedEntityPanel = new JScrollPane(namedEntityChoose);
        namedEntityPanel.createVerticalScrollBar();
        namedEntityPanel.setPreferredSize(new Dimension(100, 300));
        namedEntityPanel.setVisible(false);

        gridBagConstraints.anchor = GridBagConstraints.PAGE_END;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.insets = new Insets(0, 5, 10, 5);

        buttonPanel.add(namedEntityPanel, gridBagConstraints);

        namedEntitySelected = new JList<>();
        namedEntitySelected.setVisible(false);
        namedEntitySelected.setModel(selectedModel);

        namedEntitySelected.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String selected = namedEntitySelected.getSelectedValue();

                if(selected != null){
                    for(int i = 0; i < chooseModel.size(); i++){
                        int hotnessChoose = Integer.parseInt(chooseModel.get(i).split("\\s+")[0]);

                        int hotnessSelected = Integer.parseInt(selected.split("[ ]+")[0]);

                        if(hotnessChoose < hotnessSelected){
                            chooseModel.add(i, selected);
                            break;
                        }
                    }
                    selectedModel.removeElement(selected);

                    selectedModel.trimToSize();

                    WikiClicks.globalSettings.unselectNamedEntity(selected.substring(selected.indexOf(" "), selected.length()).trim());
                }
            }
        });

        namedEntitySelected.setCellRenderer(new ListCellRenderer<String>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = new JLabel(value);
                String namedEntity = value.substring(value.indexOf(" ")).trim();

                if(splitGraphByEntity.isSelected())
                    label.setForeground(SingleAttribGraph.attribColors.get(namedEntity));
                else
                    label.setForeground(Color.BLACK);

                return label;
            }
        });

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;

        gridBagConstraints.insets = new Insets(0, 5, 20, 5);

        buttonPanel.add(namedEntitySelected, gridBagConstraints);

        splitGraphByEntity = new JCheckBox("Split into hotness and clicks");

        splitGraphByEntity.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                WikiClicks.globalSettings.setSplitToEntities(!splitGraphByEntity.isSelected());

                if(namedEntitySelected.isVisible()){
                    namedEntitySelected.setVisible(false);
                    namedEntitySelected.setVisible(true);
                }
            }
        });

        splitGraphByEntity.setVisible(false);

        gridBagConstraints.gridy = 6;
        buttonPanel.add(splitGraphByEntity, gridBagConstraints);
    }

    public void addView(View view){
        if(selectedView.getText().isEmpty()){
            selectedView.setText(view.getIdentifier());
        }

        viewIdentifier.add(view.getIdentifier());

        for(JComponent component: view.getUIComponents()){
            add(component);
        }

        cardPanel.add(view, view.getIdentifier());
    }

    private void displayView(String identifier){
        selectedView.setText(identifier);
        cardLayout.show(cardPanel, identifier);

        if(identifier.equals("Entity Hotness")){
            namedEntityPanel.setVisible(true);
            namedEntitySelected.setVisible(true);
            splitGraphByEntity.setVisible(true);
            articleSelectButton.setVisible(false);
        }
        else{
            namedEntityPanel.setVisible(false);
            namedEntitySelected.setVisible(false);
            splitGraphByEntity.setVisible(false);
            articleSelectButton.setVisible(true);
        }
    }

    public void setNamedEntityData(Map<String, Integer> namedEntityData) {
        chooseModel = new DefaultListModel<>();
        selectedModel = new DefaultListModel<>();

        for(Map.Entry<String, Integer> entry: namedEntityData.entrySet()){
            if(WikiClicks.globalSettings.getSelectedNamedEntities().contains(entry.getKey())){
                selectedModel.addElement(String.format("%-8d%s", entry.getValue(), entry.getKey()));
            }
            else{
                chooseModel.addElement(String.format("%-8d%s", entry.getValue(), entry.getKey()));
            }
        }
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
