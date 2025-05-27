package com.aerospike.generator.annotations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedList implements Iterable<WeightedObject>{
    public interface StringPartProcessor {
        Object process(String value);
    }
    private int totalWeight = 0;
    private final List<WeightedObject> weightedObjects = new ArrayList<>();
    
    public boolean isEmpty() {
        return this.weightedObjects.isEmpty();
    }
    
    public void add(Object object, int weight) {
        int theWeight = Math.max(0, weight);
        weightedObjects.add(new WeightedObject(object, theWeight));
        totalWeight += theWeight;
    }

    @Override
    public Iterator<WeightedObject> iterator() {
        return weightedObjects.iterator();
    }
    
    /**
     * Parse a string containing comma separated parts and weights into discrete items and add them to the list.<p/>
     * For example: {@code Mark:4, Mary, Susan:1} will add 4 different parts to the list:
     * <ul>
     * <li>Bob, with weight 3</li>
     * <li>Mark, with weight 4</li>
     * <li>Mary, with weight 1</li>
     * <li>Susan, with weight 1</li>
     * </ul>
     * Dashes can also be used to indicate multiple objects with the same prefix. For example, {@code Mary[1-3]:2, Sue[2-5]} will add
     * <ul>
     *  <li>Mary1, weight 2
     *  <li>Mary2, weight 2
     *  <li>Mary3, weight 2
     *  <li>Sue2, weight 1
     *  <li>Sue3, weight 1
     *  <li>Sue4, weight 1
     *  <li>Sue5, weight 1
     * </ul>
     * If the only thing in the brackets are numbers, the brackets are optional, so {@code 1-100} is the same as {@code [1-100]}
     * Multiple objects are only recognized at the end of the part string, so {@code bob[1-10]smith} is invalid.
     * @param list
     * @param processor
     */
    public WeightedList parseFromWeightedString(String list, boolean allowWeights, boolean allowRanges, StringPartProcessor processor) {
        String[] partsWithWeights = list.split(",");
        for (String thisPartWithWeight : partsWithWeights) {
            String[] partAndWeight = thisPartWithWeight.trim().split(":");
            int weight = 1;
            if (partAndWeight.length == 2) {
                if (!allowWeights) {
                    throw new IllegalArgumentException(String.format("list %s is not allowed to contain weights", list));
                }
                weight = Integer.parseInt(partAndWeight[1].trim());
                if (weight < 0) {
                    throw new IllegalArgumentException(String.format("list %s contains an illegal weight of %d", list, weight));
                }
            }

            String base = partAndWeight[0].trim();
            boolean hasRange = false;
            if (allowRanges) {
                String start = null;
                String end = null;
                String firstPart = "";
                if (base.matches("\\d+-\\d+")) {
                    // Allow integer ranges without []
                    int dashIndex = base.indexOf('-');
                    start = base.substring(0, dashIndex);
                    end = base.substring(dashIndex+1);
                }
                else if (base.matches(".*\\[\\d+-\\d+\\]")) {
                    int startIndex = base.lastIndexOf('[');
                    firstPart = base.substring(0, startIndex);
                    int dashIndex = base.indexOf("-", startIndex);
                    start = base.substring(startIndex+1, dashIndex);
                    end = base.substring(dashIndex+1, base.length()-1);
                }
                if (start != null && end != null) {
                    hasRange = true;
                    int startInt = Integer.parseInt(start);
                    int endInt = Integer.parseInt(end);
                    if (startInt > endInt) {
                        throw new IllegalArgumentException(String.format("list %s contains an illegal range %s-%s ", list,start,end));
                    }
                    for (int i = startInt; i <= endInt; i++) {
                        add(processor.process(firstPart + i), weight);
                    }
                }
            }
            if (!hasRange) {
                add(processor.process(base), weight);
            }
        }
        return this;
    }
    
    public Object selectRandom() {
        if (totalWeight > 0) {
            int weight = ThreadLocalRandom.current().nextInt(this.totalWeight);
            int runningWeight = 0;
            for (WeightedObject wo : this.weightedObjects) {
                runningWeight += wo.getWeight();
                if (runningWeight > weight) {
                    return wo.getValue();
                }
            }
        }
        return null;

    }
}
