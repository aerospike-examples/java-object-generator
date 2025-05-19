package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GenBytesProcessor implements Processor {

    private final int minLength;
    private final int maxLength;
    private final int length;
    
    public GenBytesProcessor(GenBytes genBytes, FieldType fieldType, Field field) {
        this(genBytes.minLength(), genBytes.maxLength(), genBytes.length(), fieldType);
    }
    
    public GenBytesProcessor(int minLength, int maxLength, int length, FieldType fieldType) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.length = length;
        
        if (length < 0 && (minLength < 0 || maxLength < 0)) {
            throw new IllegalArgumentException("Either length must be specified or both minLength and maxLength");
        }
        if (length >= 0) {
            if (minLength >= 0 || maxLength >= 0) {
                throw new IllegalArgumentException("Neither minLength nor maxLength can be specified if length is specified");
            }
        }
        else if (minLength < 0 || maxLength < 0) {
            throw new IllegalArgumentException("Both minLength and maxLength must be specified, or just specify length");
        }
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
    }

    @Override
    public Object process(Map<String, Object> params) {
        int lengthToGenerate;
        if (length >= 0) {
            lengthToGenerate = length;
        }
        else {
            lengthToGenerate = ThreadLocalRandom.current().nextInt(minLength, maxLength +1);
        }
        byte[] bytes = new byte[lengthToGenerate];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    }
    
    @Override
    public boolean supports(FieldType fieldType) {
        switch (fieldType) {
        case BYTES:
            return true;
        default: 
            return false;
        }
    }
}
