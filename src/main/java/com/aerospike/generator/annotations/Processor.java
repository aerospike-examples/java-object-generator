package com.aerospike.generator.annotations;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Interface for processors that generate test data values for object fields.
 * 
 * <p>Processors are responsible for generating appropriate test data based on
 * field annotations and configuration. Each processor implementation handles
 * a specific type of data generation (strings, numbers, dates, lists, etc.).</p>
 * 
 * <p>Processors are created by {@link com.aerospike.generator.ValueCreator} based
 * on field annotations and are used to populate object fields during test data
 * generation.</p>
 * 
 * <h3>Implementation Guidelines</h3>
 * <ul>
 *   <li>Processors should be thread-safe as they may be used concurrently</li>
 *   <li>The process method should return appropriate types for the field type</li>
 *   <li>Deferred processors (like GenExpression) should return true from isDeferred()</li>
 *   <li>Processors should handle null parameters gracefully</li>
 * </ul>
 * 
 * <h3>Example Implementation</h3>
 * <pre>{@code
 * public class MyProcessor implements Processor {
 *     public Object process(Map<String, Object> params) {
 *         return generateValue();
 *     }
 * }
 * }</pre>
 */
public interface Processor {
    /**
     * Generates a test data value for a field.
     * 
     * <p>This method is called by ValueCreator to generate a value for a field.
     * The parameters map may contain context information such as:</p>
     * <ul>
     *   <li>"Key" - A unique identifier for the object being generated</li>
     *   <li>"obj" - The object being populated (for deferred processors)</li>
     *   <li>Custom parameters from GenExpression annotations</li>
     * </ul>
     * 
     * <p>The returned value should be compatible with the field type. For example,
     * a String field should receive a String, an Integer field should receive an Integer,
     * etc.</p>
     * 
     * @param params A map of parameters that may be used during value generation
     * @return The generated test data value
     */
    Object process(Map<String, Object> params);
    
    /**
     * Returns true if this processor should be deferred (processed after other fields).
     * 
     * <p>Deferred processors are processed in a second phase after all non-deferred
     * fields have been populated. This is useful for processors that may reference
     * other fields in the object, such as GenExpression processors that use
     * {@code $obj.fieldName} syntax.</p>
     * 
     * <p>This method is used to optimize the populate method by avoiding instanceof
     * checks during field processing.</p>
     * 
     * @return true if this processor should be deferred, false otherwise
     */
    default boolean isDeferred() {
        return false;
    }
    
    /**
     * Determines the length to generate based on configuration parameters.
     * 
     * <p>This utility method is used by processors that need to generate values
     * of variable length. If a fixed length is specified (>= 0), it is returned.
     * Otherwise, a random length is generated within the specified range.</p>
     * 
     * @param length The fixed length to use (if >= 0), or -1 to use random length
     * @param minLength The minimum length for random generation (when length < 0)
     * @param maxLength The maximum length for random generation (when length < 0)
     * @return The length to use for generation
     */
    static int getLengthToGenerate(int length, int minLength, int maxLength) {
        if (length >= 0) {
            return length;
        }
        else {
            return ThreadLocalRandom.current().nextInt(minLength, maxLength +1);
        }
    }

}
