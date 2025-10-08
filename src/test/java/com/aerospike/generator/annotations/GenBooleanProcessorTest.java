package com.aerospike.generator.annotations;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GenBooleanProcessorTest {

    @Test
    void testBasicGeneration() {
        GenBooleanProcessor processor = new GenBooleanProcessor(null, FieldType.BOOLEAN);
        processor.process(null);
    }
    
    @Test
    void testConstantValue() {
        GenBooleanProcessor processor = new GenBooleanProcessor("true", FieldType.BOOLEAN);
        for (int i=0; i < 100; i++) {
            assertTrue((boolean)processor.process(null));
        }
    }
    @Test
    void testWeights() {
        GenBooleanProcessor processor = new GenBooleanProcessor("true:99,false", FieldType.BOOLEAN);
        int trueCount = 0;
        for (int i=0; i < 100; i++) {
            if ((boolean) processor.process(null)) {
                trueCount++;
            }
        }
        System.out.println("True count: " + trueCount);
        assertTrue(trueCount > 90);
    }

}
