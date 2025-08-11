package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generate a value using an expression which can be very simple or very complex.
 * 
 * <p>The expression can contain the following elements:</p>
 * 
 * <h3>Numbers and Strings</h3>
 * <ul>
 *   <li>Integer numbers: {@code 42, 123, 0}</li>
 *   <li>String literals (using single quotes): {@code 'Hello', 'World'}</li>
 * </ul>
 * 
 * <h3>Arithmetic Operators</h3>
 * <ul>
 *   <li>Addition: {@code +}</li>
 *   <li>Subtraction: {@code -}</li>
 *   <li>Multiplication: {@code *}</li>
 *   <li>Division: {@code /}</li>
 *   <li>Modulo: {@code %}</li>
 *   <li>Power: {@code ^}</li>
 * </ul>
 * 
 * <h3>String Operations</h3>
 * <ul>
 *   <li>String concatenation: {@code &}</li>
 * </ul>
 * 
 * <h3>Parameters</h3>
 * <ul>
 *   <li>Parameter reference: {@code $key} - an id passed in representing a unique key to this record.</li>
 *   <li>Parameters must be provided in the parameters map</li>
 *   <li>Parameters are of type Long</li>
 * </ul>
 * 
 * <h3>Annotation References</h3>
 * <ul>
 *   <li>Annotation reference: {@code @GenNumber(start=1, end=1000)} - directly reference generator annotations</li>
 *   <li>Annotation references are evaluated at runtime and their values are used in the expression</li>
 *   <li>Annotation parameters can reference other parameters: {@code @GenNumber(start=1, end=$MAX_ACCOUNTS)}</li>
 *   <li>Supported annotations: GenNumber, GenString, GenBoolean, GenUuid, GenDate, GenEmail, GenName, etc.</li>
 * </ul>
 * 
 * <h3>Functions</h3>
 * <ul>
 *   <li>{@code NOW()} - Returns current timestamp in milliseconds</li>
 *   <li>{@code DATE(timestamp, [format])} - Formats a timestamp as a string
 *       <ul>
 *         <li>First parameter: timestamp (long)</li>
 *         <li>Second parameter (optional): date format string (defaults to 'yyyy-MM-dd')</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <h3>Operator Precedence</h3>
 * <ol>
 *   <li>Parentheses {@code ()}</li>
 *   <li>Power {@code ^}</li>
 *   <li>Multiplication/Division/Modulo {@code * / %}</li>
 *   <li>Addition/Subtraction {@code + -}</li>
 *   <li>String concatenation {@code &}</li>
 * </ol>
 * 
 * <h3>Examples</h3>
 * <pre>
 * "2 + 3 * 4"                                    // Evaluates to 14
 * "$key * 2 + 5"                                 // Uses parameter value
 * "'Hello' & ' World'"                           // String concatenation
 * "($key + 10) * 2"                              // Parentheses for grouping
 * "NOW()"                                        // Current timestamp
 * "DATE(NOW())"                                  // Current date in yyyy-MM-dd format
 * "DATE(NOW(), 'HH:mm:ss')"                      // Current time in custom format
 * "PAD(42, 5, '0')"                              // Returns "00042"
 * "PAD('ABC', 5, '*')"                           // Returns "**ABC"
 * "'txn-' & @GenNumber(start=1, end=1000)"                    // Uses annotation reference
 * "'user-' & @GenString(length=5, type=CHARACTERS)"           // Uses string annotation
 * "'acct-' & @GenNumber(start=1, end=$MAX_ACCOUNTS)"          // Uses parameterized annotation
 * "'order-' & @GenNumber(start=$MIN_ORDER, end=$MAX_ORDER)"   // Uses multiple parameter references
 * </pre>
 * 
 *
 * This annotation can be applied at the field level or the class level. If applied at the class 
 * level, ALL strings without a value will be assigned a value. Note this only affects string fields.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface GenExpression {
    String value();
}
