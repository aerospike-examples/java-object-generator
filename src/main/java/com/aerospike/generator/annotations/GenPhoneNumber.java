package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates realistic phone numbers for test data.
 * 
 * <p>This annotation can be applied to fields of type {@code String} to generate
 * various types of phone numbers, including standard phone numbers, cell phone
 * numbers, extensions, and subscriber numbers.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Standard phone number
 * @GenPhoneNumber(PhoneNumType.PHONE)
 * private String phoneNumber;
 * 
 * // Cell phone number
 * @GenPhoneNumber(PhoneNumType.CELL)
 * private String mobileNumber;
 * 
 * // Phone extension
 * @GenPhoneNumber(PhoneNumType.EXTENSION)
 * private String extension;
 * }</pre>
 * 
 * <p><b>Note:</b> When used with {@link GenMagic}, fields with names containing
 * "phone", "mobile", "cell", "fax", etc. automatically use appropriate phone
 * number types even without the annotation.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenPhoneNumber {
    /**
     * Enumeration of phone number types that can be generated.
     */
    public static enum PhoneNumType {
        /** Standard phone number */
        PHONE,
        /** Cell/mobile phone number */
        CELL,
        /** Phone extension */
        EXTENSION,
        /** Subscriber number */
        SUBSCRIBER
    }
    
    /**
     * The type of phone number to generate.
     * 
     * @return The phone number type, default is PHONE
     */
    PhoneNumType type() default PhoneNumType.PHONE;
}
