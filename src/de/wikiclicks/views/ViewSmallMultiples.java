package de.wikiclicks.views;

import de.wikiclicks.datastructures.*;
import de.wikiclicks.utils.Serializer;
import io.multimap.Callables;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ViewSmallMultiples extends View{
    private String displayedDay = "20150901";

    private List<EntityGraph> graphList;

    private Index<NamedEntity> entityHotnessIndex;
    private PersistentArticleStorage wikiArticleStorage;

    public ViewSmallMultiples(Index<NamedEntity> entityHotnessIndex, PersistentArticleStorage wikiArticleStorage){
        this.wikiArticleStorage = wikiArticleStorage;
        this.entityHotnessIndex = entityHotnessIndex;

        final int maxGraphs = 5;

        graphList = new ArrayList<>();

        Set<NamedEntity> entities = entityHotnessIndex.get(displayedDay);
        Iterator<NamedEntity> iterator = entities.iterator();

        int hotnessMax = 0;
        int clicksMax = 0;

        for(int i = 0; i < maxGraphs && iterator.hasNext(); i++){
            NamedEntity topEntity = iterator.next();
            WikiArticle article = wikiArticleStorage.get(topEntity.getNamedEntity());

            EntityGraph graph = new EntityGraph(topEntity, displayedDay);

            for(int hour = 0; hour < 24; hour++){
                String currentDate = displayedDay + hour + "00";

                Set<NamedEntity> namedEntitiesOnHour = entityHotnessIndex.getIf(currentDate,
                        new Callables.Predicate() {
                            @Override
                            public boolean call(ByteBuffer bytes) {
                                byte[] value  = new byte[bytes.remaining()];
                                bytes.get(value);

                                NamedEntity namedEntity = (NamedEntity) Serializer.deserialize(value);
                                return namedEntity.equals(topEntity);
                            }
                        });

                if(namedEntitiesOnHour.size() == 1){
                    graph.addHotnessValue(namedEntitiesOnHour.iterator().next().getHotnessScore().intValue());
                }
                else if(namedEntitiesOnHour.size() == 0){
                    graph.addHotnessValue(0);
                }
                else{
                    System.out.println("ERROR");
                }

                if(article != null){
                    graph.addClickValue(article.getClicksOnHour(currentDate).intValue());
                }
            }

            if(hotnessMax < graph.getHotnessMax()){
                hotnessMax = graph.getHotnessMax();
            }

            if(clicksMax < graph.getClicksMax()){
                clicksMax = graph.getClicksMax();
            }

            graphList.add(graph);
        }

        for(EntityGraph graph: graphList){
            graph.setHotnessMax(hotnessMax);
            graph.setClicksMax(clicksMax);
        }

    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2D = (Graphics2D) g;
        g2D.clearRect(0, 0, getWidth(), getHeight());
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int marginX = (int) (getWidth() * 0.05);
        int graphHeight = (int) (getHeight() * 0.17);

        for(EntityGraph graph: graphList){
            graph.setBounds(marginX, marginX + (graphHeight + 10) * (graphList.indexOf(graph)), getWidth()* 0.9, graphHeight);
            graph.paint(g2D);
        }
    }

    @Override
    public String getIdentifier() {
        return "Entity Hotness";
    }

    @Override
    public void changeArticle(WikiArticle newArticle) {

    }
}
