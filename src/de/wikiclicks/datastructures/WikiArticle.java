package de.wikiclicks.datastructures;

import de.wikiclicks.utils.DateComparator;
import de.wikiclicks.utils.Serializer;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class WikiArticle implements Serializable{
    private String title;

    private TreeMap<String, Long> clickStats;

    public WikiArticle(String title){
        this.title = title;

        clickStats = new TreeMap<>(new DateComparator());
    }

    public void addClickStat(String date, Long clicks){
        if(clickStats.containsKey(date)){
            System.out.println("ERROR: \""+date+"\" is already contained in article \""+title+"\"");
        }
        else{
            clickStats.put(date, clicks);
        }
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();

        builder.append(title);

        for(Map.Entry<String, Long> entry: clickStats.entrySet()){
            builder.append("\n")
                    .append(entry.getKey())
                    .append(" : ")
                    .append(entry.getValue());
        }

        return builder.toString();
    }

    public static void main(String[] args) {
        WikiArticle article = new WikiArticle("test");
        article.addClickStat("201509010600", 234567L);
        article.addClickStat("201512050200", 4567L);
        article.addClickStat("201509010100", 123456L);

        byte[] serialArticle = Serializer.serialize(article);
        WikiArticle deserialArticle = (WikiArticle) Serializer.deserialize(serialArticle);

        System.out.println(deserialArticle);
    }
}
