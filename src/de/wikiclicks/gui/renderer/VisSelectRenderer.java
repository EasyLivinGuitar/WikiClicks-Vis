package de.wikiclicks.gui.renderer;

import javax.swing.*;
import java.awt.*;

public class VisSelectRenderer implements ListCellRenderer {
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
}
