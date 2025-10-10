package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.aerospike.generator.annotations.GenAddress.AddressPart;
import com.aerospike.generator.annotations.GenListProcessor;
import com.aerospike.generator.annotations.GenName.NameType;
import com.aerospike.generator.annotations.GenPhoneNumber.PhoneNumType;
import com.aerospike.generator.annotations.GenSetProcessor;
import com.aerospike.generator.annotations.GenString;
import com.aerospike.generator.annotations.GenString.StringType;

/**
 * Intelligent processor that automatically generates meaningful test data based on field names.
 * 
 * This processor analyzes field names and class names to determine the most appropriate
 * data generator for each field. It supports a wide range of field name patterns including:
 * 
 * <h3>Financial Fields</h3>
 * <ul>
 *   <li>Pricing: price, cost, fee, charge, rate, value, worth</li>
 *   <li>Income: salary, wage, income, revenue, profit, loss</li>
 *   <li>Budget: budget, limit, quota, allowance</li>
 *   <li>Percentages: discount, tax, tip, commission</li>
 *   <li>Investment: shares, stocks, equity, dividend, yield</li>
 *   <li>Credit: credit, debt, loan, mortgage, interest</li>
 * </ul>
 * 
 * <h3>Web/Technical Fields</h3>
 * <ul>
 *   <li>URLs: url, link, href, website, homepage</li>
 *   <li>Domains: domain, subdomain</li>
 *   <li>APIs: endpoint, api, path, route</li>
 *   <li>Security: code, token, key, secret, password, hash</li>
 *   <li>Versions: version, build, release</li>
 *   <li>Configuration: config, setting, option, parameter</li>
 * </ul>
 * 
 * <h3>Status/State Fields</h3>
 * <ul>
 *   <li>Status: status, state, condition, phase, stage</li>
 *   <li>Priority: level, grade, rank, priority</li>
 *   <li>Quality: quality, performance, efficiency, reliability</li>
 *   <li>Access: availability, access, permission, visibility</li>
 * </ul>
 * 
 * <h3>Geographic Fields</h3>
 * <ul>
 *   <li>Coordinates: latitude, lat, longitude, lng, lon</li>
 *   <li>Regions: region, area, zone, district</li>
 *   <li>Time zones: timezone, tz</li>
 *   <li>Continents: continent</li>
 * </ul>
 * 
 * <h3>Professional Fields</h3>
 * <ul>
 *   <li>Job titles: title, position, role, job</li>
 *   <li>Departments: department, division, team</li>
 *   <li>Education: education, degree, qualification, certification</li>
 *   <li>Skills: experience, skill, expertise, proficiency</li>
 *   <li>Company types: company, organization, firm, corporation</li>
 * </ul>
 * 
 * <h3>Product/Service Fields</h3>
 * <ul>
 *   <li>Categories: category, type, kind, class</li>
 *   <li>Brands: brand, manufacturer, vendor</li>
 *   <li>Condition: condition, quality, grade, rating</li>
 *   <li>Sizes: size, dimension, measurement</li>
 * </ul>
 * 
 * <h3>Measurement Fields</h3>
 * <ul>
 *   <li>Dimensions: width, height, length, depth</li>
 *   <li>Temperature: temperature, temp</li>
 *   <li>Speed: speed, velocity</li>
 *   <li>Weight: weight, mass</li>
 *   <li>Volume: volume, capacity</li>
 *   <li>Pressure: pressure, force, strength</li>
 * </ul>
 * 
 * <h3>System/Technical Fields</h3>
 * <ul>
 *   <li>Infrastructure: host, server, node, instance</li>
 *   <li>Databases: database, db</li>
 *   <li>Languages: language, lang</li>
 *   <li>Frameworks: framework, library, tool, technology</li>
 *   <li>Operating systems: os, operating, platform</li>
 *   <li>Cloud: cloud, deployment, environment</li>
 * </ul>
 * 
 * <h3>Integer Field Patterns</h3>
 * <ul>
 *   <li>Financial: amount, balance, total, volume</li>
 *   <li>Time/Age: age, years, months, days, duration</li>
 *   <li>Quantities: count, quantity, number, total</li>
 *   <li>Identifiers: id, index, position</li>
 *   <li>Ratings: rating, score, grade, points</li>
 *   <li>Percentages: percent, percentage, rate, ratio</li>
 *   <li>Technical: port, version, threads, processes</li>
 * </ul>
 * 
 * The processor uses intelligent pattern matching to provide contextually appropriate
 * data generators, making test data generation more meaningful and realistic.
 */
public class GenMagicProcessor implements Processor {

    private final int percentNull;
    private final Processor processorToUse;
    private final FieldType fieldType;
    
    public GenMagicProcessor(GenMagic genMagic, FieldType fieldType, Field field) {
        this(genMagic.percentNull(), fieldType, field);
    }
    public GenMagicProcessor(int percentNull, FieldType fieldType, Field field) {
        this.percentNull = percentNull;
        
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
        this.fieldType = fieldType;
//        this.field = field;
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
    
    /**
     * Determines the most appropriate processor for a field based on its name and type.
     * This is the main intelligence of the GenMagicProcessor - it analyzes field names
     * to provide contextually appropriate data generators.
     * 
     * @param fieldType The type of the field (STRING, INTEGER, etc.)
     * @param field The field being processed
     * @return The most appropriate processor for this field
     */
    private Processor determineProcessorToUse(FieldType fieldType, Field field) {
        Class<?> clazz = field.getDeclaringClass();
        
        // Break down class and field names into words for pattern matching
        List<String> classWords = StringUtils.breakIntoWords(clazz.getSimpleName());
        List<String> fieldWords = StringUtils.breakIntoWords(field.getName());
        switch (fieldType) {
        case STRING:
            return determineStringProcessorToUse(classWords, fieldWords, field);
        case INTEGER:
        case LONG:
        case DOUBLE:
        case FLOAT:
            return determineNumberProcessorToUse(classWords, fieldWords, field);
        case BOOLEAN:
            return determineBooleanProcessorToUse(classWords, fieldWords, field);
        case OBJECT:
            return new GenObjectProcessor<>(null, percentNull, fieldType, field);
        case LIST:
            return determineListProcessorToUse(classWords, fieldWords, field);
        case SET:
            return determineSetProcessorToUse(classWords, fieldWords, field);
        case UUID:
            return new GenUuidProcessor(null, fieldType, field);
        case ENUM:
            return new GenEnumProcessor(null, fieldType, field);
        case DATE:
        case LOCALDATE:
        case LOCALDATETIME:
        case LOCALTIME:
        case INSTANT:
            return determineDateProcessorToUse(classWords, fieldWords, field);
        default:
        }
        return null;
    }
    
    /**
     * Determines the appropriate processor for string fields based on field name patterns.
     * This method handles the most complex pattern matching as strings can represent
     * many different types of data (names, addresses, URLs, etc.).
     * 
     * @param classWords List of words from the class name
     * @param fieldWords List of words from the field name  
     * @param field The field being processed
     * @return Processor instance for generating string values
     */
    private Processor determineStringProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("uuid"))) {
            return new GenUuidProcessor(null, fieldType, field);
        }
        // ===== CURRENCY FIELDS =====
        // Currency fields - must be checked early to avoid conflicts with other patterns
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("currency", "curr"))) {
            return new GenOneOfProcessor("AUD,USD,EUR,GBP,CAD,JPY,CHF,NZD,SGD,HKD,CNY,INR,BRL,MXN", fieldType);
        }
        else if (StringUtils.isLastWordOneOf(classWords, Set.of("id", "key'"))) {
            String className = classWords.stream().map(s -> StringUtils.capitalise(s)).collect(Collectors.joining());
            return new GenExpressionProcessor("'" + className + "-'$Key", this.fieldType);
        }
        else if (StringUtils.matches(fieldWords, Set.of("first", "given", "christian", "preferred"), Set.of("name"))) {
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
        else if (StringUtils.matches(fieldWords, Set.of("phone", "fax", "mobile", "cell", "direct"), Set.of("number", "num", "no")) ||
                StringUtils.matches(fieldWords, Set.of("phone", "fax", "mobile", "cell"))) {
            return new GenPhoneNumberProcessor(PhoneNumType.PHONE, fieldType, field);
        }
        // Count and quantity fields - typical quantities
        else if (StringUtils.isLastWordOneOf(fieldWords, Set.of("count", "quantity", "qty", "number", "num", "total"))) {
            return new GenExpressionProcessor("'$' & @GenNumber(start=10, end=10000)", fieldType);
        }

        // ===== FINANCIAL/CURRENCY FIELDS =====
        // Basic pricing and cost fields - typical e-commerce/product pricing
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("price", "cost", "fee", "charge", "rate", "value", "worth"))) {
            return new GenExpressionProcessor("'$' & @GenNumber(start=10, end=10000)", fieldType);
        }
        // Salary and income fields - realistic salary ranges
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("salary", "wage", "income", "revenue", "profit", "loss"))) {
            return new GenExpressionProcessor("'$' & @GenNumber(start=30000, end=200000)", fieldType);
        }
        // Budget and allocation fields - project/team budgets
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("budget", "limit", "quota", "allowance"))) {
            return new GenExpressionProcessor("'$' & @GenNumber(start=1000, end=50000)", fieldType);
        }
        // Percentage-based financial fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("discount", "tax", "tip", "commission"))) {
            return new GenExpressionProcessor("@GenNumber(start=1, end=50) & '%'", fieldType);
        }
        // Investment and trading fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("shares", "stocks", "equity", "dividend", "yield"))) {
            return new GenExpressionProcessor("'$' & @GenNumber(start=1, end=10000)", fieldType);
        }
        // Credit and loan fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("credit", "debt", "loan", "mortgage", "interest"))) {
            return new GenExpressionProcessor("'$' & @GenNumber(start=1000, end=1000000)", fieldType);
        }
        // ===== URL/WEB FIELDS =====
        // Full URL fields - generates realistic web URLs
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("url", "link", "href", "website", "homepage"))) {
            return new GenExpressionProcessor("'https://' & @GenString(length=8, type=CHARACTERS) & '.com'", fieldType);
        }
        // Domain fields - generates domain names without protocol
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("domain", "subdomain"))) {
            return new GenExpressionProcessor("@GenString(length=8, type=CHARACTERS) & '.com'", fieldType);
        }
        // API endpoint fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("endpoint", "api", "path", "route"))) {
            return new GenExpressionProcessor("'/api/v' & @GenNumber(start=1, end=3) & '/' & @GenString(length=6, type=CHARACTERS)", fieldType);
        }
        // Social media profile fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("profile", "avatar", "picture", "image"))) {
            return new GenExpressionProcessor("'https://example.com/' & @GenString(length=6, type=CHARACTERS) & '.jpg'", fieldType);
        }
        // ===== CODE/TECHNICAL FIELDS =====
        // Security and authentication fields - generates secure hex strings
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("code", "token", "key", "secret", "password", "hash"))) {
            return new GenHexStringProcessor(8, 16, -1, null, fieldType);
        }
        // Version fields - generates semantic version numbers
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("version", "build", "release"))) {
            return new GenExpressionProcessor("@GenNumber(start=1, end=9) & '.' & @GenNumber(start=0, end=9) & '.' & @GenNumber(start=0, end=99)", fieldType);
        }
        // Configuration and environment fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("config", "setting", "option", "parameter"))) {
            return new GenOneOfProcessor("development,staging,production,test,local,remote", fieldType);
        }
        // File and path fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("file", "path", "directory", "folder"))) {
            return new GenExpressionProcessor("'/home/user/' & @GenString(length=6, type=CHARACTERS) & '.txt'", fieldType);
        }
        // ===== STATUS/STATE FIELDS =====
        // General status fields - common workflow states
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("status", "state", "condition", "phase", "stage"))) {
            return new GenOneOfProcessor("active,inactive,pending,approved,rejected,completed,failed,cancelled", fieldType);
        }
        // Priority and level fields - urgency classifications
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("level", "grade", "rank", "priority"))) {
            return new GenOneOfProcessor("low,medium,high,critical,urgent", fieldType);
        }
        // Quality and performance fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("quality", "performance", "efficiency", "reliability"))) {
            return new GenOneOfProcessor("excellent,good,average,poor,terrible", fieldType);
        }
        // Availability and access fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("availability", "access", "permission", "visibility"))) {
            return new GenOneOfProcessor("public,private,restricted,confidential,secret", fieldType);
        }
        // ===== GEOGRAPHIC FIELDS =====
        // Coordinate fields - realistic latitude/longitude ranges with decimal precision
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("latitude", "lat"))) {
            return new GenExpressionProcessor("@GenNumber(start=-90, end=90) & '.' & @GenNumber(start=0, end=999999) & '°'", fieldType);
        }
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("longitude", "lng", "lon"))) {
            return new GenExpressionProcessor("@GenNumber(start=-180, end=180) & '.' & @GenNumber(start=0, end=999999) & '°'", fieldType);
        }
        // Administrative region fields - uses address processor for state codes
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("region", "area", "zone", "district"))) {
            return new GenAddressProcessor(AddressPart.STATE_ABBR, fieldType);
        }
        // Time zone fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("timezone", "tz"))) {
            return new GenOneOfProcessor("UTC,EST,PST,MST,CST,EDT,PDT,MDT,CDT,AEST,AEDT,NZST,NZDT", fieldType);
        }
        // Country and continent fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("continent"))) {
            return new GenOneOfProcessor("North America,South America,Europe,Asia,Africa,Australia,Antarctica", fieldType);
        }
        // ===== SOCIAL/PROFESSIONAL FIELDS =====
        // Job title fields - common professional roles
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("title", "position", "role", "job"))) {
            return new GenOneOfProcessor("Manager,Developer,Analyst,Designer,Consultant,Specialist,Coordinator,Director", fieldType);
        }
        // Organizational unit fields - typical company departments
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("department", "division", "team"))) {
            return new GenOneOfProcessor("Engineering,Marketing,Sales,HR,Finance,Operations,Support,Research", fieldType);
        }
        // Education and skill fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("education", "degree", "qualification", "certification"))) {
            return new GenOneOfProcessor("Bachelor's,Master's,PhD,Associate,Certificate,Diploma", fieldType);
        }
        // Experience and skill level fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("experience", "skill", "expertise", "proficiency"))) {
            return new GenOneOfProcessor("Beginner,Intermediate,Advanced,Expert,Master", fieldType);
        }
        // Company size and type fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("company", "organization", "firm", "corporation"))) {
            return new GenOneOfProcessor("Startup,Small Business,Medium Enterprise,Large Corporation,Multinational", fieldType);
        }
        // ===== PRODUCT/SERVICE FIELDS =====
        // Product category fields - common e-commerce categories
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("category", "type", "kind", "class"))) {
            return new GenOneOfProcessor("Electronics,Clothing,Books,Food,Home,Automotive,Sports,Health", fieldType);
        }
        // Brand and manufacturer fields - includes Aerospike and major tech brands
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("brand", "manufacturer", "vendor"))) {
            return new GenOneOfProcessor("Aerospike,Apple,Samsung,Microsoft,Google,Amazon,Nike,Adidas,Sony,Intel,AMD", fieldType);
        }
        // Product condition and quality fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("condition", "quality", "grade", "rating"))) {
            return new GenOneOfProcessor("New,Like New,Good,Fair,Poor,Excellent,Outstanding", fieldType);
        }
        // Product size and dimension fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("size", "dimension", "measurement"))) {
            return new GenOneOfProcessor("XS,S,M,L,XL,XXL,Small,Medium,Large,Extra Large", fieldType);
        }
        // ===== MEASUREMENT FIELDS =====
        // Physical dimension fields - typical measurement ranges
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("width", "height", "length", "depth"))) {
            return new GenExpressionProcessor("@GenNumber(start=1, end=1000) & ' cm'", fieldType);
        }
        // Temperature fields - realistic temperature ranges in Celsius
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("temperature", "temp"))) {
            return new GenExpressionProcessor("@GenNumber(start=-50, end=150) & '°C'", fieldType);
        }
        // Speed and velocity fields - typical speed ranges in km/h
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("speed", "velocity"))) {
            return new GenExpressionProcessor("@GenNumber(start=1, end=200) & ' km/h'", fieldType);
        }
        // Weight and mass fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("weight", "mass"))) {
            return new GenExpressionProcessor("@GenNumber(start=1, end=1000) & ' kg'", fieldType);
        }
        // Volume and capacity fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("volume", "capacity"))) {
            return new GenExpressionProcessor("@GenNumber(start=1, end=10000) & ' L'", fieldType);
        }
        // Pressure and force fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("pressure", "force", "strength"))) {
            return new GenExpressionProcessor("@GenNumber(start=1, end=1000) & ' Pa'", fieldType);
        }
        // ===== SYSTEM/TECHNICAL FIELDS =====
        // Infrastructure fields - generates server/instance names
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("host", "server", "node", "instance"))) {
            return new GenExpressionProcessor("'server-' & @GenString(length=4, type=CHARACTERS) & '-' & @GenNumber(start=1, end=999)", fieldType);
        }
        // Database fields - includes Aerospike and modern databases
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("database", "db"))) {
            return new GenOneOfProcessor("Aerospike,MySQL,PostgreSQL,MongoDB,YugabyteDB,Elasticsearch,Oracle,SQLite", fieldType);
        }
        // Programming language fields - modern development languages
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("language", "lang"))) {
            return new GenOneOfProcessor("Java,Python,JavaScript,TypeScript,C#,Go,Rust,PHP,Ruby,Scala", fieldType);
        }
        // Framework and technology fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("framework", "library", "tool", "technology"))) {
            return new GenOneOfProcessor("Spring,React,Angular,Vue,Django,Flask,Express,Laravel,Rails,ASP.NET", fieldType);
        }
        // Operating system fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("os", "operating", "platform"))) {
            return new GenOneOfProcessor("Windows,Linux,macOS,Ubuntu,CentOS,Debian,RedHat,FreeBSD", fieldType);
        }
        // Cloud and deployment fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("cloud", "deployment", "environment"))) {
            return new GenOneOfProcessor("AWS,Azure,GCP,Docker,Kubernetes,Heroku,DigitalOcean,Linode", fieldType);
        }
        return new GenStringProcessor(StringType.WORDS, 1, 5, -1, null, fieldType);
    }
    
    /**
     * Determines the appropriate processor for numeric fields (INTEGER, LONG, DOUBLE, FLOAT) based on field name patterns.
     * This method analyzes field names to generate contextually appropriate numeric values.
     * The GenNumberProcessor handles the specific numeric type internally.
     * 
     * @param classWords List of words from the class name
     * @param fieldWords List of words from the field name
     * @param field The field being processed
     * @return Processor instance for generating numeric values
     */
    private Processor determineNumberProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        // ===== GENERIC NUMBER FIELDS =====
        // Fields ending in Number/Num - generate random positive numbers
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("number", "num", "id", "count", "quantity", "total", "sum"))) {
            return new GenNumberProcessor(1, 1000000, fieldType);
        }
        // ===== FINANCIAL FIELDS =====
        // Basic amount fields - typical product/service pricing
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("amount", "amt", "weight"))) {
            return new GenNumberProcessor(5, 1000, fieldType);
        }
        // Price and cost fields - typical e-commerce/product pricing (rounded to nearest $1)
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("price", "cost", "fee", "charge", "rate", "value", "worth"))) {
            return new GenNumberProcessor(10, 10000, 1, fieldType);
        }
        // Salary and income fields - realistic salary ranges (rounded to nearest $500)
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("salary", "wage", "income", "revenue", "profit", "loss"))) {
            return new GenNumberProcessor(30000, 200000, 500, fieldType);
        }
        // Budget and allocation fields - project/team budgets (rounded to nearest $100)
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("budget", "limit", "quota", "allowance"))) {
            return new GenNumberProcessor(1000, 50000, 100, fieldType);
        }
        // Percentage-based financial fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("discount", "tax", "tip", "commission"))) {
            return new GenNumberProcessor(1, 50, fieldType);
        }
        // Investment and trading fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("shares", "stocks", "equity", "dividend", "yield"))) {
            return new GenNumberProcessor(1, 10000, fieldType);
        }
        // Credit and loan fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("credit", "debt", "loan", "mortgage", "interest"))) {
            return new GenNumberProcessor(1000, 1000000, fieldType);
        }
        // Balance and total fields - larger financial amounts
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("balance", "bal", "sum", "total", "volume"))) {
            return new GenNumberProcessor(500, 100000, fieldType);
        }
        
        // ===== COORDINATE FIELDS =====
        // Latitude fields - realistic latitude ranges (-90 to 90)
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("latitude", "lat"))) {
            return new GenNumberProcessor(-90, 90, fieldType);
        }
        // Longitude fields - realistic longitude ranges (-180 to 180)
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("longitude", "lng", "lon"))) {
            return new GenNumberProcessor(-180, 180, fieldType);
        }
        
        // ===== TIME/AGE FIELDS =====
        // Age-related fields - realistic age ranges
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("age", "years", "months", "days"))) {
            return new GenNumberProcessor(1, 100, fieldType);
        }
        // Duration/Time fields - time intervals in seconds
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("duration", "length", "period", "interval", "timeout"))) {
            return new GenNumberProcessor(1, 3600, fieldType); // 1 second to 1 hour
        }
        
        // ===== QUANTITY/COUNT FIELDS =====
        // Count and quantity fields - typical quantities
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("count", "quantity", "qty", "number", "num", "total"))) {
            return new GenNumberProcessor(0, 1000, fieldType);
        }
        // ID and index fields - unique identifier ranges
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("id", "index", "idx", "position", "pos"))) {
            return new GenNumberProcessor(1, 10000, fieldType);
        }
        
        // ===== RATING/SCORE FIELDS =====
        // Rating and score fields - typical rating scales
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("rating", "score", "grade", "points"))) {
            return new GenNumberProcessor(1, 10, fieldType);
        }
        // Percentage fields - percentage values
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("percent", "percentage", "rate", "ratio"))) {
            return new GenNumberProcessor(0, 100, fieldType);
        }
        
        // ===== MEASUREMENT FIELDS =====
        // Temperature fields - realistic temperature ranges
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("temperature", "temp"))) {
            return new GenNumberProcessor(-50, 150, fieldType);
        }
        // Speed and velocity fields - decimal speed values
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("speed", "velocity"))) {
            return new GenNumberProcessor(0, 200, fieldType);
        }
        // Weight and mass fields - decimal weight values
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("weight", "mass"))) {
            return new GenNumberProcessor(0, 1000, fieldType);
        }
        // Volume and capacity fields - decimal volume values
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("volume", "capacity"))) {
            return new GenNumberProcessor(0, 10000, fieldType);
        }
        // Pressure and force fields - decimal pressure values
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("pressure", "force", "strength"))) {
            return new GenNumberProcessor(0, 1000, fieldType);
        }
        // Distance and dimension fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("distance", "width", "height", "length", "depth"))) {
            return new GenNumberProcessor(0, 1000, fieldType);
        }
        // Time duration fields - decimal time values
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("time", "interval"))) {
            return new GenNumberProcessor(0, 3600, fieldType);
        }
        
        // ===== ADDITIONAL PATTERNS =====
        // Port and network fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("port", "portnumber"))) {
            return new GenNumberProcessor(1024, 65535, fieldType);
        }
        // Version number fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("version", "major", "minor", "patch"))) {
            return new GenNumberProcessor(1, 99, fieldType);
        }
        // Thread and process fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("threads", "processes", "workers"))) {
            return new GenNumberProcessor(1, 100, fieldType);
        }
        
        // Default fallback - general numeric range
        return new GenNumberProcessor(-1000, 1000, fieldType);
    }


    /**
     * Determines the appropriate date processor based on field name patterns.
     * This method analyzes field names to provide contextually appropriate date ranges.
     * 
     * @param classWords List of words from the class name
     * @param fieldWords List of words from the field name
     * @param field The field being processed
     * @return Processor instance for generating date values, or null if no pattern matches
     */
    private Processor getDateProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        // ===== BIRTH DATE FIELDS =====
        // Birth date and age-related fields - 18-90 years ago
        if (StringUtils.matches(fieldWords, Set.of("dob", "birthday")) ||
                StringUtils.matches(fieldWords, Set.of("birth"), Set.of("day", "date")) ||
                StringUtils.matches(fieldWords, Set.of("name"), Set.of("day", "date")) ||
                StringUtils.matches(fieldWords, Set.of("date"), Set.of("of"), Set.of("birth"))) {
            return new GenDateProcessor("now-90y", "now-18y", percentNull, fieldType);
        }
        
        // ===== CREATION/REGISTRATION FIELDS =====
        // Account creation, registration, and start dates - 5 years ago to 1 day ago
        if (StringUtils.areLastTwoWordsOneOf(fieldWords, 
                    Set.of("open", "opened", "created", "create", "join", "joined", "start", "stared", "begin"),
                    Set.of("date", "time", "timestamp")) ||
                StringUtils.areLastTwoWordsOneOf(fieldWords,
                    Set.of("date", "when", "time"),
                    Set.of("open", "opened", "created", "create", "join", "joined", "start", "stared", "begin"))) {
            return new GenDateProcessor("now-5y", "now-1d", percentNull, fieldType);
        }
        
        // ===== RECENT ACTIVITY FIELDS =====
        // Recent activity, login, and update dates - 1 year ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("last", "recent", "updated", "modified", "accessed", "viewed", "logged"))) {
            return new GenDateProcessor("now-1y", "now", percentNull, fieldType);
        }
        
        // ===== FUTURE/SCHEDULED FIELDS =====
        // Future dates, appointments, and scheduled events - now to 1 year ahead
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("scheduled", "appointment", "meeting", "event", "deadline", "due", "expiry", "expiration"))) {
            return new GenDateProcessor("now", "now+1y", percentNull, fieldType);
        }
        
        // ===== EMPLOYMENT FIELDS =====
        // Employment start and end dates - 20 years ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("hired", "employed", "started", "joined", "terminated", "ended", "resigned"))) {
            return new GenDateProcessor("now-20y", "now", percentNull, fieldType);
        }
        
        // ===== ACADEMIC FIELDS =====
        // Education and graduation dates - 10 years ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("graduated", "enrolled", "admitted", "completed", "education", "academic"))) {
            return new GenDateProcessor("now-10y", "now", percentNull, fieldType);
        }
        
        // ===== LEGAL/COMPLIANCE FIELDS =====
        // Legal dates, compliance, and certification dates - 5 years ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("certified", "licensed", "approved", "compliant", "legal", "contract", "agreement"))) {
            return new GenDateProcessor("now-5y", "now", percentNull, fieldType);
        }
        
        // ===== FINANCIAL FIELDS =====
        // Payment, billing, and financial transaction dates - 2 years ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("paid", "billed", "charged", "transacted", "payment", "invoice", "receipt"))) {
            return new GenDateProcessor("now-2y", "now", percentNull, fieldType);
        }
        
        // ===== MAINTENANCE/SUPPORT FIELDS =====
        // Maintenance, support, and service dates - 1 year ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("maintained", "serviced", "repaired", "updated", "patched", "fixed"))) {
            return new GenDateProcessor("now-1y", "now", percentNull, fieldType);
        }
        
        // ===== MEDICAL/HEALTH FIELDS =====
        // Medical appointments, checkups, and health-related dates - 2 years ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("appointment", "checkup", "exam", "visit", "treatment", "vaccinated", "tested"))) {
            return new GenDateProcessor("now-2y", "now", percentNull, fieldType);
        }
        
        // ===== TRAVEL FIELDS =====
        // Travel and trip dates - 1 year ago to 1 year ahead
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("travel", "trip", "departure", "arrival", "booking", "reservation"))) {
            return new GenDateProcessor("now-1y", "now+1y", percentNull, fieldType);
        }
        
        // ===== SUBSCRIPTION FIELDS =====
        // Subscription and membership dates - 1 year ago to 1 year ahead
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("subscribed", "membership", "renewal", "expires", "valid", "active"))) {
            return new GenDateProcessor("now-1y", "now+1y", percentNull, fieldType);
        }
        
        // ===== SYSTEM/LOG FIELDS =====
        // System logs, audit trails, and technical dates - 1 month ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("logged", "audit", "system", "technical", "debug", "trace"))) {
            return new GenDateProcessor("now-1M", "now", percentNull, fieldType);
        }
        
        // ===== NOTIFICATION FIELDS =====
        // Notification and communication dates - 1 week ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("notified", "sent", "received", "delivered", "read", "opened"))) {
            return new GenDateProcessor("now-1w", "now", percentNull, fieldType);
        }
        
        return null;
    }
    
    private Processor determineDateProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        Processor proc = getDateProcessorToUse(classWords, fieldWords, field);
        if (proc != null) {
            return proc;
        }
        return new GenDateProcessor("now-10y", "now+10y", percentNull, fieldType);
    }

    /**
     * Determines the appropriate processor for boolean fields based on field name patterns.
     * This method analyzes field names to provide contextually appropriate true/false ratios.
     * 
     * @param classWords List of words from the class name
     * @param fieldWords List of words from the field name
     * @param field The field being processed
     * @return Processor instance for generating boolean values
     */
    private Processor determineBooleanProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        // ===== REGISTRATION/STATUS FIELDS (High True Ratio) =====
        // Registration and membership fields - mostly true
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("registered", "enrolled", "member", "subscribed", "active", "verified", "confirmed"))) {
            return new GenBooleanProcessor("true:8,false:1", fieldType);
        }
        // Account and user status fields - mostly true
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("account", "user", "profile", "session"))) {
            return new GenBooleanProcessor("true:7,false:1", fieldType);
        }
        
        // ===== RARE/DEMOGRAPHIC FIELDS (High False Ratio) =====
        // Demographic and rare characteristic fields - mostly false
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("indigenous", "aboriginal", "torres", "strait", "islander", "native", "minority"))) {
            return new GenBooleanProcessor("true:1,false:19", fieldType);
        }
        // Disability and special needs fields - mostly false
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("disabled", "handicapped", "impaired", "special", "needs"))) {
            return new GenBooleanProcessor("true:1,false:9", fieldType);
        }
        // Veteran and military fields - mostly false
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("veteran", "military", "served", "armed", "forces"))) {
            return new GenBooleanProcessor("true:1,false:9", fieldType);
        }
        
        // ===== SECURITY/PRIVACY FIELDS (Balanced) =====
        // Security and privacy fields - balanced ratio
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("secure", "private", "confidential", "sensitive", "encrypted", "protected"))) {
            return new GenBooleanProcessor("true:1,false:1", fieldType);
        }
        // Permission and access fields - balanced ratio
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("permission", "access", "authorized", "allowed", "granted"))) {
            return new GenBooleanProcessor("true:1,false:1", fieldType);
        }
        
        // ===== FEATURE/OPTION FIELDS (Moderate True Ratio) =====
        // Feature and option fields - moderately true
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("feature", "option", "setting", "preference", "enabled", "available"))) {
            return new GenBooleanProcessor("true:3,false:1", fieldType);
        }
        // Notification and communication fields - moderately true
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("notification", "email", "sms", "alert", "reminder", "newsletter"))) {
            return new GenBooleanProcessor("true:3,false:1", fieldType);
        }
        
        // ===== ERROR/EXCEPTION FIELDS (High False Ratio) =====
        // Error and exception fields - mostly false
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("error", "exception", "failed", "invalid", "corrupted", "broken"))) {
            return new GenBooleanProcessor("true:1,false:9", fieldType);
        }
        // Warning and alert fields - mostly false
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("warning", "alert", "critical", "urgent", "emergency"))) {
            return new GenBooleanProcessor("true:1,false:9", fieldType);
        }
        
        // ===== COMPLETION/SUCCESS FIELDS (High True Ratio) =====
        // Completion and success fields - mostly true
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("completed", "finished", "done", "successful", "passed", "approved"))) {
            return new GenBooleanProcessor("true:8,false:1", fieldType);
        }
        // Payment and transaction fields - mostly true
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("paid", "billed", "charged", "processed", "transacted"))) {
            return new GenBooleanProcessor("true:7,false:1", fieldType);
        }
        
        // ===== DEFAULT PATTERN =====
        // Default balanced ratio for unknown patterns
        return new GenBooleanProcessor("true:1,false:1", fieldType);
    }
    
    /**
     * Determines the appropriate GenOneOfProcessor options for a given field.
     * This method analyzes field names to provide contextually appropriate options
     * that can be used for both single String fields and List/Set<String> fields.
     * 
     * @param classWords List of words from the class name
     * @param fieldWords List of words from the field name
     * @return String containing comma-separated options, or null if no pattern matches
     */
    public static String getOneOfOptions(List<String> classWords, List<String> fieldWords) {
        // ===== CONFIGURATION FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("config", "setting", "option", "parameter"))) {
            return "development,staging,production,test,local,remote";
        }
        
        // ===== STATUS/STATE FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("status", "state", "condition", "phase", "stage"))) {
            return "active,inactive,pending,approved,rejected,completed,failed,cancelled";
        }
        
        // ===== PRIORITY/LEVEL FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("level", "grade", "rank", "priority"))) {
            return "low,medium,high,critical,urgent";
        }
        
        // ===== QUALITY FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("quality", "performance", "efficiency", "reliability"))) {
            return "excellent,good,average,poor,terrible";
        }
        
        // ===== ACCESS FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("availability", "access", "permission", "visibility"))) {
            return "public,private,restricted,confidential,secret";
        }
        
        // ===== TIMEZONE FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("timezone", "tz"))) {
            return "UTC,EST,PST,MST,CST,EDT,PDT,MDT,CDT,AEST,AEDT,NZST,NZDT";
        }
        
        // ===== CONTINENT FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("continent"))) {
            return "North America,South America,Europe,Asia,Africa,Australia,Antarctica";
        }
        
        // ===== PROFESSIONAL FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("title", "position", "role", "job"))) {
            return "Manager,Developer,Analyst,Designer,Consultant,Specialist,Coordinator,Director";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("department", "division", "team"))) {
            return "Engineering,Marketing,Sales,HR,Finance,Operations,Support,Research";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("education", "degree", "qualification", "certification"))) {
            return "Bachelor's,Master's,PhD,Associate,Certificate,Diploma";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("experience", "skill", "expertise", "proficiency"))) {
            return "Beginner,Intermediate,Advanced,Expert,Master";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("company", "organization", "firm", "corporation"))) {
            return "Startup,Small Business,Medium Enterprise,Large Corporation,Multinational";
        }
        
        // ===== PRODUCT/SERVICE FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("category", "type", "kind", "class"))) {
            return "Electronics,Clothing,Books,Food,Home,Automotive,Sports,Health";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("brand", "manufacturer", "vendor"))) {
            return "Aerospike,Apple,Samsung,Microsoft,Google,Amazon,Nike,Adidas,Sony,Intel,AMD";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("condition", "quality", "grade", "rating"))) {
            return "New,Like New,Good,Fair,Poor,Excellent,Outstanding";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("size", "dimension", "measurement"))) {
            return "XS,S,M,L,XL,XXL,Small,Medium,Large,Extra Large";
        }
        
        // ===== TECHNICAL FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("database", "db"))) {
            return "Aerospike,MySQL,PostgreSQL,MongoDB,YugabyteDB,Elasticsearch,Oracle,SQLite";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("language", "lang"))) {
            return "Java,Python,JavaScript,TypeScript,C#,Go,Rust,PHP,Ruby,Scala";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("framework", "library", "tool", "technology"))) {
            return "Spring,React,Angular,Vue,Django,Flask,Express,Laravel,Rails,ASP.NET";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("os", "operating", "platform"))) {
            return "Windows,Linux,macOS,Ubuntu,CentOS,Debian,RedHat,FreeBSD";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("cloud", "deployment", "environment"))) {
            return "AWS,Azure,GCP,Docker,Kubernetes,Heroku,DigitalOcean,Linode";
        }
        
        return null;
    }
    
    /**
     * Determines the appropriate GenOneOfProcessor options for a given field with plural support.
     * This method analyzes field names to provide contextually appropriate options
     * that can be used for both single String fields and List/Set<String> fields.
     * 
     * @param classWords List of words from the class name
     * @param fieldWords List of words from the field name
     * @return String containing comma-separated options, or null if no pattern matches
     */
    public static String getOneOfOptionsWithPlurals(List<String> classWords, List<String> fieldWords) {
        // ===== CONFIGURATION FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("config", "setting", "option", "parameter"), true)) {
            return "development,staging,production,test,local,remote";
        }
        
        // ===== STATUS/STATE FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("status", "state", "condition", "phase", "stage"), true)) {
            return "active,inactive,pending,approved,rejected,completed,failed,cancelled";
        }
        
        // ===== PRIORITY/LEVEL FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("level", "grade", "rank", "priority"), true)) {
            return "low,medium,high,critical,urgent";
        }
        
        // ===== QUALITY FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("quality", "performance", "efficiency", "reliability"), true)) {
            return "excellent,good,average,poor,terrible";
        }
        
        // ===== ACCESS FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("availability", "access", "permission", "visibility"), true)) {
            return "public,private,restricted,confidential,secret";
        }
        
        // ===== TIMEZONE FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("timezone", "tz"), true)) {
            return "UTC,EST,PST,MST,CST,EDT,PDT,MDT,CDT,AEST,AEDT,NZST,NZDT";
        }
        
        // ===== CONTINENT FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("continent"), true)) {
            return "North America,South America,Europe,Asia,Africa,Australia,Antarctica";
        }
        
        // ===== PROFESSIONAL FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("title", "position", "role", "job"), true)) {
            return "Manager,Developer,Analyst,Designer,Consultant,Specialist,Coordinator,Director";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("department", "division", "team"), true)) {
            return "Engineering,Marketing,Sales,HR,Finance,Operations,Support,Research";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("education", "degree", "qualification", "certification"), true)) {
            return "Bachelor's,Master's,PhD,Associate,Certificate,Diploma";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("experience", "skill", "expertise", "proficiency"), true)) {
            return "Beginner,Intermediate,Advanced,Expert,Master";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("company", "organization", "firm", "corporation"), true)) {
            return "Startup,Small Business,Medium Enterprise,Large Corporation,Multinational";
        }
        
        // ===== PRODUCT/SERVICE FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("category", "type", "kind", "class"), true)) {
            return "Electronics,Clothing,Books,Food,Home,Automotive,Sports,Health";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("brand", "manufacturer", "vendor"), true)) {
            return "Aerospike,Apple,Samsung,Microsoft,Google,Amazon,Nike,Adidas,Sony,Intel,AMD";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("condition", "quality", "grade", "rating"), true)) {
            return "New,Like New,Good,Fair,Poor,Excellent,Outstanding";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("size", "dimension", "measurement"), true)) {
            return "XS,S,M,L,XL,XXL,Small,Medium,Large,Extra Large";
        }
        
        // ===== TECHNICAL FIELDS =====
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("database", "db"), true)) {
            return "Aerospike,MySQL,PostgreSQL,MongoDB,YugabyteDB,Elasticsearch,Oracle,SQLite";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("language", "lang"), true)) {
            return "Java,Python,JavaScript,TypeScript,C#,Go,Rust,PHP,Ruby,Scala";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("framework", "library", "tool", "technology"), true)) {
            return "Spring,React,Angular,Vue,Django,Flask,Express,Laravel,Rails,ASP.NET";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("os", "operating", "platform"), true)) {
            return "Windows,Linux,macOS,Ubuntu,CentOS,Debian,RedHat,FreeBSD";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("cloud", "deployment", "environment"), true)) {
            return "AWS,Azure,GCP,Docker,Kubernetes,Heroku,DigitalOcean,Linode";
        }
        
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("dependency", "library", "package", "module"), true)) {
            return "Spring Boot,React,Angular,Vue.js,Express.js,Lodash,Moment.js,Axios,Bootstrap,jQuery";
        }
        
        return null;
    }
    
    /**
     * Determines the appropriate processor for List fields based on field name patterns.
     * This method analyzes field names to provide contextually appropriate List generators.
     * 
     * @param classWords List of words from the class name
     * @param fieldWords List of words from the field name
     * @param field The field being processed
     * @return Processor instance for generating List values
     */
    private Processor determineListProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        // Try to use intelligent pattern matching for List<String> fields with plural support
        String oneOfOptions = getOneOfOptionsWithPlurals(classWords, fieldWords);
        
        if (oneOfOptions != null) {
            // Use intelligent pattern matching with GenOneOfProcessor
            return new GenListProcessor(new Class<?>[0], percentNull, 1, 5, -1, 
                    -1, 3, 10, GenString.StringType.WORDS, "", oneOfOptions, fieldType, field);
        } else {
            // Use default GenListProcessor with standard string generation
            return new GenListProcessor(new Class<?>[0], percentNull, 1, 5, -1, fieldType, field);
        }
    }
    
    /**
     * Determines the appropriate processor for Set fields based on field name patterns.
     * This method analyzes field names to provide contextually appropriate Set generators.
     * 
     * @param classWords List of words from the class name
     * @param fieldWords List of words from the field name
     * @param field The field being processed
     * @return Processor instance for generating Set values
     */
    private Processor determineSetProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        // Try to use intelligent pattern matching for Set<String> fields with plural support
        String oneOfOptions = getOneOfOptionsWithPlurals(classWords, fieldWords);
        
        if (oneOfOptions != null) {
            // Use intelligent pattern matching with GenOneOfProcessor
            return new GenSetProcessor(new Class<?>[0], percentNull, 1, 5, -1, -1, 3, 10, 
                    GenString.StringType.WORDS, "", oneOfOptions, fieldType, field);
        } else {
            // Use default GenSetProcessor with standard string generation
            return new GenSetProcessor(new Class<?>[0], percentNull, 1, 5, -1, fieldType, field);
        }
    }
}
