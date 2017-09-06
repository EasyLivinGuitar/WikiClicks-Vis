package de.wikiclicks.views;

import de.wikiclicks.datastructures.*;
import de.wikiclicks.launcher.WikiClicks;
import de.wikiclicks.utils.Serializer;
import io.multimap.Callables;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ViewSmallMultiples extends View{
    private String displayedDay = "20150901";

    private List<EntityGraph> entityGraphList;

    private SingleAttribGraph hotnessGraph;
    private SingleAttribGraph clicksGraph;

    private Index<NamedEntity> entityHotnessIndex;
    private PersistentArticleStorage wikiArticleStorage;

    private int hotnessMax;
    private int clicksMax;

    private Rectangle2D titleField;

    private Image backwardIcon, forwardIcon;
    private Rectangle2D backwardBounds, forwardBounds;

    private boolean splitIntoEntities = true;

    public ViewSmallMultiples(Index<NamedEntity> entityHotnessIndex, PersistentArticleStorage wikiArticleStorage){
        this.wikiArticleStorage = wikiArticleStorage;
        this.entityHotnessIndex = entityHotnessIndex;

        entityGraphList = new ArrayList<>();

        hotnessMax = 0;
        clicksMax = 0;

        hotnessGraph = new SingleAttribGraph();
        clicksGraph = new SingleAttribGraph();

        for(String selectedNamedEntity: WikiClicks.globalSettings.getSelectedNamedEntities()){
            initEntityGraph(selectedNamedEntity);
        }

        titleField = new Rectangle2D.Double();

        try {
            backwardIcon = ImageIO.read(getClass().getResource("/icons/left-arrow.png"));
            forwardIcon = ImageIO.read(getClass().getResource("/icons/right-arrow.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        backwardBounds = new Rectangle2D.Double();
        forwardBounds = new Rectangle2D.Double();


    }

    private void initEntityGraph(String namedEntity){
        EntityGraph entityGraph = new EntityGraph(namedEntity);

        for(int hour = 0; hour < 24; hour++){
            String currentDate = displayedDay + hour + "00";

            Set<NamedEntity> namedEntitiesOnDate = entityHotnessIndex.getIf(currentDate, new Callables.Predicate() {
                @Override
                public boolean call(ByteBuffer bytes) {
                    byte[] value  = new byte[bytes.remaining()];
                    bytes.get(value);

                    NamedEntity desNamedEntity = (NamedEntity) Serializer.deserialize(value);
                    return desNamedEntity.getNamedEntity().equals(namedEntity);
                }
            });

            if(namedEntitiesOnDate.size() == 0){
                entityGraph.addHotnessValue(0);
            }
            else if(namedEntitiesOnDate.size() == 1){
                entityGraph.addHotnessValue(namedEntitiesOnDate.iterator().next().getHotnessScore().intValue());
            }

            WikiArticle wikiArticle = wikiArticleStorage.get(namedEntity);

            if(wikiArticle != null){
                entityGraph.addClickValue(Math.toIntExact(wikiArticle.getClicksOnHour(currentDate)));
            }
        }

        boolean updateMax = false;

        if(entityGraph.getHotnessMax() > hotnessMax){
            hotnessMax = entityGraph.getHotnessMax();
            updateMax = true;
        }

        if(entityGraph.getClicksMax() > clicksMax){
            clicksMax = entityGraph.getClicksMax();
            updateMax = true;
        }

        if(updateMax){
            updateMaxima();
        }

        entityGraph.setHotnessMax(hotnessMax);
        entityGraph.setClicksMax(clicksMax);

        hotnessGraph.addAttribValues(entityGraph.getEntity(), entityGraph.getHotnessValues());
        clicksGraph.addAttribValues(entityGraph.getEntity(), entityGraph.getClickValues());

        entityGraphList.add(entityGraph);
    }

    public void selectedEntitiesChanged(){
        Set<String> selectedNamedEntities = WikiClicks.globalSettings.getSelectedNamedEntities();


        if(selectedNamedEntities.size() > entityGraphList.size()){
            Iterator<String> iterator = selectedNamedEntities.iterator();
            String lastElement = "";
            while(iterator.hasNext()){
                lastElement = iterator.next();
            }

            initEntityGraph(lastElement);
        }
        else{
            for(EntityGraph graph: entityGraphList){
                if(!selectedNamedEntities.contains(graph.getEntity())){
                    clicksGraph.removeAttribValues(graph.getEntity());
                    hotnessGraph.removeAttribValues(graph.getEntity());
                    entityGraphList.remove(graph);
                    break;
                }
            }
        }


        repaint();
    }

    private void updateMaxima(){
        for(EntityGraph graph: entityGraphList){
            graph.setClicksMax(clicksMax);
            graph.setHotnessMax(hotnessMax);
        }
    }

    private void updateGraphs(){
        entityGraphList.clear();

        hotnessMax = 0;
        clicksMax = 0;

        for(String selectedNamedEntity: WikiClicks.globalSettings.getSelectedNamedEntities()){
            initEntityGraph(selectedNamedEntity);
        }
    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2D = (Graphics2D) g;
        g2D.clearRect(0, 0, getWidth(), getHeight());
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int margin = (int) (getWidth() * 0.08);
        int spaceBetweenGraphs = 10;
        int graphHeight = 0;

        drawTitleField(g2D, margin);

        if(splitIntoEntities){
            if(!entityGraphList.isEmpty())
                graphHeight = (getHeight() -  2 * margin - (entityGraphList.size() - 1) * spaceBetweenGraphs) / entityGraphList.size();

            for(EntityGraph graph: entityGraphList){
                graph.setBounds(margin,
                        margin + (graphHeight + spaceBetweenGraphs) * (entityGraphList.indexOf(graph)),
                        getWidth() - 2 * margin,
                        graphHeight);

                graph.paint(g2D);
            }
        }else{
            graphHeight = (int) ((getHeight() - 2 * margin - spaceBetweenGraphs) / 2.0);

            hotnessGraph.setBounds(
                    margin,
                    margin,
                    getWidth() - 2 * margin,
                    graphHeight);

            clicksGraph.setBounds(
                    margin,
                    margin + graphHeight + spaceBetweenGraphs,
                    getWidth() - 2 * margin,
                    graphHeight);

            hotnessGraph.paint(g2D);
            clicksGraph.paint(g2D);
        }

        drawScale(g2D);
    }

    private void drawTitleField(Graphics2D g2D, int margin){
        double width = (getWidth() - 2.0 * margin) * 0.6;
        double height = margin * 0.5;

        double x = getWidth() / 2 - width / 2;
        double y = - height + 2 * margin / 3.0;

        titleField.setRect(x, y, width, height);
        g2D.setColor(Color.WHITE);
        g2D.fill(titleField);
        g2D.setColor(Color.BLACK);

        g2D.draw(titleField);

        String formattedDate = "";
        SimpleDateFormat dateFormatOut = new SimpleDateFormat("dd/MM/yyyy");

        try {
            formattedDate = dateFormatOut.format(WikiClicks.globalSettings.dayFormat.parse(displayedDay));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        g2D.setFont(g2D.getFont().deriveFont(25.0f));

        double stringOffsetX = g2D.getFontMetrics().stringWidth(formattedDate);
        double stringOffsetY = g2D.getFontMetrics().getHeight();

        x = titleField.getCenterX() - stringOffsetX / 2.0;
        y = titleField.getCenterY() + stringOffsetY / 2.0;

        g2D.drawString(formattedDate, (int)x , (int) y);
        g2D.setFont(g2D.getFont().deriveFont(12.0f));

        backwardBounds.setRect(x - 50, y - 23, 28, 28);

        g2D.drawImage(
                backwardIcon,
                (int)backwardBounds.getX(),
                (int)backwardBounds.getY() ,
                (int)backwardBounds.getWidth(),
                (int)backwardBounds.getHeight(),
                Color.WHITE,
                null);

        forwardBounds.setRect(x + stringOffsetX + 50 - 28, (int) y - 23, 28, 28);

        g2D.drawImage(
                forwardIcon,
                (int) forwardBounds.getX(),
                (int) forwardBounds.getY(),
                (int) forwardBounds.getWidth(),
                (int) forwardBounds.getHeight(),
                Color.WHITE,
                null);
    }

    private void drawScale(Graphics2D g2D){
        g2D.setColor(Color.BLACK);

        if(!entityGraphList.isEmpty()){
            double xScale;
            double yScale;

            double widthScale;

            if(splitIntoEntities){
                xScale = entityGraphList.get(0).getGraphArea().getX();
                yScale = entityGraphList.get(entityGraphList.size() - 1).getBounds().getMaxY() + 20.0;
                widthScale = entityGraphList.get(0).getGraphArea().getWidth();
            }else{
                xScale = hotnessGraph.getGraphArea().getX();
                yScale = clicksGraph.getGraphArea().getMaxY() + 20;

                widthScale = hotnessGraph.getGraphArea().getWidth();
            }

            g2D.drawLine((int)xScale, (int)yScale, (int) (xScale + widthScale), (int)yScale);
            double scaleLength = widthScale / 24.0;

            g2D.setFont(g2D.getFont().deriveFont(10.0f));

            for(int i = 0; i < 25; i++){
                int xLine = (int) (xScale + scaleLength * i);

                g2D.drawLine(
                        xLine,
                        (int) yScale + 3,
                        xLine,
                        (int) (yScale - 3)
                );

                if(i < 24){
                    String hour = i + ":00";
                    int stringOffsetX = g2D.getFontMetrics().stringWidth(hour);
                    int xString = (int) (xLine + 0.5 * scaleLength - stringOffsetX * 0.5);

                    g2D.drawString(hour, xString, (int) (yScale + 3+ g2D.getFontMetrics().getHeight()));
                }

            }
        }
    }

    public void changeDay(int mouseX, int mouseY){
        if(forwardBounds.contains(mouseX, mouseY)){
            if(Integer.valueOf(displayedDay.substring(6)) < 30){
                displayedDay = String.valueOf(Long.parseLong(displayedDay) + 1L);
                updateGraphs();
            }

        }
        else if(backwardBounds.contains(mouseX, mouseY)){
            if(Integer.valueOf(displayedDay.substring(6)) > 1) {
                displayedDay = String.valueOf(Long.parseLong(displayedDay) - 1L);
                updateGraphs();
            }
        }


        repaint();
    }

    public void setSplitIntoEntities(boolean splitIntoEntities) {
        this.splitIntoEntities = splitIntoEntities;
        repaint();
    }

    @Override
    public String getIdentifier() {
        return "Entity Hotness";
    }

    @Override
    public void changeArticle(WikiArticle newArticle) {}
}
