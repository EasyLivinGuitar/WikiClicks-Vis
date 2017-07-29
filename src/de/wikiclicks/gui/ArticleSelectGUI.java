package de.wikiclicks.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class ArticleSelectGUI extends JPanel {
    private Rectangle2D background;
    private RoundRectangle2D window;

    private JButton xButton;

    private String headline;
    private String subHeadline;

    private float headlineSize = 40.0f;
    private float subheadlineSize = 35.0f;

    public ArticleSelectGUI(){
        background = new Rectangle2D.Double();
        window = new RoundRectangle2D.Double();

        xButton = new JButton("X");
        xButton.setMargin(new Insets(0, 0,0,0));
        xButton.setBackground(Color.RED);
        xButton.setForeground(Color.WHITE);
        xButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
//        xButton.setBorder(BorderFactory.createCompoundBorder());
        add(xButton);

        headline = "Wiki-Clicks";
        subHeadline = "Visualization";
    }


    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double windowPosX = getWidth() * 0.35;
        double windowPosY = getHeight() * 0.1;
        double windowWidth = getWidth() - 2.0 * windowPosX;
        double windowHeight = getHeight() - 2.0 * windowPosY;

        background.setRect(0, 0, getWidth(), getHeight());

        g2D.setColor(new Color(50, 50, 50, 200));
        g2D.fill(background);
        g2D.draw(background);


        window.setRoundRect(
                windowPosX,
                windowPosY,
                windowWidth,
                windowHeight,
                10.0,
                10.0);
        g2D.setColor(Color.WHITE);
        g2D.fill(window);
        g2D.draw(window);

        g2D.setFont(g2D.getFont().deriveFont(40.0f).deriveFont(Font.BOLD));
        int headlineWidth = g2D.getFontMetrics().stringWidth(headline);
        g2D.setColor(Color.BLACK);
        g2D.drawString(headline, (int)(windowPosX + windowWidth / 2 - headlineWidth / 2), (int) (windowPosY + 100));

        g2D.setFont(g2D.getFont().deriveFont(35.0f).deriveFont(Font.PLAIN));
        int subheadlineWidth = g2D.getFontMetrics().stringWidth(subHeadline);
        g2D.setColor(Color.DARK_GRAY);
        g2D.drawString(subHeadline, (int)(windowPosX + windowWidth/2 - subheadlineWidth / 2), (int)(windowPosY + 100 + headlineSize ));

        xButton.setBounds((int)windowPosX,(int)windowPosY, 40 ,30);
    }
}