package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GenNumberProcessor implements Processor {

    private final long start;
    private final long end;
    private final long roundToClosest;
    private final FieldType fieldType;
    
    public GenNumberProcessor(GenNumber genNumber, FieldType fieldType, Field field) {
        this(genNumber.start(), genNumber.end(), genNumber.roundToClosest(), fieldType);
    }
    
    public GenNumberProcessor(long start, long end, FieldType fieldType) {
        this(start, end, 1, fieldType);
    }
    
    public GenNumberProcessor(long start, long end, long roundToClosest, FieldType fieldType) {
        this.start = start;
        this.end = end;
        this.roundToClosest = roundToClosest;
        this.fieldType = fieldType;
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
        if (start > end) {
            throw new IllegalArgumentException("Start must be <= end");
        }
        if (roundToClosest <= 0) {
            throw new IllegalArgumentException("roundToClosest must be > 0");
        }
    }

    @Override
    public Object process(Map<String, Object> params) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long rawValue;
        
        // Generate a random value in the range
        if (end == Long.MAX_VALUE) {
            rawValue = random.nextLong(start, end);
        } else {
            rawValue = random.nextLong(start, end + 1);
        }
        
        // Apply rounding if roundToClosest > 1
        if (roundToClosest > 1) {
            rawValue = roundToNearestMultiple(rawValue, roundToClosest);
        }
        
        switch (fieldType) {
        case LONG:
            return rawValue;
        case INTEGER:
            return (int) rawValue;
        case DOUBLE:
            return (double) rawValue;
        case FLOAT:
            return (float) rawValue;
        default: 
            return null;
        }
    }
    
    /**
     * Rounds a value to the nearest multiple of roundToClosest, ensuring the result
     * stays within the start and end bounds.
     * 
     * @param value The value to round
     * @param multiple The multiple to round to
     * @return The rounded value, clamped to [start, end] range
     */
    private long roundToNearestMultiple(long value, long multiple) {
        // Calculate the nearest multiple
        long rounded = Math.round((double) value / multiple) * multiple;
        
        // Clamp to the valid range
        if (rounded < start) {
            return start;
        } else if (rounded > end) {
            return end;
        } else {
            return rounded;
        }
    }
    
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
