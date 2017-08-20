package de.wikiclicks.views;

import de.wikiclicks.datastructures.*;
import de.wikiclicks.launcher.WikiClicks;
import de.wikiclicks.utils.DateComparator;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.math.util.MathUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class ViewClicksGraph extends View {
    private PersistentArticleStorage wikiArticleStorage;
    private EntityIndex newsEntityIndex;

    private WikiArticle currentWikiArticle;
    private Map<String, Set<NewsArticle>> currentNewsArticlesDay;
    private Map<String, Set<NewsArticle>> currentNewsArticlesHour;

    private int numNewsArticles;

    private Date startDate;
    private Date endDate;

    private Rectangle2D graphBackground;
    private Rectangle2D titleField;
    private Line2D xAxis, yAxis;
    private List<Rectangle2D> unitRects;

    private Integer highlightedUnit;

    private List<DataPoint> dataPoints;
    private List<Line2D> dataLines;

    private boolean isDayView = true;

    //TODO: Replace hardcoded values
    private String displayedMonth;
    private String displayedDay;

//    private Date chosenDate;

    public ViewClicksGraph(PersistentArticleStorage wikiArticleStorage, EntityIndex newsEntityIndex){
        setLayout(new FlowLayout());
        this.wikiArticleStorage = wikiArticleStorage;
        this.newsEntityIndex = newsEntityIndex;

        currentNewsArticlesDay = new TreeMap<>(new DateComparator(new SimpleDateFormat("yyyyMMdd").toPattern()));
        currentNewsArticlesHour = new TreeMap<>(new DateComparator(new SimpleDateFormat("yyyyMMddHH").toPattern()));

        currentWikiArticle = WikiClicks.globalSettings.currentArticle;

        graphBackground = new Rectangle2D.Double();
        titleField = new Rectangle2D.Double();
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

        initNewsArticles();
    }

    private void initNewsArticles(){
        Set<NewsArticle> articles = this.newsEntityIndex.get(currentWikiArticle.getTitle());
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat outputFormatDay = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat outputFormatHour = new SimpleDateFormat("yyyyMMddHH");
        numNewsArticles = 0;

        currentNewsArticlesDay.clear();
        currentNewsArticlesHour.clear();

        for(NewsArticle article: articles){
            Date date = null;
            try {
                date = inputFormat.parse(article.getPublished());

                if(date.compareTo(outputFormatHour.parse("2015090100")) < 0){
                    date = null;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(date != null){
                numNewsArticles++;
                currentNewsArticlesDay.putIfAbsent(outputFormatDay.format(date), new HashSet<>());
                currentNewsArticlesDay.get(outputFormatDay.format(date)).add(article);

                currentNewsArticlesHour.putIfAbsent(outputFormatHour.format(date), new HashSet<>());
                currentNewsArticlesHour.get(outputFormatHour.format(date)).add((article));
            }
        }
    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.clearRect(0, 0, getWidth(), getHeight());

        float maxGraph = 0;
        double stepWidth = 0;
        int units = 0;
        Long maxClicks = 0L;

        if (!isDayView) {
            units = currentWikiArticle.getNumDays();
            maxClicks = currentWikiArticle.getMaxOfMonth(displayedMonth);
        }
        else{
            units = 24; //hours
            maxClicks = currentWikiArticle.getMaxOfDay(displayedDay);
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

        /** y-axis labeling*/
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
                g2D.setColor(Color.GRAY);
                g2D.setStroke(new BasicStroke(
                        0.5f,
                        BasicStroke.CAP_SQUARE,
                        BasicStroke.JOIN_MITER,
                        1,
                        new float[]{4.0f},
                        0));

                g2D.drawLine(
                        (int) dataPoint.getX(),
                        (int) dataPoint.getY(),
                        (int) dataPoint.getX(),
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

                String value = String.valueOf(dataPoint.getValue());

                g2D.setColor(new Color(0.95f, 0.95f, 0.95f));
                g2D.setFont(g2D.getFont().deriveFont(Font.BOLD));

                Rectangle2D rect = new Rectangle2D.Double(
                        (int)dataPoint.getX() - g2D.getFontMetrics().stringWidth(value) - 10,
                        (int)dataPoint.getY() - g2D.getFontMetrics().getHeight() - 10,
                        g2D.getFontMetrics().stringWidth(value) + 10,
                        g2D.getFontMetrics().getHeight() + 10) ;


                g2D.fill(rect);
                g2D.setColor(Color.WHITE);
                g2D.draw(rect);

                g2D.setColor(Color.BLACK);
                g2D.drawString(value, (int)dataPoint.getX() - g2D.getFontMetrics().stringWidth(value) - 5, (int) dataPoint.getY() - 5);

                g2D.setColor(Color.RED);
                g2D.setFont(g2D.getFont().deriveFont(Font.PLAIN));
            }
            else{
                g2D.setColor(Color.GRAY);
            }

            g2D.fill(centerEllipse);
            g2D.draw(centerEllipse);
            g2D.setStroke(new BasicStroke(0.5f));
        }
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
        g2D.setFont(font.deriveFont(Font.PLAIN).deriveFont(25.0f));

        String formattedStartDate = "";
        String formattedEndDate = "";

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
            currentDateString = displayedDay + String.format("%02d", unit);
        }

        double currentX = xAxis.getX1() + (unit - 1) * unitLength;
        Rectangle2D currentRect = unitRects.get(unit - 1);
        currentRect.setRect(currentX, xAxis.getY1(), unitLength, graphBackground.getHeight() * 0.03);

        double percentage = 0.0;
        if(numNewsArticles > 0){
            if(!isDayView) {
                percentage = (double)(currentNewsArticlesDay.getOrDefault(currentDateString, new HashSet<>()).size())
                        / (double)(numNewsArticles) * 100.0;
            }
            else {
                int numNewsArticlesThisDay = currentNewsArticlesDay.getOrDefault(currentDateString.substring(0, currentDateString.length() -2), new HashSet<>()).size();
                percentage = (double)(currentNewsArticlesHour.getOrDefault(currentDateString, new HashSet<>()).size())
                        / (double)(numNewsArticlesThisDay) * 100.0;
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

    public void setHighlightedUnit(int mouseX, int mouseY){
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

    @Override
    public void triggerPopup(int x, int y) {}

    @Override
    public String getIdentifier() {
        return "clicks-graph";
    }

    @Override
    public void changeArticle(WikiArticle newArticle) {
        currentWikiArticle = newArticle;

        initNewsArticles();
        repaint();
    }
}
