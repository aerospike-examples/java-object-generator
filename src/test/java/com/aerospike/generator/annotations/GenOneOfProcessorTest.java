package com.aerospike.generator.annotations;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GenOneOfProcessorTest {

    @Test
    void testStringRange() {
        GenOneOfProcessor processor = new GenOneOfProcessor("Sue,Bob[1-3]:2", FieldType.STRING);
        for (int i = 0; i < 100; i++) {
            Object value = processor.process(null);
            assertTrue(value.equals("Sue") || value.equals("Bob1") || value.equals("Bob2") || value.equals("Bob3"));
        }
    }
    
    @Test
    void testNumericRange() {
        GenOneOfProcessor processor = new GenOneOfProcessor("10-20", FieldType.INTEGER);
        for (int i = 0; i < 100; i++) {
            Object value = processor.process(null);
            int intValue = (int)value;
            assertTrue(intValue >= 10 && intValue <= 20);
        }
    }

    @Test
    void testNumericRangeWithBrackets() {
        GenOneOfProcessor processor = new GenOneOfProcessor("[10-20]", FieldType.LONG);
        for (int i = 0; i < 100; i++) {
            Object value = processor.process(null);
            long intValue = (long)value;
            assertTrue(intValue >= 10 && intValue <= 20);
        }
    }

    @Test
    void testNumericRangeWitTextPrefix() {
        GenOneOfProcessor processor = new GenOneOfProcessor("10[10-20]", FieldType.LONG);
        for (int i = 0; i < 100; i++) {
            Object value = processor.process(null);
            long intValue = (long)value;
            assertTrue(intValue >= 1010 && intValue <= 1020);
        }
    }
}
