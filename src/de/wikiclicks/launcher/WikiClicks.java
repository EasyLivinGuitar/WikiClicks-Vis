package de.wikiclicks.launcher;

import de.wikiclicks.controller.ClicksGraphMouseController;
import de.wikiclicks.datastructures.GlobalSettings;
import de.wikiclicks.datastructures.PersistentArticleStorage;
import de.wikiclicks.gui.GUI;
import de.wikiclicks.listener.ArticleListener;
import de.wikiclicks.parser.WikiParser;
import de.wikiclicks.views.View;
import de.wikiclicks.views.ViewClicksGraph;
import de.wikiclicks.views.ViewPieNews;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WikiClicks {
    private GUI gui;
    private List<View> views;

    private PersistentArticleStorage wikiArticleStorage;

    public GlobalSettings globalSettings;

    private WikiClicks() {
        gui = new GUI();
        views = new ArrayList<>();
        globalSettings = new GlobalSettings();
    }

    private GUI initGUI() {
        System.out.print("Initialize GUI...");
        for(View view: views){
            gui.addView(view);
        }

        gui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(wikiArticleStorage != null)
                    wikiArticleStorage.close();
            }
        });

        gui.getArticleSelectGUI().addArticleListener(new ArticleListener() {
            @Override
            public void articleChanged(String title) {
                globalSettings.articleTitle = title;
            }
        });

        System.out.println("Done.");

        return gui;
    }

    private void initViews(){
        System.out.print("Initialize views...");

        views.add(initClicksGraphView());
        views.add(new ViewPieNews());
        System.out.println("Done. "+views.size()+" views initialized.");
    }

    private void initWikiArticles(String wikiPath){
        System.out.print("Initialize wiki articles...");
        wikiArticleStorage = new PersistentArticleStorage("./data/wiki-article-storage");

        if(!wikiArticleStorage.isFilled()){
            File wikiDir = new File(wikiPath);
            WikiParser parser = new WikiParser();

            if(wikiDir.isDirectory()){
                int i = 0;
                for(File file: wikiDir.listFiles()){
                    long start = System.currentTimeMillis();
                    System.out.print("["+i+"] ");
                    parser.parseFile(file, wikiArticleStorage);
                    i++;

                    System.out.println(" ["+(System.currentTimeMillis() - start)+" ms]");
                    wikiArticleStorage.flush();
                }

                wikiArticleStorage.setFilled();
            }
        }

        System.out.println("Done. "/*+wikiArticleStorage.size()+ " articles initialized."*/);
    }

    public static void main(String[] args) {
        WikiClicks wikiClicks = new WikiClicks();
        wikiClicks.initWikiArticles("/media/storage1/corpora/corpus-wiki-pageview/filtered/2015/2015-09");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                wikiClicks.initViews();
                GUI gui = wikiClicks.initGUI();

                gui.start();
            }
        });
    }

    private View initClicksGraphView(){
        ViewClicksGraph clicksGraph = new ViewClicksGraph(wikiArticleStorage);

        ClicksGraphMouseController controller = new ClicksGraphMouseController(clicksGraph);
        clicksGraph.addMouseListener(controller);
        clicksGraph.addMouseMotionListener(controller);

        return clicksGraph;

    }
}
