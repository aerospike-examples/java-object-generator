package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates browser names or user agent strings for test data.
 * 
 * <p>This annotation can be applied to fields of type {@code String} to generate
 * either browser names (e.g., "Chrome", "Firefox") or full user agent strings
 * (e.g., "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36...").</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Browser name only
 * @GenBrowser(BrowserType.NAME)
 * private String browser;
 * 
 * // Full user agent string
 * @GenBrowser(BrowserType.USERAGENT)
 * private String userAgent;
 * }</pre>
 * 
 * <p><b>Note:</b> Generated user agent strings are realistic and include
 * common browser/OS combinations for web testing scenarios.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenBrowser {
    /**
     * Enumeration of browser information types that can be generated.
     */
    public enum BrowserType {
        /** Browser name only (e.g., "Chrome", "Firefox", "Safari") */
        NAME,
        /** Full user agent string (e.g., "Mozilla/5.0 (Windows NT 10.0...)") */
        USERAGENT
    };
    
    /**
     * The type of browser information to generate.
     * 
     * @return The browser type, default is NAME
     */
    BrowserType value() default BrowserType.NAME;
}
