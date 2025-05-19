package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;

public class GenExcludeProcessor implements Processor {
    public GenExcludeProcessor(GenExclude ignored, FieldType fieldType, Field field) {
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        return null;
    }
    @Override
    public boolean supports(FieldType fieldType) {
        return true;
    }
}
