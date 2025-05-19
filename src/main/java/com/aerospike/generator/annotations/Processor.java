package com.aerospike.generator.annotations;

import java.util.Map;

public interface Processor {
    Object process(Map<String, Object> params);
    boolean supports(FieldType fieldType);
}
