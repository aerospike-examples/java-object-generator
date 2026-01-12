package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates hexadecimal string representations of random bytes.
 * 
 * <p>This annotation can be applied to fields of type {@code String} to generate
 * hexadecimal strings representing random byte sequences. This is useful for
 * generating encryption keys, hashes, tokens, and other cryptographic values.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Generate exactly 16 bytes as hex (32 characters)
 * @GenHexString(length = 16)
 * private String encryptionKey;
 * 
 * // Generate 8-32 bytes as hex with space separator
 * @GenHexString(minLength = 8, maxLength = 32, separator = " ")
 * private String token;  // e.g., "34 65 9d ac 20"
 * 
 * // Generate hex string with colon separator
 * @GenHexString(length = 6, separator = ":")
 * private String macAddress;  // e.g., "a1:b2:c3:d4:e5:f6"
 * }</pre>
 * 
 * <p><b>Note:</b> Each byte generates 2 hexadecimal characters. If a separator
 * is specified, it is placed between each byte pair. For example, 5 bytes with
 * a space separator generates 5*2 + 4 = 14 characters (e.g., "34 65 9d ac 20").</p>
 * 
 * <p><b>Parameter Rules:</b>
 * <ul>
 *   <li>Specify either {@code length} OR both {@code minLength} and {@code maxLength}</li>
 *   <li>If {@code length} is specified (>= 0), it takes precedence</li>
 *   <li>If using range, both {@code minLength} and {@code maxLength} must be specified</li>
 * </ul>
 * </p>
 * 
 * @see GenBytes For generating raw byte arrays
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenHexString {
    /**
     * The minimum number of bytes to generate.
     * 
     * <p>This parameter is only used when {@code length} is -1. Must be specified
     * together with {@code maxLength}.</p>
     * 
     * @return The minimum number of bytes, or -1 if not specified
     */
    int minLength() default -1;
    
    /**
     * The maximum number of bytes to generate.
     * 
     * <p>This parameter is only used when {@code length} is -1. Must be specified
     * together with {@code minLength}.</p>
     * 
     * @return The maximum number of bytes, or -1 if not specified
     */
    int maxLength() default -1;
    
    /**
     * The exact number of bytes to generate.
     * 
     * <p>If this is specified (>= 0), it takes precedence over {@code minLength}
     * and {@code maxLength}. Set to -1 to use the range-based approach.</p>
     * 
     * <p>Each byte generates 2 hexadecimal characters, plus separators if specified.</p>
     * 
     * @return The exact number of bytes, or -1 to use minLength/maxLength
     */
    int length() default -1;
    
    /**
     * Separator to place between hexadecimal byte pairs.
     * 
     * <p>For example, with {@code separator = " "}, 5 bytes generates:
     * "34 65 9d ac 20". With {@code separator = ":"}, generates:
     * "34:65:9d:ac:20".</p>
     * 
     * @return The separator string, or empty string for no separator
     */
    String separator() default "";
}
