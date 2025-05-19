package com.aerospike.generator.example;

import java.util.Date;

import com.aerospike.generator.annotations.GenAddress;
import com.aerospike.generator.annotations.GenAddress.AddressPart;
import com.aerospike.generator.annotations.GenDate;
import com.aerospike.generator.annotations.GenEmail;
import com.aerospike.generator.annotations.GenEnum;
import com.aerospike.generator.annotations.GenExclude;
import com.aerospike.generator.annotations.GenExpression;
import com.aerospike.generator.annotations.GenIpV4;
import com.aerospike.generator.annotations.GenName;
import com.aerospike.generator.annotations.GenName.NameType;
import com.aerospike.generator.annotations.GenOneOf;
import com.aerospike.generator.annotations.GenRange;
import com.aerospike.generator.annotations.GenString;
import com.aerospike.generator.annotations.GenString.StringType;
import com.aerospike.generator.annotations.GenUuid;

@GenString(type = StringType.WORDS, length = 2)
public class Member {
    public static enum MembershipLevel {
        BRONZE,
        SILVER,
        GOLD,
        PLATINUM
    }
    
    @GenExpression("'Person-' & $Key")
    private String id;
    @GenName(NameType.FIRST)
    private String firstName;
    @GenName(NameType.LAST)
    private String lastName;
    @GenEmail
    private String email;
    @GenDate(start = "now - 85years", end = "now - 18 years")
    private Date dob;
    @GenDate(start = "now - 6 years", end = "now")
    private Date dateJoined;
    @GenEnum
    private MembershipLevel level;
    @GenString(type = StringType.SENTENCES, minLength = 1, maxLength = 5)
    private String description;
    @GenAddress(AddressPart.STREET_ADDRESS)
    private String line1;
    @GenExclude     // do not generate a value for this string
    private String line2;
    @GenAddress(AddressPart.CITY)
    private String city;
    @GenAddress(AddressPart.COUNTRY)
    private String country;
    @GenAddress(AddressPart.STATE)
    private String state;
    @GenString(type = StringType.LETTERIFY, format = "???-???")
    private String licensePlate;
    @GenOneOf("Apple:10, Walmart:15, Safeway:8, Wing Stop, company1-10")
    private String employer;
    @GenUuid
    private String extId;
    @GenDate(start = "now-30d", end = "now-5m")
    private long lastUpdate;
    @GenRange(start = 0, end = 10)
    int familyMembers;
    @GenIpV4
    String ipAddress;
    
    // Dummy strings to be filled by the default 
    String dummy1;
    String dummy2;
    String dummy3;
    
    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Date getDob() {
        return dob;
    }

    public Date getDateJoined() {
        return dateJoined;
    }

    public MembershipLevel getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getEmployer() {
        return employer;
    }

    public String getExtId() {
        return extId;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public int getFamilyMembers() {
        return familyMembers;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDummy1() {
        return dummy1;
    }

    public String getDummy2() {
        return dummy2;
    }

    public String getDummy3() {
        return dummy3;
    }

    @Override
    public String toString() {
        return "Member [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email
                + ", dob=" + dob + ", dateJoined=" + dateJoined + ", level=" + level + ", description=" + description
                + ", line1=" + line1 + ", line2=" + line2 + ", city=" + city + ", country=" + country + ", state="
                + state + ", licensePlate=" + licensePlate + ", employer=" + employer + ", extId=" + extId
                + ", lastUpdate=" + lastUpdate + ", familyMembers=" + familyMembers + ", ipAddress=" + ipAddress
                + ", dummy1=" + dummy1 + ", dummy2=" + dummy2 + ", dummy3=" + dummy3 + "]";
    }
 }
