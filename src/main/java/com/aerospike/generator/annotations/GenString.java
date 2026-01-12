package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates string values for test data using various generation strategies.
 * 
 * <p>This annotation can be applied at the field level or the class level. If applied
 * at the class level, ALL string fields without explicit annotations will be assigned
 * a value using the class-level configuration.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Generate random words
 * @GenString(type = StringType.WORDS, minLength = 2, maxLength = 5)
 * private String description;
 * 
 * // Generate a formatted string with letters
 * @GenString(type = StringType.LETTERIFY, format = "???-???")
 * private String code;  // Generates "ABC-DEF"
 * 
 * // Generate a string matching a regex pattern
 * @GenString(type = StringType.REGEXIFY, format = "[a-z]{5}\\d{3}")
 * private String identifier;  // Generates "abcde123"
 * 
 * // Class-level: all string fields get random words
 * @GenString(type = StringType.WORDS)
 * public class MyClass {
 *     private String field1;  // Will be populated
 *     private String field2;  // Will be populated
 * }
 * }</pre>
 * 
 * <p><b>Performance Note:</b> {@code REGEXIFY} type generation may be substantially
 * slower than other types due to the complexity of regex matching.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface GenString {
    /**
     * Enumeration of string generation types.
     */
    public enum StringType {
        /** Generate a string of random characters */
        CHARACTERS,
        /** Generate a string of random words (lorem ipsum style) */
        WORDS,
        /** Generate a string of random sentences */
        SENTENCES,
        /** Generate a string of random paragraphs */
        PARAGRAPHS,
        /** Generate a string matching the format with question marks replaced with letters.
         * Example: "???-???" generates "ABC-DEF" */
        LETTERIFY,
        /** Generate a string which matches the regular expression in the format.
         * Example: "[a-z]{5}\\d{3}" generates "abcde123".
         * <b>WARNING:</b> this type of generation may be substantially slower than other types */
        REGEXIFY
    }

    /**
     * The minimum length for the generated string.
     * 
     * <p>This parameter is only used when {@code length} is -1. The meaning depends
     * on the {@code type}:
     * <ul>
     *   <li>{@code CHARACTERS}: minimum number of characters</li>
     *   <li>{@code WORDS}: minimum number of words</li>
     *   <li>{@code SENTENCES}: minimum number of sentences</li>
     *   <li>{@code PARAGRAPHS}: minimum number of paragraphs</li>
     *   <li>{@code LETTERIFY}, {@code REGEXIFY}: ignored (format determines length)</li>
     * </ul>
     * </p>
     * 
     * @return The minimum length, or -1 if not specified
     */
    int minLength() default -1;
    
    /**
     * The maximum length for the generated string.
     * 
     * <p>This parameter is only used when {@code length} is -1. The meaning depends
     * on the {@code type}:
     * <ul>
     *   <li>{@code CHARACTERS}: maximum number of characters</li>
     *   <li>{@code WORDS}: maximum number of words</li>
     *   <li>{@code SENTENCES}: maximum number of sentences</li>
     *   <li>{@code PARAGRAPHS}: maximum number of paragraphs</li>
     *   <li>{@code LETTERIFY}, {@code REGEXIFY}: ignored (format determines length)</li>
     * </ul>
     * </p>
     * 
     * @return The maximum length, or -1 if not specified
     */
    int maxLength() default -1;
    
    /**
     * The exact length for the generated string.
     * 
     * <p>If this is specified (>= 0), it takes precedence over {@code minLength}
     * and {@code maxLength}. Set to -1 to use the range-based approach.</p>
     * 
     * <p>The meaning depends on the {@code type}:
     * <ul>
     *   <li>{@code CHARACTERS}: exact number of characters</li>
     *   <li>{@code WORDS}: exact number of words</li>
     *   <li>{@code SENTENCES}: exact number of sentences</li>
     *   <li>{@code PARAGRAPHS}: exact number of paragraphs</li>
     *   <li>{@code LETTERIFY}, {@code REGEXIFY}: ignored (format determines length)</li>
     * </ul>
     * </p>
     * 
     * @return The exact length, or -1 to use minLength/maxLength
     */
    int length() default -1;
    
    /**
     * The format pattern for LETTERIFY or REGEXIFY types.
     * 
     * <p>For {@code LETTERIFY}: Use question marks (?) as placeholders for letters.
     * Example: "???-???" generates "ABC-DEF"</p>
     * 
     * <p>For {@code REGEXIFY}: Provide a regular expression pattern.
     * Example: "[a-z]{5}\\d{3}" generates "abcde123"</p>
     * 
     * <p>For other types, this parameter is ignored.</p>
     * 
     * @return The format pattern, or empty string if not specified
     */
    String format() default "";
    
    /**
     * The type of string generation to use.
     * 
     * @return The string generation type
     */
    StringType type();
}
