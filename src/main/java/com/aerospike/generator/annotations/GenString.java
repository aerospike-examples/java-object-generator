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
public @interface GenString {
    public enum StringType {
        /** Generate a number of words */
        CHARACTERS,
        /** Generate a number of words */
        WORDS,
        /** Generate a number of sentences */
        SENTENCES,
        /** Generate a number of paragraphs */
        PARAGRAPHS,
        /** Generate a string matching the format with question marks replaced with letters. Eg "???-???" -> "ABD-EWF" */
        LETTERIFY,
        /** Generate a string which matches the regular expression in the format. Eg "[a-z]{5}\\d{3}" -> "abcde123".
         * <b>WARNING:</b> this type of generations may be substantially slower than other generations */
        REGEXIFY
    }

    int minLength() default -1;
    int maxLength() default -1;
    int length() default -1;
    String format() default "";
    StringType type();
}
