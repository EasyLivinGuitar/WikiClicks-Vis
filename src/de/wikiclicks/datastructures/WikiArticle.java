package de.wikiclicks.datastructures;

import de.wikiclicks.launcher.WikiClicks;
import de.wikiclicks.utils.DateComparator;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;

public class WikiArticle implements Serializable{
    private String title;

    private TreeMap<String, Long> clickStatsPerHour;
    private TreeMap<String, Long> clickStatsPerDay;
    private TreeMap<String, Long> clickStatsPerMonth;

    /*static final long serialVersionUID = -6572202523954191783L;*/

    public WikiArticle(String title){
        this.title = title;

        clickStatsPerHour = new TreeMap<>(new DateComparator(WikiClicks.globalSettings.hourFormat.toPattern()));
        clickStatsPerDay = new TreeMap<>(new DateComparator(WikiClicks.globalSettings.dayFormat.toPattern()));
        clickStatsPerMonth = new TreeMap<>(new DateComparator(WikiClicks.globalSettings.monthFormat.toPattern()));
    }

    public void addClickStat(String date, Long clicks){
        clickStatsPerHour.put(date, clicks);
    }

    public void replace(WikiArticle article){
        clickStatsPerHour.putAll(article.clickStatsPerHour);
    }

    public void merge(WikiArticle article){
        clickStatsPerHour.putAll(article.clickStatsPerHour);
    }

    public void preCalculateValues(){
        SimpleDateFormat hourFormat = WikiClicks.globalSettings.hourFormat;
        SimpleDateFormat dayFormat = WikiClicks.globalSettings.dayFormat;
        SimpleDateFormat monthFormat = WikiClicks.globalSettings.monthFormat;

        for(Map.Entry<String, Long> entry: clickStatsPerHour.entrySet()){
            try {
                String day = dayFormat.format(hourFormat.parse(entry.getKey()));
                clickStatsPerDay.put(day,
                        entry.getValue() + clickStatsPerDay.getOrDefault(day, 0L));

                String month = monthFormat.format(hourFormat.parse(entry.getKey()));
                clickStatsPerMonth.put(month,
                        entry.getValue() + clickStatsPerMonth.getOrDefault(month, 0L));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

    public int getNumDays(){ return clickStatsPerDay.size(); }

    public Long getClicksOnHour(String date){
        return clickStatsPerHour.getOrDefault(date, 0L);
    }

    public Long getClicksOnDay(String dayString){
        return clickStatsPerDay.getOrDefault(dayString, null);
    }

    public Map.Entry<String, Long> getMaxOfDay(String dayString){
        Long max =0L;
        Map.Entry<String, Long> maxPair = null;
        boolean found = false;

        for(Map.Entry<String, Long> entry: clickStatsPerHour.entrySet()){
            if(entry.getKey().startsWith(dayString)){
                found = true;

                if(entry.getValue() > max){
                    max = entry.getValue();
                    maxPair = entry;
                }
            }
            else if(found){
                break;
            }
        }

        return maxPair;
    }

    public Map.Entry<String, Long> getMaxOfMonth(String monthString) {
        Long max = 0L;
        Map.Entry<String, Long> maxPair = null;

        for(Map.Entry<String, Long> entry: clickStatsPerDay.entrySet()){
            if(entry.getKey().startsWith(monthString)){
                Long clicksOnDay = getClicksOnDay(entry.getKey());

                if(clicksOnDay > max){
                    max = clicksOnDay;
                    maxPair = entry;

                }
            }
        }

        return maxPair;
    }

    public Map.Entry<String, Long> getMinOfMonth(String monthString) {
        Long min = Long.MAX_VALUE;
        Map.Entry<String, Long> minPair = null;

        for(Map.Entry<String, Long> entry: clickStatsPerDay.entrySet()){
            if(entry.getKey().startsWith(monthString)){
                Long clicksOnDay = getClicksOnDay(entry.getKey());

                if(clicksOnDay < min){
                    min = clicksOnDay;
                    minPair = entry;

                }
            }
        }

        return minPair;
    }

    public Long getTotalClicks(){
        Long sum = 0L;

        for(Map.Entry<String, Long> entry: clickStatsPerMonth.entrySet()){
            sum += entry.getValue();
        }

        return sum;
    }

    public Map.Entry<String, Long> getMinOfDay(String dayString){
        Long min =Long.MAX_VALUE;
        Map.Entry<String, Long> minPair = null;
        boolean found = false;

        for(Map.Entry<String, Long> entry: clickStatsPerHour.entrySet()){
            if(entry.getKey().startsWith(dayString)){
                found = true;

                if(entry.getValue() < min){
                    min = entry.getValue();
                    minPair = entry;
                }
            }
            else if(found){
                break;
            }
        }

        return minPair;
    }

    public Long getTotalClicksDay(String date) {
        Long sum = 0L;

        for (Map.Entry<String, Long> entry: clickStatsPerHour.entrySet()){
            if (entry.getKey().startsWith(date)) {
                sum += entry.getValue();
            }

        }

        return sum;
    }

    public boolean isValid(){
        SimpleDateFormat hourFormat = WikiClicks.globalSettings.hourFormat;
        SimpleDateFormat dayFormat = WikiClicks.globalSettings.dayFormat;

        TreeMap<String, Long> refClicksDay = new TreeMap<>(new DateComparator(dayFormat.toPattern()));

        for(Map.Entry<String, Long> entry: clickStatsPerHour.entrySet()){
            try {
                String day = dayFormat.format(hourFormat.parse(entry.getKey()));
                refClicksDay.put(day,
                        entry.getValue() + refClicksDay.getOrDefault(day, 0L));

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return refClicksDay.equals(clickStatsPerDay);
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();

        builder/*.append("\n\n")*/.append(title).append(": ").append(getTotalClicks());

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
