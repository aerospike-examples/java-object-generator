package com.aerospike.generator.example;

import com.aerospike.generator.annotations.GenExpression;

/**
 * Example class demonstrating the use of annotation references in GenExpression.
 * This shows how to directly reference generator annotations in expressions.
 */
public class AnnotationExpressionExample {
    
    // Use annotation reference directly in expression
    @GenExpression("'txn-' & @GenNumber(start=1, end=1000)")
    private String transactionId;
    
    // Use string annotation reference
    @GenExpression("'user-' & @GenString(length=5, type=CHARACTERS)")
    private String userId;
    
    // Use boolean annotation reference
    @GenExpression("'status-' & @GenBoolean")
    private String status;
    
    // Use UUID annotation reference
    @GenExpression("'session-' & @GenUuid")
    private String sessionId;
    
    // Use email annotation reference
    @GenExpression("'contact-' & @GenEmail")
    private String contactInfo;
    
    // Getters for testing
    public String getTransactionId() {
        return transactionId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public String getContactInfo() {
        return contactInfo;
    }
} 