package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GenHexStringProcessor implements Processor {

    private final int minLength;
    private final int maxLength;
    private final int length;
    private final String separator;
    private final static String digits = "0123456789abcdef";
    
    public GenHexStringProcessor(GenHexString genHexString, FieldType fieldType, Field field) {
        this(genHexString.minLength(), genHexString.maxLength(), genHexString.length(), genHexString.separator(), fieldType);
    }
    
    public GenHexStringProcessor(int minLength, int maxLength, int length, String separator, FieldType fieldType) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.length = length;
        this.separator = separator;
        
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
        StringBuilder sb = new StringBuilder(lengthToGenerate * (2+separator.length()) );
        for (int i = 0; i < lengthToGenerate; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            byte b = bytes[i];
            sb.append(digits.charAt((b >>> 4) & 0x0f)).append(digits.charAt(b & 0x0f));
        }
        return sb.toString();
    }
    
    @Override
    public boolean supports(FieldType fieldType) {
        switch (fieldType) {
        case STRING:
            return true;
        default: 
            return false;
        }
    }
}
