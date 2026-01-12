package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates Set collections for test data.
 * 
 * <p>This annotation can be applied to fields of type {@code Set<T>} to generate
 * sets with random unique elements. The element type is automatically determined from
 * the generic type parameter.</p>
 * 
 * <p><b>Note:</b> Sets automatically ensure uniqueness of elements. For object types,
 * uniqueness is based on object equality.</p>
 * 
 * <h3>Element Generation</h3>
 * <p>For primitive and common types (String, Integer, Date, etc.), elements are
 * generated using appropriate processors. For object types, elements are generated
 * using {@link GenObject} logic, potentially selecting from {@code subclasses}.</p>
 * 
 * <p>For {@code Set<String>} fields, you can configure string generation using
 * the string-specific parameters.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Generate a set of 3-10 unique strings
 * @GenSet(minItems = 3, maxItems = 10)
 * private Set<String> tags;
 * 
 * // Generate exactly 5 unique integers
 * @GenSet(items = 5)
 * private Set<Integer> uniqueIds;
 * 
 * // Generate a set of custom objects
 * @GenSet(minItems = 1, maxItems = 5, subclasses = {Address.class})
 * private Set<Location> locations;
 * }</pre>
 * 
 * <p><b>Note:</b> When used with {@link GenMagic}, the processor automatically
 * determines appropriate element generators based on field name patterns.</p>
 * 
 * @see GenList For generating lists (allows duplicates)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenSet {
    /**
     * Percentage chance (0-100) that the generated set will be null.
     * 
     * @return The null percentage (0-100), default is 0 (never null)
     */
    int percentNull() default 0;
    
    /**
     * The minimum number of items in the generated set.
     * 
     * <p>This parameter is only used when {@code items} is -1.</p>
     * 
     * @return The minimum items, or -1 if not specified
     */
    int minItems() default -1;
    
    /**
     * The maximum number of items in the generated set.
     * 
     * <p>This parameter is only used when {@code items} is -1.</p>
     * 
     * @return The maximum items, or -1 if not specified
     */
    int maxItems() default -1;
    
    /**
     * The exact number of items in the generated set.
     * 
     * <p>If this is specified (>= 0), it takes precedence over {@code minItems}
     * and {@code maxItems}. Set to -1 to use the range-based approach.</p>
     * 
     * @return The exact number of items, or -1 to use minItems/maxItems
     */
    int items() default -1;
    
    /**
     * Subclasses to choose from when generating object elements.
     * 
     * <p>When the set element type is an object (not a primitive or common type),
     * this specifies which subclasses can be instantiated. If empty, only the
     * declared element type is used.</p>
     * 
     * @return Array of subclasses to choose from
     */
    Class<?>[] subclasses() default {};
    
    /**
     * String generation options (only applies to Set&lt;String&gt; fields).
     * The exact length for generated strings.
     * 
     * @return The exact string length, or -1 to use minStringLength/maxStringLength
     */
    int stringLength() default -1;
    
    /**
     * The minimum length for generated strings (only for Set&lt;String&gt; fields).
     * 
     * @return The minimum string length, default is 3
     */
    int minStringLength() default 3;
    
    /**
     * The maximum length for generated strings (only for Set&lt;String&gt; fields).
     * 
     * @return The maximum string length, default is 10
     */
    int maxStringLength() default 10;
    
    /**
     * The type of string generation to use (only for Set&lt;String&gt; fields).
     * 
     * @return The string generation type, default is WORDS
     */
    GenString.StringType stringType() default GenString.StringType.WORDS;
    
    /**
     * The format pattern for string generation (only for Set&lt;String&gt; fields).
     * Used with LETTERIFY or REGEXIFY types.
     * 
     * @return The format pattern, or empty string if not specified
     */
    String stringPattern() default "";
    
    /**
     * Comma-separated options for string generation (only for Set&lt;String&gt; fields).
     * Used to generate strings from a predefined set of options.
     * 
     * @return The options string, or empty string if not specified
     */
    String stringOptions() default "";
}
