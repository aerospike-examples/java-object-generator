package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GenOneOfProcessor implements Processor {

    private class WeightedObject {
        private int weight;
        private Object value;
        public WeightedObject(Object obj, int weight) {
            this.weight = weight;
            this.value = obj;
        }
        
        public Object getValue() {
            return value;
        }
        public int getWeight() {
            return weight;
        }
    }
    
    private final List<WeightedObject> weightedObjects;
    private final int totalWeight;
    private final FieldType fieldType;

    public GenOneOfProcessor(GenOneOf list, FieldType fieldType, Field field) {
        this(list.value(), fieldType);
    }
    
    /**
     * Parse a string into a list of values. The values are comma separated like "a,b,c,d"
     * They can also have weights, with a  colon after the value followed by a number: "a:10,b:5,c,d". In this case there will be a
     * total weight of 17, 10 of which are "a"s, 5 b's, c, d.
     * There can also be ranges: eg a,b,c,d[1-4], corresponding to a,b,c,d1,d2,d3,d4
     * @param list
     * @param fieldType
     */
    public GenOneOfProcessor(String list, FieldType fieldType) {
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }

        this.fieldType = fieldType;
        if (fieldType == FieldType.STRING) {
            this.weightedObjects = parseAsStringList(list);
        }
        else {
            this.weightedObjects = parseAsIntegerList(list, true);
        }
        if (this.weightedObjects.isEmpty()) {
            throw new IllegalArgumentException("list contains no values!");
        }
        this.totalWeight = sumTotalWeights();
    }
    
    public boolean supports(FieldType fieldType) {
        switch (fieldType) {
        case INTEGER:
        case LONG:
        case STRING:
            return true;
        default:
            return false;
        }
    }

    private List<WeightedObject> parseAsStringList(String list) {
        List<WeightedObject> weightedObjects = new ArrayList<>();
        String[] values = list.split(",");
        for (String thisValue : values) {
            
            int weight = 1;
            String[] weightValues = thisValue.trim().split(":");
            if (weightValues.length == 2) {
                weight = Integer.parseInt(weightValues[1].trim());
                if (weight < 0) {
                    throw new IllegalArgumentException(String.format("list %s contains an illegal weight of %d", list, weight));
                }
            }
            final int weightToUse = weight;
            String base = weightValues[0].trim();
            int index = base.indexOf('[');
            if (index > 0) {
                int endIndex = base.indexOf(']',  index);
                if (endIndex < 0) {
                    throw new IllegalArgumentException(String.format("list %s contains an illegal range on term ", list, base));
                }
                List<WeightedObject> indexObjects = parseAsIntegerList(base.substring(index+1, endIndex), false);
                final String prefix = base.substring(0, index);
                List<WeightedObject> objs = indexObjects.stream().map(indexObj -> new WeightedObject(prefix + indexObj.value, weightToUse)).collect(Collectors.toList());
                weightedObjects.addAll(objs);
            }
            else {
                weightedObjects.add(new WeightedObject(base, weightToUse));
            }
        }
        return weightedObjects;
    }
    
    private List<WeightedObject> parseAsIntegerList(String list, boolean allowWeights) {
        List<WeightedObject> weightedObjects = new ArrayList<>();
        String[] values = list.split(",");
        for (String thisValue : values) {
            
            int weight = 1;
            String[] weightValues = thisValue.trim().split(":");
            if (weightValues.length == 2) {
                weight = Integer.parseInt(weightValues[1].trim());
                if (weight < 0) {
                    throw new IllegalArgumentException(String.format("list %s contains an illegal weight of %d", list, weight));
                }
                if (!allowWeights) {
                    throw new IllegalArgumentException(String.format("list %s is not allowed to contain weights", list));

                }
            }
            String base = weightValues[0].trim();
            int dashIndex = base.indexOf('-');
            if (dashIndex > 0) {
                long startValue = Long.parseLong(base.substring(0, dashIndex).trim());
                long endValue = Long.parseLong(base.substring(dashIndex+1).trim());
                for (long i = startValue; i <= endValue; i++) {
                    if (fieldType == FieldType.INTEGER) {
                        weightedObjects.add(new WeightedObject((int)i, weight));
                    }
                    else {
                        weightedObjects.add(new WeightedObject(i, weight));
                    }
                }
            }
            else {
                long value = Long.parseLong(base);
                if (fieldType == FieldType.INTEGER) {
                    weightedObjects.add(new WeightedObject((int)value, weight));
                }
                else {
                    weightedObjects.add(new WeightedObject(value, weight));
                }
            }
        }
        return weightedObjects;
    }

    private int sumTotalWeights() {
        int total = 0;
        for (WeightedObject thisObject : this.weightedObjects) {
            total += thisObject.getWeight();
        }
        return total;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (WeightedObject wo : this.weightedObjects) {
            if (!isFirst) {
                sb.append(',');
            }
            isFirst = false;
            sb.append(wo.getValue());
            if (wo.getWeight() > 1) {
                sb.append(':').append(wo.getWeight());
            }
        }
        return sb.toString();
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        int weight = ThreadLocalRandom.current().nextInt(this.totalWeight);
        int runningWeight = 0;
        for (WeightedObject wo : this.weightedObjects) {
            runningWeight += wo.getWeight();
            if (runningWeight > weight) {
                return wo.getValue();
            }
        }
        // This should never happen
        return this.weightedObjects.get(0).getValue();
    }
    
    public static void main(String[] args) {
        GenOneOfProcessor oop = new GenOneOfProcessor("a,b:10,c,d[1-5]", FieldType.STRING);
        System.out.println(oop);
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            String val = (String)oop.process(null);
            Integer count = map.get(val);
            if (count == null) {
                map.put(val, 1);
            }
            else {
                map.put(val, count+1);
            }
        }
        for (String key : map.keySet()) {
            System.out.printf("%s -> %,d\n", key, map.get(key));
        }
        
        oop = new GenOneOfProcessor("upi,netbanking,credit card, wallet, sodexo, duitnow pay", FieldType.STRING);
        for (int i = 0; i < 10; i ++) System.out.println(oop.process(null));
    }
}
