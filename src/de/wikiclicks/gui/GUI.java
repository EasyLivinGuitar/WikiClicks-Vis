package de.wikiclicks.gui;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public class GUI {
    private JFrame jFrame;

    private CardLayout cardLayout;
    private JPanel cardPanel;

    public GUI(){
        jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setTitle("WikiClicks-Vis");
        jFrame.setSize(800, 800);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        jFrame.add(cardPanel);

        initGlobalButtons();
    }

    private void initGlobalButtons(){
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(200, jFrame.getHeight()));

        String[] items = {"main", "test"};
        JList list = new JList(items);

        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                displayView(items[list.getSelectedIndex()]);
            }
        });

        list.setCellRenderer(new ListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String item = (String) value;

                JLabel label = new JLabel();
                label.setText(item);
                label.setOpaque(true);

                if(isSelected){
                    label.setForeground(Color.BLACK);
                    label.setBackground(Color.ORANGE);
                }
                else{
                    label.setForeground(Color.BLACK);
                    label.setBackground(Color.WHITE);
                }
                label.setFont(new Font("Lucida Blackletter", Font.BOLD,30));

                label.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, Color.BLACK));

                return label;
            }
        });

        list.setFixedCellHeight(50);
        list.setFixedCellWidth(200);

        buttonPanel.add(list);

        /*buttonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;

        buttonPanel.setPreferredSize(new Dimension(200, jFrame.getHeight()));

        JButton testButton1 = new JButton("Main");
        testButton1.addActionListener(e -> cardLayout.show(cardPanel, "main"));

        JButton testButton2 = new JButton("Test");
        testButton2.addActionListener(e -> cardLayout.show(cardPanel, "test"));

        JButton testButton3 = new JButton("Test 3");
        testButton3.addActionListener(e -> {});

        buttonPanel.add(testButton1, gridBagConstraints);
        buttonPanel.add(testButton2, gridBagConstraints);
        buttonPanel.add(testButton3, gridBagConstraints);*/

        buttonPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        jFrame.add(buttonPanel, BorderLayout.WEST);
    }

    public void addView(String identifier, JPanel view){
        cardPanel.add(view, identifier);
    }

    public void displayView(String identifier){
        cardLayout.show(cardPanel, identifier);
    }

    public void start(){
        jFrame.setVisible(true);
    }
}
