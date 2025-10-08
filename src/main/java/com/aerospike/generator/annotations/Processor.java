package com.aerospike.generator.annotations;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public interface Processor {
    Object process(Map<String, Object> params);
    
    /**
     * Returns true if this processor should be deferred (processed after other fields).
     * This is used to optimize the populate method by avoiding instanceof checks.
     * 
     * @return true if this processor should be deferred, false otherwise
     */
    default boolean isDeferred() {
        return false;
    }
    
    static int getLengthToGenerate(int length, int minLength, int maxLength) {
        if (length >= 0) {
            return length;
        }
        else {
            return ThreadLocalRandom.current().nextInt(minLength, maxLength +1);
        }
    }

}
