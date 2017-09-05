package de.wikiclicks.utils;

import java.util.Comparator;
import java.util.Map;

public class ValueComparatorDESC<K extends Comparable<K>, V  extends Comparable<V>>
        implements Comparator<K> {
    private Map<K, V> map;

    /**
     * Class constructor specifying the input map.
     * @param input map to sort
     */
    public ValueComparatorDESC(Map<K, V> input) {
        map = input;
    }

    @Override
    public int compare(K o1, K o2) {
        return map.get(o2).compareTo(map.get(o1));
    }
}