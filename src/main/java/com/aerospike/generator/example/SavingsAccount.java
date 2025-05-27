package com.aerospike.generator.example;

import com.aerospike.generator.annotations.GenDate;

public class SavingsAccount extends Account {

    @GenDate(start = "now - 20y", end = "now")
    private long savingHistory;
    
    @Override
    public String getName() {
        return "Savings Account";
    }

    public long getSavingHistory() {
        return savingHistory;
    }

    public void setSavingHistory(long savingHistory) {
        this.savingHistory = savingHistory;
    }

    @Override
    public String toString() {
        return "SavingsAccount [savingHistory=" + savingHistory + " super:" +super.toString() + "]";
    }
    
}
