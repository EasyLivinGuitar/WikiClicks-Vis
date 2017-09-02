package de.wikiclicks.datastructures;

import de.wikiclicks.utils.Serializer;
import org.rocksdb.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PersistentArticleStorage {
    private String filesDir;
    private static RocksDB articleStore;

    private boolean filled;

    private WriteOptions writeOptions;

    public PersistentArticleStorage(String filesDir){
        this.filesDir = filesDir;
        File file = new File(filesDir);

        if(!exists()){
            boolean created = file.mkdirs();

            if(created){
                System.out.println(file+" created successfully");
            }
            else{
                System.out.println("ERROR: "+file+" couldn't be created");
            }
        }

        RocksDB.loadLibrary();
        Options options = new Options().setCreateIfMissing(true);

        try {
            articleStore = RocksDB.open(options, file.getPath());
        } catch (RocksDBException e) {
            e.printStackTrace();
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

    public void store(WikiArticle article){
        try {
            byte[] serialArticle = Serializer.serialize(article);

            articleStore.put(article.getTitle().getBytes(),
                    serialArticle);

        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    public void replaceArticle(WikiArticle newArticle){
        try {
            articleStore.remove(newArticle.getTitle().getBytes());

            store(newArticle);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    public WikiArticle get(String title){
        try {
            byte[] serialArticle = articleStore.get(title.toLowerCase().getBytes());

            if(serialArticle != null){
                return (WikiArticle) Serializer.deserialize(serialArticle);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<WikiArticle> getTop(int numTop){
        List<WikiArticle> top = new ArrayList<>(numTop);
        top.add(new WikiArticle("dummy"));

        RocksIterator iterator = articleStore.newIterator();

        for(iterator.seekToFirst(); iterator.isValid(); iterator.next()){
            WikiArticle current = (WikiArticle) Serializer.deserialize(iterator.value());
            Long totalClicks = current.getTotalClicks();

            if(totalClicks > top.get(top.size() - 1).getTotalClicks()){
                for(int i = 0; i < top.size(); i++){
                    if(top.get(i).getTotalClicks() <= totalClicks){
                        top.add(i, current);
                        break;
                    }
                }

                top = new ArrayList<>(top.subList(0, Math.min(numTop, top.size())));
            }

        }

        iterator.close();

        return top;
    }

    public boolean containsTitle(String title){
        try {
            return articleStore.get(title.getBytes()) != null;
        } catch (RocksDBException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void flush(){
        if(articleStore != null){
            try {
                articleStore.flush(new FlushOptions().setWaitForFlush(true));
            } catch (RocksDBException e) {
                e.printStackTrace();
            }
        }
    }

    public void close(){
        if(articleStore != null){
            articleStore.close();
        }
    }

    public boolean exists(){
        Path idPath = Paths.get(filesDir, "IDENTITY");

        return idPath.toFile().exists();
    }

    public boolean isFilled(){
        return filled;
    }

    public void setFilled(){
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(filesDir+"/filled"));

            writer.print("true");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long size(){
        RocksIterator iterator = articleStore.newIterator();
        iterator.seekToFirst();
        long count = 0;

        while(iterator.isValid()){
            count++;
            iterator.next();
        }

        iterator.close();

        return count;
    }

    public RocksIterator iterator(){
        return articleStore.newIterator();
    }
}
