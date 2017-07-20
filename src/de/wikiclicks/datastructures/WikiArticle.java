package de.wikiclicks.datastructures;

import de.wikiclicks.utils.DateComparator;

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

    public void join(WikiArticle article){
        clickStats.putAll(article.clickStats);
    }

    public Long getClicksOnDate(String date){
        return clickStats.getOrDefault(date, 0L);
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

    public String getTitle() {
        return title;
    }
}
