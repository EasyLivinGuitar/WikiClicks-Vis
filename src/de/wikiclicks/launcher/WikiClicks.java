package de.wikiclicks.launcher;

import de.wikiclicks.controller.ClicksGraphMouseController;
import de.wikiclicks.datastructures.*;
import de.wikiclicks.gui.GUI;
import de.wikiclicks.listener.ArticleListener;
import de.wikiclicks.parser.NewsParser;
import de.wikiclicks.parser.WikiParser;
import de.wikiclicks.utils.Serializer;
import de.wikiclicks.views.View;
import de.wikiclicks.views.ViewClicksGraph;
import de.wikiclicks.views.ViewSmallMultiples;
import org.rocksdb.RocksIterator;

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
    private Index<NewsArticle> newsEntityIndex;
    private Index<NamedEntity> entityHotnessIndex;

    public static GlobalSettings globalSettings;

    private WikiClicks() {
        globalSettings = new GlobalSettings();
    }

    private GUI initGUI() {
        System.out.print("Initialize GUI...");
        gui = new GUI();

        for(View view: views){
            gui.addView(view);
        }

        gui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        gui.getArticleSelectGUI().addArticleListener(new ArticleListener() {
            @Override
            public void articleChanged(String title) {
                if(wikiArticleStorage.containsTitle(title.toLowerCase())){
                    globalSettings.currentArticle = wikiArticleStorage.get(title.toLowerCase());

                    for(View view: views){
                        view.changeArticle(globalSettings.currentArticle);
                    }
                }
                else{
                    System.out.println("ERROR: Not found \""+title+"\"");
                }
            }

            @Override
            public void articleSearched(String title) {
                globalSettings.articleChangeSuccess = wikiArticleStorage.containsTitle(title.toLowerCase());
            }
        });

        gui.initComponents();

        System.out.println("Done.");

        return gui;
    }

    private void initViews(){
        System.out.print("Initialize views...");
        views = new ArrayList<>();

        views.add(initClicksGraphView());
        views.add(initSmallMultiplesView());
        System.out.println("Done. "+views.size()+" views initialized.");
    }

    private void initWikiArticles(){
        System.out.print("Initialize wiki articles...");
        wikiArticleStorage = new PersistentArticleStorage("./data/wiki-article-storage");

        if(!wikiArticleStorage.isFilled()){
//            System.out.println();
            File wikiDir = new File("/media/kipu5728/92e4d620-8187-4d97-a7bb-ecbe1408e352/corpora/corpus-wiki-pageview/filtered/2015/2015-09");
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

                System.out.println("Finalize...");
                RocksIterator iterator = wikiArticleStorage.iterator();

                for(iterator.seekToFirst(); iterator.isValid(); iterator.next()){
                    WikiArticle article = (WikiArticle) Serializer.deserialize(iterator.value());

                    article.preCalculateValues();

                    if(!article.isValid()){
                        System.out.println("ERROR: Not Valid");
                    }

                    wikiArticleStorage.replaceArticle(article);
                }

                iterator.close();

                wikiArticleStorage.setFilled();
            }
        }

        globalSettings.currentArticle = wikiArticleStorage.get("white house");

        /*List<WikiArticle> topClicks = wikiArticleStorage.getTop(10);
        for(int i = 0; i < topClicks.size(); i++){
            System.out.println(i+": "+topClicks.get(i).getTitle()+", "+topClicks.get(i).getTotalClicks());
        }*/

        System.out.println("Done. "/*+wikiArticleStorage.size()+ " articles initialized."*/);
    }

    private void initNewsArticles(){
        System.out.println("Inititalize news articles:");
        NewsParser parser = new NewsParser();

        System.out.println("Index news article entities...");
        newsEntityIndex = parser.indexNewsEntities("./data/news-entity-index", wikiArticleStorage);

        System.out.println("Index news article hotness...");
        entityHotnessIndex = parser.indexEntityHotness("./data/news-hotness-index", newsEntityIndex);

        System.out.println("Done. ");
    }

    private View initClicksGraphView(){
        ViewClicksGraph clicksGraph = new ViewClicksGraph(newsEntityIndex);

        ClicksGraphMouseController controller = new ClicksGraphMouseController(clicksGraph);
        clicksGraph.addMouseListener(controller);
        clicksGraph.addMouseMotionListener(controller);

        return clicksGraph;
    }

    private View initSmallMultiplesView(){
        ViewSmallMultiples smallMultiples = new ViewSmallMultiples(entityHotnessIndex, wikiArticleStorage);

        return smallMultiples;
    }

    private void close(){
        if(wikiArticleStorage != null)
            wikiArticleStorage.close();

        if(newsEntityIndex != null)
            newsEntityIndex.close();

        if(entityHotnessIndex != null)
            entityHotnessIndex.close();
    }

    public static void main(String[] args) {
        WikiClicks wikiClicks = new WikiClicks();
        wikiClicks.initWikiArticles();
        wikiClicks.initNewsArticles();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                wikiClicks.initViews();
                GUI gui = wikiClicks.initGUI();

                gui.start();
            }
        });
    }
}
