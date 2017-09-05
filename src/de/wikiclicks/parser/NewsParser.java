package de.wikiclicks.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.wikiclicks.datastructures.Index;
import de.wikiclicks.datastructures.NamedEntity;
import de.wikiclicks.datastructures.NewsArticle;
import de.wikiclicks.datastructures.PersistentArticleStorage;
import de.wikiclicks.launcher.WikiClicks;
import de.wikiclicks.utils.EntityExtractor;
import de.wikiclicks.utils.ValueComparatorASC;
import io.multimap.Callables;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NewsParser {
    private String corpusPath =
            "/media/kipu5728/92e4d620-8187-4d97-a7bb-ecbe1408e352/corpora/corpus-signalmedia-1m/root/signalmedia-1m.jsonl";

    private NewsArticle parseLine(String line){
        ObjectMapper mapper = new ObjectMapper();
        NewsArticle article = null;

        try {
            article = mapper.readValue(line, NewsArticle.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(article != null){
            if(article.getMediaType().equals("News")){
                return article;
            }
        }

        return null;
    }

    public Index<NewsArticle> indexNewsEntities(String indexDir, PersistentArticleStorage wikiArticleStorage){
        Index<NewsArticle> entityIndex = new Index<>(indexDir);

        if(!entityIndex.isFilled()){
            EntityExtractor entityExtractor = new EntityExtractor();
            try {
                BufferedReader reader = new BufferedReader(
                        new FileReader(corpusPath)
                );

                String line = "";

                int lineCount = 0;

                while((line = reader.readLine()) != null){
                    NewsArticle article = parseLine(line);

                    if(article != null){
                        Set<String> entities = entityExtractor.getEntities(
                                article.getTitle() + " " + article.getContent());

                        for(String entity: entities){
                            if(wikiArticleStorage.containsTitle(entity))
                                entityIndex.put(entity, article);
                        }
                    }

                    lineCount++;

                    if(lineCount % 1000 == 0){
                        System.out.print("\rProcessed articles: "+lineCount);
                    }
                }

                entityIndex.setFilled();
                System.out.println();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return entityIndex;
    }

    public Index<NamedEntity> indexEntityHotness(String indexDir, Index<NewsArticle> entityIndex){
        Index<NamedEntity> entityHotnessIndex = new Index<>(indexDir);
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        Map<String, Map<String, Double>> hotnessIndex = new HashMap<>();

        if(!entityHotnessIndex.isFilled()){
            entityIndex.forEachKey(new Callables.Procedure() {
                private int i = 0;

                @Override
                public void call(ByteBuffer bytes) {

                    String entity = Charset.forName("UTF-8").decode(bytes).toString();

                    for(NewsArticle article: entityIndex.get(entity)){
                        Date date = null;

                        try {
                            date = inFormat.parse(article.getPublished());
                            date.setMinutes(0);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        String dateString = WikiClicks.globalSettings.hourFormat.format(date);

                        hotnessIndex.putIfAbsent(dateString, new HashMap<>());

                        hotnessIndex.get(dateString).put(
                                entity, hotnessIndex.get(dateString).getOrDefault(entity, 0.0) + 1.0);
                    }
                    i++;

                    if(i % 10000 == 0){
                        System.out.print("\rProcessed entities: "+i);
                    }
                }
            });

            entityIndex.forEachKey(new Callables.Procedure() {
                @Override
                public void call(ByteBuffer bytes) {

                }
            });


            for(Map.Entry<String, Map<String, Double>> topLevelEntry: hotnessIndex.entrySet()){
                TreeMap<String, Double> sortedValueMap = new TreeMap<>(new ValueComparatorASC<>(topLevelEntry.getValue()));
                sortedValueMap.putAll(topLevelEntry.getValue());
                topLevelEntry.setValue(sortedValueMap);


                for(Map.Entry<String, Double> botLevelEntry: topLevelEntry.getValue().entrySet()){
                    entityHotnessIndex.put(
                            topLevelEntry.getKey(),
                            new NamedEntity(
                                    botLevelEntry.getKey(),
                                    botLevelEntry.getValue()));
                }
            }

            Map<String, List<NamedEntity>> entityScoresOnDay = new HashMap<>();

            entityHotnessIndex.forEachKey(new Callables.Procedure() {
                @Override
                public void call(ByteBuffer bytes) {
                    String key = Charset.forName("UTF-8").decode(bytes).toString();
                    String currentDay = key.substring(0, key.length() - 4);

                    Set<NamedEntity> values = entityHotnessIndex.get(key);

                    for(NamedEntity entity: values){
                        entityScoresOnDay.putIfAbsent(currentDay, new ArrayList<>());
                        List<NamedEntity> currentEntities = entityScoresOnDay.get(currentDay);

                        if(currentEntities.contains(entity)) {
                            NamedEntity currentEntity = currentEntities.get(currentEntities.indexOf(entity));
                            currentEntities.remove(currentEntity);
                            currentEntities.add(entity.merge(currentEntity));
                        }
                        else{
                            currentEntities.add(entity);
                        }
                    }
                }
            });

            for(Map.Entry<String, List<NamedEntity>> entry: entityScoresOnDay.entrySet()){
                entry.getValue().sort(new Comparator<NamedEntity>() {
                    @Override
                    public int compare(NamedEntity o1, NamedEntity o2) {
                        return o2.getHotnessScore().compareTo(o1.getHotnessScore());
                    }
                });

                for(NamedEntity entity: entry.getValue()){
                    entityHotnessIndex.put(entry.getKey(), entity);
                }
            }

            System.out.println(entityScoresOnDay.get("20150901"));

            entityHotnessIndex.setFilled();
            System.out.println();
        }


        return entityHotnessIndex;
    }
}
