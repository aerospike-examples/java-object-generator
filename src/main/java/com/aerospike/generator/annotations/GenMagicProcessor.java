package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.aerospike.generator.annotations.GenAddress.AddressPart;
import com.aerospike.generator.annotations.GenName.NameType;
import com.aerospike.generator.annotations.GenString.StringType;

public class GenMagicProcessor implements Processor {

    private final int percentNull;
    private final Processor processorToUse;
    private final FieldType fieldType;
    private final Field field;
    
    public GenMagicProcessor(GenMagic genMagic, FieldType fieldType, Field field) {
        this(genMagic.percentNull(), fieldType, field);
    }
    public GenMagicProcessor(int percentNull, FieldType fieldType, Field field) {
        this.percentNull = percentNull;
        
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
        this.fieldType = fieldType;
        this.field = field;
        this.processorToUse = determineProcessorToUse(fieldType, field);
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        if (processorToUse == null) {
            return null;
        }
        return processorToUse.process(params);
    }
    
    @Override
    public boolean supports(FieldType fieldType) {
        return true;
    }
    
    private Processor determineProcessorToUse(FieldType fieldType, Field field) {
        Class<?> clazz = field.getDeclaringClass();
        
        List<String> classWords = StringUtils.breakIntoWords(clazz.getSimpleName());
        List<String> fieldWords = StringUtils.breakIntoWords(field.getName());
        switch (fieldType) {
        case STRING:
            return determineStringProcessorToUse(classWords, fieldWords, field);
        case INTEGER:
            return determineIntegerProcessorToUse(classWords, fieldWords, field);
        case LONG:
            return determineLongProcessorToUse(classWords, fieldWords, field);
        case BOOLEAN:
            return determineBooleanProcessorToUse(classWords, fieldWords, field);
        case OBJECT:
            return new GenObjectProcessor<>(null, percentNull, fieldType, field);
        default:
        }
        return null;
    }
    
    private Processor determineStringProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        if (StringUtils.isLastWordOneOf(classWords, Set.of("id", "key'"))) {
            String className = classWords.stream().map(s -> StringUtils.capitalise(s)).collect(Collectors.joining());
            return new GenExpressionProcessor("'" + className + "-'$Key", this.fieldType);
        }
        else if (StringUtils.matches(fieldWords, Set.of("first", "given", "christian"), Set.of("name"))) {
            return new GenNameProcessor(NameType.FIRST, fieldType);
        }
        else if (StringUtils.matches(fieldWords, Set.of("last", "family"), Set.of("name")) ||
                StringUtils.matches(fieldWords, Set.of("surname"))) {
            return new GenNameProcessor(NameType.LAST, fieldType);
        }
        else if (StringUtils.matches(fieldWords, Set.of("addr", "address"))) {
            return new GenAddressProcessor(AddressPart.FULL_ADDRESS, fieldType);
        }
        else if (StringUtils.matches(fieldWords,  Set.of("line1", "street"))) {
            return new GenAddressProcessor(AddressPart.STREET_ADDRESS, fieldType);
        }
        else if (StringUtils.matches(fieldWords,  Set.of("line2"))) {
            return new GenAddressProcessor(AddressPart.SECONDARY, fieldType);
        }
        else if ((StringUtils.matches(fieldWords,  Set.of("state")) || 
                StringUtils.matches(fieldWords, Set.of("state", "st"), Set.of("code", "cd"))) 
                && StringUtils.matches(classWords, Set.of("address", "addr"))) {
            return new GenAddressProcessor(AddressPart.STATE_ABBR, fieldType);
        }
        else if (StringUtils.matches(fieldWords,  Set.of("state", "st"), Set.of("name", "nm")) 
                && StringUtils.matches(classWords, Set.of("address", "addr"))) {
            return new GenAddressProcessor(AddressPart.STATE_ABBR, fieldType);
        }
        else if (StringUtils.isLastWordOneOf(fieldWords, Set.of("city", "town"))) {
            return new GenAddressProcessor(AddressPart.CITY, fieldType);
        }
        else if (StringUtils.isLastWordOneOf(fieldWords,  Set.of("country", "cntry"))) {
            return new GenAddressProcessor(AddressPart.COUNTRY, fieldType);
        }
        return new GenStringProcessor(StringType.WORDS, 1, 5, -1, null, fieldType);
    }
    
    private Processor determineIntegerProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        return new GenNumberProcessor(-1000, 1000, fieldType);
    }

    private Processor determineLongProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        if (StringUtils.matches(fieldWords, Set.of("dob", "birthday")) ||
                StringUtils.matches(fieldWords, Set.of("birth"), Set.of("day", "date")) ||
                StringUtils.matches(fieldWords, Set.of("name"), Set.of("day", "date")) ||
                StringUtils.matches(fieldWords, Set.of("date"), Set.of("of"), Set.of("birth")) 
                ) {
            return new GenDateProcessor("now-90y", "now-18y", percentNull, fieldType);
        }
            
        return new GenNumberProcessor(-1000, 1000, fieldType);
    }
    
    private Processor determineBooleanProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        return new GenBooleanProcessor("true:4,false:1", fieldType);
    }
}
