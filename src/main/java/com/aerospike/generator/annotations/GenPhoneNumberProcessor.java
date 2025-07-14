package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;

import com.aerospike.generator.annotations.GenPhoneNumber.PhoneNumType;
import com.github.javafaker.Faker;
import com.github.javafaker.PhoneNumber;

public class GenPhoneNumberProcessor implements Processor {
    private final PhoneNumber phoneNumber = Faker.instance().phoneNumber();
    private final PhoneNumType type;

    public GenPhoneNumberProcessor(GenPhoneNumber phoneNumber, FieldType fieldType, Field field) {
        this(phoneNumber.type(), fieldType, field);
    }
    public GenPhoneNumberProcessor(PhoneNumType type, FieldType fieldType, Field field) {
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
        this.type = type;
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        switch (type) {
        case CELL:
            return phoneNumber.cellPhone();
        case EXTENSION:
            return phoneNumber.extension();
        case SUBSCRIBER:
            return phoneNumber.subscriberNumber();
        default: 
            return phoneNumber.phoneNumber();
        }
    }

    public boolean supports(FieldType fieldType) {
        return fieldType == FieldType.STRING;
    }
}
