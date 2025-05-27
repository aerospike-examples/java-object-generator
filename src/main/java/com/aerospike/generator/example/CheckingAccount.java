package com.aerospike.generator.example;

import com.aerospike.generator.annotations.GenNumber;

public class CheckingAccount extends Account {

    @GenNumber(start = 0, end = 100)
    private int numChecksUsed;
    
    @Override
    public String getName() {
        return "Checking Account";
    }

    public int getNumChecksUsed() {
        return numChecksUsed;
    }

    public void setNumChecksUsed(int numChecksUsed) {
        this.numChecksUsed = numChecksUsed;
    }

    @Override
    public String toString() {
        return "CheckingAccount [numChecksUsed=" + numChecksUsed + " super:" +super.toString() + "]";
    }

}
