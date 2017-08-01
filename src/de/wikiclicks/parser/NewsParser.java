package de.wikiclicks.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.wikiclicks.datastructures.EntityIndex;
import de.wikiclicks.datastructures.NewsArticle;
import de.wikiclicks.utils.EntityExtractor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

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

    public EntityIndex index(String indexDir){
        EntityIndex entityIndex = new EntityIndex(indexDir);

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
                                article.getTitle() + " "+article.getContent());

                        for(String entity: entities){
                            entityIndex.put(entity, article);
                        }
                    }

                    lineCount++;

                    if(lineCount % 1000 == 0){
                        System.out.print("\r"+lineCount);
                    }
                }

                entityIndex.setFilled();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return entityIndex;
    }
}
