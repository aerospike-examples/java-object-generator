package com.aerospike.generator.example;

import com.aerospike.generator.annotations.GenExpression;

/**
 * Example class demonstrating the use of parameterized annotation parameters in GenExpression.
 * This shows how to use parameter references within annotation parameters.
 */
public class ParameterizedAnnotationExample {
    
    // Use parameter reference in annotation parameter
    @GenExpression("'acct-' & @GenNumber(start=1, end=$MAX_ACCOUNTS)")
    private String accountId;
    
    // Use parameter reference for string length
    @GenExpression("'user-' & @GenString(length=$USER_ID_LENGTH, type=CHARACTERS)")
    private String userId;
    
    // Use parameter reference for range
    @GenExpression("'order-' & @GenNumber(start=$MIN_ORDER, end=$MAX_ORDER)")
    private String orderId;
    
    // Use parameter reference for boolean probability
    @GenExpression("'status-' & @GenBoolean")
    private String status;
    
    // Use parameter reference for UUID
    @GenExpression("'session-' & @GenUuid")
    private String sessionId;
    
    // Getters for testing
    public String getAccountId() {
        return accountId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getSessionId() {
        return sessionId;
    }
} 