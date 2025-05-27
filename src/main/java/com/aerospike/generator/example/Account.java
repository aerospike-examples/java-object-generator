package com.aerospike.generator.example;

import com.aerospike.generator.annotations.GenDate;
import com.aerospike.generator.annotations.GenNumber;

public abstract class Account {
    @GenNumber( start = -1000, end = 10_000_000)
    private double balance;
    @GenDate(start = "now - 20y", end = "now")
    private long openDate;
    public abstract String getName();
    public double getBalance() {
        return balance;
    }
    @Override
    public String toString() {
        return "Account [balance=" + balance + ", getName()=" + getName() + "]";
    }
}
