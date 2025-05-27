package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * This annotation can be applied at the field level or the class level. If applied at the class 
 * level, ALL strings without a value will be assigned a value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface GenNumber {
    long start() default Long.MIN_VALUE;
    long end() default Long.MAX_VALUE;
}
