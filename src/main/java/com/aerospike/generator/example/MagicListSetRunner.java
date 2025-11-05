package com.aerospike.generator.example;

import com.aerospike.generator.ValueCreator;
import com.aerospike.generator.ValueCreatorCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Runner class to demonstrate the MagicListSetExample with comprehensive
 * List and Set pattern matching using @GenMagic annotation.
 */
public class MagicListSetRunner {

    public static void main(String[] args) {
        ValueCreator<MagicListSetExample> valueCreator = ValueCreatorCache.getInstance().get(MagicListSetExample.class);

        System.out.println("=".repeat(80));
        System.out.println("Comprehensive List/Set Pattern Matching Example with @GenMagic");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("This example demonstrates how @GenMagic intelligently generates");
        System.out.println("appropriate test data for Lists and Sets based on field name patterns.");
        System.out.println();

        for (int i = 1; i <= 2; i++) {
            try {
                MagicListSetExample example = new MagicListSetExample();
                Map<String, Object> params = new HashMap<>();
                params.put("Key", (long) i);
                valueCreator.populate(example, params);

                System.out.println("=".repeat(80));
                System.out.println("INSTANCE " + i);
                System.out.println("=".repeat(80));
                System.out.println();
                
                System.out.println("--- STRING LISTS (Address & Location) ---");
                System.out.println("zipCodes: " + example.getZipCodes());
                System.out.println("postalCodes: " + example.getPostalCodes());
                System.out.println("cities: " + example.getCities());
                System.out.println("countries: " + example.getCountries());
                System.out.println("locations: " + example.getLocations());
                System.out.println("addresses: " + example.getAddresses());
                System.out.println();
                
                System.out.println("--- STRING LISTS (Names) ---");
                System.out.println("surnames: " + example.getSurnames());
                System.out.println("firstNames: " + example.getFirstNames());
                System.out.println("fullNames: " + example.getFullNames());
                System.out.println();
                
                System.out.println("--- STRING LISTS (Contact Information) ---");
                System.out.println("emailAddresses: " + example.getEmailAddresses());
                System.out.println("phoneNumbers: " + example.getPhoneNumbers());
                System.out.println("mobileNumbers: " + example.getMobileNumbers());
                System.out.println();
                
                System.out.println("--- STRING LISTS (Status & Categories) ---");
                System.out.println("statuses: " + example.getStatuses());
                System.out.println("priorities: " + example.getPriorities());
                System.out.println("categories: " + example.getCategories());
                System.out.println();
                
                System.out.println("--- STRING LISTS (Professional) ---");
                System.out.println("jobTitles: " + example.getJobTitles());
                System.out.println("departments: " + example.getDepartments());
                System.out.println("skills: " + example.getSkills());
                System.out.println();
                
                System.out.println("--- STRING LISTS (Technical) ---");
                System.out.println("languages: " + example.getLanguages());
                System.out.println("frameworks: " + example.getFrameworks());
                System.out.println("databases: " + example.getDatabases());
                System.out.println();
                
                System.out.println("--- STRING LISTS (Web & URLs) ---");
                System.out.println("urls: " + example.getUrls());
                System.out.println("domains: " + example.getDomains());
                System.out.println();
                
                System.out.println("--- NUMBER LISTS (Age & Time) ---");
                System.out.println("ages: " + example.getAges());
                System.out.println("durations: " + example.getDurations());
                System.out.println();
                
                System.out.println("--- NUMBER LISTS (Financial) ---");
                System.out.println("prices: " + example.getPrices());
                System.out.println("salaries: " + example.getSalaries());
                System.out.println("budgets: " + example.getBudgets());
                System.out.println();
                
                System.out.println("--- NUMBER LISTS (Quantity & Count) ---");
                System.out.println("orderNumbers: " + example.getOrderNumbers());
                System.out.println("itemCounts: " + example.getItemCounts());
                System.out.println("quantities: " + example.getQuantities());
                System.out.println();
                
                System.out.println("--- NUMBER LISTS (Measurement) ---");
                System.out.println("temperatures: " + example.getTemperatures());
                System.out.println("weights: " + example.getWeights());
                System.out.println("distances: " + example.getDistances());
                System.out.println();
                
                System.out.println("--- NUMBER LISTS (Rating & Score) ---");
                System.out.println("ratings: " + example.getRatings());
                System.out.println("scores: " + example.getScores());
                System.out.println();
                
                System.out.println("--- BOOLEAN LISTS ---");
                System.out.println("isActiveFlags: " + example.getIsActiveFlags());
                System.out.println("isRegisteredFlags: " + example.getIsRegisteredFlags());
                System.out.println("isVerifiedFlags: " + example.getIsVerifiedFlags());
                System.out.println();
                
                System.out.println("--- DATE LISTS ---");
                System.out.println("birthdays: " + example.getBirthdays());
                System.out.println("createdDates: " + example.getCreatedDates());
                System.out.println("lastLoginDates: " + example.getLastLoginDates());
                System.out.println("scheduledDates: " + example.getScheduledDates());
                System.out.println();
                
                System.out.println("--- SETS (String) ---");
                System.out.println("continents: " + example.getContinents());
                System.out.println("currencies: " + example.getCurrencies());
                System.out.println("skillLevels: " + example.getSkillLevels());
                System.out.println();
                
                System.out.println("--- SETS (Number) ---");
                System.out.println("userIds: " + example.getUserIds());
                System.out.println("accountNumbers: " + example.getAccountNumbers());
                System.out.println();
                
                System.out.println("--- SETS (Boolean) ---");
                System.out.println("featureFlags: " + example.getFeatureFlags());
                System.out.println();
                
                System.out.println("--- SETS (Date) ---");
                System.out.println("appointmentDates: " + example.getAppointmentDates());
                System.out.println();
                
            } catch (Exception e) {
                System.err.println("Error generating instance " + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("=".repeat(80));
        System.out.println("Example complete!");
        System.out.println("=".repeat(80));
    }
}

