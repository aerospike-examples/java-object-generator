package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenSet {
    int percentNull() default 0;
    int minItems() default -1;
    int maxItems() default -1;
    int items() default -1;
    Class<?>[] subclasses() default {};
    
    // String generation options (only applies to Set<String> fields)
    int stringLength() default -1;
    int minStringLength() default 3;
    int maxStringLength() default 10;
    GenString.StringType stringType() default GenString.StringType.WORDS;
    String stringPattern() default "";
    String stringOptions() default "";
}
