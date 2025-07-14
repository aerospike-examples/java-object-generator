package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

public class GenUuidProcessor implements Processor {
    private final FieldType fieldType;

    public GenUuidProcessor(GenUuid ignored, FieldType fieldType, Field field) {
        this.fieldType = fieldType;
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        switch (this.fieldType) {
        case UUID:
            return UUID.randomUUID();
        case STRING:
            return UUID.randomUUID().toString();
        case BYTES:
            UUID uuid = UUID.randomUUID();
            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(uuid.getMostSignificantBits());
            bb.putLong(uuid.getLeastSignificantBits());
            return bb.array();
        default: return null;
        }
    }
    public boolean supports(FieldType fieldType) {
        switch (fieldType) {
        case STRING:
        case UUID:
        case BYTES:
            return true;
        default: 
            return false;
        }
    }
}
