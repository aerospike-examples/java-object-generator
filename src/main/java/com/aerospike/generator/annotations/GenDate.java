package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates date/time values for test data.
 * 
 * <p>This annotation can be applied to fields of type {@code Date}, {@code LocalDate},
 * {@code LocalDateTime}, {@code LocalTime}, or {@code Instant} to generate random
 * dates within a specified range.</p>
 * 
 * <h3>Date Range Format</h3>
 * <p>The {@code start} and {@code end} parameters support multiple formats:</p>
 * 
 * <h4>Relative Date Format</h4>
 * <ul>
 *   <li>{@code "now"} - Current date/time</li>
 *   <li>{@code "now-1d"} - 1 day ago</li>
 *   <li>{@code "now-1w"} - 1 week ago</li>
 *   <li>{@code "now-1M"} - 1 month ago</li>
 *   <li>{@code "now-1y"} - 1 year ago</li>
 *   <li>{@code "now+1d"} - 1 day from now</li>
 *   <li>{@code "now+1w"} - 1 week from now</li>
 *   <li>{@code "now+1M"} - 1 month from now</li>
 *   <li>{@code "now+1y"} - 1 year from now</li>
 * </ul>
 * 
 * <p>You can combine units: {@code "now-2y-3M-5d"} means 2 years, 3 months, and 5 days ago.</p>
 * 
 * <h4>Static Date Formats</h4>
 * <p>The following date formats are automatically recognized:</p>
 * <ul>
 *   <li>{@code "dd/MM/yyyy"} - e.g., "15/03/2023"</li>
 *   <li>{@code "dd/MM/yy"} - e.g., "15/03/23"</li>
 *   <li>{@code "dd/MM/yyyy-hh:mm:ss"} - e.g., "15/03/2023-14:30:45"</li>
 *   <li>{@code "dd/MM/yy-hh:mm:ss"} - e.g., "15/03/23-14:30:45"</li>
 *   <li>{@code "dd/MM/yyyy-hh:mm"} - e.g., "15/03/2023-14:30"</li>
 *   <li>{@code "dd/MM/yy-hh:mm"} - e.g., "15/03/23-14:30"</li>
 *   <li>{@code "dd-MMM-yyyy"} - e.g., "3-Oct-1923", "15-Jan-2023"</li>
 *   <li>{@code "dd-MMM-yy"} - e.g., "3-Oct-23", "15-Jan-23"</li>
 *   <li>{@code "dd MMM yyyy"} - e.g., "3 Oct 1923", "15 Jan 2023" (space-separated)</li>
 *   <li>{@code "dd MMM yy"} - e.g., "3 Oct 23", "15 Jan 23" (space-separated)</li>
 *   <li>{@code "dd/MMM/yyyy"} - e.g., "3/Oct/1923", "15/Jan/2023" (slash-separated)</li>
 *   <li>{@code "dd/MMM/yy"} - e.g., "3/Oct/23", "15/Jan/23" (slash-separated)</li>
 * </ul>
 * 
 * <h4>Explicit Format Specification</h4>
 * <p>You can specify an explicit format using square brackets. The format template uses
 * simplified symbols that are automatically converted to SimpleDateFormat patterns:</p>
 * <ul>
 *   <li>{@code "y"}, {@code "yy"}, {@code "yyyy"} - Year (1, 2, or 4 digits)</li>
 *   <li>{@code "M"}, {@code "MM"} - Month as number (1-2 digits or 2 digits)</li>
 *   <li>{@code "MMM"} - Month as abbreviation (e.g., "Jan", "Oct")</li>
 *   <li>{@code "d"}, {@code "dd"} - Day (1-2 digits or 2 digits)</li>
 *   <li>{@code "h"}, {@code "hh"} - Hour (1-2 digits or 2 digits)</li>
 *   <li>{@code "m"}, {@code "mm"} - Minute (1-2 digits or 2 digits)</li>
 *   <li>{@code "s"}, {@code "ss"} - Second (1-2 digits or 2 digits)</li>
 * </ul>
 * 
 * <p>Examples:</p>
 * <ul>
 *   <li>{@code "02/08/2022 [M/d/y]"} - Explicitly specifies month/day/year format</li>
 *   <li>{@code "15-Oct-2023 [d-MMM-yyyy]"} - Day-month abbreviation-year</li>
 *   <li>{@code "2023-08-02 14:30:45 [yyyy-M-d h:m:s]"} - Full date-time format</li>
 * </ul>
 * 
 * <p><b>Note:</b> The format template symbols are counted to determine the SimpleDateFormat
 * pattern. For example, {@code "y"} becomes {@code "y"}, {@code "yy"} becomes {@code "yy"},
 * and {@code "yyyy"} becomes {@code "yyyy"}. The same applies to other symbols.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Generate dates in the past year
 * @GenDate(start = "now-1y", end = "now")
 * private Date lastLoginDate;
 * 
 * // Generate future dates (appointments)
 * @GenDate(start = "now", end = "now+1y")
 * private LocalDate appointmentDate;
 * 
 * // Generate birth dates (18-90 years ago)
 * @GenDate(start = "now-90y", end = "now-18y")
 * private LocalDate dateOfBirth;
 * 
 * // Use static date format
 * @GenDate(start = "01/01/2020", end = "31/12/2023")
 * private Date dateRange;
 * 
 * // Use word-based month format
 * @GenDate(start = "1-Jan-2020", end = "31-Dec-2023")
 * private Date dateRangeWithMonths;
 * 
 * // Use explicit format specification
 * @GenDate(start = "02/08/2022 [M/d/y]", end = "12/31/2023 [M/d/y]")
 * private Date explicitFormat;
 * 
 * // Allow some null values
 * @GenDate(start = "now-5y", end = "now", percentNull = 10)
 * private LocalDateTime optionalDate;
 * }</pre>
 * 
 * <p><b>Note:</b> The generated date type automatically matches the field type
 * (Date, LocalDate, LocalDateTime, LocalTime, or Instant).</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenDate {
    /**
     * The start of the date range (inclusive).
     * 
     * <p>Uses relative date format like "now-1y" (1 year ago) or "now+1M" (1 month from now).</p>
     * 
     * @return The start date expression
     */
    String start();
    
    /**
     * The end of the date range (inclusive).
     * 
     * <p>Uses relative date format like "now-1y" (1 year ago) or "now+1M" (1 month from now).</p>
     * 
     * @return The end date expression
     */
    String end();
    
    /**
     * Percentage chance (0-100) that the generated value will be null.
     * 
     * <p>Useful for modeling optional date fields. For example, {@code percentNull = 20}
     * means 20% of generated values will be null.</p>
     * 
     * @return The null percentage (0-100), default is 0 (never null)
     */
    int percentNull() default 0;
}
