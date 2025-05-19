package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;

import com.github.javafaker.Faker;
import com.github.javafaker.Internet;

public class GenIpV4Processor implements Processor {
    private final Internet internet = Faker.instance().internet();

    public GenIpV4Processor(GenIpV4 ignored, FieldType fieldType, Field field) {
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        return internet.ipV4Address();
    }
    @Override
    public boolean supports(FieldType fieldType) {
        return fieldType == FieldType.STRING;
    }
}
