package com.aerospike.generator.example;

import com.aerospike.generator.annotations.GenAddress;
import com.aerospike.generator.annotations.GenAddress.AddressPart;

public class Address {
    @GenAddress(AddressPart.STREET_ADDRESS)
    private String line1;
    @GenAddress(AddressPart.SECONDARY)
    private String line2;
    @GenAddress(AddressPart.CITY)
    private String city;
    @GenAddress(AddressPart.COUNTRY)
    private String country;
    @GenAddress(AddressPart.STATE)
    private String state;
    
    @Override
    public String toString() {
        return "Address [line1=" + line1 + ", line2=" + line2 + ", city=" + city + ", country=" + country + ", state="
                + state + "]";
    }
    public String getLine1() {
        return line1;
    }
    public void setLine1(String line1) {
        this.line1 = line1;
    }
    public String getLine2() {
        return line2;
    }
    public void setLine2(String line2) {
        this.line2 = line2;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    
}
