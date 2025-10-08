package com.aerospike.generator.example;

import com.aerospike.generator.ValueCreator;
import com.aerospike.generator.ValueCreatorCache;
import java.util.HashMap;
import java.util.Map;

/**
 * Example runner to demonstrate the enhanced GenMagicProcessor functionality.
 */
public class MagicProcessorRunner {
    
    public static void main(String[] args) {
        // Get the ValueCreator and manually create instances
        ValueCreator<MagicProcessorExample> valueCreator = ValueCreatorCache.getInstance().get(MagicProcessorExample.class);
        
        // Generate a few examples
        System.out.println("Generating MagicProcessorExample instances:");
        System.out.println("=========================================");
        
        for (int i = 1; i <= 3; i++) {
            try {
                // Create a new instance
                MagicProcessorExample example = new MagicProcessorExample();
                
                // Create parameter map
                Map<String, Object> params = new HashMap<>();
                params.put("Key", (long) i);
                
                // Populate the object
                valueCreator.populate(example, params);
                
                // Print the results
                System.out.println("=== Instance " + i + " ===");
                System.out.println("Financial Fields:");
                System.out.println("  Price: $" + example.getPrice());
                System.out.println("  Salary: $" + example.getSalary());
                System.out.println("  Budget: $" + example.getBudget());
                System.out.println("  Discount: " + example.getDiscount() + "%");
                
                System.out.println("Web Fields:");
                System.out.println("  URL: " + example.getUrl());
                System.out.println("  Domain: " + example.getDomain());
                
                System.out.println("Technical Fields:");
                System.out.println("  Code: " + example.getCode());
                System.out.println("  Version: " + example.getVersion());
                System.out.println("  Host: " + example.getHost());
                System.out.println("  Database: " + example.getDatabase());
                System.out.println("  Language: " + example.getLanguage());
                
                System.out.println("Status Fields:");
                System.out.println("  Status: " + example.getStatus());
                System.out.println("  Level: " + example.getLevel());
                
                System.out.println("Geographic Fields:");
                System.out.println("  Latitude: " + example.getLatitude());
                System.out.println("  Longitude: " + example.getLongitude());
                System.out.println("  Region: " + example.getRegion());
                
                System.out.println("Professional Fields:");
                System.out.println("  Title: " + example.getTitle());
                System.out.println("  Department: " + example.getDepartment());
                
                System.out.println("Product Fields:");
                System.out.println("  Category: " + example.getCategory());
                System.out.println("  Brand: " + example.getBrand());
                
                System.out.println("Measurement Fields:");
                System.out.println("  Size: " + example.getSize());
                System.out.println("  Temperature: " + example.getTemperature() + "°C");
                System.out.println("  Speed: " + example.getSpeed() + " km/h");
                
                System.out.println("Numeric Fields:");
                System.out.println("  Age: " + example.getAge() + " years");
                System.out.println("  Duration: " + example.getDuration() + " seconds");
                System.out.println("  Count: " + example.getCount());
                System.out.println("  Rating: " + example.getRating() + "/10");
                System.out.println("  Percentage: " + example.getPercentage() + "%");
                System.out.println("---");
                
            } catch (Exception e) {
                System.err.println("Error generating instance " + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("Generation complete!");
    }
}
