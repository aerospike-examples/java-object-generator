package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenName {
    public enum NameType {FIRST, LAST, FULL, FULL_WITH_MIDDLE, PREFIX, SUFFIX, TITLE, USERNAME};
    NameType value() default NameType.FULL;
}
