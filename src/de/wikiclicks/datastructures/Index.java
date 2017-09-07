package de.wikiclicks.datastructures;

import de.wikiclicks.utils.Serializer;
import io.multimap.Callables;
import io.multimap.Iterator;
import io.multimap.Map;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

public class Index<ValueType> {
    private String filesDir;
    private boolean filled;

    private Map index;

    public Index(String filesDir){
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

    public void put(String key, ValueType value){
        try {
            index.put(key.getBytes(), Serializer.serialize(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<ValueType> get(String key){
        Iterator resIt = index.get(key);
        Set<ValueType> resArticles = new LinkedHashSet<>();

        while(resIt.hasNext()){
            resArticles.add(
                    (ValueType) Serializer.deserialize(resIt.nextAsByteArray())
            );
        }

        resIt.close();

        return resArticles;
    }

    public Set<ValueType> getIf(String key, Callables.Predicate predicate){
         Iterator resIt = index.get(key);
         Set<ValueType> resSet = new LinkedHashSet<>();

         while(resIt.hasNext()){
             byte[] value = resIt.peekNextAsByteArray();
             ByteBuffer valueBuffer = resIt.next();

             if(predicate.call(valueBuffer)){
                 resSet.add(
                         (ValueType) Serializer.deserialize(value)
                 );
             }
         }

         resIt.close();

         return resSet;
    }

    public void forEachKey(Callables.Procedure procedure){
        index.forEachKey(procedure);
    }

    public boolean contains(String key){
        return index.contains(key);
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

    public void clean(PersistentArticleStorage wikiArticleStorage){
        try {
            Map.optimize(Paths.get(filesDir), Paths.get(filesDir,"optimized"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close(){
        if(index != null){
            index.close();
        }
    }
}
