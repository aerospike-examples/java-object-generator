package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates boolean values for test data with configurable true/false ratios.
 * 
 * <p>This annotation can be applied to fields of type {@code boolean} or {@code Boolean}
 * to generate random boolean values. By default, it generates balanced true/false values,
 * but you can specify custom ratios.</p>
 * 
 * <h3>Ratio Format</h3>
 * <p>The {@code value} parameter uses a weighted ratio format:</p>
 * <ul>
 *   <li>{@code ""} or {@code "true:1,false:1"} - Balanced (50% true, 50% false)</li>
 *   <li>{@code "true:8,false:1"} - 8:1 ratio (89% true, 11% false)</li>
 *   <li>{@code "true:1,false:9"} - 1:9 ratio (10% true, 90% false)</li>
 * </ul>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Balanced boolean (default)
 * @GenBoolean
 * private boolean isActive;
 * 
 * // Mostly true (for fields like "isRegistered", "isVerified")
 * @GenBoolean("true:8,false:1")
 * private boolean isVerified;
 * 
 * // Mostly false (for rare conditions)
 * @GenBoolean("true:1,false:19")
 * private boolean isVeteran;
 * 
 * // Custom ratio
 * @GenBoolean("true:3,false:1")
 * private Boolean hasPermission;
 * }</pre>
 * 
 * <p><b>Note:</b> When used with {@link GenMagic}, the processor automatically
 * determines appropriate ratios based on field name patterns (e.g., "isActive"
 * gets a high true ratio, "isError" gets a high false ratio).</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenBoolean {
    /**
     * The true/false ratio in the format "true:X,false:Y".
     * 
     * <p>Examples:
     * <ul>
     *   <li>{@code ""} or {@code "true:1,false:1"} - Balanced ratio</li>
     *   <li>{@code "true:8,false:1"} - 8:1 ratio favoring true</li>
     *   <li>{@code "true:1,false:9"} - 1:9 ratio favoring false</li>
     * </ul>
     * </p>
     * 
     * @return The ratio string, or empty string for balanced (50/50)
     */
    String value() default "";
}
