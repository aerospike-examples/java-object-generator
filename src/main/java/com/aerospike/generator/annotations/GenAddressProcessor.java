package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;

import com.aerospike.generator.annotations.GenAddress.AddressPart;
import com.github.javafaker.Address;
import com.github.javafaker.Faker;

public class GenAddressProcessor implements Processor {
    private final Address address = Faker.instance().address();
    private final AddressPart part;
    private final FieldType fieldType;
    public GenAddressProcessor(GenAddress addr, FieldType fieldType, Field field) {
        this(addr.value(), fieldType);
    }
    
    public GenAddressProcessor(AddressPart part, FieldType fieldType) {
        this.part = part;
        this.fieldType = fieldType;
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        switch (part) {
        case BUIDING_NUMBER:    return address.buildingNumber();
        case CITY:              return address.city();
        case COUNTRY:           return address.country();
        case COUNTRY_CODE:      return address.countryCode();
        case FULL_ADDRESS:      return address.fullAddress();
        case LATITUDE:          return coordinate(address.latitude());
        case LONGITUDE:         return coordinate(address.longitude());
        case SECONDARY:         return address.secondaryAddress();
        case STATE:             return address.state();
        case STATE_ABBR:        return address.stateAbbr();
        case STREET_ADDRESS:    return address.streetAddress();
        case STREET_ADDRESS_NUMBER: return address.streetAddressNumber();
        case STREET_NAME:       return address.streetName();
        case STREET_PREFIX:     return address.streetPrefix();
        case STREET_SUFFIX:     return address.streetSuffix();
        case TIMEZONE:          return address.timeZone();
        case ZIPCODE:           return address.zipCode();
        default:                return address.fullAddress();
        }
    }
    
    private Object coordinate(String coordinateValue) {
        if (fieldType == FieldType.DOUBLE) {
            return Double.parseDouble(coordinateValue);
        }
        else if (fieldType == FieldType.FLOAT) {
            return Float.parseFloat(coordinateValue);
        }
        return coordinateValue;
    }
    
    public boolean supports(FieldType fieldType) {
        if (part == AddressPart.LATITUDE || part == AddressPart.LONGITUDE) {
            return fieldType == FieldType.STRING || fieldType == FieldType.DOUBLE || fieldType == FieldType.FLOAT;
        }
        return fieldType == FieldType.STRING;
    }
}
