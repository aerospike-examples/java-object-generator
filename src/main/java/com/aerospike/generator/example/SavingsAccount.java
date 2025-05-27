package com.aerospike.generator.example;

public class SavingsAccount extends Account {

//    @Gen
    private long savingHistory;
    
    @Override
    public String getName() {
        return "Savings Account";
    }
    
}
