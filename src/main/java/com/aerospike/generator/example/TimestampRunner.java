package com.aerospike.generator.example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.aerospike.generator.ValueCreator;
import com.aerospike.generator.ValueCreatorCache;

/**
 * Example runner to demonstrate timestamp generation with object property references.
 */
public class TimestampRunner {
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static void main(String[] args) {
        // Get the ValueCreator and manually create instances
        ValueCreator<TimestampExample> valueCreator = ValueCreatorCache.getInstance().get(TimestampExample.class);
        
        // Generate a few examples
        System.out.println("Generating TimestampExample instances:");
        System.out.println("=====================================");
        
        for (int i = 1; i <= 3; i++) {
            try {
                // Create a new instance
                TimestampExample example = new TimestampExample();
                
                // Create parameter map
                Map<String, Object> params = new HashMap<>();
                params.put("Key", (long) i);
                
                // Populate the object
                valueCreator.populate(example, params);
                
                // Print the results
                System.out.println("Start Timestamp: " + formatDate(example.getStartTimestamp()));
                System.out.println("Duration: " + example.getDurationMinutes() + " minutes");
                System.out.println("End Timestamp: " + formatDate(example.getEndTimestamp()));
                System.out.println("Session ID: " + example.getSessionId());
                System.out.println("Description: " + example.getDescription());
                
                // Verify the duration is correct
                long actualDuration = example.getEndTimestamp().getTime() - example.getStartTimestamp().getTime();
                long expectedDuration = example.getDurationMinutes() * 60 * 1000;
                System.out.println("Duration Check: " + (actualDuration == expectedDuration ? "✓ PASS" : "✗ FAIL"));
                System.out.println("---");
                
            } catch (Exception e) {
                System.err.println("Error generating instance " + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("Generation complete!");
    }
    
    private static String formatDate(Date date) {
        return dateFormat.format(date);
    }
}
