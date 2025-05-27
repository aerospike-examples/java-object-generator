package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GenNumberProcessor implements Processor {

    private final long start;
    private final long end;
    private final FieldType fieldType;
    
    public GenNumberProcessor(GenNumber genNumber, FieldType fieldType, Field field) {
        this(genNumber.start(), genNumber.end(), fieldType);
    }
    
    public GenNumberProcessor(long start, long end, FieldType fieldType) {
        this.start = start;
        this.end = end;
        this.fieldType = fieldType;
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
        if (start > end) {
            throw new IllegalArgumentException("Start must be <= end");
        }
    }

    @Override
    public Object process(Map<String, Object> params) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        switch (fieldType) {
        case LONG: {
            long end = this.end == Long.MAX_VALUE ? this.end : this.end + 1;
            return random.nextLong(start, end);
        }
        case INTEGER: {
            int end = this.end >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)(this.end + 1);
            return random.nextInt((int)start, end);
        }
        case DOUBLE:
            return random.nextDouble((double)start, (double)end);
        case FLOAT:
            return random.nextFloat() * (end - start) + start;
        default: return null;
        }
    }
    
    @Override
    public boolean supports(FieldType fieldType) {
        switch (fieldType) {
        case LONG:
        case INTEGER:
        case DOUBLE:
        case FLOAT:
            return true;
        default: 
            return false;
        }
    }
}
