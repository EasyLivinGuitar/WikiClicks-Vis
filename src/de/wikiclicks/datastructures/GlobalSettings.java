package de.wikiclicks.datastructures;

import java.text.SimpleDateFormat;

public class GlobalSettings {
    public WikiArticle currentArticle;
    public boolean articleChangeSuccess;

    public SimpleDateFormat hourFormat = new SimpleDateFormat("yyyyMMddHHmm");
    public SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
    public SimpleDateFormat monthFormat = new SimpleDateFormat("yyyyMM");
}
