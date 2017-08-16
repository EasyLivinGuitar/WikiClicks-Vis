package de.wikiclicks.gui;

import de.wikiclicks.launcher.WikiClicks;
import de.wikiclicks.listener.ArticleListener;
import org.apache.commons.lang3.text.WordUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

public class ArticleSelectGUI extends JPanel {
    private Rectangle2D background;
    private RoundRectangle2D window;

    private JButton xButton;
    private JButton searchButton;
    private JButton topButton;
    private JButton randomButton;
    private JButton visualizeButton;

    private JTextField searchBar;
    private JLabel selectedLabel;
    private JLabel articleTitle;
    private JLabel changeState;

    private String headline;
    private String subHeadline;

    private float headlineSize = 40.0f;
    private float subheadlineSize = 35.0f;

    private List<ArticleListener> articleListener;

    public ArticleSelectGUI(){
        background = new Rectangle2D.Double();
        window = new RoundRectangle2D.Double();

        ActionListener searchListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchArticle(searchBar.getText());
                changeState.setVisible(true);

                if(WikiClicks.globalSettings.articleChangeSuccess){
                    articleTitle.setText("\""+WordUtils.capitalize(searchBar.getText())+"\"");
                    changeState.setForeground(new Color(0, 100, 10));
                    changeState.setText("Changed successful!");
                }
                else{
                    changeState.setForeground(Color.RED);
                    changeState.setText("Not found!");
                }

                repaint();
            }
        };

        xButton = new JButton("X");
        xButton.setMargin(new Insets(0, 0,0,0));
        xButton.setBackground(Color.RED);
        xButton.setForeground(Color.WHITE);
        xButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);

                resetValues();
            }
        });
        add(xButton);

        searchBar = new JTextField();
        searchBar.addActionListener(searchListener);
        searchBar.setMargin(new Insets(0, 0, 0,0));
        add(searchBar);

        searchButton = new JButton();
        searchButton.setMargin(new Insets(0,0,0,0));

        try {
            Image searchIcon = ImageIO.read(getClass().getResource("/icons/search-icon.png"));
            searchButton.setIcon(new ImageIcon(searchIcon.getScaledInstance(20,20, Image.SCALE_SMOOTH)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        searchButton.addActionListener(searchListener);

        add(searchButton);

        topButton = new JButton("Select from Top 10");
        add(topButton);

        randomButton = new JButton("Select random");
        add(randomButton);

        visualizeButton = new JButton("Vizualize");
        add(visualizeButton);

        selectedLabel = new JLabel("Selected article:");
        selectedLabel.setFont(selectedLabel.getFont().deriveFont(25.0f).deriveFont(Font.PLAIN));
        selectedLabel.setForeground(Color.DARK_GRAY);
        add(selectedLabel);

        articleTitle = new JLabel("\""+""+"\"");
        articleTitle.setFont(articleTitle.getFont().deriveFont(25.0f).deriveFont(Font.PLAIN));
        articleTitle.setForeground(Color.DARK_GRAY);
        add(articleTitle);

        changeState = new JLabel();
        changeState.setFont(changeState.getFont().deriveFont(15.0f).deriveFont(Font.PLAIN));
        changeState.setVisible(false);
        add(changeState);

        headline = "Wiki-Clicks";
        subHeadline = "Visualization";

        articleListener = new ArrayList<>();

        visualizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeArticle(articleTitle.getText().substring(1, articleTitle.getText().length() - 1));
                setVisible(false);

                resetValues();
            }
        });
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

        double windowLeftAlignment = windowPosX + 20;
        double windowRightAlignment = windowPosX + windowWidth - 20;

        AttributedString articleSelection = new AttributedString("Article selection");
        articleSelection.addAttribute(TextAttribute.FONT, g2D.getFont().deriveFont(25.0f));
        g2D.drawString(articleSelection.getIterator(), (int)(windowLeftAlignment), (int) (windowPosY + windowHeight * 0.35));

        g2D.setColor(Color.LIGHT_GRAY);
        g2D.drawLine((int)(windowLeftAlignment), (int)(windowPosY + windowHeight * 0.37), (int)(windowRightAlignment), (int) (windowPosY + windowHeight * 0.37));

        xButton.setBounds((int)windowPosX,(int)windowPosY, 40 ,30);
        searchBar.setBounds((int)(windowPosX + 50), (int) (windowPosY + windowHeight * 0.4), (int) (windowWidth * 0.7), 30);

        searchButton.setBounds((int)(windowLeftAlignment), (int) (windowPosY + windowHeight * 0.4), 30, 30);

        changeState.setBounds((int) windowLeftAlignment, (int)(windowPosY + windowHeight * 0.45), (int)(windowWidth * 0.7), 30);

        topButton.setBounds((int) windowLeftAlignment, (int)(windowPosY + windowHeight * 0.55), (int)(windowWidth * 0.7 + 30), 30);
        randomButton.setBounds((int) windowLeftAlignment, (int)(windowPosY + windowHeight * 0.65), (int)(windowWidth * 0.7 + 30), 30);

        g2D.setColor(Color.LIGHT_GRAY);
        g2D.drawLine((int)(windowLeftAlignment), (int)(windowPosY + windowHeight * 0.8), (int)(windowRightAlignment), (int) (windowPosY + windowHeight * 0.8));

        selectedLabel.setBounds((int) windowLeftAlignment, (int)(windowPosY + windowHeight * 0.75), (int)(windowWidth * 0.7 + 30), 30);

        int titleWidth = articleTitle.getFontMetrics(articleTitle.getFont()).stringWidth(articleTitle.getText());
        articleTitle.setBounds((int) (windowPosX + windowWidth / 2 - titleWidth /  2), (int) (windowPosY+ windowHeight * 0.82), (int)(windowWidth * 0.7 + 30), 30);

        visualizeButton.setBounds((int)(windowPosX + windowWidth/2) - 50, (int) (windowPosY+ windowHeight * 0.9), 100, 30);

    }

    public void addArticleListener(ArticleListener listener){
        articleListener.add(listener);
    }

    private void changeArticle(String title){
        for(ArticleListener listener: articleListener){
            listener.articleChanged(title);
        }
    }

    private void searchArticle(String title){
        for(ArticleListener listener: articleListener){
            listener.articleSearched(title);
        }
    }

    private void resetValues(){
        changeState.setVisible(false);
        searchBar.setText("");

        articleTitle.setText("\""+WordUtils.capitalize(WikiClicks.globalSettings.currentArticle.getTitle())+"\"");
    }
}
