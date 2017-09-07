package de.wikiclicks.gui.renderer;

import javax.swing.*;
import java.awt.*;

public class VisSelectRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String item = (String) value;

        JLabel label = new JLabel();
        label.setText(item);
        label.setOpaque(true);
        label.setForeground(Color.BLACK);

        if(isSelected){
            label.setBackground(Color.LIGHT_GRAY);
        }
        else{
            label.setBackground(Color.WHITE);
        }

        label.setFont(label.getFont().deriveFont(Font.BOLD).deriveFont(20.0f));

        label.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, Color.BLACK));

        label.setHorizontalAlignment(JLabel.CENTER);

        return label;
    }
}
