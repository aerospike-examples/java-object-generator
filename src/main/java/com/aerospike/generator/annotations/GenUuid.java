package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates UUID (Universally Unique Identifier) values for test data.
 * 
 * <p>This annotation can be applied to fields of type {@code String} or {@code UUID}
 * to generate random UUIDs. The generated UUIDs follow the standard UUID format
 * (e.g., "550e8400-e29b-41d4-a716-446655440000").</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Generate UUID as String
 * @GenUuid
 * private String id;
 * 
 * // Generate UUID as UUID type
 * @GenUuid
 * private UUID uniqueId;
 * }</pre>
 * 
 * <p><b>Note:</b> When used with {@link GenMagic}, fields with names containing
 * "uuid" automatically use this generator even without the annotation.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenUuid {
}
