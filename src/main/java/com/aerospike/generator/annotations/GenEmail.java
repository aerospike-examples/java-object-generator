package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates realistic email addresses for test data.
 * 
 * <p>This annotation can be applied to fields of type {@code String} to generate
 * random but realistic email addresses. The generated emails use common domains
 * and follow standard email format patterns.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * @GenEmail
 * private String emailAddress;
 * 
 * @GenEmail
 * private String contactEmail;
 * }</pre>
 * 
 * <p><b>Note:</b> When used with {@link GenMagic}, fields with names containing
 * "email" (e.g., "email", "emailAddress", "emailAddresses") automatically use
 * this generator even without the annotation.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenEmail {
}
