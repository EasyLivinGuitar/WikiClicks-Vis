package de.wikiclicks.datastructures;

import de.wikiclicks.utils.Serializer;
import org.rocksdb.FlushOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PersistentArticleStorage {
    private String filesDir;
    private static RocksDB articleStore;

    private boolean filled;

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
            byte[] serialArticle = articleStore.get(article.getTitle().getBytes());

            if(serialArticle == null){
                articleStore.put(article.getTitle().getBytes(),
                        Serializer.serialize(article));
            }
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
            byte[] serialArticle = articleStore.get(title.getBytes());

            if(serialArticle != null){
                return (WikiArticle) Serializer.deserialize(serialArticle);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean containsTitle(String title){
        try {
            return articleStore.get(title.getBytes()) != null;
        } catch (RocksDBException e) {
            e.printStackTrace();
        }

        return true;
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
}
