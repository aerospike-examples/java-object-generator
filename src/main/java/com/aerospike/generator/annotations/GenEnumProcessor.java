package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GenEnumProcessor implements Processor {

    private Object[] enumConstants;
    public GenEnumProcessor(GenEnum ignored, FieldType fieldType, Field field) {
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
        if (field.getType().isEnum()) {
            Class<?> enumClass = field.getType();
            enumConstants = enumClass.getEnumConstants();
        }
        else {
            throw new IllegalArgumentException("Field " + field + " is not an enum type");
        }
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        return enumConstants[ThreadLocalRandom.current().nextInt(enumConstants.length)];
    }
    public boolean supports(FieldType fieldType) {
        return fieldType == FieldType.ENUM;
    }
}
