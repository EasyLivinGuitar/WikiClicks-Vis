package de.wikiclicks.views;

import de.wikiclicks.datastructures.DataPoint;
import de.wikiclicks.datastructures.Index;
import de.wikiclicks.datastructures.NewsArticle;
import de.wikiclicks.datastructures.WikiArticle;
import de.wikiclicks.launcher.WikiClicks;
import de.wikiclicks.utils.DateComparator;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.math.util.MathUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static java.awt.Font.PLAIN;


public class ViewClicksGraph extends View {
    private Index<NewsArticle> newsEntityIndex;

    private WikiArticle currentWikiArticle;
    private Map<String, Set<NewsArticle>> currentNewsArticlesDay;
    private Map<String, Set<NewsArticle>> currentNewsArticlesHour;

    private int numNewsArticles;

    private Date startDate;
    private Date endDate;

    private String displayedMonth;
    private String displayedDay;

    private Rectangle2D graphBackground;
    private Rectangle2D titleField;
    private Line2D xAxis, yAxis;
    private List<Rectangle2D> unitRects;

    private Integer highlightedUnit;

    private Rectangle2D legendField;

    private List<DataPoint> dataPoints;
    private List<Line2D> dataLines;

    private boolean isDayView = false;

    private int mouseX, mouseY;

    private Image backwardIcon, forwardIcon, returnIcon;

    private Rectangle2D backwardBounds, forwardBounds, returnBounds;

//    private JButton goBackwardsButton, goForwardButton;

    public ViewClicksGraph(Index<NewsArticle> newsEntityIndex){
        setLayout(new FlowLayout());

        this.newsEntityIndex = newsEntityIndex;

        currentNewsArticlesDay = new TreeMap<>(new DateComparator(new SimpleDateFormat("yyyyMMdd").toPattern()));
        currentNewsArticlesHour = new TreeMap<>(new DateComparator(new SimpleDateFormat("yyyyMMddHH").toPattern()));

        currentWikiArticle = WikiClicks.globalSettings.currentArticle;

        graphBackground = new Rectangle2D.Double();
        titleField = new Rectangle2D.Double();
        legendField = new Rectangle2D.Double();
        xAxis = new Line2D.Double();
        yAxis = new Line2D.Double();

        unitRects = new ArrayList<>(currentWikiArticle.getNumDays());
        dataPoints = new ArrayList<>(currentWikiArticle.getNumDays());
        dataLines = new ArrayList<>(currentWikiArticle.getNumDays() - 1);

        for(int i = 0; i < currentWikiArticle.getNumDays(); i++){
            unitRects.add(new Rectangle2D.Double());
            dataPoints.add(new DataPoint());
            dataLines.add(new Line2D.Double());
        }

        try {
            startDate = WikiClicks.globalSettings.hourFormat.parse(currentWikiArticle.getStartDate());
            displayedMonth = WikiClicks.globalSettings.monthFormat.format(startDate);
            displayedDay = WikiClicks.globalSettings.dayFormat.format(startDate);

            if(!isDayView) {
                endDate = WikiClicks.globalSettings.hourFormat.parse(currentWikiArticle.getEndDate());
            }
            else {
                endDate = startDate;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        highlightedUnit = null;

        try {
            backwardIcon = ImageIO.read(getClass().getResource("/icons/left-arrow.png"));
            forwardIcon = ImageIO.read(getClass().getResource("/icons/right-arrow.png"));
            returnIcon = ImageIO.read(getClass().getResource("/icons/up-arrow.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        backwardBounds = new Rectangle2D.Double();
        forwardBounds = new Rectangle2D.Double();
        returnBounds = new Rectangle2D.Double();

        initNewsArticles();
    }

    private void initNewsArticles(){
        Set<NewsArticle> articles = this.newsEntityIndex.get(currentWikiArticle.getTitle());
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        numNewsArticles = 0;

        currentNewsArticlesDay.clear();
        currentNewsArticlesHour.clear();

        for(NewsArticle article: articles){
            Date date = null;
            try {
                date = inputFormat.parse(article.getPublished());
                date.setMinutes(0);

                if(date.compareTo(WikiClicks.globalSettings.hourFormat.parse("201509010000")) < 0){
                    date = null;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(date != null){
                numNewsArticles++;
                currentNewsArticlesDay.putIfAbsent(WikiClicks.globalSettings.dayFormat.format(date), new HashSet<>());
                currentNewsArticlesDay.get(WikiClicks.globalSettings.dayFormat.format(date)).add(article);

                currentNewsArticlesHour.putIfAbsent(WikiClicks.globalSettings.hourFormat.format(date), new HashSet<>());
                currentNewsArticlesHour.get(WikiClicks.globalSettings.hourFormat.format(date)).add((article));
            }
        }
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);

        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.clearRect(0, 0, getWidth(), getHeight());


        float maxGraph;
        double stepWidth;
        int units;
        Long maxClicks;

        if (!isDayView) {
            units = currentWikiArticle.getNumDays();
            maxClicks = currentWikiArticle.getMaxOfMonth(displayedMonth).getValue();
        }
        else{
            units = 24; //hours
            maxClicks = currentWikiArticle.getMaxOfDay(displayedDay).getValue();
        }

        drawBackground(g2D);

        drawTitleField(g2D);

        drawAxis(g2D);

        int roundPrecision = String.valueOf(maxClicks).length() - 2;
        maxGraph = MathUtils.round(maxClicks, -roundPrecision, BigDecimal.ROUND_UP);
        stepWidth = maxGraph / 10.0;

        //calculate length of unit rectangle
        double unitLength = (xAxis.getX2() - xAxis.getX1()) / (double)(units);
        double lengthYAxis = yAxis.getY2() - yAxis.getY1();
        double scaling = (lengthYAxis) / maxGraph;

        g2D.setColor(Color.BLACK);
        g2D.setFont(g2D.getFont().deriveFont(10.0f));

        for(int i = 1; i <= 10; i++){
            int x = (int) yAxis.getX1();
            int y = (int) (yAxis.getY1() + (stepWidth *  i * scaling));
            String stepValueString = String.valueOf((int) (stepWidth * i));

            if(stepValueString.length() > 6){
                stepValueString = String.format("%.1fM", Double.valueOf(stepValueString) / 1000000.0);
            }

            int stringOffset = g2D.getFontMetrics().stringWidth(stepValueString);

            g2D.drawLine(x - 10, y, x, y);
            g2D.drawString(stepValueString, x - stringOffset - 15, y + 3);
        }

        g2D.setFont(g2D.getFont().deriveFont(12.0f));


        for(int i = 1; i <= units; i++){
            Rectangle2D currentRect = drawUnitRect(g2D, i, unitLength);

            DataPoint dataPoint = dataPoints.get(i - 1);

            if(!isDayView) {
                dataPoint.setValue(currentWikiArticle.getClicksOnDay(
                        displayedMonth + String.format("%02d", i)));
            }
            else {
                dataPoint.setValue(currentWikiArticle.getClicksOnHour(
                        displayedDay + String.format("%02d", i-1) + "00")
                );
            }
            double dataY = dataPoint.getValue() * scaling + yAxis.getY1();

            g2D.setStroke(new BasicStroke(2.0f));

            //Draw click function (points)
            dataPoint.setCoord(currentRect.getCenterX(), dataY);

            //Draw click function (lines)
            if(i > 1){
                DataPoint lastDataPoint = dataPoints.get(i - 2);
                Line2D dataLine = dataLines.get(i - 1);

                dataLine.setLine(lastDataPoint.getX(), lastDataPoint.getY(), dataPoint.getX(), dataPoint.getY());


                g2D.setColor(Color.BLUE);
                g2D.draw(dataLine);
            }

            Ellipse2D centerEllipse = dataPoint.getDrawGeom();

            if(highlightedUnit != null && highlightedUnit == i - 1){
                drawHighlighting(g2D);

                g2D.setColor(Color.RED);
                g2D.setFont(g2D.getFont().deriveFont(PLAIN));
            }
            else{
                g2D.setColor(Color.GRAY);
            }

            g2D.fill(centerEllipse);
            g2D.draw(centerEllipse);
            g2D.setStroke(new BasicStroke(0.5f));
        }

        drawLegend(g2D);
    }

    private void drawBackground(Graphics2D g2D){
        double marginFTBX = getWidth() * 0.05;
        double marginFTBY = getHeight() * 0.2;

        graphBackground.setRect(
                marginFTBX,
                marginFTBY,
                getWidth() -  2.0 * marginFTBX,
                getHeight() - 2.0 * marginFTBY
        );

        g2D.setColor(Color.WHITE);
        g2D.fill(graphBackground);
        g2D.setColor(Color.BLACK);
        g2D.draw(graphBackground);
    }

    private void drawTitleField(Graphics2D g2D){
        double width = graphBackground.getWidth() * 0.75;
        double height = graphBackground.getHeight() * 0.2;

        double x = graphBackground.getX() + (graphBackground.getWidth() - width) / 2.0;
        double y = graphBackground.getY() - height;

        titleField.setRect(x, y, width, height);

        g2D.setColor(Color.WHITE);
        g2D.fill(titleField);

        g2D.setColor(Color.BLACK);

        g2D.draw(titleField);

        String title = currentWikiArticle.getTitle();
        title = WordUtils.capitalize(title);
        Font font = g2D.getFont();
        font = font.deriveFont(Font.BOLD).deriveFont(30.0f);
        g2D.setFont(font);

        int stringOffset = g2D.getFontMetrics().stringWidth(title);

        x = titleField.getCenterX() - stringOffset / 2;
        y = titleField.getY() + 40;

        g2D.drawString(title, (int)(x), (int) y);
        g2D.setFont(font.deriveFont(PLAIN).deriveFont(25.0f));

        String formattedStartDate = "";
        String formattedEndDate;

        if(!isDayView) {
            SimpleDateFormat dateFormatOut = new SimpleDateFormat("dd/MM/yyyy");

            formattedStartDate = dateFormatOut.format(startDate);
            formattedEndDate = dateFormatOut.format(endDate);
        }
        else {
            SimpleDateFormat dateFormatOutStart = new SimpleDateFormat("dd/MM, 00:00");

            try {
                formattedStartDate = dateFormatOutStart.format(
                        WikiClicks.globalSettings.dayFormat.parse(displayedDay)
                );
            } catch (ParseException e) {
                e.printStackTrace();
            }

            formattedEndDate = "23:00";
        }

        String dateString = formattedStartDate + " - " + formattedEndDate;
        stringOffset = g2D.getFontMetrics().stringWidth(dateString);

        x = titleField.getCenterX() - stringOffset / 2;
        y = titleField.getY() + 80;

        g2D.drawString(dateString, (int) x, (int) y);

        if(isDayView){

            backwardBounds.setRect(x - 50, y - 23, 28, 28);

            g2D.drawImage(
                    backwardIcon,
                    (int)backwardBounds.getX(),
                    (int)backwardBounds.getY() ,
                    (int)backwardBounds.getWidth(),
                    (int)backwardBounds.getHeight(),
                    Color.WHITE,
                    null);

            forwardBounds.setRect(x + stringOffset + 50 - 28, (int) y - 23, 28, 28);

            g2D.drawImage(
                    forwardIcon,
                    (int) forwardBounds.getX(),
                    (int) forwardBounds.getY(),
                    (int) forwardBounds.getWidth(),
                    (int) forwardBounds.getHeight(),
                    Color.WHITE,
                    null);

            x = titleField.getCenterX() - stringOffset / 2 - 30;
            y = titleField.getY() + 15;

            returnBounds.setRect(x + 5, y + 2, 28, 28);

            g2D.drawImage(
                    returnIcon,
                    (int) returnBounds.getX(),
                    (int) returnBounds.getY(),
                    (int) returnBounds.getWidth(),
                    (int) returnBounds.getHeight(),
                    Color.WHITE,
                    null);
        }

    }

    private void drawAxis(Graphics2D g2D){
        double marginBTGX = graphBackground.getWidth() * 0.05;
        double marginBTGY = graphBackground.getHeight() * 0.08;

        double backgroundX = graphBackground.getX();
        double backgroundY = graphBackground.getY();

        xAxis.setLine(
                backgroundX + marginBTGX,
                backgroundY + graphBackground.getHeight() - marginBTGY,
                backgroundX + graphBackground.getWidth() -  marginBTGX,
                backgroundY + graphBackground.getHeight() - marginBTGY
        );

        yAxis.setLine(
                backgroundX + marginBTGX,
                backgroundY + graphBackground.getHeight() - marginBTGY,
                backgroundX + marginBTGX,
                backgroundY + marginBTGY
        );

        g2D.setStroke(new BasicStroke(0.5f));

        g2D.draw(xAxis);
        g2D.draw(yAxis);
    }

    private Rectangle2D drawUnitRect(Graphics2D g2D, int unit, double unitLength){
        String currentDateString;
        if(!isDayView){
            currentDateString = displayedMonth + String.format("%02d", unit);
        }
        else{
            currentDateString = displayedDay + String.format("%02d", unit - 1) + "00";
        }

        double currentX = xAxis.getX1() + (unit - 1) * unitLength;
        Rectangle2D currentRect = unitRects.get(unit - 1);
        currentRect.setRect(currentX, xAxis.getY1(), unitLength, graphBackground.getHeight() * 0.03);

        double percentage;
        if(numNewsArticles > 0){
            if(!isDayView) {
                percentage = (double)(currentNewsArticlesDay.getOrDefault(currentDateString, new HashSet<>()).size())
                        / (double)(numNewsArticles) * 100.0;
            }
            else {
                int numNewsArticlesThisDay = currentNewsArticlesDay.getOrDefault(currentDateString.substring(0, currentDateString.length() - 4), new HashSet<>()).size();
                int numNewsArticlesThisHour = currentNewsArticlesHour.getOrDefault(currentDateString, new HashSet<>()).size();

                percentage = (double)(numNewsArticlesThisHour) / (double)(numNewsArticlesThisDay) * 100.0;
            }

            if(percentage < 1.0){
                g2D.setColor(Color.WHITE);
            }else if(percentage >= 1.0 && percentage < 5.0){
                g2D.setColor(Color.PINK);
            }else if(percentage >= 5.0 && percentage < 10.0){
                g2D.setColor(new Color(239, 77, 84));
            }else if(percentage >= 10.0){
                g2D.setColor(Color.RED);
            }

            g2D.fill(currentRect);
        }

        g2D.setColor(Color.BLACK);
        g2D.draw(currentRect);


        float stringWidth = g2D.getFontMetrics().stringWidth(String.valueOf(unit));
        float stringHeight = g2D.getFont().getSize();

        if (isDayView) {
            unit -=1;
        }

        g2D.drawString(
                String.valueOf(unit),
                (float)(currentX + unitLength / 2.0 - stringWidth / 2.0),
                (float) (xAxis.getY1() + graphBackground.getHeight() * 0.015 + stringHeight / 2.0)
        );

        return currentRect;
    }

    private void drawLegend(Graphics2D g2D){

        double width = graphBackground.getWidth();
        double height = graphBackground.getHeight() * 0.3;

        double x = graphBackground.getX() + (graphBackground.getWidth() - width) / 2.0;
        double y = graphBackground.getY() + (graphBackground.getHeight() + height * 0.03);

        legendField.setRect(x, y, width, height);

        g2D.setColor(Color.LIGHT_GRAY);
        g2D.fill(legendField);

        g2D.draw(legendField);

        g2D.setColor(Color.BLACK);

        String captionColors = "Percentage of news articles per";
        String clicks = "Total number of clicks";
        String news = "Total number of news articles";
        String least = "Least: ";
        String most = "Most: ";
        String mostClicks = most;
        String mostNews = most;
        String leastClicks = least;
        String leastNews = least;

        if(!isDayView){

            //news for the month
            int totalNews = 0;

            Long minNewsMonth = Long.MAX_VALUE;
            Long maxNewsMonth = 0L;
            Map.Entry<String, Set<NewsArticle>> maxNews = null;
            Map.Entry<String, Set<NewsArticle>> minNews = null;

            for(Map.Entry<String, Set<NewsArticle>> entity: currentNewsArticlesDay.entrySet()) {
                totalNews += entity.getValue().size();
                if (entity.getValue().size() > maxNewsMonth) {
                    maxNewsMonth = (long) entity.getValue().size();
                    maxNews = entity;
                }
                else if (entity.getValue().size() < minNewsMonth) {
                    minNewsMonth = (long) entity.getValue().size();
                    minNews = entity;
                }
            }



            captionColors = captionColors + " day (out of all)";
            clicks = clicks + " for this month: " + WikiClicks.globalSettings.currentArticle.getTotalClicks();
            news = news + " for this month: " + totalNews;
            SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd/MM/yyyy");
            Date dateMax = null;
            Date dateMin = null;
            try {
                dateMin = WikiClicks.globalSettings.dayFormat.parse(WikiClicks.globalSettings.currentArticle.getMinOfMonth(displayedMonth).getKey());
                dateMax = WikiClicks.globalSettings.dayFormat.parse(WikiClicks.globalSettings.currentArticle.getMaxOfMonth(displayedMonth).getKey());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            mostClicks = most + WikiClicks.globalSettings.currentArticle.getMaxOfMonth(displayedMonth).getValue() + " on " + dateFormatDay.format(dateMax);
            leastClicks = least + WikiClicks.globalSettings.currentArticle.getMinOfMonth(displayedMonth).getValue() + " on " + dateFormatDay.format(dateMin);

            Date dateMaxNews = null;
            Date dateMinNews = null;
            try {
                dateMinNews = WikiClicks.globalSettings.dayFormat.parse(minNews.getKey());
                dateMaxNews = WikiClicks.globalSettings.dayFormat.parse(maxNews.getKey());
            } catch (ParseException e) {
                e.printStackTrace();
            }


            mostNews = most + maxNews.getValue().size() + " on " + dateFormatDay.format(dateMaxNews);
            leastNews = least + minNews.getValue().size() + " on " + dateFormatDay.format(dateMinNews);


        }
        else{

            //news for the day
            int totalNews = 0;

            Long minNewsDay = Long.MAX_VALUE;
            Long maxNewsDay = 0L;
            Map.Entry<String, Set<NewsArticle>> maxNews = null;
            Map.Entry<String, Set<NewsArticle>> minNews = null;

            for(Map.Entry<String, Set<NewsArticle>> entity: currentNewsArticlesHour.entrySet()) {
                if (entity.getKey().startsWith(displayedDay)) {
                    totalNews += entity.getValue().size();
                    if (entity.getValue().size() > maxNewsDay) {
                        maxNewsDay = (long) entity.getValue().size();
                        maxNews = entity;
                    }
                    else if (entity.getValue().size() < minNewsDay) {
                        minNewsDay = (long) entity.getValue().size();
                        minNews = entity;
                    }
                }

            }

            captionColors = captionColors + " hour (out of all)";
            clicks = clicks + " for this day: " + WikiClicks.globalSettings.currentArticle.getTotalClicksDay(displayedDay);
            news = news + " for this day: " + totalNews;

            SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH:mm");
            Date dateMax = null;
            Date dateMin = null;
            try {
                dateMin = WikiClicks.globalSettings.hourFormat.parse(WikiClicks.globalSettings.currentArticle.getMinOfDay(displayedDay).getKey());
                dateMax = WikiClicks.globalSettings.hourFormat.parse(WikiClicks.globalSettings.currentArticle.getMaxOfDay(displayedDay).getKey());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            mostClicks = most + WikiClicks.globalSettings.currentArticle.getMaxOfDay(displayedDay).getValue() + " at " + dateFormatHour.format(dateMax);
            leastClicks = least + WikiClicks.globalSettings.currentArticle.getMinOfDay(displayedDay).getValue() + " at " + dateFormatHour.format(dateMin);

            Date dateMaxNews = null;
            Date dateMinNews = null;
            try {
                dateMinNews = WikiClicks.globalSettings.hourFormat.parse(minNews.getKey());
                dateMaxNews = WikiClicks.globalSettings.hourFormat.parse(maxNews.getKey());
            } catch (ParseException e) {
                e.printStackTrace();
            }


            mostNews = most + maxNews.getValue().size() + " on " + dateFormatHour.format(dateMaxNews);
            leastNews = least + minNews.getValue().size() + " on " + dateFormatHour.format(dateMinNews);

        }

        x = legendField.getX() + 5;
        y = legendField.getY() + 15;

        Font font = g2D.getFont();
        font = font.deriveFont(Font.BOLD);
        g2D.setFont(font);

//        clicks statistics
        g2D.drawString(clicks, (int) x, (int) y);

        double fontSize = font.getSize();

        y += fontSize + 2;
        g2D.drawString(mostClicks, (int) x, (int) y);


        y += fontSize + 2;
        g2D.drawString(leastClicks, (int) x, (int) y);

//        news statistics
        x = legendField.getCenterX() + 5;
        y = legendField.getY() + 15;

        g2D.drawString(news, (int) x, (int) y);

        y += fontSize + 2;
        g2D.drawString(mostNews, (int) x, (int) y);


        y += fontSize + 2;
        g2D.drawString(leastNews, (int) x, (int) y);

// color legend
        x = legendField.getX();

        y += 3 * (fontSize + 2);

        g2D.drawString(captionColors, (int) x, (int) y);

        y += 10;

        int rectWidth = 20;
        int rectHeight = 15;

        g2D.setFont(font.deriveFont(Font.PLAIN));

//        white
        Rectangle2D white = new Rectangle2D.Double(x, y, rectWidth, rectHeight);
        g2D.setColor(Color.WHITE);
        g2D.fill(white);
        g2D.setColor(Color.BLACK);
        g2D.draw(white);

        y += fontSize;
        x += rectWidth + 4;
        g2D.drawString( " less than 1%", (int) x, (int) y);

//        light pink
        y += fontSize + 2;
        x = legendField.getX();

        Rectangle2D light = new Rectangle2D.Double(x, y, rectWidth, rectHeight);
        g2D.setColor(Color.PINK);
        g2D.fill(light);
        g2D.setColor(Color.BLACK);
        g2D.draw(light);

        y += fontSize;
        x += rectWidth + 4;
        g2D.drawString( " between 1% and 5%", (int) x, (int) y);

//        dark pink

        y -= (fontSize * 3 + 2);
        x = legendField.getCenterX() * 0.38;

        Rectangle2D dark = new Rectangle2D.Double(x, y, rectWidth, rectHeight);
        g2D.setColor(new Color(239, 77, 84));
        g2D.fill(dark);
        g2D.setColor(Color.BLACK);
        g2D.draw(dark);

        y += fontSize;
        x += rectWidth + 4;
        g2D.drawString( " between 5% and 10%", (int) x, (int) y);

//        red
        y += fontSize + 2;
        x = legendField.getCenterX() * 0.38;

        Rectangle2D red = new Rectangle2D.Double(x, y, rectWidth, rectHeight);
        g2D.setColor(Color.RED);
        g2D.fill(red);
        g2D.setColor(Color.BLACK);
        g2D.draw(red);

        y += fontSize;
        x += rectWidth + 4;
        g2D.drawString( " over 10%", (int) x, (int) y);
    }

    private void drawHighlighting(Graphics2D g2D){
        g2D.setColor(Color.GRAY);
        g2D.setStroke(new BasicStroke(
                0.5f,
                BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER,
                1,
                new float[]{4.0f},
                0));

        g2D.drawLine(
                (int) dataPoints.get(highlightedUnit).getX(),
                (int) dataPoints.get(highlightedUnit).getY(),
                (int) dataPoints.get(highlightedUnit).getX(),
                (int) xAxis.getY1()
        );

        g2D.setStroke(new BasicStroke(1.0f));

        if(highlightedUnit - 1 >= 0){
            double heightL = (dataPoints.get(highlightedUnit).getY() + dataPoints.get(highlightedUnit - 1).getY()) / 2.0;

            g2D.drawLine(
                    (int) unitRects.get(highlightedUnit).getX(),
                    (int) heightL,
                    (int) unitRects.get(highlightedUnit).getX(),
                    (int) xAxis.getY1()
            );
        }

        if(highlightedUnit + 1 < dataPoints.size()){
            double heightR = (dataPoints.get(highlightedUnit).getY() + dataPoints.get(highlightedUnit + 1).getY()) / 2.0;

            g2D.drawLine(
                    (int) unitRects.get(highlightedUnit).getMaxX(),
                    (int) heightR,
                    (int) unitRects.get(highlightedUnit).getMaxX(),
                    (int) xAxis.getY1()
            );
        }


        RoundRectangle2D infoBox = new RoundRectangle2D.Double(
                dataPoints.get(highlightedUnit).getX() - 150.0 - unitRects.get(0).getWidth(),
                mouseY,
                150.0,
                160.0,
                5.0,
                5.0
        );

        g2D.setColor(new Color(30, 30, 30, 230));
        g2D.fill(infoBox);

        g2D.draw(infoBox);

        String highlightedDate;
        String formattedDate = "";

        SimpleDateFormat outFormat;

        if(!isDayView){
            highlightedDate = displayedMonth + String.format("%02d", highlightedUnit + 1);

            outFormat = new SimpleDateFormat("dd/MM/yyyy");

            try {
                formattedDate = outFormat.format(WikiClicks.globalSettings.dayFormat.parse(highlightedDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else{
            highlightedDate = displayedDay + String.format("%02d", highlightedUnit) + "00";

            outFormat = new SimpleDateFormat("HH:mm");

            try {
                formattedDate = outFormat.format(WikiClicks.globalSettings.hourFormat.parse(highlightedDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        g2D.setColor(Color.WHITE);
        g2D.setFont(g2D.getFont().deriveFont(Font.BOLD).deriveFont(15.0f));

        g2D.drawString(formattedDate, (int)infoBox.getX() + 10, (int) (infoBox.getY() + 10 + g2D.getFontMetrics().getHeight()));

        String clicks = String.valueOf(dataPoints.get(highlightedUnit).getValue());
        g2D.drawString(
                "Wiki-Clicks: ",
                (int)infoBox.getX() + 10,
                (int) (infoBox.getY() + 10 + 3 * g2D.getFontMetrics().getHeight())
        );

        g2D.drawString(
                clicks,
                (int)infoBox.getX() + 10,
                (int) (infoBox.getY() + 10 + 4 * g2D.getFontMetrics().getHeight())
        );

        g2D.drawString(
                "News-Articles: ",
                (int)infoBox.getX() + 10,
                (int) (infoBox.getY() + 10 + 6 * g2D.getFontMetrics().getHeight())
        );

        String articles;

        if(!isDayView)
            articles = String.valueOf(currentNewsArticlesDay.getOrDefault(highlightedDate,new HashSet<>()).size());
        else
            articles = String.valueOf(currentNewsArticlesHour.getOrDefault(highlightedDate,new HashSet<>()).size());

        g2D.drawString(
                articles,
                (int)infoBox.getX() + 10,
                (int) (infoBox.getY() + 10 + 7 * g2D.getFontMetrics().getHeight())
        );

        g2D.setFont(g2D.getFont().deriveFont(12.0f));
    }

    public void setHighlightedUnit(int mouseX, int mouseY){
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        if(mouseY < xAxis.getY1() && mouseY > yAxis.getY2()){
            double dayLength = unitRects.get(0).getWidth();
            int index = (int) ((mouseX - yAxis.getX1()) / dayLength);

            if(index >= 0 && index < unitRects.size()){
                setHighlighted(index);
            }
            else{
                setHighlighted(null);
            }
        }
        else{
            setHighlighted(null);
        }
    }

    private void setHighlighted(Integer highlighted){
        if(!Objects.equals(highlightedUnit, highlighted)){
            highlightedUnit = highlighted;
            repaint();
        }
    }

    public void setDayGraph(int mouseX, int mouseY){
        if(!isDayView){
            double dayLength = unitRects.get(0).getWidth();
            int index = (int) ((mouseX - yAxis.getX1()) / dayLength);

            if(index >= 0 && index < unitRects.size()){
                if(unitRects.get(index).contains(mouseX, mouseY)){
                    isDayView = true;
                    displayedDay = displayedMonth + String.format("%02d", index + 1);
                    repaint();
                }
            }
        }
    }

    public void changeDate(int mouseX, int mouseY){
        if(isDayView){
            if(forwardBounds.contains(mouseX, mouseY)){
                displayedDay = String.valueOf(Long.parseLong(displayedDay) + 1L);
                repaint();
            }
            else if(backwardBounds.contains(mouseX, mouseY)){
                displayedDay = String.valueOf(Long.parseLong(displayedDay) - 1L);
                repaint();
            }
        }

    }

    public void returnToMonth(int mouseX, int mouseY){
        if(isDayView) {
            if(returnBounds.contains(mouseX, mouseY)){
                isDayView = false;
                System.out.print("should return");
                repaint();
            }
        }
    }

    @Override
    public String getIdentifier() {
        return "Clicks Graph";
    }

    @Override
    public void changeArticle(WikiArticle newArticle) {
        currentWikiArticle = newArticle;

        initNewsArticles();
        repaint();
    }
}
