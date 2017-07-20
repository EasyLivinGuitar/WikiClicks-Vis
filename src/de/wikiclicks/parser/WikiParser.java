package de.wikiclicks.parser;

import de.wikiclicks.datastructures.PersistentArticleStorage;
import de.wikiclicks.datastructures.WikiArticle;

import java.io.*;
import java.net.URLDecoder;

public class WikiParser {
    public WikiArticle parseLine(String line, String date){
        WikiArticle article = null;
        String[] attrib = line.split(" ");
        String title = null;

        if(Long.parseLong(attrib[2]) < 3){
            return null;
        }

        try {
            title = URLDecoder.decode(attrib[1].replace("_"," "), "UTF-8");
            title = title.toLowerCase();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e){
            return null;
        }

        if(title != null){
            article = new WikiArticle(title);
        }

        if(article != null){
            article.addClickStat(date, Long.parseLong(attrib[2]));
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
                    if(articleStorage.containsTitle(parsedArticle.getTitle())){
                        WikiArticle existingArticle = articleStorage.get(parsedArticle.getTitle());

                        if(existingArticle != null)
                        if(parsedArticle.getClicksOnDate(date) > existingArticle.getClicksOnDate(date))
                            existingArticle.join(parsedArticle);

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
