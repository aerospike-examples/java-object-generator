package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates numeric values (Integer, Long, Double, Float) for test data.
 * 
 * <p>This annotation can be applied to numeric fields to generate random numbers
 * within a specified range. The generated value is automatically converted to the
 * appropriate numeric type (int, long, double, float) based on the field type.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Generate integers between 1 and 100
 * @GenNumber(start = 1, end = 100)
 * private Integer age;
 * 
 * // Generate prices rounded to nearest dollar
 * @GenNumber(start = 10, end = 1000, roundToClosest = 1)
 * private Double price;
 * 
 * // Generate decimal values (using divisor)
 * @GenNumber(start = 0, end = 100, divisor = 100)
 * private Double percentage;  // Generates 0.0 to 1.0
 * 
 * // Generate salaries rounded to nearest $500
 * @GenNumber(start = 30000, end = 200000, roundToClosest = 500)
 * private Long salary;
 * }</pre>
 * 
 * <p><b>Note:</b> The generated number is first created as a long in the range
 * [start, end], then rounded to the nearest multiple of {@code roundToClosest},
 * and finally divided by {@code divisor} if divisor != 1.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface GenNumber {
    /**
     * The starting value (inclusive) for the random number range.
     * 
     * @return The start value, default is Long.MIN_VALUE
     */
    long start() default Long.MIN_VALUE;
    
    /**
     * The ending value (inclusive) for the random number range.
     * 
     * @return The end value, default is Long.MAX_VALUE
     */
    long end() default Long.MAX_VALUE;
    
    /**
     * Round the generated number to the nearest multiple of this value.
     * 
     * <p>For example, if {@code roundToClosest = 100}, generated numbers will be
     * rounded to the nearest 100 (e.g., 1234 becomes 1200, 1256 becomes 1300).</p>
     * 
     * <p>This is useful for generating realistic values like prices (round to $1),
     * salaries (round to $500), etc.</p>
     * 
     * @return The rounding increment, default is 1 (no rounding)
     */
    long roundToClosest() default 1;
    
    /**
     * Number to use as a divisor to convert the generated number to a decimal.
     * 
     * <p>If this is not 1, the generated number will be converted to a double
     * and divided by this number. This is useful for generating decimal values
     * like percentages, probabilities, or precise measurements.</p>
     * 
     * <p>Example: {@code start = 0, end = 100, divisor = 100} generates values
     * from 0.0 to 1.0 (e.g., 0.0, 0.25, 0.75, 1.0).</p>
     * 
     * @return The divisor, default is 1 (no division)
     */
    long divisor() default 1;
}
