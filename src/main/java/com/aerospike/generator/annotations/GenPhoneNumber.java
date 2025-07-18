package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenPhoneNumber {
    public static enum PhoneNumType {
        PHONE,
        CELL,
        EXTENSION,
        SUBSCRIBER
    }
    
    PhoneNumType type() default PhoneNumType.PHONE;
}
