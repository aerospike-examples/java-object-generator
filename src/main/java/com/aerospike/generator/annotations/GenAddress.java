package com.aerospike.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates realistic address components for test data.
 * 
 * <p>This annotation can be applied to fields of type {@code String} to generate
 * various parts of addresses, including street addresses, cities, states, countries,
 * zip codes, and geographic coordinates.</p>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Full address
 * @GenAddress(AddressPart.FULL_ADDRESS)
 * private String address;
 * 
 * // City name
 * @GenAddress(AddressPart.CITY)
 * private String city;
 * 
 * // State abbreviation
 * @GenAddress(AddressPart.STATE_ABBR)
 * private String state;
 * 
 * // Zip code
 * @GenAddress(AddressPart.ZIPCODE)
 * private String zipCode;
 * 
 * // Geographic coordinates
 * @GenAddress(AddressPart.LATITUDE)
 * private String latitude;
 * }</pre>
 * 
 * <p><b>Note:</b> When used with {@link GenMagic}, fields with names containing
 * address-related terms (e.g., "address", "city", "zipCode", "state") automatically
 * use appropriate address parts even without the annotation.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenAddress {
    /**
     * Enumeration of address components that can be generated.
     */
    public enum AddressPart {
        /** Building number only (e.g., "123") */
        BUIDING_NUMBER,
        /** City name (e.g., "New York") */
        CITY,
        /** Country name (e.g., "United States") */
        COUNTRY,
        /** Country code (e.g., "US") */
        COUNTRY_CODE,
        /** Complete address (e.g., "123 Main St, New York, NY 10001") */
        FULL_ADDRESS,
        /** Latitude coordinate as string */
        LATITUDE,
        /** Longitude coordinate as string */
        LONGITUDE,
        /** Secondary address line (e.g., "Apt 4B") */
        SECONDARY,
        /** Full state name (e.g., "New York") */
        STATE,
        /** State abbreviation (e.g., "NY") */
        STATE_ABBR,
        /** Street address (e.g., "123 Main St") */
        STREET_ADDRESS,
        /** Street address number only (e.g., "123") */
        STREET_ADDRESS_NUMBER,
        /** Street name only (e.g., "Main") */
        STREET_NAME,
        /** Street prefix (e.g., "North", "South") */
        STREET_PREFIX,
        /** Street suffix (e.g., "Street", "Avenue") */
        STREET_SUFFIX,
        /** Timezone (e.g., "America/New_York") */
        TIMEZONE,
        /** Zip/postal code (e.g., "10001") */
        ZIPCODE
    };
    
    /**
     * The address component to generate.
     * 
     * @return The address part type
     */
    AddressPart value();
}
