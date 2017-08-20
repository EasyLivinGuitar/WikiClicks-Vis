package de.wikiclicks.parser;

import de.wikiclicks.datastructures.PersistentArticleStorage;
import de.wikiclicks.datastructures.WikiArticle;

import java.io.*;
import java.net.URLDecoder;

public class WikiParser {
    public WikiArticle parseLine(String line, String date){
        WikiArticle article = null;
        String[] attrib = line.split(" ");

        Long clicks = Long.parseLong(attrib[2]);

        if(clicks < 3){
            return null;
        }

        String title = null;

        try {
            title = URLDecoder.decode(attrib[1].replace("_"," "), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e){
            return null;
        }

        if(title != null){
            title = title.toLowerCase();
            article = new WikiArticle(title);
        }

        if(article != null){
            article.addClickStat(date, clicks);
        }

        return article;
    }

    public void parseFile(File file, PersistentArticleStorage articleStorage){
        System.out.print("Parse file "+file.getPath()+" ...");
        String fileName = file.getName();

        String[] attrib = fileName.split("-");
        String date = attrib[1] + attrib[2].substring(0, 4);
        String line;
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(file));

            while((line = reader.readLine()) != null){
                WikiArticle parsedArticle = parseLine(line, date);

                if(parsedArticle != null){
                    WikiArticle existingArticle = articleStorage.get(parsedArticle.getTitle());

                    if(existingArticle != null){
                        Long existingClicksOnHour = existingArticle.getClicksOnHour(date);

                        if(existingClicksOnHour == null)
                            existingArticle.merge(parsedArticle);
                        else if(parsedArticle.getClicksOnHour(date) > existingClicksOnHour)
                            existingArticle.replace(parsedArticle);

                        articleStorage.replaceArticle(existingArticle);
                    }
                    else{
                        articleStorage.store(parsedArticle);
                    }
                }

            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
