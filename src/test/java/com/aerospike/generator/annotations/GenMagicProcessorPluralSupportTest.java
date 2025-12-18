package com.aerospike.generator.annotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.aerospike.generator.ValueCreator;

class GenMagicProcessorPluralSupportTest {
    
    @GenMagic
    public static class Car {
        public java.util.List<Integer> years;
        
        public Car() {}
    }
    
    @Test
    void carYearsPluralMatchesCarYearRule() throws Exception {
        ValueCreator<Car> creator = new ValueCreator<>(Car.class);
        Car car = new Car();
        
        creator.populate(car, Map.of("Key", 1L));
        
        assertNotNull(car.years);
        assertFalse(car.years.isEmpty());
        
        int currentYear = LocalDate.now().getYear();
        for (Integer year : car.years) {
            assertNotNull(year);
            assertTrue(year >= 1998 && year <= currentYear, "year out of range: " + year);
        }
    }
}


