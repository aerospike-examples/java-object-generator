package com.aerospike.generator.annotations;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public interface Processor {
    Object process(Map<String, Object> params);
    boolean supports(FieldType fieldType);
    
    static int getLengthToGenerate(int length, int minLength, int maxLength) {
        if (length >= 0) {
            return length;
        }
        else {
            return ThreadLocalRandom.current().nextInt(minLength, maxLength +1);
        }
    }

}
