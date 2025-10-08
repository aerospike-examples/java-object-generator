package com.aerospike.generator.example;

import java.util.Date;

import com.aerospike.generator.annotations.GenDate;
import com.aerospike.generator.annotations.GenExpression;
import com.aerospike.generator.annotations.GenOneOf;

/**
 * Example class demonstrating timestamp generation with object property references.
 * This shows how to generate a start timestamp and an end timestamp that is
 * a specific duration later than the start timestamp.
 */
public class TimestampExample {
    
    // Generate a start timestamp from now to now+14 days
    @GenDate(start = "now", end = "now+14d")
    private Date startTimestamp;
    
    // Generate a duration in minutes (30, 60, 90, or 120 minutes)
    @GenOneOf("30,60,90,120")
    private int durationMinutes;
    
    // Generate an end timestamp that is the duration after the start timestamp
    @GenExpression("$obj.startTimestamp + $obj.durationMinutes * 60 * 1000")
    private Date endTimestamp;
    
    // Generate a session ID that includes the start timestamp
    @GenExpression("'session-' & $obj.startTimestamp & '-' & $obj.durationMinutes")
    private String sessionId;
    
    // Generate a description that includes both timestamps
    @GenExpression("'Event from ' & $obj.startTimestamp & ' to ' & $obj.endTimestamp & ' (' & $obj.durationMinutes & ' min)'")
    private String description;
    
    // Getters for testing
    public Date getStartTimestamp() {
        return startTimestamp;
    }
    
    public int getDurationMinutes() {
        return durationMinutes;
    }
    
    public Date getEndTimestamp() {
        return endTimestamp;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public String getDescription() {
        return description;
    }
}
