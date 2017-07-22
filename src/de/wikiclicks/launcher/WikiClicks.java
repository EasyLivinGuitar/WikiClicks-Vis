package de.wikiclicks.launcher;

import de.wikiclicks.controller.ClicksGraphMouseController;
import de.wikiclicks.datastructures.PersistentArticleStorage;
import de.wikiclicks.gui.GUI;
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

    private WikiClicks(){
        gui = new GUI();
        views = new ArrayList<>();
    }

    private GUI initGUI() {
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

        return gui;
    }

    private void initViews(){
        views.add(initClicksGraphView());
        views.add(new ViewPieNews());
    }

    private void initWikiArticles(String wikiPath){
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
    }

    public static void main(String[] args) {
        WikiClicks wikiClicks = new WikiClicks();
        wikiClicks.initWikiArticles("/media/storage1/corpora/corpus-wiki-pageview/filtered/2015/2015-09");
//        System.out.println(wikiClicks.wikiArticleStorage.get("main page"));

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
