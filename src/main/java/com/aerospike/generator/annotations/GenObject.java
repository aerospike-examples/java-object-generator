package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates nested object instances for test data.
 * 
 * <p>This annotation can be applied to fields of object types (non-primitive,
 * non-collection types) to generate nested object instances. The generated objects
 * are populated recursively using their own field annotations.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Generate an Address object
 * @GenObject
 * private Address address;
 * 
 * // Generate with possibility of null
 * @GenObject(percentNull = 20)
 * private ContactInfo contactInfo;
 * 
 * // Generate from a set of subclasses
 * @GenObject(subclasses = {CheckingAccount.class, SavingsAccount.class})
 * private Account account;
 * }</pre>
 * 
 * <p><b>Note:</b> The object type must have a public no-argument constructor.
 * If {@code subclasses} is specified, one of the subclasses is randomly selected
 * for instantiation.</p>
 * 
 * <p><b>Recursive Generation:</b> Nested objects are populated recursively,
 * so if an Address object has a City field with @GenString, that field will
 * also be populated automatically.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenObject {
    /**
     * Percentage chance (0-100) that the generated object will be null.
     * 
     * <p>Useful for modeling optional nested objects.</p>
     * 
     * @return The null percentage (0-100), default is 0 (never null)
     */
    int percentNull() default 0;
    
    /**
     * Subclasses to choose from when generating the object.
     * 
     * <p>If specified, one of these subclasses is randomly selected and instantiated
     * instead of the declared field type. This is useful for polymorphic fields
     * or when you want to generate specific implementations.</p>
     * 
     * <p>Example: If the field is of type {@code Account}, you might specify
     * {@code {CheckingAccount.class, SavingsAccount.class}} to randomly generate
     * one of these specific account types.</p>
     * 
     * @return Array of subclasses to choose from
     */
    Class<?>[] subclasses() default {};
}
