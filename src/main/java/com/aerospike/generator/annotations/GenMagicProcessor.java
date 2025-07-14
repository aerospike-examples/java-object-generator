package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.aerospike.generator.annotations.GenAddress.AddressPart;
import com.aerospike.generator.annotations.GenName.NameType;
import com.aerospike.generator.annotations.GenPhoneNumber.PhoneNumType;
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
        case UUID:
            return new GenUuidProcessor(null, fieldType, field);
        case DATE:
        case LOCALDATE:
        case LOCALDATETIME:
            return determineDateProcessorToUse(classWords, fieldWords, field);
        default:
        }
        return null;
    }
    
    private Processor determineStringProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("uuid"))) {
            return new GenUuidProcessor(null, fieldType, field);
        }
        else if (StringUtils.isLastWordOneOf(classWords, Set.of("id", "key'"))) {
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
        else if (StringUtils.matches(fieldWords, Set.of("account", "portfolio", "journal"), Set.of("name"))) {
            final GenNameProcessor nameProcessor = new GenNameProcessor(NameType.FULL, FieldType.STRING);
            return params -> StringUtils.makePossessive((String)nameProcessor.process(params)) + " " + fieldWords.get(0); 
        }
        else if (StringUtils.matches(fieldWords, Set.of("business", "bus", "company", "co"), Set.of("name")) ||
                (StringUtils.matches(classWords, Set.of("business", "company", "firm", "org", "organization")) &&
                        StringUtils.matches(fieldWords, Set.of("name")))) {
            final GenAddressProcessor cityProcessor = new GenAddressProcessor(AddressPart.CITY, FieldType.STRING);
            final GenOneOfProcessor businessProcessor = new GenOneOfProcessor(
                    "Hardware,Bikes,Seafood,Dog Walking,Tours,Bakery,Tea House,Books,Records,Pest Control,Personal Training,Phone Repair,"
                    + "Catering,Real Estate,Tattoo Parlor,Cleaning,Dry Cleaning,Coffee Shop,Florist,Hair Salon,Ice Creamery,Childcare,Computer Repair",
                    fieldType);
            return params -> (String)cityProcessor.process(params) + " " + businessProcessor.process(params); 
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
        else if (StringUtils.isLastWordOneOf(fieldWords, Set.of("city", "town", "parish", "village"))) {
            return new GenAddressProcessor(AddressPart.CITY, fieldType);
        }
        else if (StringUtils.isLastWordOneOf(fieldWords,  Set.of("country", "cntry"))) {
            return new GenAddressProcessor(AddressPart.COUNTRY, fieldType);
        }
        else if ((StringUtils.matches(fieldWords,  Set.of("zip", "post"),Set.of("code", "cd")) ||
                (StringUtils.matches(fieldWords, Set.of("zip", "zipcode", "postcode"))))) {
            return new GenAddressProcessor(AddressPart.ZIPCODE, fieldType);
        }
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("email")) ) {
            return new GenEmailProcessor(null, fieldType, field);
        }
        else if (StringUtils.matches(fieldWords, Set.of("phone", "fax", "mobile", "cell", "direct"), Set.of("number", "num", "no"))) {
            return new GenPhoneNumberProcessor(PhoneNumType.PHONE, fieldType, field);
        }
        return new GenStringProcessor(StringType.WORDS, 1, 5, -1, null, fieldType);
    }
    
    private Processor determineIntegerProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("amount", "amt", "weight"))) {
            return new GenNumberProcessor(5, 1000, fieldType);
        }
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("balance", "bal", "sum", "total", "volume"))) {
            return new GenNumberProcessor(500, 100000, fieldType);
        }
        return new GenNumberProcessor(-1000, 1000, fieldType);
    }

    private Processor getDateProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        if (StringUtils.matches(fieldWords, Set.of("dob", "birthday")) ||
                StringUtils.matches(fieldWords, Set.of("birth"), Set.of("day", "date")) ||
                StringUtils.matches(fieldWords, Set.of("name"), Set.of("day", "date")) ||
                StringUtils.matches(fieldWords, Set.of("date"), Set.of("of"), Set.of("birth")) 
                ) {
            return new GenDateProcessor("now-90y", "now-18y", percentNull, fieldType);
        }
        if (StringUtils.areLastTwoWordsOneOf(fieldWords, 
                    Set.of("open", "opened", "created", "create", "join", "joined", "start", "stared", "begin" ),
                    Set.of("date", "time", "timestamp")) ||
                StringUtils.areLastTwoWordsOneOf(fieldWords,
                    Set.of("date", "when", "time"),
                    Set.of("open", "opened", "created", "create", "join", "joined", "start", "stared", "begin" ))) {
            return new GenDateProcessor("now - 5y", "now-1d", percentNull, fieldType);
        }
        return null;
    }
    
    private Processor determineLongProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        Processor proc = getDateProcessorToUse(classWords, fieldWords, field);
        if (proc != null) {
            return proc;
        }
            
        return new GenNumberProcessor(-1000, 1000, fieldType);
    }
    
    private Processor determineDateProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        Processor proc = getDateProcessorToUse(classWords, fieldWords, field);
        if (proc != null) {
            return proc;
        }
        return new GenDateProcessor("now-10y", "now+10y", percentNull, fieldType);
    }

    private Processor determineBooleanProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        return new GenBooleanProcessor("true:4,false:1", fieldType);
    }
}
