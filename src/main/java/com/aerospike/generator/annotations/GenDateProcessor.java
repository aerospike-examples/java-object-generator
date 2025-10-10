package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class GenDateProcessor implements Processor {
    private final long startTime;
    private final long endTime;
    private final int percentNull;
    private final FieldType fieldType;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    
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
    
    @Override
    public String toString() {
        return String.format("min: %d (%s), max %d (%s), format: %s", startTime, new Date(startTime), endTime, new Date(endTime), fieldType);
    }
    
    private Long parseDate(String dateType) {
        if (dateType == null || dateType.isEmpty()) {
            throw new IllegalArgumentException("@GenDate must take a value start date and valid end date");
        }
        List<String> strings = new ArrayList<>();
        int index = 0;
        while (true) {
            int nextPlusIndex = dateType.indexOf('+', index);
            int nextMinusIndex = dateType.indexOf('-', index);
            if (nextPlusIndex < 0 && nextMinusIndex < 0) {
                strings.add(dateType.substring(index).trim());
                break;
            }
            else if (nextPlusIndex < 0) {
                strings.add(dateType.substring(index, nextMinusIndex));
                strings.add("-");
                index = nextMinusIndex + 1;
            }
            else {
                strings.add(dateType.substring(index, nextPlusIndex).trim());
                strings.add("+");
                index = nextPlusIndex + 1;
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
        List<String> dateFormats = List.of(     // Use a list to allow in-order traversal
                "\\d{1,2}/\\d{1,2}/\\d{4}-\\d{1,2}:\\d\\d:\\d\\d", "dd/MM/yyyy-hh:mm:ss",
                "\\d{1,2}/\\d{1,2}/\\d{2}-\\d{1,2}:\\d\\d:\\d\\d", "dd/MM/yy-hh:mm:ss",
                "\\d{1,2}/\\d{1,2}/\\d{4}-\\d{1,2}:\\d\\d", "dd/MM/yyyy-hh:mm",
                "\\d{1,2}/\\d{1,2}/\\d{2}-\\d{1,2}:\\d\\d", "dd/MM/yy-hh:mm",
                "\\d{1,2}/\\d{1,2}/\\d{4}", "dd/MM/yyyy",
                "\\d{1,2}/\\d{1,2}/\\d{2}", "dd/MM/yy"
            );
        if (dateType == null) {
            return null;
        }
        if (dateType.equalsIgnoreCase("now")) {
            return new Date().getTime();
        }
        for (int i = 0; i < dateFormats.size() - 1; i+=2) {
            if (dateType.matches(dateFormats.get(i))) {
                String format = dateFormats.get(i+1);
                System.out.println(dateType);
                System.out.println(format);
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                try {
                    return sdf.parse(dateType).getTime();
                } catch (ParseException e) {
                    throw new IllegalArgumentException(String.format("Could not parse %s with format of %s", dateType, format));
                }
            }
            
        }
        return parseTimeOffset(dateType);
    }
    
    private long parseTimeOffset(String offset) {
        String trimOffset = offset.trim();
        int loc = 0;
        while (loc < offset.length() && Character.isDigit(trimOffset.charAt(loc))) {
            loc++;
        }
        long number = Long.parseLong(offset.substring(0, loc));
        if (loc == offset.length()) {
            return number;
        }
        while (loc < offset.length() && Character.isWhitespace(offset.charAt(loc))) {
            loc++;
        }
        int wordOffset = loc;
        while (loc < offset.length() && Character.isLetter(offset.charAt(loc))) {
            loc++;
        }
        String word = offset.substring(wordOffset, loc);
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
        GenDateProcessor proc = new GenDateProcessor("03/06/89 + 35d + 12h", "now-7 days", 20, FieldType.DATE);
        System.out.println(proc);
        System.out.println(proc.process(null));
        
        System.out.println(proc.process(null));
        System.out.println(proc.process(null));
    }
}
