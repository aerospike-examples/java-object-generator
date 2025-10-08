package com.aerospike.generator.example;

import com.aerospike.generator.annotations.GenExpression;
import com.aerospike.generator.annotations.GenNumber;
import com.aerospike.generator.annotations.GenString;

/**
 * Simple example demonstrating object property references in GenExpression.
 */
public class SimpleObjectReferenceExample {
    
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
    
    // Getters for testing
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
}
