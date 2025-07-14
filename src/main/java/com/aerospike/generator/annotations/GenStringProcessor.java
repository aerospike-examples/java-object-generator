package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.aerospike.generator.annotations.GenString.StringType;
import com.github.javafaker.Faker;

public class GenStringProcessor implements Processor {

    private final StringType type;
    private final int minLength;
    private final int maxLength;
    private final int length;
    private final String format;
    
    private final Faker faker = Faker.instance();
    
    public GenStringProcessor(GenString genString, FieldType fieldType, Field field) {
        this(genString.type(), genString.minLength(), genString.maxLength(), genString.length(), genString.format(), fieldType);
    }
    
    public GenStringProcessor(StringType type, int minLength, int maxLength, int length, String format, FieldType fieldType) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.length = length;
        this.format = format == null ? null : format.trim();
        this.type = type;
        
        switch (type) {
        case CHARACTERS:
        case WORDS:
        case SENTENCES:
        case PARAGRAPHS:
            if (length < 0 && (minLength < 0 || maxLength < 0)) {
                throw new IllegalArgumentException("Either length must be specified or both minLength and maxLength");
            }
            if (length >= 0) {
                if (minLength >= 0 || maxLength >= 0) {
                    throw new IllegalArgumentException("Neither minLength nor maxLength can be specified if length is specified");
                }
            }
            else if (minLength < 0 || maxLength < 0) {
                throw new IllegalArgumentException("Both minLength and maxLength must be specified, or just specify length");
            }
            break;
            
        case LETTERIFY:
        case REGEXIFY:
            if (format.isEmpty()) {
                throw new IllegalArgumentException("format must be specified when using a type of " + type);
            }
            break;
        default:
            throw new IllegalArgumentException("type must be specified");
        }
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
    }

    private int getLengthToGenerate() {
        return Processor.getLengthToGenerate(length, minLength, maxLength);
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        switch (type) {
        case CHARACTERS:
            return faker.lorem().characters(getLengthToGenerate(), true, false);
        case WORDS:
            return String.join(" ", faker.lorem().words(getLengthToGenerate()));
        case SENTENCES:
            return String.join(" ", faker.lorem().sentences(getLengthToGenerate()));
        case PARAGRAPHS:
            return String.join(" ", faker.lorem().paragraphs(getLengthToGenerate()));
        case LETTERIFY:
            return faker.letterify(format);
        case REGEXIFY:
            return faker.regexify(format);
        default:
            return "String";
        }
    }
    
    public boolean supports(FieldType fieldType) {
        switch (fieldType) {
        case STRING:
            return true;
        default: 
            return false;
        }
    }
}
