package de.wikiclicks.datastructures;

import de.wikiclicks.utils.DateComparator;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class WikiArticle implements Serializable{
    private String title;

    private TreeMap<String, Long> clickStatsPerHour;
    // TODO: click stats per day pre-calculation
    // TODO: click stats per month pre-calculation

    public WikiArticle(String title){
        this.title = title;

        clickStatsPerHour = new TreeMap<>(new DateComparator());
    }

    public void addClickStat(String date, Long clicks){
        if(clickStatsPerHour.containsKey(date)){
            System.out.println("ERROR: \""+date+"\" is already contained in article \""+title+"\"");
        }
        else{
            clickStatsPerHour.put(date, clicks);
        }
    }

    public void join(WikiArticle article){
        clickStatsPerHour.putAll(article.clickStatsPerHour);
    }

    public Long getClicksOnHour(String date){
        return clickStatsPerHour.getOrDefault(date, 0L);
    }

    public Long getClicksOnDay(String dayString){
        Long sum = 0L;
        boolean found = false;

        for(Map.Entry<String, Long> entry: clickStatsPerHour.entrySet()){
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

        for(Map.Entry<String, Long> entry: clickStatsPerHour.entrySet()){
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

    public Long getMaxOfDay(String dayString){
        Long max =0l;
        boolean found = false;

        for(Map.Entry<String, Long> entry: clickStatsPerHour.entrySet()){
            if(entry.getKey().startsWith(dayString)){
                found = true;

                if(entry.getValue() > max){
                    max = entry.getValue();
                }
            }
            else if(found){
                break;
            }
        }

        return max;
    }

    public Long getTotalClicks(){
        Long sum = 0L;

        for(Map.Entry<String, Long> entry: clickStatsPerHour.entrySet()){
            sum += entry.getValue();
        }

        return sum;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();

        builder.append(title);

        for(Map.Entry<String, Long> entry: clickStatsPerHour.entrySet()){
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
        return clickStatsPerHour.firstKey();
    }

    public String getEndDate(){
        return clickStatsPerHour.lastKey();
    }


}
