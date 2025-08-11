package com.aerospike.generator.example;

import com.aerospike.generator.Generator;
import com.aerospike.generator.ValueCreator;
import com.aerospike.generator.ValueCreatorCache;
import java.util.HashMap;
import java.util.Map;

/**
 * Example runner to demonstrate the parameterized annotation functionality in GenExpression.
 */
public class ParameterizedAnnotationRunner {
    
    public static void main(String[] args) {
        // Create parameter map with values for the annotation parameters
        Map<String, Object> params = new HashMap<>();
        params.put("MAX_ACCOUNTS", 1000L);
        params.put("USER_ID_LENGTH", 6L);
        params.put("MIN_ORDER", 100L);
        params.put("MAX_ORDER", 9999L);
        
        // Generate a few examples
        System.out.println("Generating ParameterizedAnnotationExample instances:");
        System.out.println("==================================================");
        System.out.println("Parameters: " + params);
        System.out.println();
        
        // Get the ValueCreator and manually create instances with our parameters
        ValueCreator<ParameterizedAnnotationExample> valueCreator = ValueCreatorCache.getInstance().get(ParameterizedAnnotationExample.class);
        
        for (int i = 1; i <= 3; i++) {
            try {
                // Create a new instance
                ParameterizedAnnotationExample example = new ParameterizedAnnotationExample();
                
                // Add the Key parameter and our custom parameters
                Map<String, Object> allParams = new HashMap<>(params);
                allParams.put("Key", (long) i);
                

                
                // Populate the object with our parameters
                valueCreator.populate(example, allParams);
                
                // Print the results
                System.out.println("Account ID: " + example.getAccountId());
                System.out.println("User ID: " + example.getUserId());
                System.out.println("Order ID: " + example.getOrderId());
                System.out.println("Status: " + example.getStatus());
                System.out.println("Session ID: " + example.getSessionId());
                System.out.println("---");
                
            } catch (Exception e) {
                System.err.println("Error generating instance " + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("Generation complete!");
    }
} 