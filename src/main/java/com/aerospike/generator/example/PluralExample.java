package com.aerospike.generator.example;

import com.aerospike.generator.annotations.GenMagic;

import java.util.List;
import java.util.Set;

@GenMagic
public class PluralExample {
    
    // Test singular field names
    private String skill;
    private String status;
    private String category;
    private String department;
    private String language;
    private String dependency;
    private String priority;
    private String company;
    
    // Test plural field names with different patterns
    private List<String> skills;        // skill -> skills (s)
    private Set<String> statuses;      // status -> statuses (es)
    private List<String> categories;   // category -> categories (y -> ies)
    private Set<String> departments;   // department -> departments (s)
    private List<String> languages;     // language -> languages (s)
    private Set<String> dependencies; // dependency -> dependencies (y -> ies)
    private List<String> priorities;   // priority -> priorities (y -> ies)
    private Set<String> companies;    // company -> companies (y -> ies)
    
    // Test fields that should NOT match (no pattern)
    private List<String> randomList;
    private Set<String> randomSet;
    
    // Getters for testing
    public String getSkill() { return skill; }
    public String getStatus() { return status; }
    public String getCategory() { return category; }
    public String getDepartment() { return department; }
    public String getLanguage() { return language; }
    public String getDependency() { return dependency; }
    public String getPriority() { return priority; }
    public String getCompany() { return company; }
    
    public List<String> getSkills() { return skills; }
    public Set<String> getStatuses() { return statuses; }
    public List<String> getCategories() { return categories; }
    public Set<String> getDepartments() { return departments; }
    public List<String> getLanguages() { return languages; }
    public Set<String> getDependencies() { return dependencies; }
    public List<String> getPriorities() { return priorities; }
    public Set<String> getCompanies() { return companies; }
    
    public List<String> getRandomList() { return randomList; }
    public Set<String> getRandomSet() { return randomSet; }
}
