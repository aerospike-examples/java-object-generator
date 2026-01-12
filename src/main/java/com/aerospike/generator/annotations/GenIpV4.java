package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates IPv4 addresses for test data.
 * 
 * <p>This annotation can be applied to fields of type {@code String} to generate
 * random but valid IPv4 addresses in the standard dotted-decimal format
 * (e.g., "192.168.1.1").</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * @GenIpV4
 * private String ipAddress;
 * 
 * @GenIpV4
 * private String clientIp;
 * }</pre>
 * 
 * <p><b>Note:</b> Generated IP addresses are valid IPv4 addresses but may include
 * private, public, or reserved ranges. For network testing, you may want to filter
 * or validate the generated addresses based on your requirements.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenIpV4 {
}
