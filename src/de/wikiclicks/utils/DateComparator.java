package de.wikiclicks.utils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;

public class DateComparator implements Comparator<String>, Serializable {
    private String format;

    public DateComparator(String format){
        this.format = format;
    }

    @Override
    public int compare(String o1, String o2) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            return dateFormat.parse(o1).compareTo(dateFormat.parse(o2));
        } catch (ParseException e) {
            System.out.println("ERROR: \"" + o1 + "\" or \"" + o2 + "\" does not match " + dateFormat.toPattern());
        }

        return 0;
    }
}
