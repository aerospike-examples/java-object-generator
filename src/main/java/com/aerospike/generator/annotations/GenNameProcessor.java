package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;

import com.aerospike.generator.annotations.GenName.NameType;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;

public class GenNameProcessor implements Processor {
    private final Name name = Faker.instance().name();
    private final NameType type;

    public GenNameProcessor(GenName genName, FieldType fieldType, Field field) {
        this(genName.value(), fieldType);
    }
    
    public GenNameProcessor(NameType nameType, FieldType fieldType) {
        this.type = nameType;
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        switch (type) {
        case FIRST: return name.firstName();
        case FULL: return name.fullName();
        case FULL_WITH_MIDDLE: return name.nameWithMiddle();
        case LAST: return name.lastName();
        case PREFIX: return name.prefix();
        case SUFFIX: return name.suffix();
        case TITLE: return name.title();
        case USERNAME: return name.username();
        default: return name.fullName();
        }
    }
    @Override
    public boolean supports(FieldType fieldType) {
        return fieldType == FieldType.STRING;
    }
}
