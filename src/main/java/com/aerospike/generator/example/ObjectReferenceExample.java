package com.aerospike.generator.example;

import java.util.Date;

import com.aerospike.generator.annotations.GenDate;
import com.aerospike.generator.annotations.GenExpression;
import com.aerospike.generator.annotations.GenNumber;
import com.aerospike.generator.annotations.GenString;

/**
 * Example class demonstrating the use of object property references in GenExpression.
 * This shows how expressions can reference other fields of the object being generated.
 */
public class ObjectReferenceExample {
    
    // Generate a start timestamp
    @GenDate(start = "01/01/2024", end = "31/12/2024")
    private Date startTimestamp;
    
    // Generate an end timestamp that is 30 days after the start timestamp
    @GenExpression("$obj.startTimestamp + 30 * 24 * 60 * 60 * 1000")
    private Date endTimestamp;
    
    // Generate a user ID
    @GenString(length = 5, type = GenString.StringType.CHARACTERS)
    private String userId;
    
    // Generate a session ID that includes the user ID
    @GenExpression("'session-' & $obj.userId & '-' & @GenNumber(start=1000, end=9999)")
    private String sessionId;
    
    // Generate a transaction amount
    @GenNumber(start = 10, end = 1000)
    private int amount;
    
    // Generate a transaction reference that includes the amount
    @GenExpression("'TXN-' & $obj.amount & '-' & @GenString(length=3, type=CHARACTERS)")
    private String transactionRef;
    
    // Generate an array of tags
    @GenString(length = 10, type = GenString.StringType.CHARACTERS)
    private String[] tags;
    
    // Generate a description that references the first tag
    @GenExpression("'Description: ' & $obj.tags[0]")
    private String description;
    
    // Getters for testing
    public Date getStartTimestamp() {
        return startTimestamp;
    }
    
    public Date getEndTimestamp() {
        return endTimestamp;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public String getTransactionRef() {
        return transactionRef;
    }
    
    public String[] getTags() {
        return tags;
    }
    
    public String getDescription() {
        return description;
    }
}
