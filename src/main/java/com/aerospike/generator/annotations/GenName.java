package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates realistic person names for test data.
 * 
 * <p>This annotation can be applied to fields of type {@code String} to generate
 * various types of person names, including first names, last names, full names,
 * and related components.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Full name (first + last)
 * @GenName(NameType.FULL)
 * private String fullName;
 * 
 * // First name only
 * @GenName(NameType.FIRST)
 * private String firstName;
 * 
 * // Last name only
 * @GenName(NameType.LAST)
 * private String lastName;
 * 
 * // Full name with middle name
 * @GenName(NameType.FULL_WITH_MIDDLE)
 * private String completeName;
 * 
 * // Username based on name
 * @GenName(NameType.USERNAME)
 * private String username;
 * }</pre>
 * 
 * <p><b>Note:</b> When used with {@link GenMagic}, fields with names containing
 * "firstName", "lastName", "surname", "fullName", etc. automatically use
 * appropriate name types even without the annotation.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenName {
    /**
     * Enumeration of name types that can be generated.
     */
    public enum NameType {
        /** First name only (e.g., "John") */
        FIRST,
        /** Last name only (e.g., "Smith") */
        LAST,
        /** Full name (e.g., "John Smith") */
        FULL,
        /** Full name with middle name (e.g., "John Michael Smith") */
        FULL_WITH_MIDDLE,
        /** Name prefix (e.g., "Mr.", "Dr.", "Ms.") */
        PREFIX,
        /** Name suffix (e.g., "Jr.", "Sr.", "III") */
        SUFFIX,
        /** Title (e.g., "Mr", "Mrs", "Dr") */
        TITLE,
        /** Username based on name (e.g., "jsmith", "john.smith") */
        USERNAME
    };
    
    /**
     * The type of name to generate.
     * 
     * @return The name type, default is FULL
     */
    NameType value() default NameType.FULL;
}
