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

    public Long getClicksOnHour(String date){
        return clickStats.getOrDefault(date, 0L);
    }

    public Long getClicksOnDay(String dayString){
        Long sum = 0L;
        boolean found = false;

        for(Map.Entry<String, Long> entry: clickStats.entrySet()){
            if(entry.getKey().startsWith(dayString)){
                found = true;
                sum += entry.getValue();
            }
            else if(found){
                break;
            }
        }

        return sum;
    }

    public Long getMaxOfMonth(String monthString){
        Long max = 0L;

        for(Map.Entry<String, Long> entry: clickStats.entrySet()){
            if(entry.getKey().startsWith(monthString)){
                String day = entry.getKey().substring(0, entry.getKey().length() - 4);
                Long clicksOnDay = getClicksOnDay(day);

                if(clicksOnDay > max){
                    max = clicksOnDay;
                }
            }
        }

        return max;
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

    public String getStartDate(){
        return clickStats.firstKey();
    }

    public String getEndDate(){
        return clickStats.lastKey();
    }


}
