package de.wikiclicks.utils;

import de.wikiclicks.datastructures.WikiArticle;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.simpleapi.DefaultCoder;

import java.util.TreeMap;

public class Serializer {
    private static FSTConfiguration serialConf = FSTConfiguration.createFastBinaryConfiguration();
    private static DefaultCoder coder = new DefaultCoder(true, WikiArticle.class, TreeMap.class);

    public static byte[] serialize(Object obj){
        return coder.toByteArray(obj);
    }

    public static Object deserialize(byte[] byteArray){
        return coder.toObject(byteArray);
    }
}
