package de.wikiclicks.datastructures;

import de.wikiclicks.utils.DateComparator;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class WikiArticle implements Serializable{
    private String title;

    private TreeMap<String, Long> clickStatsPerHour;
    private TreeMap<String, Long> clickStatsPerDay;
    private TreeMap<String, Long> clickStatsPerMonth;

    static final long serialVersionUID = -6572202523954191783L;

    public WikiArticle(String title){
        this.title = title;

        clickStatsPerHour = new TreeMap<>(new DateComparator("yyyyMMddHHmm"));
        clickStatsPerDay = new TreeMap<>(new DateComparator("yyyyMMdd"));
        clickStatsPerMonth = new TreeMap<>(new DateComparator("yyyyMM"));
    }

    public void addClickStat(String date, Long clicks){
        if(clickStatsPerHour.containsKey(date)){
            System.out.println("ERROR: \""+date+"\" is already contained in article \""+title+"\"");
        }
        else{
            clickStatsPerHour.put(date, clicks);

            String day = date.substring(0, date.length() - 4);
            clickStatsPerDay.put(day, clickStatsPerDay.getOrDefault(day, 0L) + clicks);

            String month = date.substring(0, date.length() - 6);
            clickStatsPerMonth.put(month, clickStatsPerMonth.getOrDefault(month, 0L) + clicks);
        }
    }

    public void replace(WikiArticle article){
        clickStatsPerHour.putAll(article.clickStatsPerHour);
        clickStatsPerDay.putAll(article.clickStatsPerDay);
        clickStatsPerMonth.putAll(article.clickStatsPerMonth);
    }

    public void merge(WikiArticle article){
        clickStatsPerHour.putAll(article.clickStatsPerHour);

        article.clickStatsPerDay.forEach((key, value) -> clickStatsPerDay.merge(key, value, (val1, val2) -> val1 + val2));
        article.clickStatsPerMonth.forEach((key, value) -> clickStatsPerMonth.merge(key, value, (val1, val2) -> val1 + val2));
    }

    public Long getClicksOnHour(String date){
        return clickStatsPerHour.getOrDefault(date, null);
    }

    public Long getClicksOnDay(String dayString){
        return clickStatsPerDay.getOrDefault(dayString, null);
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

    public Long getMaxOfMonth(String monthString) {
        Long max = 0L;

        for(Map.Entry<String, Long> entry: clickStatsPerDay.entrySet()){
            if(entry.getKey().startsWith(monthString)){
                Long clicksOnDay = getClicksOnDay(entry.getKey());

                if(clicksOnDay > max){
                    max = clicksOnDay;
                }
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

        builder.append("\n\n").append(title);

        for(Map.Entry<String, Long> entry: clickStatsPerHour.entrySet()){
            builder.append("\n")
                    .append(entry.getKey())
                    .append(" : ")
                    .append(entry.getValue());
        }

        for(Map.Entry<String, Long> entry: clickStatsPerDay.entrySet()){
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
