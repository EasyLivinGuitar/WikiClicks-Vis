package de.wikiclicks.utils;

import java.util.Comparator;
import java.util.Map;

/**
 * Comparator used for a <code>TreeMap</code> to sort by its values.
 */
public class ValueComparatorASC<K extends Comparable<K>, V  extends Comparable<V>>
        implements Comparator<K> {
    private Map<K, V> map;

    /**
     * Class constructor specifying the input map.
     * @param input map to sort
     */
    public ValueComparatorASC(Map<K, V> input) {
        map = input;
    }

    @Override
    public int compare(K o1, K o2) {
        return map.get(o1).compareTo(map.get(o2));
    }
}


