package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;

public class GenExcludeProcessor implements Processor {
    private final FieldType fieldType;
    public GenExcludeProcessor(GenExclude ignored, FieldType fieldType, Field field) {
        this.fieldType = fieldType;
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        switch (fieldType) {
        case BOOLEAN:
            return false;
        case DOUBLE:
            return 0.0;
        case FLOAT:
            return 0.0f;
        case INTEGER:
            return 0;
        case LONG:
            return 0l;
        default:
            return null;
        }
    }
    public boolean supports(FieldType fieldType) {
        return true;
    }
}
