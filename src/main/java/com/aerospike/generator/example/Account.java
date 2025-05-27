package com.aerospike.generator.example;

public abstract class Account {
    private double balance;
    public abstract String getName();
    public double getBalance() {
        return balance;
    }
}
