package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates integer values within a specified range for test data.
 * 
 * <p>This annotation can be applied to fields of type {@code int} or {@code Integer}
 * to generate random integers within a specified inclusive range. This is a simpler
 * alternative to {@link GenNumber} when you only need basic integer ranges.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Generate integers from 1 to 100
 * @GenRange(start = 1, end = 100)
 * private Integer score;
 * 
 * // Generate integers from 0 to 10
 * @GenRange(start = 0, end = 10)
 * private int rating;
 * }</pre>
 * 
 * <p><b>Note:</b> For more advanced number generation (decimals, rounding, divisors),
 * use {@link GenNumber} instead. This annotation is specifically for simple integer
 * ranges.</p>
 * 
 * @see GenNumber For more advanced numeric generation with decimals, rounding, etc.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenRange {
    /**
     * The starting value (inclusive) for the integer range.
     * 
     * @return The start value
     */
    int start();
    
    /**
     * The ending value (inclusive) for the integer range.
     * 
     * @return The end value
     */
    int end();
}
