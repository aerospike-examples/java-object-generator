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
        return determineStringProcessorToUse(classWords, fieldWords, field, this.fieldType, true);
    }
    
    /**
     * Determines the appropriate processor for string fields based on field name patterns.
     * This method handles the most complex pattern matching as strings can represent
     * many different types of data (names, addresses, URLs, etc.).
     * 
     * @param classWords List of words from the class name
     * @param fieldWords List of words from the field name  
     * @param field The field being processed
     * @param targetFieldType The FieldType to use for processor creation (e.g., STRING for elements)
     * @param allowPlurals Whether to allow plural forms when matching patterns (defaults to true for all fields)
     * @return Processor instance for generating string values
     */
    private Processor determineStringProcessorToUse(List<String> classWords, List<String> fieldWords, Field field, FieldType targetFieldType, boolean allowPlurals) {
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("uuid"), allowPlurals)) {
            return new GenUuidProcessor(null, targetFieldType, field);
        }
        // ===== CURRENCY FIELDS =====
        // Currency fields - must be checked early to avoid conflicts with other patterns
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("currency", "curr"), allowPlurals)) {
            return new GenOneOfProcessor("AUD,USD,EUR,GBP,CAD,JPY,CHF,NZD,SGD,HKD,CNY,INR,BRL,MXN", targetFieldType);
        }
        else if (StringUtils.isLastWordOneOf(classWords, Set.of("id", "key'"))) {
            String className = classWords.stream().map(s -> StringUtils.capitalise(s)).collect(Collectors.joining());
            return new GenExpressionProcessor("'" + className + "-'$Key", targetFieldType);
        }
        else if (StringUtils.matches(fieldWords, allowPlurals, Set.of("first", "given", "christian", "preferred"), Set.of("name"))) {
            return new GenNameProcessor(NameType.FIRST, targetFieldType);
        }
        else if (StringUtils.matches(fieldWords, allowPlurals, Set.of("last", "family"), Set.of("name")) ||
                StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("surname"), allowPlurals)) {
            return new GenNameProcessor(NameType.LAST, targetFieldType);
        }
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("fullname", "full"), allowPlurals) ||
                StringUtils.matches(fieldWords, allowPlurals, Set.of("full"), Set.of("name"))) {
            return new GenNameProcessor(NameType.FULL, targetFieldType);
        }
        // Email must be checked before address patterns to avoid "emailAddresses" matching "addresses"
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("email"), allowPlurals) ) {
            return new GenEmailProcessor(null, targetFieldType, field);
        }
        // Phone must be checked before address patterns to avoid conflicts
        else if (StringUtils.matches(fieldWords, allowPlurals, Set.of("phone", "fax", "mobile", "cell", "direct"), Set.of("number", "num", "no")) ||
                StringUtils.matches(fieldWords, allowPlurals, Set.of("phone", "fax", "mobile", "cell"))) {
            return new GenPhoneNumberProcessor(PhoneNumType.PHONE, targetFieldType, field);
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
                    targetFieldType);
            return params -> (String)cityProcessor.process(params) + " " + businessProcessor.process(params); 
        }
        // Address patterns - check these after email/phone to avoid conflicts
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("addr", "address"), allowPlurals)) {
            return new GenAddressProcessor(AddressPart.FULL_ADDRESS, targetFieldType);
        }
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("location", "locations"), allowPlurals)) {
            // Location can be a city name or address
            return new GenAddressProcessor(AddressPart.CITY, targetFieldType);
        }
        else if (StringUtils.matches(fieldWords, allowPlurals, Set.of("line1", "street"))) {
            return new GenAddressProcessor(AddressPart.STREET_ADDRESS, targetFieldType);
        }
        else if (StringUtils.matches(fieldWords, allowPlurals, Set.of("line2"))) {
            return new GenAddressProcessor(AddressPart.SECONDARY, targetFieldType);
        }
        else if ((StringUtils.matches(fieldWords,  Set.of("state")) || 
                StringUtils.matches(fieldWords, Set.of("state", "st"), Set.of("code", "cd"))) 
                && StringUtils.matches(classWords, Set.of("address", "addr"))) {
            return new GenAddressProcessor(AddressPart.STATE_ABBR, targetFieldType);
        }
        else if (StringUtils.matches(fieldWords,  Set.of("state", "st"), Set.of("name", "nm")) 
                && StringUtils.matches(classWords, Set.of("address", "addr"))) {
            return new GenAddressProcessor(AddressPart.STATE_ABBR, targetFieldType);
        }
        else if (StringUtils.isLastWordOneOf(fieldWords, Set.of("city", "town", "parish", "village"), allowPlurals)) {
            return new GenAddressProcessor(AddressPart.CITY, targetFieldType);
        }
        else if (StringUtils.isLastWordOneOf(fieldWords,  Set.of("country", "cntry"), allowPlurals)) {
            return new GenAddressProcessor(AddressPart.COUNTRY, targetFieldType);
        }
        else if ((StringUtils.matches(fieldWords, allowPlurals, Set.of("zip", "post", "postal"), Set.of("code", "cd")) ||
                (StringUtils.matches(fieldWords, allowPlurals, Set.of("zip", "zipcode", "postcode", "postalcode"))))) {
            return new GenAddressProcessor(AddressPart.ZIPCODE, targetFieldType);
        }
        // Count and quantity fields - typical quantities
        else if (StringUtils.isLastWordOneOf(fieldWords, Set.of("count", "quantity", "qty", "number", "num", "total"))) {
            return new GenExpressionProcessor("'$' & @GenNumber(start=10, end=10000)", targetFieldType);
        }

        // ===== FINANCIAL/CURRENCY FIELDS =====
        // Basic pricing and cost fields - typical e-commerce/product pricing
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("price", "cost", "fee", "charge", "rate", "value", "worth"), allowPlurals)) {
            return new GenExpressionProcessor("'$' & @GenNumber(start=10, end=10000)", targetFieldType);
        }
        // Salary and income fields - realistic salary ranges
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("salary", "wage", "income", "revenue", "profit", "loss"), allowPlurals)) {
            return new GenExpressionProcessor("'$' & @GenNumber(start=30000, end=200000)", targetFieldType);
        }
        // Budget and allocation fields - project/team budgets
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("budget", "limit", "quota", "allowance"), allowPlurals)) {
            return new GenExpressionProcessor("'$' & @GenNumber(start=1000, end=50000)", targetFieldType);
        }
        // Percentage-based financial fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("discount", "tax", "tip", "commission"), allowPlurals)) {
            return new GenExpressionProcessor("@GenNumber(start=1, end=50) & '%'", targetFieldType);
        }
        // Investment and trading fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("shares", "stocks", "equity", "dividend", "yield"), allowPlurals)) {
            return new GenExpressionProcessor("'$' & @GenNumber(start=1, end=10000)", targetFieldType);
        }
        // Credit and loan fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("credit", "debt", "loan", "mortgage", "interest"), allowPlurals)) {
            return new GenExpressionProcessor("'$' & @GenNumber(start=1000, end=1000000)", targetFieldType);
        }
        // ===== URL/WEB FIELDS =====
        // Full URL fields - generates realistic web URLs
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("url", "link", "href", "website", "homepage"), allowPlurals)) {
            return new GenExpressionProcessor("'https://' & @GenString(length=8, type=CHARACTERS) & '.com'", targetFieldType);
        }
        // Domain fields - generates domain names without protocol
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("domain", "subdomain"), allowPlurals)) {
            return new GenExpressionProcessor("@GenString(length=8, type=CHARACTERS) & '.com'", targetFieldType);
        }
        // API endpoint fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("endpoint", "api", "path", "route"), allowPlurals)) {
            return new GenExpressionProcessor("'/api/v' & @GenNumber(start=1, end=3) & '/' & @GenString(length=6, type=CHARACTERS)", targetFieldType);
        }
        // Social media profile fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("profile", "avatar", "picture", "image"), allowPlurals)) {
            return new GenExpressionProcessor("'https://example.com/' & @GenString(length=6, type=CHARACTERS) & '.jpg'", targetFieldType);
        }
        // ===== CODE/TECHNICAL FIELDS =====
        // Security and authentication fields - generates secure hex strings
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("code", "token", "key", "secret", "password", "hash"), allowPlurals)) {
            return new GenHexStringProcessor(8, 16, -1, null, targetFieldType);
        }
        // Version fields - generates semantic version numbers
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("version", "build", "release"), allowPlurals)) {
            return new GenExpressionProcessor("@GenNumber(start=1, end=9) & '.' & @GenNumber(start=0, end=9) & '.' & @GenNumber(start=0, end=99)", targetFieldType);
        }
        // Configuration and environment fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("config", "setting", "option", "parameter"), allowPlurals)) {
            return new GenOneOfProcessor("development,staging,production,test,local,remote", targetFieldType);
        }
        // File and path fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("file", "path", "directory", "folder"), allowPlurals)) {
            return new GenExpressionProcessor("'/home/user/' & @GenString(length=6, type=CHARACTERS) & '.txt'", targetFieldType);
        }
        // ===== STATUS/STATE FIELDS =====
        // General status fields - common workflow states
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("status", "state", "condition", "phase", "stage"), allowPlurals)) {
            return new GenOneOfProcessor("active,inactive,pending,approved,rejected,completed,failed,cancelled", targetFieldType);
        }
        // Priority and level fields - urgency classifications
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("level", "grade", "rank", "priority"), allowPlurals)) {
            return new GenOneOfProcessor("low,medium,high,critical,urgent", targetFieldType);
        }
        // Quality and performance fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("quality", "performance", "efficiency", "reliability"), allowPlurals)) {
            return new GenOneOfProcessor("excellent,good,average,poor,terrible", targetFieldType);
        }
        // Availability and access fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("availability", "access", "permission", "visibility"), allowPlurals)) {
            return new GenOneOfProcessor("public,private,restricted,confidential,secret", targetFieldType);
        }
        // ===== GEOGRAPHIC FIELDS =====
        // Coordinate fields - realistic latitude/longitude ranges with decimal precision
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("latitude", "lat"), allowPlurals)) {
            return new GenExpressionProcessor("@GenNumber(start=-90, end=90) & '.' & @GenNumber(start=0, end=999999) & '°'", targetFieldType);
        }
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("longitude", "lng", "lon"), allowPlurals)) {
            return new GenExpressionProcessor("@GenNumber(start=-180, end=180) & '.' & @GenNumber(start=0, end=999999) & '°'", targetFieldType);
        }
        // Administrative region fields - uses address processor for state codes
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("region", "area", "zone", "district"), allowPlurals)) {
            return new GenAddressProcessor(AddressPart.STATE_ABBR, targetFieldType);
        }
        // Time zone fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("timezone", "tz"), allowPlurals)) {
            return new GenOneOfProcessor("UTC,EST,PST,MST,CST,EDT,PDT,MDT,CDT,AEST,AEDT,NZST,NZDT", targetFieldType);
        }
        // Country and continent fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("continent"), allowPlurals)) {
            return new GenOneOfProcessor("North America,South America,Europe,Asia,Africa,Australia,Antarctica", targetFieldType);
        }
        // ===== SOCIAL/PROFESSIONAL FIELDS =====
        // Job title fields - common professional roles
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("title", "position", "role", "job"), allowPlurals)) {
            return new GenOneOfProcessor("Manager,Developer,Analyst,Designer,Consultant,Specialist,Coordinator,Director", targetFieldType);
        }
        // Organizational unit fields - typical company departments
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("department", "division", "team"), allowPlurals)) {
            return new GenOneOfProcessor("Engineering,Marketing,Sales,HR,Finance,Operations,Support,Research", targetFieldType);
        }
        // Education and skill fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("education", "degree", "qualification", "certification"), allowPlurals)) {
            return new GenOneOfProcessor("Bachelor's,Master's,PhD,Associate,Certificate,Diploma", targetFieldType);
        }
        // Experience and skill level fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("experience", "skill", "expertise", "proficiency"), allowPlurals)) {
            return new GenOneOfProcessor("Beginner,Intermediate,Advanced,Expert,Master", targetFieldType);
        }
        // Company size and type fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("company", "organization", "firm", "corporation"), allowPlurals)) {
            return new GenOneOfProcessor("Startup,Small Business,Medium Enterprise,Large Corporation,Multinational", targetFieldType);
        }
        // ===== PRODUCT/SERVICE FIELDS =====
        // Product category fields - common e-commerce categories
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("category", "type", "kind", "class"), allowPlurals)) {
            return new GenOneOfProcessor("Electronics,Clothing,Books,Food,Home,Automotive,Sports,Health", targetFieldType);
        }
        // Brand and manufacturer fields - includes Aerospike and major tech brands
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("brand", "manufacturer", "vendor"), allowPlurals)) {
            return new GenOneOfProcessor("Aerospike,Apple,Samsung,Microsoft,Google,Amazon,Nike,Adidas,Sony,Intel,AMD", targetFieldType);
        }
        // Product condition and quality fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("condition", "quality", "grade", "rating"), allowPlurals)) {
            return new GenOneOfProcessor("New,Like New,Good,Fair,Poor,Excellent,Outstanding", targetFieldType);
        }
        // Product size and dimension fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("size", "dimension", "measurement"), allowPlurals)) {
            return new GenOneOfProcessor("XS,S,M,L,XL,XXL,Small,Medium,Large,Extra Large", targetFieldType);
        }
        // ===== MEASUREMENT FIELDS =====
        // Physical dimension fields - typical measurement ranges
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("width", "height", "length", "depth"), allowPlurals)) {
            return new GenExpressionProcessor("@GenNumber(start=1, end=1000) & ' cm'", targetFieldType);
        }
        // Temperature fields - realistic temperature ranges in Celsius
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("temperature", "temp"), allowPlurals)) {
            return new GenExpressionProcessor("@GenNumber(start=-50, end=150) & '°C'", targetFieldType);
        }
        // Speed and velocity fields - typical speed ranges in km/h
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("speed", "velocity"), allowPlurals)) {
            return new GenExpressionProcessor("@GenNumber(start=1, end=200) & ' km/h'", targetFieldType);
        }
        // Weight and mass fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("weight", "mass"), allowPlurals)) {
            return new GenExpressionProcessor("@GenNumber(start=1, end=1000) & ' kg'", targetFieldType);
        }
        // Volume and capacity fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("volume", "capacity"), allowPlurals)) {
            return new GenExpressionProcessor("@GenNumber(start=1, end=10000) & ' L'", targetFieldType);
        }
        // Pressure and force fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("pressure", "force", "strength"), allowPlurals)) {
            return new GenExpressionProcessor("@GenNumber(start=1, end=1000) & ' Pa'", targetFieldType);
        }
        // ===== SYSTEM/TECHNICAL FIELDS =====
        // Infrastructure fields - generates server/instance names
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("host", "server", "node", "instance"), allowPlurals)) {
            return new GenExpressionProcessor("'server-' & @GenString(length=4, type=CHARACTERS) & '-' & @GenNumber(start=1, end=999)", targetFieldType);
        }
        // Database fields - includes Aerospike and modern databases
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("database", "db"), allowPlurals)) {
            return new GenOneOfProcessor("Aerospike,MySQL,PostgreSQL,MongoDB,YugabyteDB,Elasticsearch,Oracle,SQLite", targetFieldType);
        }
        // Programming language fields - modern development languages
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("language", "lang"), allowPlurals)) {
            return new GenOneOfProcessor("Java,Python,JavaScript,TypeScript,C#,Go,Rust,PHP,Ruby,Scala", targetFieldType);
        }
        // Framework and technology fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("framework", "library", "tool", "technology"), allowPlurals)) {
            return new GenOneOfProcessor("Spring,React,Angular,Vue,Django,Flask,Express,Laravel,Rails,ASP.NET", targetFieldType);
        }
        // Operating system fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("os", "operating", "platform"), allowPlurals)) {
            return new GenOneOfProcessor("Windows,Linux,macOS,Ubuntu,CentOS,Debian,RedHat,FreeBSD", targetFieldType);
        }
        // Cloud and deployment fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("cloud", "deployment", "environment"), allowPlurals)) {
            return new GenOneOfProcessor("AWS,Azure,GCP,Docker,Kubernetes,Heroku,DigitalOcean,Linode", targetFieldType);
        }
        return new GenStringProcessor(StringType.WORDS, 1, 5, -1, null, targetFieldType);
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
        return determineNumberProcessorToUse(classWords, fieldWords, field, this.fieldType);
    }
    
    /**
     * Determines the appropriate processor for numeric fields (INTEGER, LONG, DOUBLE, FLOAT) based on field name patterns.
     * This method analyzes field names to generate contextually appropriate numeric values.
     * The GenNumberProcessor handles the specific numeric type internally.
     * 
     * @param classWords List of words from the class name
     * @param fieldWords List of words from the field name
     * @param field The field being processed
     * @param targetFieldType The FieldType to use for processor creation (e.g., INTEGER for elements)
     * @return Processor instance for generating numeric values
     */
    private Processor determineNumberProcessorToUse(List<String> classWords, List<String> fieldWords, Field field, FieldType targetFieldType) {
        // Note: Plural support is enabled by default (true) for all patterns to handle common nouns
        // that can be pluralized even in singular field contexts (e.g., "skills", "categories")
        
        // ===== QUANTITY FIELDS (must come before generic number pattern) =====
        // Quantity fields - specific range matching string pattern (10-10000)
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("quantity", "qty"), true)) {
            return new GenNumberProcessor(10, 10000, targetFieldType);
        }
        // Count fields - positive counts
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("count"), true)) {
            return new GenNumberProcessor(1, 1000, targetFieldType);
        }
        
        // ===== FINANCIAL FIELDS =====
        // Basic amount fields - typical product/service pricing
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("amount", "amt", "weight"), true)) {
            return new GenNumberProcessor(5, 1000, targetFieldType);
        }
        // Price and cost fields - typical e-commerce/product pricing (rounded to nearest $1)
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("price", "cost", "fee", "charge", "rate", "value", "worth"), true)) {
            return new GenNumberProcessor(10, 10000, 1, targetFieldType);
        }
        // Salary and income fields - realistic salary ranges (rounded to nearest $500)
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("salary", "wage", "income", "revenue", "profit", "loss"), true)) {
            return new GenNumberProcessor(30000, 200000, 500, targetFieldType);
        }
        // Budget and allocation fields - project/team budgets (rounded to nearest $100)
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("budget", "limit", "quota", "allowance"), true)) {
            return new GenNumberProcessor(1000, 50000, 100, targetFieldType);
        }
        // Percentage-based financial fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("discount", "tax", "tip", "commission"), true)) {
            return new GenNumberProcessor(1, 50, targetFieldType);
        }
        // Investment and trading fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("shares", "stocks", "equity", "dividend", "yield"), true)) {
            return new GenNumberProcessor(1, 10000, targetFieldType);
        }
        // Credit and loan fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("credit", "debt", "loan", "mortgage", "interest"), true)) {
            return new GenNumberProcessor(1000, 1000000, targetFieldType);
        }
        // Balance and total fields - larger financial amounts
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("balance", "bal", "sum", "total", "volume"), true)) {
            return new GenNumberProcessor(500, 100000, targetFieldType);
        }
        
        // ===== COORDINATE FIELDS =====
        // Latitude fields - realistic latitude ranges (-90 to 90)
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("latitude", "lat"), true)) {
            return new GenNumberProcessor(-90, 90, targetFieldType);
        }
        // Longitude fields - realistic longitude ranges (-180 to 180)
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("longitude", "lng", "lon"), true)) {
            return new GenNumberProcessor(-180, 180, targetFieldType);
        }
        
        // ===== TIME/AGE FIELDS =====
        // Age-related fields - realistic age ranges
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("age", "years", "months", "days"), true)) {
            return new GenNumberProcessor(1, 100, targetFieldType);
        }
        // Duration/Time fields - time intervals in seconds
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("duration", "length", "period", "interval", "timeout"), true)) {
            return new GenNumberProcessor(1, 3600, targetFieldType); // 1 second to 1 hour
        }
        
        // ===== ID AND INDEX FIELDS =====
        // ID and index fields - unique identifier ranges (positive only)
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("id", "index", "idx", "position", "pos"), true)) {
            return new GenNumberProcessor(1, 10000, targetFieldType);
        }
        
        // ===== GENERIC NUMBER FIELDS (must come last) =====
        // Fields ending in Number/Num - generate random positive numbers
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("number", "num"), true)) {
            return new GenNumberProcessor(1, 1000000, targetFieldType);
        }
        
        // ===== RATING/SCORE FIELDS =====
        // Rating and score fields - typical rating scales
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("rating", "score", "grade", "points"), true)) {
            return new GenNumberProcessor(1, 10, targetFieldType);
        }
        // Percentage fields - percentage values
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("percent", "percentage", "rate", "ratio"), true)) {
            return new GenNumberProcessor(0, 100, targetFieldType);
        }
        
        // ===== MEASUREMENT FIELDS =====
        // Temperature fields - realistic temperature ranges
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("temperature", "temp"), true)) {
            return new GenNumberProcessor(-50, 150, targetFieldType);
        }
        // Speed and velocity fields - decimal speed values
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("speed", "velocity"), true)) {
            return new GenNumberProcessor(0, 200, targetFieldType);
        }
        // Weight and mass fields - decimal weight values (positive only)
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("weight", "mass"), true)) {
            return new GenNumberProcessor(1, 1000, targetFieldType);
        }
        // Volume and capacity fields - decimal volume values
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("volume", "capacity"), true)) {
            return new GenNumberProcessor(0, 10000, targetFieldType);
        }
        // Pressure and force fields - decimal pressure values
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("pressure", "force", "strength"), true)) {
            return new GenNumberProcessor(0, 1000, targetFieldType);
        }
        // Distance and dimension fields (positive only)
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("distance", "width", "height", "length", "depth"), true)) {
            return new GenNumberProcessor(1, 1000, targetFieldType);
        }
        // Time duration fields - decimal time values
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("time", "interval"), true)) {
            return new GenNumberProcessor(0, 3600, targetFieldType);
        }
        
        // ===== ADDITIONAL PATTERNS =====
        // Port and network fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("port", "portnumber"), true)) {
            return new GenNumberProcessor(1024, 65535, targetFieldType);
        }
        // Version number fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("version", "major", "minor", "patch"), true)) {
            return new GenNumberProcessor(1, 99, targetFieldType);
        }
        // Thread and process fields
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("threads", "processes", "workers"), true)) {
            return new GenNumberProcessor(1, 100, targetFieldType);
        }
        
        // Default fallback - general numeric range (positive only)
        return new GenNumberProcessor(1, 1000, targetFieldType);
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
    private Processor getDateProcessorToUse(List<String> classWords, List<String> fieldWords, Field field, FieldType targetFieldType) {
        // ===== BIRTH DATE FIELDS =====
        // Birth date and age-related fields - 18-90 years ago
        // Check for plural forms (birthdays) as well as singular
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("dob", "birthday"), true) ||
                StringUtils.matches(fieldWords, Set.of("birth"), Set.of("day", "date")) ||
                StringUtils.matches(fieldWords, Set.of("name"), Set.of("day", "date")) ||
                StringUtils.matches(fieldWords, Set.of("date"), Set.of("of"), Set.of("birth"))) {
            return new GenDateProcessor("now-90y", "now-18y", percentNull, targetFieldType);
        }
        
        // ===== CREATION/REGISTRATION FIELDS =====
        // Account creation, registration, and start dates - 5 years ago to 1 day ago
        if (StringUtils.areLastTwoWordsOneOf(fieldWords, 
                    Set.of("open", "opened", "created", "create", "join", "joined", "start", "stared", "begin"),
                    Set.of("date", "time", "timestamp")) ||
                StringUtils.areLastTwoWordsOneOf(fieldWords,
                    Set.of("date", "when", "time"),
                    Set.of("open", "opened", "created", "create", "join", "joined", "start", "stared", "begin"))) {
            return new GenDateProcessor("now-5y", "now-1d", percentNull, targetFieldType);
        }
        
        // ===== RECENT ACTIVITY FIELDS =====
        // Recent activity, login, and update dates - 1 year ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("last", "recent", "updated", "modified", "accessed", "viewed", "logged"))) {
            return new GenDateProcessor("now-1y", "now", percentNull, targetFieldType);
        }
        
        // ===== FUTURE/SCHEDULED FIELDS =====
        // Future dates, appointments, and scheduled events - now to 1 year ahead
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("scheduled", "appointment", "meeting", "event", "deadline", "due", "expiry", "expiration"))) {
            return new GenDateProcessor("now", "now+1y", percentNull, targetFieldType);
        }
        
        // ===== EMPLOYMENT FIELDS =====
        // Employment start and end dates - 20 years ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("hired", "employed", "started", "joined", "terminated", "ended", "resigned"))) {
            return new GenDateProcessor("now-20y", "now", percentNull, targetFieldType);
        }
        
        // ===== ACADEMIC FIELDS =====
        // Education and graduation dates - 10 years ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("graduated", "enrolled", "admitted", "completed", "education", "academic"))) {
            return new GenDateProcessor("now-10y", "now", percentNull, targetFieldType);
        }
        
        // ===== LEGAL/COMPLIANCE FIELDS =====
        // Legal dates, compliance, and certification dates - 5 years ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("certified", "licensed", "approved", "compliant", "legal", "contract", "agreement"))) {
            return new GenDateProcessor("now-5y", "now", percentNull, targetFieldType);
        }
        
        // ===== FINANCIAL FIELDS =====
        // Payment, billing, and financial transaction dates - 2 years ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("paid", "billed", "charged", "transacted", "payment", "invoice", "receipt"))) {
            return new GenDateProcessor("now-2y", "now", percentNull, targetFieldType);
        }
        
        // ===== MAINTENANCE/SUPPORT FIELDS =====
        // Maintenance, support, and service dates - 1 year ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("maintained", "serviced", "repaired", "updated", "patched", "fixed"))) {
            return new GenDateProcessor("now-1y", "now", percentNull, targetFieldType);
        }
        
        // ===== MEDICAL/HEALTH FIELDS =====
        // Medical appointments, checkups, and health-related dates - 2 years ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("appointment", "checkup", "exam", "visit", "treatment", "vaccinated", "tested"))) {
            return new GenDateProcessor("now-2y", "now", percentNull, targetFieldType);
        }
        
        // ===== TRAVEL FIELDS =====
        // Travel and trip dates - 1 year ago to 1 year ahead
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("travel", "trip", "departure", "arrival", "booking", "reservation"))) {
            return new GenDateProcessor("now-1y", "now+1y", percentNull, targetFieldType);
        }
        
        // ===== SUBSCRIPTION FIELDS =====
        // Subscription and membership dates - 1 year ago to 1 year ahead
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("subscribed", "membership", "renewal", "expires", "valid", "active"))) {
            return new GenDateProcessor("now-1y", "now+1y", percentNull, targetFieldType);
        }
        
        // ===== SYSTEM/LOG FIELDS =====
        // System logs, audit trails, and technical dates - 1 month ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("logged", "audit", "system", "technical", "debug", "trace"))) {
            return new GenDateProcessor("now-1M", "now", percentNull, targetFieldType);
        }
        
        // ===== NOTIFICATION FIELDS =====
        // Notification and communication dates - 1 week ago to now
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("notified", "sent", "received", "delivered", "read", "opened"))) {
            return new GenDateProcessor("now-1w", "now", percentNull, targetFieldType);
        }
        
        return null;
    }
    
    private Processor determineDateProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        return determineDateProcessorToUse(classWords, fieldWords, field, this.fieldType);
    }
    
    private Processor determineDateProcessorToUse(List<String> classWords, List<String> fieldWords, Field field, FieldType targetFieldType) {
        Processor proc = getDateProcessorToUse(classWords, fieldWords, field, targetFieldType);
        if (proc != null) {
            return proc;
        }
        return new GenDateProcessor("now-10y", "now+10y", percentNull, targetFieldType);
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
        return determineBooleanProcessorToUse(classWords, fieldWords, field, this.fieldType);
    }
    
    private Processor determineBooleanProcessorToUse(List<String> classWords, List<String> fieldWords, Field field, FieldType targetFieldType) {
        // Note: Plural support is enabled by default (true) for all patterns to handle common nouns
        // ===== REGISTRATION/STATUS FIELDS (High True Ratio) =====
        // Registration and membership fields - mostly true
        if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("registered", "enrolled", "member", "subscribed", "active", "verified", "confirmed"), true)) {
            return new GenBooleanProcessor("true:8,false:1", targetFieldType);
        }
        // Account and user status fields - mostly true
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("account", "user", "profile", "session"), true)) {
            return new GenBooleanProcessor("true:7,false:1", targetFieldType);
        }
        
        // ===== RARE/DEMOGRAPHIC FIELDS (High False Ratio) =====
        // Demographic and rare characteristic fields - mostly false
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("indigenous", "aboriginal", "torres", "strait", "islander", "native", "minority"), true)) {
            return new GenBooleanProcessor("true:1,false:19", targetFieldType);
        }
        // Disability and special needs fields - mostly false
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("disabled", "handicapped", "impaired", "special", "needs"), true)) {
            return new GenBooleanProcessor("true:1,false:9", targetFieldType);
        }
        // Veteran and military fields - mostly false
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("veteran", "military", "served", "armed", "forces"), true)) {
            return new GenBooleanProcessor("true:1,false:9", targetFieldType);
        }
        
        // ===== SECURITY/PRIVACY FIELDS (Balanced) =====
        // Security and privacy fields - balanced ratio
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("secure", "private", "confidential", "sensitive", "encrypted", "protected"), true)) {
            return new GenBooleanProcessor("true:1,false:1", targetFieldType);
        }
        // Permission and access fields - balanced ratio
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("permission", "access", "authorized", "allowed", "granted"), true)) {
            return new GenBooleanProcessor("true:1,false:1", targetFieldType);
        }
        
        // ===== FEATURE/OPTION FIELDS (Moderate True Ratio) =====
        // Feature and option fields - moderately true
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("feature", "option", "setting", "preference", "enabled", "available"), true)) {
            return new GenBooleanProcessor("true:3,false:1", targetFieldType);
        }
        // Notification and communication fields - moderately true
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("notification", "email", "sms", "alert", "reminder", "newsletter"), true)) {
            return new GenBooleanProcessor("true:3,false:1", targetFieldType);
        }
        
        // ===== ERROR/EXCEPTION FIELDS (High False Ratio) =====
        // Error and exception fields - mostly false
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("error", "exception", "failed", "invalid", "corrupted", "broken"), true)) {
            return new GenBooleanProcessor("true:1,false:9", targetFieldType);
        }
        // Warning and alert fields - mostly false
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("warning", "alert", "critical", "urgent", "emergency"), true)) {
            return new GenBooleanProcessor("true:1,false:9", targetFieldType);
        }
        
        // ===== COMPLETION/SUCCESS FIELDS (High True Ratio) =====
        // Completion and success fields - mostly true
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("completed", "finished", "done", "successful", "passed", "approved"), true)) {
            return new GenBooleanProcessor("true:8,false:1", targetFieldType);
        }
        // Payment and transaction fields - mostly true
        else if (StringUtils.isFirstOrLastWordOneOf(fieldWords, Set.of("paid", "billed", "charged", "processed", "transacted"), true)) {
            return new GenBooleanProcessor("true:7,false:1", targetFieldType);
        }
        
        // ===== DEFAULT PATTERN =====
        // Default balanced ratio for unknown patterns
        return new GenBooleanProcessor("true:1,false:1", targetFieldType);
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
     * It extracts the element type and uses intelligent pattern matching to create appropriate
     * processors for each element type (String, Integer, Date, Boolean, etc.).
     * 
     * @param classWords List of words from the class name
     * @param fieldWords List of words from the field name
     * @param field The field being processed
     * @return Processor instance for generating List values
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Processor determineListProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        // Extract element type from List/Set
        Class<?> elementType = getElementType(field);
        if (elementType == null) {
            // Fallback to default if element type cannot be determined
            return new GenListProcessor(new Class<?>[0], percentNull, 1, 5, -1, fieldType, field);
        }
        
        // Get appropriate element processor based on element type and field name patterns
        Processor elementProcessor = getElementProcessorForListOrSet(elementType, classWords, fieldWords, field);
        
        // For String elements, try OneOf first (for backward compatibility and common patterns)
        if (String.class.isAssignableFrom(elementType)) {
            String oneOfOptions = getOneOfOptionsWithPlurals(classWords, fieldWords);
            if (oneOfOptions != null) {
                // Use intelligent pattern matching with GenOneOfProcessor
                return new GenListProcessor(new Class<?>[0], percentNull, 1, 5, -1, 
                        -1, 3, 10, GenString.StringType.WORDS, "", oneOfOptions, fieldType, field);
            }
            // If we have a specific string processor, use it
            if (elementProcessor != null) {
                return new GenListProcessor(new Class<?>[0], percentNull, 1, 5, -1, 
                        -1, 3, 10, GenString.StringType.WORDS, "", "", fieldType, field, elementProcessor);
            }
        }
        
        // For non-string types or when elementProcessor is available, use it
        if (elementProcessor != null) {
            return new GenListProcessor(new Class<?>[0], percentNull, 1, 5, -1, fieldType, field, elementProcessor);
        }
        
        // Fallback to default GenListProcessor
        return new GenListProcessor(new Class<?>[0], percentNull, 1, 5, -1, fieldType, field);
    }
    
    /**
     * Determines the appropriate processor for Set fields based on field name patterns.
     * This method analyzes field names to provide contextually appropriate Set generators.
     * It extracts the element type and uses intelligent pattern matching to create appropriate
     * processors for each element type (String, Integer, Date, Boolean, etc.).
     * 
     * @param classWords List of words from the class name
     * @param fieldWords List of words from the field name
     * @param field The field being processed
     * @return Processor instance for generating Set values
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Processor determineSetProcessorToUse(List<String> classWords, List<String> fieldWords, Field field) {
        // Extract element type from List/Set
        Class<?> elementType = getElementType(field);
        if (elementType == null) {
            // Fallback to default if element type cannot be determined
            return new GenSetProcessor(new Class<?>[0], percentNull, 1, 5, -1, fieldType, field);
        }
        
        // Get appropriate element processor based on element type and field name patterns
        Processor elementProcessor = getElementProcessorForListOrSet(elementType, classWords, fieldWords, field);
        
        // For String elements, try OneOf first (for backward compatibility and common patterns)
        if (String.class.isAssignableFrom(elementType)) {
            String oneOfOptions = getOneOfOptionsWithPlurals(classWords, fieldWords);
            if (oneOfOptions != null) {
                // Use intelligent pattern matching with GenOneOfProcessor
                return new GenSetProcessor(new Class<?>[0], percentNull, 1, 5, -1, -1, 3, 10, 
                        GenString.StringType.WORDS, "", oneOfOptions, fieldType, field);
            }
            // If we have a specific string processor, use it
            if (elementProcessor != null) {
                return new GenSetProcessor(new Class<?>[0], percentNull, 1, 5, -1, -1, 3, 10, 
                        GenString.StringType.WORDS, "", "", fieldType, field, elementProcessor);
            }
        }
        
        // For non-string types or when elementProcessor is available, use it
        if (elementProcessor != null) {
            return new GenSetProcessor(new Class<?>[0], percentNull, 1, 5, -1, fieldType, field, elementProcessor);
        }
        
        // Fallback to default GenSetProcessor
        return new GenSetProcessor(new Class<?>[0], percentNull, 1, 5, -1, fieldType, field);
    }
    
    /**
     * Extracts the element type from a List or Set field.
     * 
     * @param field The field to extract the element type from
     * @return The element type, or null if it cannot be determined
     */
    private Class<?> getElementType(Field field) {
        if (field.getType().isArray()) {
            return field.getType().getComponentType();
        }
        
        java.lang.reflect.Type genericType = field.getGenericType();
        if (genericType instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) genericType;
            return (Class<?>) pt.getActualTypeArguments()[0];
        }
        
        return null;
    }
    
    /**
     * Determines the appropriate processor for a List/Set element based on its type and field name patterns.
     * This method reuses the pattern matching logic from determineStringProcessorToUse,
     * determineNumberProcessorToUse, determineBooleanProcessorToUse, and determineDateProcessorToUse.
     * 
     * @param elementType The type of the element (String, Integer, Date, etc.)
     * @param classWords List of words from the class name
     * @param fieldWords List of words from the field name
     * @param field The field being processed
     * @return Processor instance for generating element values, or null if no pattern matches
     */
    private Processor getElementProcessorForListOrSet(Class<?> elementType, List<String> classWords, List<String> fieldWords, Field field) {
        // Map element type to FieldType
        FieldType elementFieldType = mapClassToFieldType(elementType);
        
        if (elementFieldType == null) {
            return null;
        }
        
        // Use the same pattern matching logic as single fields, but with the correct element field type
        // Note: allowPlurals is true because List/Set field names are often plural (e.g., "zipCodes", "cities")
        switch (elementFieldType) {
        case STRING:
            return determineStringProcessorToUse(classWords, fieldWords, field, FieldType.STRING, true);
        case INTEGER:
            return determineNumberProcessorToUse(classWords, fieldWords, field, FieldType.INTEGER);
        case LONG:
            return determineNumberProcessorToUse(classWords, fieldWords, field, FieldType.LONG);
        case DOUBLE:
            return determineNumberProcessorToUse(classWords, fieldWords, field, FieldType.DOUBLE);
        case FLOAT:
            return determineNumberProcessorToUse(classWords, fieldWords, field, FieldType.FLOAT);
        case BOOLEAN:
            return determineBooleanProcessorToUse(classWords, fieldWords, field, FieldType.BOOLEAN);
        case DATE:
        case LOCALDATE:
        case LOCALDATETIME:
        case LOCALTIME:
        case INSTANT:
            return determineDateProcessorToUse(classWords, fieldWords, field, elementFieldType);
        default:
            return null;
        }
    }
    
    /**
     * Maps a Java class to the corresponding FieldType enum.
     * 
     * @param clazz The class to map
     * @return The corresponding FieldType, or null if not supported
     */
    private FieldType mapClassToFieldType(Class<?> clazz) {
        if (String.class.isAssignableFrom(clazz)) {
            return FieldType.STRING;
        } else if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)) {
            return FieldType.INTEGER;
        } else if (Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz)) {
            return FieldType.LONG;
        } else if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)) {
            return FieldType.DOUBLE;
        } else if (Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz)) {
            return FieldType.FLOAT;
        } else if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz)) {
            return FieldType.BOOLEAN;
        } else if (java.util.Date.class.isAssignableFrom(clazz)) {
            return FieldType.DATE;
        } else if (java.time.LocalDate.class.isAssignableFrom(clazz)) {
            return FieldType.LOCALDATE;
        } else if (java.time.LocalDateTime.class.isAssignableFrom(clazz)) {
            return FieldType.LOCALDATETIME;
        } else if (java.time.LocalTime.class.isAssignableFrom(clazz)) {
            return FieldType.LOCALTIME;
        } else if (java.time.Instant.class.isAssignableFrom(clazz)) {
            return FieldType.INSTANT;
        }
        return null;
    }
}
