package de.wikiclicks.utils;

import org.nustaq.serialization.FSTConfiguration;

public class Serializer {
    private static FSTConfiguration serialConf = FSTConfiguration.createDefaultConfiguration();

    public static byte[] serialize(Object obj){
        return serialConf.asByteArray(obj);
    }

    public static Object deserialize(byte[] byteArray){
        return serialConf.asObject(byteArray);
    }
}
