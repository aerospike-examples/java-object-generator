package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GenRangeProcessor implements Processor {

    private final long startVal;
    private final long endVal;
    private final FieldType fieldType;
    
    public GenRangeProcessor(GenRange range, FieldType fieldType, Field field) {
        this(range.start(), range.end(), fieldType);
    }
    
    public GenRangeProcessor(int startVal, int endVal, FieldType fieldType) {
        this.startVal = startVal;
        this.endVal = endVal;
        this.fieldType = fieldType;
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
    }

    @Override
    public Object process(Map<String, Object> params) {
        long result = ThreadLocalRandom.current().nextLong(startVal, endVal+1) ;
        if (fieldType == FieldType.INTEGER) {
            return (int)result;
        }
        else {
            return result;
        }
    }
    
    @Override
    public boolean supports(FieldType fieldType) {
        switch (fieldType) {
        case INTEGER:
        case LONG:
            return true;
        default: 
            return false;
        }
    }
}
