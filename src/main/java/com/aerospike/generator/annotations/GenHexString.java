package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generate a string representing a number of bytes in hexadecimal format. There are 4 parameters:
 * <ul>
 * <li><b>length:</b> The number of bytes to be generated and represented as a string. One byte will generate
 * at least 2 characters (hexadecimal equivalent), plus the length of any separator. If this parameter is
 * specified, neither the <code>minLength</code> nor <code>maxLength</code> can be specified.</li>
 * <li><b>minLength</b> and <b>maxLength</b>: A range for the number of bytes to generated. The number will be
 * from <code>minLength</code> to <code>maxLength</code> (both inclusive). If both parameters must be specified
 * or neither, and if they are specified then <code>length</code> cannot be specified.</li>
 * <li><b>separator</b>: a separator to place between the hexbytes. </li>
 * </ul>
 * For example, specifying <code>length = 5, separator = " "</code> might generate:
 * <pre>
 * "34 65 9d ac 20"
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenHexString {
    int minLength() default -1;
    int maxLength() default -1;
    int length() default -1;
    String separator() default "";
}
