package com.aerospike.generator.example;

import com.aerospike.generator.annotations.GenMagic;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Comprehensive example demonstrating intelligent pattern matching for Lists and Sets
 * with @GenMagic annotation. This shows how field names are analyzed to generate
 * contextually appropriate test data for collections of different types.
 */
@GenMagic
public class MagicListSetExample {
    
    // ===== STRING LISTS =====
    // Address-related fields
    private List<String> zipCodes;           // Should generate zipcode patterns
    private List<String> postalCodes;        // Should generate zipcode patterns
    private List<String> cities;             // Should generate city names
    private List<String> countries;          // Should generate country names
    private List<String> addresses;          // Should generate full addresses
    
    // Name-related fields
    private List<String> surnames;           // Should generate last names
    private List<String> firstNames;         // Should generate first names
    private List<String> fullNames;          // Should generate full names
    
    // Contact information fields
    private List<String> emailAddresses;     // Should generate email addresses
    private List<String> phoneNumbers;       // Should generate phone numbers
    private List<String> mobileNumbers;      // Should generate phone numbers
    
    // Location and geographic fields
    private List<String> locations;          // Should generate location names
    private List<String> regions;            // Should generate region codes
    private List<String> timezones;         // Should generate timezone codes
    
    // Status and state fields
    private List<String> statuses;          // Should generate status values
    private List<String> priorities;        // Should generate priority levels
    private List<String> categories;        // Should generate category names
    
    // Professional fields
    private List<String> jobTitles;         // Should generate job titles
    private List<String> departments;       // Should generate department names
    private List<String> skills;            // Should generate skill levels
    
    // Technical fields
    private List<String> languages;         // Should generate programming languages
    private List<String> frameworks;        // Should generate framework names
    private List<String> databases;         // Should generate database names
    
    // URL and web fields
    private List<String> urls;              // Should generate URLs
    private List<String> domains;           // Should generate domain names
    
    // ===== NUMBER LISTS =====
    // Age and time fields
    private List<Integer> ages;              // Should generate ages (1-100)
    private List<Long> durations;            // Should generate durations
    
    // Financial fields
    private List<Double> prices;             // Should generate prices (10-10000)
    private List<Integer> salaries;          // Should generate salaries (30000-200000)
    private List<Long> budgets;              // Should generate budgets
    
    // Quantity and count fields
    private List<Integer> orderNumbers;     // Should generate numbers (1-1000000)
    private List<Long> itemCounts;           // Should generate counts
    private List<Integer> quantities;       // Should generate quantities
    
    // Measurement fields
    private List<Double> temperatures;       // Should generate temperatures
    private List<Double> weights;            // Should generate weights
    private List<Integer> distances;         // Should generate distances
    
    // Rating and score fields
    private List<Integer> ratings;           // Should generate ratings (1-10)
    private List<Integer> scores;           // Should generate scores
    
    // ===== BOOLEAN LISTS =====
    private List<Boolean> isActiveFlags;     // Should generate boolean values
    private List<Boolean> isRegisteredFlags; // Should generate mostly true values
    private List<Boolean> isVerifiedFlags;  // Should generate mostly true values
    
    // ===== DATE LISTS =====
    private List<Date> birthdays;            // Should generate birth dates (18-90 years ago)
    private List<Date> createdDates;         // Should generate creation dates (5 years ago to 1 day ago)
    private List<Date> lastLoginDates;       // Should generate recent dates (1 year ago to now)
    private List<Date> scheduledDates;       // Should generate future dates (now to 1 year ahead)
    
    // ===== SETS (Similar patterns as Lists) =====
    private Set<String> continents;         // Should use OneOf pattern
    private Set<String> currencies;         // Should use currency codes
    private Set<String> skillLevels;       // Should use skill levels
    
    private Set<Integer> userIds;           // Should generate IDs (1-10000)
    private Set<Long> accountNumbers;       // Should generate numbers (1-1000000)
    
    private Set<Boolean> featureFlags;      // Should generate boolean values
    
    private Set<Date> appointmentDates;     // Should generate appointment dates
    
    // Getters for all fields
    public List<String> getZipCodes() { return zipCodes; }
    public List<String> getPostalCodes() { return postalCodes; }
    public List<String> getCities() { return cities; }
    public List<String> getCountries() { return countries; }
    public List<String> getAddresses() { return addresses; }
    public List<String> getSurnames() { return surnames; }
    public List<String> getFirstNames() { return firstNames; }
    public List<String> getFullNames() { return fullNames; }
    public List<String> getEmailAddresses() { return emailAddresses; }
    public List<String> getPhoneNumbers() { return phoneNumbers; }
    public List<String> getMobileNumbers() { return mobileNumbers; }
    public List<String> getLocations() { return locations; }
    public List<String> getRegions() { return regions; }
    public List<String> getTimezones() { return timezones; }
    public List<String> getStatuses() { return statuses; }
    public List<String> getPriorities() { return priorities; }
    public List<String> getCategories() { return categories; }
    public List<String> getJobTitles() { return jobTitles; }
    public List<String> getDepartments() { return departments; }
    public List<String> getSkills() { return skills; }
    public List<String> getLanguages() { return languages; }
    public List<String> getFrameworks() { return frameworks; }
    public List<String> getDatabases() { return databases; }
    public List<String> getUrls() { return urls; }
    public List<String> getDomains() { return domains; }
    
    public List<Integer> getAges() { return ages; }
    public List<Long> getDurations() { return durations; }
    public List<Double> getPrices() { return prices; }
    public List<Integer> getSalaries() { return salaries; }
    public List<Long> getBudgets() { return budgets; }
    public List<Integer> getOrderNumbers() { return orderNumbers; }
    public List<Long> getItemCounts() { return itemCounts; }
    public List<Integer> getQuantities() { return quantities; }
    public List<Double> getTemperatures() { return temperatures; }
    public List<Double> getWeights() { return weights; }
    public List<Integer> getDistances() { return distances; }
    public List<Integer> getRatings() { return ratings; }
    public List<Integer> getScores() { return scores; }
    
    public List<Boolean> getIsActiveFlags() { return isActiveFlags; }
    public List<Boolean> getIsRegisteredFlags() { return isRegisteredFlags; }
    public List<Boolean> getIsVerifiedFlags() { return isVerifiedFlags; }
    
    public List<Date> getBirthdays() { return birthdays; }
    public List<Date> getCreatedDates() { return createdDates; }
    public List<Date> getLastLoginDates() { return lastLoginDates; }
    public List<Date> getScheduledDates() { return scheduledDates; }
    
    public Set<String> getContinents() { return continents; }
    public Set<String> getCurrencies() { return currencies; }
    public Set<String> getSkillLevels() { return skillLevels; }
    public Set<Integer> getUserIds() { return userIds; }
    public Set<Long> getAccountNumbers() { return accountNumbers; }
    public Set<Boolean> getFeatureFlags() { return featureFlags; }
    public Set<Date> getAppointmentDates() { return appointmentDates; }
}

