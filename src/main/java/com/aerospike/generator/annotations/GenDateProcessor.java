package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class GenDateProcessor implements Processor {
    private final long startTime;
    private final long endTime;
    private final int percentNull;
    private final FieldType fieldType;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    
    // Cache of DateTimeFormatter instances by pattern string (thread-safe)
    private static final Map<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>();
    
    public GenDateProcessor(GenDate genDate, FieldType fieldType, Field field) {
        this(genDate.start(), genDate.end(), genDate.percentNull(), fieldType);
    }
    
    public GenDateProcessor(String startDate, String endDate, int percentNull, FieldType fieldType) {
        this.startTime = parseDate(startDate);
        this.endTime = parseDate(endDate);
        this.percentNull = percentNull;
        this.fieldType = fieldType;
        if (startTime > endTime) {
            throw new IllegalArgumentException(String.format("start date (%d) > end date (%d)", startTime, endTime));
        }
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
        if (percentNull <0 || percentNull > 100) {
            throw new IllegalArgumentException("PercentNull must in the range 0-100, not " + percentNull);
        }
    }
    
    public boolean supports(FieldType fieldType) {
        switch (fieldType) {
        case LONG:
        case DATE:
        case LOCALDATE:
        case LOCALDATETIME:
        case LOCALTIME:
        case INSTANT:
        case STRING:
            return true;
        default:
            return false;
        }
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    @Override
    public String toString() {
        return String.format("min: %d (%s), max %d (%s), format: %s", startTime, new Date(startTime), endTime, new Date(endTime), fieldType);
    }
    
    private Long parseDate(String dateType) {
        if (dateType == null || dateType.isEmpty()) {
            throw new IllegalArgumentException("@GenDate must take a value start date and valid end date");
        }
        
        // First, check if this is a simple date format (no operators) or has explicit format
        // If it matches a known pattern or has explicit format, parse it directly
        String trimmed = dateType.trim();
        if (trimmed.indexOf('[') >= 0 || 
            trimmed.matches(".*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}.*") ||
            trimmed.matches(".*\\d{1,2}[-/\\s]+[A-Za-z]{3}[-/\\s]+\\d{2,4}.*")) {
            // This looks like a date format, try parsing it directly first
            try {
                return parseDateComponent(trimmed);
            } catch (IllegalArgumentException e) {
                // If direct parsing fails, fall through to operator-based parsing
            }
        }
        
        // Parse as expression with operators (+ and -)
        List<String> strings = new ArrayList<>();
        int index = 0;
        while (true) {
            int nextPlusIndex = dateType.indexOf('+', index);
            int nextMinusIndex = dateType.indexOf('-', index);
            
            // Skip minus signs that are part of date formats (e.g., "1-Jan-2020")
            // A minus is an operator if it's followed by whitespace or a digit and not part of a date pattern
            if (nextMinusIndex >= 0) {
                // Check if this minus is part of a date format (e.g., "dd-MMM-yyyy")
                String beforeMinus = dateType.substring(Math.max(0, index), nextMinusIndex);
                String afterMinus = nextMinusIndex + 1 < dateType.length() ? 
                    dateType.substring(nextMinusIndex + 1, Math.min(nextMinusIndex + 5, dateType.length())) : "";
                
                // If the part before minus ends with digits and after starts with letters, it's a date separator
                if (beforeMinus.matches(".*\\d$") && afterMinus.matches("^[A-Za-z].*")) {
                    // This is a date separator, not an operator - treat the whole thing as one date
                    strings.add(dateType.substring(index).trim());
                    break;
                }
            }
            
            if (nextPlusIndex < 0 && nextMinusIndex < 0) {
                strings.add(dateType.substring(index).trim());
                break;
            }
            else if (nextPlusIndex < 0) {
                strings.add(dateType.substring(index, nextMinusIndex));
                strings.add("-");
                index = nextMinusIndex + 1;
            }
            else if (nextMinusIndex < 0 || nextPlusIndex < nextMinusIndex) {
                strings.add(dateType.substring(index, nextPlusIndex).trim());
                strings.add("+");
                index = nextPlusIndex + 1;
            }
            else {
                strings.add(dateType.substring(index, nextMinusIndex));
                strings.add("-");
                index = nextMinusIndex + 1;
            }
        }
        long val = parseDateComponent(strings.get(0).trim());
        for (int i = 1; i < strings.size(); i+=2) {
            boolean isPlus;
            if ("+".equals(strings.get(i))) {
                isPlus = true;
            }
            else if ("-".equals(strings.get(i))) {
                isPlus = false;
            }
            else {
                throw new IllegalArgumentException(String.format("Could not understand expression %s. Expecting + or - between terms but didn't receive them", dateType));
            }
            if (i+1 >= strings.size()) {
                throw new IllegalArgumentException(String.format("Could not understand expression %s. It ended with an operation not an operand", dateType));
            }
            long opVal = parseDateComponent(strings.get(i+1));
            if (isPlus) {
                val += opVal;
            }
            else {
                val -= opVal;
            }
        }
        return val;
    }
    private Long parseDateComponent(String dateType) {
        if (dateType == null) {
            return null;
        }
        if (dateType.equalsIgnoreCase("now")) {
            return System.currentTimeMillis();
        }
        
        // Check for explicit format in square brackets: "02/08/2022 [M/d/y]"
        int bracketStart = dateType.indexOf('[');
        int bracketEnd = dateType.indexOf(']');
        if (bracketStart >= 0 && bracketEnd > bracketStart) {
            String datePart = dateType.substring(0, bracketStart).trim();
            String formatTemplate = dateType.substring(bracketStart + 1, bracketEnd).trim();
            String dtfFormat = convertFormatTemplateToDateTimeFormatter(datePart, formatTemplate);
            try {
                return parseWithDateTimeFormatter(datePart, dtfFormat);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(String.format("Could not parse %s with format template %s (converted to %s)", 
                        datePart, formatTemplate, dtfFormat), e);
            }
        }
        
        // Static date format patterns (in order of specificity)
        // Use "d" and "M" instead of "dd" and "MM" to handle both 1-digit and 2-digit values
        List<String> dateFormats = List.of(     // Use a list to allow in-order traversal
                // Date with time (4-digit year)
                "\\d{1,2}/\\d{1,2}/\\d{4}-\\d{1,2}:\\d\\d:\\d\\d", "d/M/yyyy-HH:mm:ss",
                "\\d{1,2}/\\d{1,2}/\\d{2}-\\d{1,2}:\\d\\d:\\d\\d", "d/M/yy-HH:mm:ss",
                "\\d{1,2}/\\d{1,2}/\\d{4}-\\d{1,2}:\\d\\d", "d/M/yyyy-HH:mm",
                "\\d{1,2}/\\d{1,2}/\\d{2}-\\d{1,2}:\\d\\d", "d/M/yy-HH:mm",
                // Date only (4-digit year)
                "\\d{1,2}/\\d{1,2}/\\d{4}", "d/M/yyyy",
                "\\d{1,2}/\\d{1,2}/\\d{2}", "d/M/yy",
                // Word-based month formats (e.g., "3-Oct-1923", "15-Jan-2023")
                // Use "d" instead of "dd" to handle both 1-digit and 2-digit days
                "\\d{1,2}-[A-Za-z]{3}-\\d{4}", "d-MMM-yyyy",
                "\\d{1,2}-[A-Za-z]{3}-\\d{2}", "d-MMM-yy",
                // Alternative separators for word-based months (e.g., "3 Oct 1923", "15/Jan/2023")
                "\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}", "d MMM yyyy",
                "\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{2}", "d MMM yy",
                "\\d{1,2}/[A-Za-z]{3}/\\d{4}", "d/MMM/yyyy",
                "\\d{1,2}/[A-Za-z]{3}/\\d{2}", "d/MMM/yy"
            );
        
        for (int i = 0; i < dateFormats.size() - 1; i+=2) {
            if (dateType.matches(dateFormats.get(i))) {
                String format = dateFormats.get(i+1);
                try {
                    return parseWithDateTimeFormatter(dateType, format);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException(String.format("Could not parse %s with format of %s: %s", 
                            dateType, format, e.getMessage()));
                }
            }
        }
        
        // If no pattern matched, try to parse as time offset
        try {
            return parseTimeOffset(dateType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format(
                    "Could not parse date '%s'. It doesn't match any known date format pattern and is not a valid time offset. " +
                    "Supported formats include: dd/MM/yyyy, dd-MMM-yyyy, or explicit format like 'date [M/d/y]'", 
                    dateType), e);
        }
    }
    
    /**
     * Parses a date string using DateTimeFormatter and returns the timestamp in milliseconds.
     * 
     * <p>This method uses a cached DateTimeFormatter instance for better performance.
     * DateTimeFormatter instances are thread-safe and immutable, so they can be safely cached.
     * Formatters with month abbreviations (MMM) are created as case-insensitive to handle
     * various capitalizations like "Jan", "JAN", "jan".</p>
     * 
     * @param dateString The date string to parse
     * @param formatPattern The DateTimeFormatter pattern
     * @return The timestamp in milliseconds since epoch
     * @throws DateTimeParseException If the date cannot be parsed
     */
    private long parseWithDateTimeFormatter(String dateString, String formatPattern) {
        // Get or create a cached DateTimeFormatter instance
        DateTimeFormatter formatter = FORMATTER_CACHE.computeIfAbsent(formatPattern, 
                pattern -> createDateTimeFormatter(pattern));
        
        // Handle 2-digit years: if pattern has "yy" (not "yyyy"), adjust the year
        // SimpleDateFormat default pivot is 80: years 00-79 = 2000-2079, 80-99 = 1980-1999
        boolean hasTwoDigitYear = formatPattern.contains("yy") && !formatPattern.contains("yyyy");
        
        // Try parsing as LocalDateTime first (for dates with time)
        if (formatPattern.contains("HH") || formatPattern.contains("mm") || formatPattern.contains("ss")) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(dateString, formatter);
                if (hasTwoDigitYear) {
                    ldt = adjustTwoDigitYear(ldt);
                }
                return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (DateTimeParseException e) {
                // If parsing as LocalDateTime fails, try LocalDate
            }
        }
        
        // Parse as LocalDate (for dates without time)
        LocalDate ld = LocalDate.parse(dateString, formatter);
        if (hasTwoDigitYear) {
            ld = adjustTwoDigitYear(ld);
        }
        return ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    
    /**
     * Adjusts a 2-digit year to match SimpleDateFormat's default pivot behavior.
     * Years 00-79 become 2000-2079, years 80-99 become 1980-1999.
     * 
     * @param date The date with potentially incorrect year
     * @return The date with adjusted year
     */
    private LocalDate adjustTwoDigitYear(LocalDate date) {
        int year = date.getYear();
        // If year is >= 2080, it was parsed from a 2-digit year >= 80, so subtract 100
        if (year >= 2080) {
            return date.withYear(year - 100);
        }
        return date;
    }
    
    /**
     * Adjusts a 2-digit year to match SimpleDateFormat's default pivot behavior.
     * Years 00-79 become 2000-2079, years 80-99 become 1980-1999.
     * 
     * @param dateTime The date-time with potentially incorrect year
     * @return The date-time with adjusted year
     */
    private LocalDateTime adjustTwoDigitYear(LocalDateTime dateTime) {
        int year = dateTime.getYear();
        // If year is >= 2080, it was parsed from a 2-digit year >= 80, so subtract 100
        if (year >= 2080) {
            return dateTime.withYear(year - 100);
        }
        return dateTime;
    }
    
    /**
     * Creates a DateTimeFormatter from a pattern string.
     * If the pattern contains month abbreviations (MMM), the formatter is made case-insensitive
     * to handle various capitalizations like "Jan", "JAN", "jan".
     * 
     * @param pattern The date/time pattern string
     * @return A DateTimeFormatter configured for the pattern
     */
    private DateTimeFormatter createDateTimeFormatter(String pattern) {
        // If pattern contains MMM (month abbreviation), make it case-insensitive
        if (pattern.contains("MMM")) {
            DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
            builder.parseCaseInsensitive();
            builder.appendPattern(pattern);
            return builder.toFormatter(Locale.ENGLISH);
        } else {
            return DateTimeFormatter.ofPattern(pattern);
        }
    }
    
    /**
     * Converts a user-friendly format template to DateTimeFormatter format string
     * by analyzing the actual date string to determine digit counts.
     * 
     * <p>This method infers the DateTimeFormatter pattern by:
     * <ol>
     *   <li>Parsing the template to identify format symbols (y, M, d, h, m, s) and separators</li>
     *   <li>Using the separators to split the date string into parts</li>
     *   <li>Mapping each format symbol to its corresponding date part</li>
     *   <li>Counting digits/characters in each part to determine the format pattern</li>
     * </ol>
     * </p>
     * 
     * <p>Examples:
     * <ul>
     *   <li>{@code "02/08/2022 [M/d/y]"} -> "MM/dd/yyyy" (2 digits for month, 2 for day, 4 for year)</li>
     *   <li>{@code "2/8/22 [M/d/y]"} -> "M/d/yy" (1 digit for month, 1 for day, 2 for year)</li>
     *   <li>{@code "15-Oct-2023 [d-MMM-yyyy]"} -> "dd-MMM-yyyy" (2 digits for day, 3 letters for month, 4 for year)</li>
     * </ul>
     * </p>
     * 
     * <p>Format symbol mapping (based on actual date string):
     * <ul>
     *   <li>{@code "y"} -> "y" (1 digit), "yy" (2 digits), or "yyyy" (4 digits)</li>
     *   <li>{@code "M"} -> "M" (1 digit), "MM" (2 digits), or "MMM" (3+ letters for abbreviation)</li>
     *   <li>{@code "d"} -> "d" (1 digit) or "dd" (2 digits)</li>
     *   <li>{@code "h"}, {@code "m"}, {@code "s"} -> "H"/"HH" (hour-of-day), "m"/"mm", "s"/"ss" based on digit count</li>
     * </ul>
     * </p>
     * 
     * @param dateString The actual date string to parse (e.g., "02/08/2022")
     * @param template The user-friendly format template (e.g., "M/d/y")
     * @return The DateTimeFormatter format string
     */
    private String convertFormatTemplateToDateTimeFormatter(String dateString, String template) {
        if (template == null || template.isEmpty()) {
            throw new IllegalArgumentException("Format template cannot be null or empty");
        }
        if (dateString == null || dateString.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }
        
        // Parse template to extract format symbols and separators in order
        List<String> formatSymbols = new ArrayList<>();
        List<String> separators = new ArrayList<>();
        StringBuilder currentSymbol = new StringBuilder();
        StringBuilder currentSeparator = new StringBuilder();
        boolean inSymbol = false;
        
        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);
            boolean isFormatChar = (c == 'y' || c == 'Y' || c == 'M' || c == 'd' || c == 'D' || 
                                    c == 'h' || c == 'H' || c == 'm' || c == 's' || c == 'S');
            
            if (isFormatChar) {
                if (!inSymbol && currentSeparator.length() > 0) {
                    separators.add(currentSeparator.toString());
                    currentSeparator.setLength(0);
                }
                inSymbol = true;
                currentSymbol.append(c);
            } else {
                if (inSymbol && currentSymbol.length() > 0) {
                    formatSymbols.add(currentSymbol.toString());
                    currentSymbol.setLength(0);
                }
                inSymbol = false;
                currentSeparator.append(c);
            }
        }
        if (currentSymbol.length() > 0) {
            formatSymbols.add(currentSymbol.toString());
        }
        if (currentSeparator.length() > 0 && formatSymbols.size() > separators.size()) {
            separators.add(currentSeparator.toString());
        }
        
        // Split date string using separators
        List<String> dateParts = new ArrayList<>();
        String remaining = dateString;
        for (int i = 0; i < separators.size() && i < formatSymbols.size() - 1; i++) {
            String sep = separators.get(i);
            int sepIndex = remaining.indexOf(sep);
            if (sepIndex >= 0) {
                dateParts.add(remaining.substring(0, sepIndex));
                remaining = remaining.substring(sepIndex + sep.length());
            } else {
                // Separator not found, try splitting by any non-word character
                break;
            }
        }
        // Add the last part
        if (!remaining.isEmpty()) {
            dateParts.add(remaining);
        }
        
        // If splitting by separators didn't work, try splitting by non-word characters
        if (dateParts.size() != formatSymbols.size()) {
            dateParts.clear();
            String[] parts = dateString.split("[^\\w]+");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    dateParts.add(part);
                }
            }
        }
        
        // Validate that we have enough date parts
        if (dateParts.size() < formatSymbols.size()) {
            throw new IllegalArgumentException(String.format(
                    "Date string '%s' has %d parts but template '%s' expects %d format symbols",
                    dateString, dateParts.size(), template, formatSymbols.size()));
        }
        
        // Build SimpleDateFormat string by mapping symbols to date parts
        StringBuilder result = new StringBuilder();
        int datePartIndex = 0;
        
        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);
            
            if (c == 'y' || c == 'Y') {
                // Collect all consecutive y's
                while (i < template.length() && (template.charAt(i) == 'y' || template.charAt(i) == 'Y')) {
                    i++;
                }
                i--; // Back up one
                
                // Get corresponding date part and determine format
                if (datePartIndex < dateParts.size()) {
                    String datePart = dateParts.get(datePartIndex++);
                    if (datePart == null || datePart.isEmpty()) {
                        throw new IllegalArgumentException(String.format(
                                "Empty date part at index %d when parsing '%s' with template '%s'",
                                datePartIndex - 1, dateString, template));
                    }
                    int digitCount = countDigits(datePart);
                    if (digitCount <= 2) {
                        result.append(digitCount == 1 ? "y" : "yy");
                    } else {
                        result.append("yyyy");
                    }
                } else {
                    // Fallback
                    result.append("yyyy");
                }
            } else if (c == 'M') {
                // Collect all consecutive M's
                while (i < template.length() && template.charAt(i) == 'M') {
                    i++;
                }
                i--; // Back up one
                
                // Get corresponding date part
                if (datePartIndex < dateParts.size()) {
                    String datePart = dateParts.get(datePartIndex++);
                    if (isAllLetters(datePart) && datePart.length() >= 3) {
                        // Month abbreviation
                        result.append("MMM");
                    } else {
                        int digitCount = countDigits(datePart);
                        result.append(digitCount == 1 ? "M" : "MM");
                    }
                } else {
                    // Fallback
                    result.append("MM");
                }
            } else if (c == 'd' || c == 'D') {
                // Collect all consecutive d's
                while (i < template.length() && (template.charAt(i) == 'd' || template.charAt(i) == 'D')) {
                    i++;
                }
                i--; // Back up one
                
                if (datePartIndex < dateParts.size()) {
                    String datePart = dateParts.get(datePartIndex++);
                    int digitCount = countDigits(datePart);
                    result.append(digitCount == 1 ? "d" : "dd");
                } else {
                    result.append("dd");
                }
            } else if (c == 'h' || c == 'H') {
                while (i < template.length() && (template.charAt(i) == 'h' || template.charAt(i) == 'H')) {
                    i++;
                }
                i--; // Back up one
                
                if (datePartIndex < dateParts.size()) {
                    String datePart = dateParts.get(datePartIndex++);
                    int digitCount = countDigits(datePart);
                    // DateTimeFormatter uses H for hour-of-day (0-23), not h (hour-of-am-pm)
                    result.append(digitCount == 1 ? "H" : "HH");
                } else {
                    result.append("HH");
                }
            } else if (c == 'm') {
                while (i < template.length() && template.charAt(i) == 'm') {
                    i++;
                }
                i--; // Back up one
                
                if (datePartIndex < dateParts.size()) {
                    String datePart = dateParts.get(datePartIndex++);
                    int digitCount = countDigits(datePart);
                    result.append(digitCount == 1 ? "m" : "mm");
                } else {
                    result.append("mm");
                }
            } else if (c == 's' || c == 'S') {
                while (i < template.length() && (template.charAt(i) == 's' || template.charAt(i) == 'S')) {
                    i++;
                }
                i--; // Back up one
                
                if (datePartIndex < dateParts.size()) {
                    String datePart = dateParts.get(datePartIndex++);
                    int digitCount = countDigits(datePart);
                    result.append(digitCount == 1 ? "s" : "ss");
                } else {
                    result.append("ss");
                }
            } else {
                // Literal character (separator) - preserve it
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * Counts the number of digits in a string.
     * 
     * @param str The string to count digits in
     * @return The number of digits
     */
    private int countDigits(String str) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Checks if a string contains only letters.
     * 
     * @param str The string to check
     * @return true if the string contains only letters
     */
    private boolean isAllLetters(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }
        return str.length() > 0;
    }
    
    private long parseTimeOffset(String offset) {
        String trimOffset = offset.trim();
        if (trimOffset.isEmpty()) {
            throw new IllegalArgumentException("Cannot parse empty string as time offset");
        }
        int loc = 0;
        while (loc < trimOffset.length() && Character.isDigit(trimOffset.charAt(loc))) {
            loc++;
        }
        if (loc == 0) {
            throw new IllegalArgumentException(String.format("Time offset '%s' must start with a number", offset));
        }
        long number = Long.parseLong(trimOffset.substring(0, loc));
        if (loc == trimOffset.length()) {
            return number;
        }
        while (loc < trimOffset.length() && Character.isWhitespace(trimOffset.charAt(loc))) {
            loc++;
        }
        int wordOffset = loc;
        while (loc < trimOffset.length() && Character.isLetter(trimOffset.charAt(loc))) {
            loc++;
        }
        if (wordOffset >= loc) {
            throw new IllegalArgumentException(String.format("Time offset '%s' has no valid time unit after the number", offset));
        }
        String word = trimOffset.substring(wordOffset, loc);
        switch (word.toLowerCase()) {
        case "y":
        case "year":
        case "years":
            return TimeUnit.MILLISECONDS.convert(number * 365, TimeUnit.DAYS);
        case "n":
        case "mon":
        case "mons":
        case "month":
        case "months":
            return TimeUnit.MILLISECONDS.convert(number * 30, TimeUnit.DAYS);
        case "d":
        case "day":
        case "days":
            return TimeUnit.MILLISECONDS.convert(number, TimeUnit.DAYS);
        case "h":
        case "hour":
        case "hours":
            return TimeUnit.MILLISECONDS.convert(number, TimeUnit.HOURS);
        case "m":
        case "min":
        case "mins":
        case "minute":
        case "minutes":
            return TimeUnit.MILLISECONDS.convert(number, TimeUnit.MINUTES);
        case "s":
        case "sec":
        case "secs":
        case "second":
        case "seconds":
            return TimeUnit.MILLISECONDS.convert(number, TimeUnit.SECONDS);
        default:
            throw new IllegalArgumentException(String.format("Received an invalid offset of %s the word '%s' was unexpected",
                    offset, word));
        }
    }
    
    public Object process(Map<String, Object> params) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long time;
        if (random.nextInt(101) < this.percentNull) {
            time = 0;
        }
        else {
            time = random.nextLong(this.startTime, this.endTime+1);
        }
        switch (this.fieldType) {
        case DATE:
            return time == 0 ? null : new Date(time);
        case LONG:
            return time;
        case LOCALDATE:
            return time == 0 ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).toLocalDate();
        case LOCALDATETIME:
            return time == 0 ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        case LOCALTIME:
            return time == 0 ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).toLocalTime();
        case INSTANT:
            return time == 0 ? null : Instant.ofEpochMilli(time);
        case STRING:
            // TODO: Format
//            return time == 0 ? "" : new SimpleDateFormat().format(new Date(time));
            if (time == 0) {
                return "";
            }
            LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
            return dateTimeFormatter.format(ldt);
        default:
            return time;
        }
    }
    
    public static void main(String[] args) throws Exception {
        // Example 1: Relative dates with offsets
        GenDateProcessor proc = new GenDateProcessor("3/6/89 + 35d + 12h", "now-7 days", 20, FieldType.DATE);
        System.out.println("Example 1: Relative dates with offsets");
        System.out.println(proc);
        System.out.println(proc.process(null));
        System.out.println(proc.process(null));
        System.out.println(proc.process(null));
        System.out.println();
        
        // Example 2: Word-based month format
        GenDateProcessor proc2 = new GenDateProcessor("1-Jan-2020", "31-Dec-2023", 0, FieldType.DATE);
        System.out.println("Example 2: Word-based month format (dd-MMM-yyyy)");
        System.out.println(proc2);
        System.out.println(proc2.process(null));
        System.out.println(proc2.process(null));
        System.out.println(proc2.process(null));
        System.out.println();
        
        // Example 3: Explicit format specification
        GenDateProcessor proc3 = new GenDateProcessor("02/08/2022 [M/d/y]", "12/31/2023 [M/d/y]", 0, FieldType.DATE);
        System.out.println("Example 3: Explicit format specification [M/d/y]");
        System.out.println(proc3);
        System.out.println(proc3.process(null));
        System.out.println(proc3.process(null));
        System.out.println(proc3.process(null));
        System.out.println();
        
        // Example 4: Word-based month with explicit format
        GenDateProcessor proc4 = new GenDateProcessor("15-Oct-2023 [d-MMM-yyyy]", "20-Dec-2023 [d-MMM-yyyy]", 0, FieldType.DATE);
        System.out.println("Example 4: Word-based month with explicit format [d-MMM-yyyy]");
        System.out.println(proc4);
        System.out.println(proc4.process(null));
        System.out.println(proc4.process(null));
        System.out.println(proc4.process(null));
    }
}
