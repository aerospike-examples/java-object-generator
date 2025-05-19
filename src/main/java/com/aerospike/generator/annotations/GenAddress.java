package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenAddress {
    public enum AddressPart {
        BUIDING_NUMBER,
        CITY,
        COUNTRY,
        COUNTRY_CODE,
        FULL_ADDRESS,
        LATITUDE,
        LONGITUDE,
        SECONDARY,
        STATE,
        STATE_ABBR,
        STREET_ADDRESS,
        STREET_ADDRESS_NUMBER,
        STREET_NAME,
        STREET_PREFIX,
        STREET_SUFFIX,
        TIMEZONE,
        ZIPCODE
    };
    AddressPart value();
}
