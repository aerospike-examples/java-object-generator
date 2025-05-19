package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenBrowser {
    public enum BrowserType {
        NAME,
        USERAGENT
    };
    BrowserType value() default BrowserType.NAME;
}
