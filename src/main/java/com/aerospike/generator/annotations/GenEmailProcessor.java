package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;

import com.github.javafaker.Faker;
import com.github.javafaker.Internet;

public class GenEmailProcessor implements Processor {
    private final Internet internet = Faker.instance().internet();

    public GenEmailProcessor(GenEmail ignored, FieldType fieldType, Field field) {
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        return internet.emailAddress();
    }
    @Override
    public boolean supports(FieldType fieldType) {
        return fieldType == FieldType.STRING;
    }
}
