package com.aerospike.generator.example;

import com.aerospike.generator.ValueCreator;
import com.aerospike.generator.ValueCreatorCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PluralRunner {

    public static void main(String[] args) {
        ValueCreator<PluralExample> valueCreator = ValueCreatorCache.getInstance().get(PluralExample.class);

        System.out.println("Testing All Pluralization Patterns with @GenMagic:");
        System.out.println("=================================================");
        System.out.println("Singular fields should use intelligent patterns");
        System.out.println("Plural List/Set fields should use the SAME intelligent patterns");
        System.out.println("Random fields should fall back to Lorem ipsum");
        System.out.println();
        System.out.println("Pluralization patterns tested:");
        System.out.println("- 's' pattern: skill -> skills, department -> departments, language -> languages");
        System.out.println("- 'es' pattern: status -> statuses");
        System.out.println("- 'y' to 'ies' pattern: category -> categories, dependency -> dependencies, priority -> priorities, company -> companies");
        System.out.println();

        for (int i = 1; i <= 3; i++) {
            try {
                PluralExample example = new PluralExample();
                Map<String, Object> params = new HashMap<>();
                params.put("Key", (long) i);
                valueCreator.populate(example, params);

                System.out.println("=== Instance " + i + " ===");
                System.out.println("SINGULAR FIELDS:");
                System.out.println("  skill: " + example.getSkill());
                System.out.println("  status: " + example.getStatus());
                System.out.println("  category: " + example.getCategory());
                System.out.println("  department: " + example.getDepartment());
                System.out.println("  language: " + example.getLanguage());
                System.out.println("  dependency: " + example.getDependency());
                System.out.println("  priority: " + example.getPriority());
                System.out.println("  company: " + example.getCompany());
                System.out.println();
                System.out.println("PLURAL FIELDS (should match singular patterns):");
                System.out.println("  skills: " + example.getSkills());
                System.out.println("  statuses: " + example.getStatuses());
                System.out.println("  categories: " + example.getCategories());
                System.out.println("  departments: " + example.getDepartments());
                System.out.println("  languages: " + example.getLanguages());
                System.out.println("  dependencies: " + example.getDependencies());
                System.out.println("  priorities: " + example.getPriorities());
                System.out.println("  companies: " + example.getCompanies());
                System.out.println();
                System.out.println("RANDOM FIELDS (should use Lorem ipsum):");
                System.out.println("  randomList: " + example.getRandomList());
                System.out.println("  randomSet: " + example.getRandomSet());
                System.out.println("---");

            } catch (Exception e) {
                System.err.println("Error generating instance " + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Test complete!");
    }
}
