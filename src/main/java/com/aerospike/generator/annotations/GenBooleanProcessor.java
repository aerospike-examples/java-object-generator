package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GenBooleanProcessor implements Processor {
    private final WeightedList list;

    public GenBooleanProcessor(GenBoolean value, FieldType fieldType, Field field) {
        this(value.value(), fieldType);
    }
    public GenBooleanProcessor(String value, FieldType fieldType) {
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
        if (value == null || value.isEmpty()) {
            list = null;
        }
        else {
            list = new WeightedList().parseFromWeightedString(value, true, false, str -> Boolean.valueOf(str));
        }
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        if (list == null) {
            return ThreadLocalRandom.current().nextBoolean();
        }
        else {
            return list.selectRandom();
        }
    }
    @Override
    public boolean supports(FieldType fieldType) {
        return fieldType == FieldType.BOOLEAN;
    }
}
