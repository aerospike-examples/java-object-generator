package com.aerospike.generator.example;

import com.aerospike.generator.Generator;

/**
 * Example runner to demonstrate the annotation reference functionality in GenExpression.
 */
public class AnnotationExpressionRunner {
    
    public static void main(String[] args) {
        // Create a generator for the AnnotationExpressionExample class
        Generator generator = new Generator(AnnotationExpressionExample.class);
        
        // Generate a few examples
        System.out.println("Generating AnnotationExpressionExample instances:");
        System.out.println("================================================");
        
        generator.generate(1, 3, AnnotationExpressionExample.class, example -> {
            System.out.println("Transaction ID: " + example.getTransactionId());
            System.out.println("User ID: " + example.getUserId());
            System.out.println("Status: " + example.getStatus());
            System.out.println("Session ID: " + example.getSessionId());
            System.out.println("Contact Info: " + example.getContactInfo());
            System.out.println("---");
        });
        
        System.out.println("Generation complete!");
    }
} 