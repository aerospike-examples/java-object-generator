package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates random enum values for test data.
 * 
 * <p>This annotation can be applied to fields of enum types to generate random
 * values from the enum's available constants. Each enum constant has an equal
 * probability of being selected.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * public enum Status { ACTIVE, INACTIVE, PENDING }
 * 
 * @GenEnum
 * private Status status;
 * 
 * public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
 * 
 * @GenEnum
 * private Priority priority;
 * }</pre>
 * 
 * <p><b>Note:</b> The enum type is automatically determined from the field type.
 * All enum constants are equally likely to be selected. For weighted selection,
 * consider using {@link GenOneOf} with a string field and converting, or implement
 * custom logic in your enum.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenEnum {
}
