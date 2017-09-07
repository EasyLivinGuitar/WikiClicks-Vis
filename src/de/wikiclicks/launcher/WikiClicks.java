package de.wikiclicks.launcher;

import de.wikiclicks.controller.ClicksGraphMouseController;
import de.wikiclicks.controller.SmallMultiplesMouseController;
import de.wikiclicks.datastructures.*;
import de.wikiclicks.gui.GUI;
import de.wikiclicks.listener.ArticleListener;
import de.wikiclicks.listener.GlobalSettingsListener;
import de.wikiclicks.parser.NewsParser;
import de.wikiclicks.parser.WikiParser;
import de.wikiclicks.utils.Serializer;
import de.wikiclicks.utils.ValueComparatorDESC;
import de.wikiclicks.views.View;
import de.wikiclicks.views.ViewClicksGraph;
import de.wikiclicks.views.ViewSmallMultiples;
import io.multimap.Callables;
import org.rocksdb.RocksIterator;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

public class WikiClicks {
    private GUI gui;
    private List<View> views;

    private PersistentArticleStorage wikiArticleStorage;
    private Index<NewsArticle> newsEntityIndex;
    private Index<NamedEntity> entityHotnessIndex;

    private Map<String, Integer> sortedNamedEntities;

    public static GlobalSettings globalSettings;

    private WikiClicks() {
        globalSettings = new GlobalSettings();

        globalSettings.addListener(new GlobalSettingsListener() {
            @Override
            public void entitySelectionChanged() {
                for(View view: views){
                    if(view instanceof ViewSmallMultiples){
                        ((ViewSmallMultiples) view).selectedEntitiesChanged();
                    }
                }
            }

            @Override
            public void splitToEntitiesChanged() {
                for(View view: views){
                    if(view instanceof ViewSmallMultiples){
                        ((ViewSmallMultiples) view).setSplitIntoEntities(globalSettings.isSplitToEntities());
                    }
                }
            }
        });
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

        gui.setNamedEntityData(sortedNamedEntities);

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

        Set<String> publisherBlacklist = new HashSet<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader("./data/publisher.txt"));

            String line;

            while((line = reader.readLine()) != null){
                publisherBlacklist.add(line);
            }
        } catch (FileNotFoundException e) {
            newsEntityIndex.forEachKey(new Callables.Procedure() {
                @Override
                public void call(ByteBuffer bytes) {
                    String key = Charset.forName("UTF-8").decode(bytes).toString();

                    for(NewsArticle article: newsEntityIndex.get(key)){
                        publisherBlacklist.add(article.getSource().toLowerCase());
                    }
                }
            });

            try {
                PrintWriter writer = new PrintWriter(new FileWriter("./data/publisher.txt"));

                for(String publisher: publisherBlacklist){
                    writer.println(publisher);
                }

                writer.close();
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("Index news article hotness...");
        entityHotnessIndex = parser.indexEntityHotness("./data/news-hotness-index", newsEntityIndex);

        System.out.println("Get location blacklist...");
        Set<String> locationBlacklist = new HashSet<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader("./data/locations.txt"));
            String line;

            while((line = reader.readLine()) != null){
                if(!line.isEmpty()){
                    if(line.contains(",")){
                        String[] split = line.split(",");

                        for(String word: split){
                            locationBlacklist.add(word.toLowerCase());
                        }
                    }
                    else{
                        locationBlacklist.add(line.toLowerCase());
                    }
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Integer> namedEntities = new HashMap<>();

        entityHotnessIndex.forEachKey(new Callables.Procedure() {
            @Override
            public void call(ByteBuffer bytes) {
                String key = Charset.forName("UTF-8").decode(bytes).toString();

                if(key.length() == 8){
                    Set<NamedEntity> entities = entityHotnessIndex.get(key);

                    for(NamedEntity entity: entities){
                        if(!locationBlacklist.contains(entity.getNamedEntity()) && !publisherBlacklist.contains(entity.getNamedEntity()))
                            namedEntities.put(entity.getNamedEntity(),
                                    (int) (namedEntities.getOrDefault(entity.getNamedEntity(), 0) + entity.getHotnessScore()));
                    }
                }
            }
        });

        sortedNamedEntities = new TreeMap<>(new ValueComparatorDESC<>(namedEntities));
        sortedNamedEntities.putAll(namedEntities);

        int i = 0;
        for(Map.Entry<String, Integer> entry: sortedNamedEntities.entrySet()){
            globalSettings.getSelectedNamedEntities().add(entry.getKey());

            if(i == 4){
                break;
            }

            i++;
        }


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
        SmallMultiplesMouseController controller = new SmallMultiplesMouseController(smallMultiples);

        smallMultiples.addMouseListener(controller);
        smallMultiples.addMouseMotionListener(controller);

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
