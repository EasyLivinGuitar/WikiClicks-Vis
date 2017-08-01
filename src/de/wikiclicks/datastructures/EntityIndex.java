package de.wikiclicks.datastructures;

import de.wikiclicks.utils.Serializer;
import io.multimap.Iterator;
import io.multimap.Map;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class EntityIndex {
    private String filesDir;
    private boolean filled;

    private static Map index;

    public EntityIndex(String filesDir){
        this.filesDir = filesDir;
        File dir = new File(filesDir);

        if(!dir.exists()){
            dir.mkdirs();
        }

        if(Paths.get(filesDir, "multimap.map.id").toFile().exists()){
            try {
                index = new Map(filesDir);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            Map.Options options = new Map.Options();
            options.setCreateIfMissing(true);
            try {
                index = new Map(filesDir, options);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(Paths.get(filesDir, "filled").toFile()));
            filled = Boolean.parseBoolean(reader.readLine());
        } catch (FileNotFoundException e){
            filled = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void put(String keyWord, NewsArticle valueArticle){
        try {
            index.put(keyWord.getBytes(), Serializer.serialize(valueArticle));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<NewsArticle> get(String keyWord){
        Iterator resIt = index.get(keyWord);
        Set<NewsArticle> resArticles = new HashSet<>();

        while(resIt.hasNext()){
            resArticles.add((NewsArticle) Serializer.deserialize(resIt.nextAsByteArray()));
        }

        resIt.close();

        return resArticles;
    }

    public boolean isFilled(){
        return filled;
    }

    public void setFilled(){
        try {
            PrintWriter writer = new PrintWriter(
                    new FileWriter(
                            Paths.get(filesDir,"filled").toFile()
                    )
            );

            writer.print("true");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        if(index != null){
            index.close();
        }
    }
}
