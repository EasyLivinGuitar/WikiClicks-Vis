package de.wikiclicks.views;

import de.wikiclicks.datastructures.DataPoint;
import de.wikiclicks.datastructures.PersistentArticleStorage;
import de.wikiclicks.datastructures.WikiArticle;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.math.util.MathUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class ViewClicksGraph extends View {
    private PersistentArticleStorage wikiArticleStorage;

    private Rectangle2D graphBackground;
    private Rectangle2D titleField;
    private Line2D xAxis, yAxis;
    private List<Rectangle2D> dayRects;

    private List<DataPoint> dataPoints;
    private List<Line2D> dataLines;

    private WikiArticle currentArticle;

    public ViewClicksGraph(PersistentArticleStorage wikiArticleStorage){
        this.wikiArticleStorage = wikiArticleStorage;

        graphBackground = new Rectangle2D.Double();
        titleField = new Rectangle2D.Double();
        xAxis = new Line2D.Double();
        yAxis = new Line2D.Double();

        dayRects = new ArrayList<>(31);
        dataPoints = new ArrayList<>(31);
        dataLines = new ArrayList<>(30);
        for(int i = 0; i < 31; i++){
            dayRects.add(new Rectangle2D.Double());
            dataPoints.add(new DataPoint());
            dataLines.add(new Line2D.Double());
        }

        currentArticle = wikiArticleStorage.get("main page");
    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.clearRect(0, 0, getWidth(), getHeight());

        //TODO: Replace hardcoded values
        int days = 30;
        String month = "201509";
        Long maxClicks = currentArticle.getMaxOfMonth(month);
        int roundPrecision = String.valueOf(maxClicks).length() - 2;
        float maxGraph = MathUtils.round(maxClicks, -roundPrecision, BigDecimal.ROUND_UP);
        double stepWidth = maxGraph / 10.0;

        drawBackground(g2D);

        drawTitleField(g2D);

        drawAxis(g2D);

        //calculate length of day rectangle
        double dayLength = (xAxis.getX2() - xAxis.getX1()) / (double)(days);
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
        for(int i = 1; i <= days; i++){
            Rectangle2D currentRect = drawDayRects(g2D, i, dayLength);

            DataPoint dataPoint = dataPoints.get(i - 1);

            dataPoint.setValue(currentArticle.getClicksOnDay(month + String.format("%02d", i)));
            double dataY = dataPoint.getValue() * scaling + yAxis.getY1();


            //Draw click function (points)
            dataPoint.setCoord(currentRect.getCenterX(), dataY);

            Ellipse2D centerEllipse = dataPoint.getDrawGeom();

            if(dataPoint.isHighlighted()){
                g2D.setColor(Color.BLACK);
                g2D.drawString(String.valueOf(dataPoint.getValue()), (int)dataPoint.getX(), (int) dataPoint.getY());

                g2D.setColor(Color.RED);
            }
            else{
                g2D.setColor(Color.GRAY);
            }

            g2D.fill(centerEllipse);
            g2D.draw(centerEllipse);

            //Draw click function (lines)
            if(i > 1){
                DataPoint lastDataPoint = dataPoints.get(i - 2);
                Line2D dataLine = dataLines.get(i - 1);

                dataLine.setLine(lastDataPoint.getX(), lastDataPoint.getY(), dataPoint.getX(), dataPoint.getY());
                g2D.setColor(Color.BLUE);
                g2D.draw(dataLine);
            }
        }
    }

    public DataPoint getRelevantDataPoint(int mouseX){
        double dayLength = dayRects.get(0).getWidth();
        int index = (int) ((mouseX - yAxis.getX1()) / dayLength);

        if(index >= 0 && index < dataPoints.size()){
            return dataPoints.get(index);
        }


        return null;
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

        String title = currentArticle.getTitle();
        title = WordUtils.capitalize(title);
        Font font = g2D.getFont();
        font = font.deriveFont(Font.BOLD).deriveFont(30.0f);
        g2D.setFont(font);

        int stringOffset = g2D.getFontMetrics().stringWidth(title);

        x = titleField.getCenterX() - stringOffset / 2;
        y = titleField.getY() + 40;

        g2D.drawString(title, (int)(x), (int) y);
        g2D.setFont(font.deriveFont(Font.PLAIN).deriveFont(25.0f));

        String startDate = currentArticle.getStartDate();
        String endDate = currentArticle.getEndDate();

        SimpleDateFormat dateFormatIn = new SimpleDateFormat("yyyyMMddHHmm");
        SimpleDateFormat dateFormatOut = new SimpleDateFormat("dd/MM/yyyy");

        String formattedStartDate = "";
        String formattedEndDate = "";
        try {
            formattedStartDate = dateFormatOut.format(dateFormatIn.parse(startDate));
            formattedEndDate = dateFormatOut.format(dateFormatIn.parse(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(formattedStartDate).append(" - ").append(formattedEndDate);
        String dateString = builder.toString();
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

    private Rectangle2D drawDayRects(Graphics2D g2D, int day, double dayLength){
        double currentX = xAxis.getX1() + (day - 1) * dayLength;
        Rectangle2D currentRect = dayRects.get(day - 1);
        currentRect.setRect(currentX, xAxis.getY1(), dayLength, graphBackground.getHeight() * 0.03);
        g2D.setColor(Color.BLACK);
        g2D.draw(currentRect);

        float stringWidth = g2D.getFontMetrics().stringWidth(String.valueOf(day));
        float stringHeight = g2D.getFont().getSize();

        g2D.drawString(
                String.valueOf(day),
                (float)(currentX + dayLength / 2.0 - stringWidth / 2.0),
                (float) (xAxis.getY1() + graphBackground.getHeight() * 0.015 + stringHeight / 2.0)
        );

        return currentRect;
    }

    @Override
    public void triggerPopup(int x, int y) {}

    @Override
    public String getIdentifier() {
        return "clicks-graph";
    }

    @Override
    public void resize() {
        System.out.println("resize");
    }
}
