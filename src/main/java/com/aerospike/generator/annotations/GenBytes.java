package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates random byte arrays for test data.
 * 
 * <p>This annotation can be applied to fields of type {@code byte[]} to generate
 * random byte arrays of a specified length or within a specified range.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Generate exactly 16 bytes
 * @GenBytes(length = 16)
 * private byte[] encryptionKey;
 * 
 * // Generate between 8 and 32 bytes
 * @GenBytes(minLength = 8, maxLength = 32)
 * private byte[] randomData;
 * }</pre>
 * 
 * <p><b>Note:</b> Either specify {@code length} OR both {@code minLength} and
 * {@code maxLength}. If {@code length} is specified (>= 0), it takes precedence.</p>
 * 
 * @see GenHexString For generating hexadecimal string representations of bytes
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenBytes {
    /**
     * The minimum length of the byte array to generate.
     * 
     * <p>This parameter is only used when {@code length} is -1. Must be specified
     * together with {@code maxLength}.</p>
     * 
     * @return The minimum length, or -1 if not specified
     */
    int minLength() default -1;
    
    /**
     * The maximum length of the byte array to generate.
     * 
     * <p>This parameter is only used when {@code length} is -1. Must be specified
     * together with {@code minLength}.</p>
     * 
     * @return The maximum length, or -1 if not specified
     */
    int maxLength() default -1;
    
    /**
     * The exact length of the byte array to generate.
     * 
     * <p>If this is specified (>= 0), it takes precedence over {@code minLength}
     * and {@code maxLength}. Set to -1 to use the range-based approach.</p>
     * 
     * @return The exact length, or -1 to use minLength/maxLength
     */
    int length() default -1;
}
