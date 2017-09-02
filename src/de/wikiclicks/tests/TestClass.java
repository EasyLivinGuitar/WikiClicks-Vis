package de.wikiclicks.tests;

import de.wikiclicks.datastructures.Index;
import de.wikiclicks.utils.ValueComparator;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.*;

public class TestClass extends TestCase{
    @Test
    public void testIndex(){
        Index<Integer> testIndex = new Index<>("./data/test-index");
        testIndex.put("k1", 1);
        testIndex.put("k1", 2);
        testIndex.put("k1", 3);
        testIndex.close();

        testIndex = new Index<>("./data/test-index");
        Set<Integer> testValues = testIndex.get("k1");

        Set<Integer> expectedValues = new LinkedHashSet<>();
        expectedValues.add(1);
        expectedValues.add(2);
        expectedValues.add(3);

        assertEquals(testValues, expectedValues);
        testIndex.close();
    }

    @Test
    public void testValueComparator(){
        Map<String, Integer> testMap = new LinkedHashMap<>();

        testMap.put("k1", 1);
        testMap.put("k3", 3);
        testMap.put("k2", 2);

        Map<String, Integer> sortedMap = new TreeMap<>(new ValueComparator<>(testMap));
        sortedMap.putAll(testMap);

        Collection<Integer> expectedValues = new ArrayList<>(Arrays.asList(1, 2, 3));

        assertEquals(new ArrayList<>(sortedMap.values()), expectedValues);
    }

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(TestClass.class);

        System.out.println("Failures: "+result.getFailureCount());

        for(Failure failure: result.getFailures()){
            System.out.println(failure.toString());
        }
    }
}
