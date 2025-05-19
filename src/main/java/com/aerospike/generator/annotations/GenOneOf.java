package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Select one value in a given list. The list values are comma-separated, so for example
 * <pre>@GetOneOf("a,b,c,d")
 * </pre>
 * would populate the field with either <code>a</code>, <code>b</code>, <code>c</code>, <code>d</code>. 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenOneOf {
    String value();
}
