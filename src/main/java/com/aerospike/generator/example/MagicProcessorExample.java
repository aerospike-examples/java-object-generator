package com.aerospike.generator.example;

import com.aerospike.generator.annotations.GenMagic;

/**
 * Example class demonstrating the enhanced GenMagicProcessor functionality.
 * This shows how the magic processor can automatically generate meaningful
 * values based on field names without explicit annotations.
 */
@GenMagic
public class MagicProcessorExample {
    
    // Financial fields
    private Integer price;
    private Integer salary;
    private Integer budget;
    private Integer discount;
    
    // URL/Web fields
    private String url;
    private String domain;
    
    // Code/Technical fields
    private String code;
    private String version;
    
    // Status/State fields
    private String status;
    private String level;
    
    // Geographic fields
    private Double latitude;
    private Double longitude;
    private String region;
    
    // Social/Professional fields
    private String title;
    private String department;
    
    // Product/Service fields
    private String category;
    private String brand;
    
    // Measurement fields
    private Integer size;
    private Integer temperature;
    private Integer speed;
    
    // System/Technical fields
    private String host;
    private String database;
    private String language;
    
    // Age/Duration fields
    private int age;
    private int duration;
    private int count;
    private int rating;
    private int percentage;
    
    // Getters for testing
    public Integer getPrice() { return price; }
    public Integer getSalary() { return salary; }
    public Integer getBudget() { return budget; }
    public Integer getDiscount() { return discount; }
    public String getUrl() { return url; }
    public String getDomain() { return domain; }
    public String getCode() { return code; }
    public String getVersion() { return version; }
    public String getStatus() { return status; }
    public String getLevel() { return level; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getRegion() { return region; }
    public String getTitle() { return title; }
    public String getDepartment() { return department; }
    public String getCategory() { return category; }
    public String getBrand() { return brand; }
    public Integer getSize() { return size; }
    public Integer getTemperature() { return temperature; }
    public Integer getSpeed() { return speed; }
    public String getHost() { return host; }
    public String getDatabase() { return database; }
    public String getLanguage() { return language; }
    public Integer getAge() { return age; }
    public Integer getDuration() { return duration; }
    public Integer getCount() { return count; }
    public Integer getRating() { return rating; }
    public Integer getPercentage() { return percentage; }
}
