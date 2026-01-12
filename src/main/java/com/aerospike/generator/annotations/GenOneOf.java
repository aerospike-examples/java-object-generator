package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Selects one value from a given list of options.
 * 
 * <p>This annotation can be applied to fields to generate values by randomly selecting
 * from a predefined set of options. The options are comma-separated.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Simple string options
 * @GenOneOf("red,blue,green,yellow")
 * private String color;
 * 
 * // Options with weights (more likely to select weighted options)
 * @GenOneOf("active:8,inactive:1,pending:1")
 * private String status;
 * 
 * // Numeric ranges
 * @GenOneOf("10-20")
 * private Integer score;  // Generates numbers between 10 and 20
 * 
 * // Options with numeric ranges
 * @GenOneOf("low[1-3],medium[4-7],high[8-10]")
 * private String priority;  // Generates "low1", "low2", "low3", "medium4", etc.
 * }</pre>
 * 
 * <h3>Format Options</h3>
 * <ul>
 *   <li><b>Simple list:</b> {@code "option1,option2,option3"}</li>
 *   <li><b>Weighted options:</b> {@code "option1:8,option2:1"} (8:1 ratio)</li>
 *   <li><b>Numeric range:</b> {@code "10-20"} (for numeric fields)</li>
 *   <li><b>Ranges with text:</b> {@code "prefix[1-100]"} generates "prefix1" to "prefix100"</li>
 * </ul>
 * 
 * <p><b>Note:</b> When used with {@link GenMagic}, fields with names matching
 * common patterns (e.g., "status", "category", "level") automatically use
 * appropriate option lists even without the annotation.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenOneOf {
    /**
     * Comma-separated list of options to choose from.
     * 
     * <p>Can include:
     * <ul>
     *   <li>Simple options: {@code "a,b,c"}</li>
     *   <li>Weighted options: {@code "a:8,b:1"}</li>
     *   <li>Numeric ranges: {@code "10-20"}</li>
     *   <li>Ranges with text: {@code "prefix[1-100]"}</li>
     * </ul>
     * </p>
     * 
     * @return The options string
     */
    String value();
}
