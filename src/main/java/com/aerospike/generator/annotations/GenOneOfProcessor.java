package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;

public class GenOneOfProcessor implements Processor {

    private final WeightedList weightedObjects;
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

    private WeightedList parseAsStringList(String list) {
        WeightedList weightedObjects = new WeightedList();
        weightedObjects.parseFromWeightedString(list, true, true, str -> str);
        return weightedObjects;
    }
    
    private WeightedList parseAsIntegerList(String list, boolean allowWeights) {
        WeightedList weightedObjects = new WeightedList();
        return weightedObjects.parseFromWeightedString(list, true, true, 
                (str) -> {
                   if (fieldType == FieldType.INTEGER) {
                       return Integer.parseInt(str);
                   }
                   else {
                       return Long.parseLong(str);
                   }
                });
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
        return weightedObjects.selectRandom();
    }
}
