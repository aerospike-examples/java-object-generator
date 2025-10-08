package com.aerospike.generator.example;

import java.util.HashMap;
import java.util.Map;

import com.aerospike.generator.ValueCreator;
import com.aerospike.generator.ValueCreatorCache;

/**
 * Simple example runner to demonstrate the object property reference functionality in GenExpression.
 */
public class SimpleObjectReferenceRunner {
    
    public static void main(String[] args) {
        // Get the ValueCreator and manually create instances
        ValueCreator<SimpleObjectReferenceExample> valueCreator = ValueCreatorCache.getInstance().get(SimpleObjectReferenceExample.class);
        
        // Generate a few examples
        System.out.println("Generating SimpleObjectReferenceExample instances:");
        System.out.println("================================================");
        
        for (int i = 1; i <= 3; i++) {
            try {
                // Create a new instance
                SimpleObjectReferenceExample example = new SimpleObjectReferenceExample();
                
                // Create parameter map
                Map<String, Object> params = new HashMap<>();
                params.put("Key", (long) i);
                
                // Populate the object
                valueCreator.populate(example, params);
                
                // Print the results
                System.out.println("User ID: " + example.getUserId());
                System.out.println("Session ID: " + example.getSessionId());
                System.out.println("Amount: " + example.getAmount());
                System.out.println("Transaction Ref: " + example.getTransactionRef());
                System.out.println("---");
                
            } catch (Exception e) {
                System.err.println("Error generating instance " + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("Generation complete!");
    }
}
