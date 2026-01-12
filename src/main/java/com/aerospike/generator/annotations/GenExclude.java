package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excludes a field from automatic value generation.
 * 
 * <p>This annotation can be applied to fields to prevent them from being populated
 * by class-level annotations such as {@link GenMagic}, {@link GenString}, or
 * {@link GenExpression}. This is useful when you want to exclude certain fields
 * from automatic generation while still using class-level annotations for other fields.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * @GenMagic
 * public class User {
 *     private String name;  // Will be generated
 *     
 *     @GenExclude
 *     private String password;  // Will NOT be generated
 *     
 *     private String email;  // Will be generated
 * }
 * }</pre>
 * 
 * <p><b>Note:</b> Field-level annotations (e.g., {@code @GenString}) still take
 * precedence and will generate values even if the class has {@code @GenExclude}
 * on the field. This annotation only prevents class-level annotations from
 * affecting the field.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenExclude {
}
