# java-object-generator
This repository allows Java POJOs to have sample data generated for them. Control of the generated data is done by annotating the business objects, and multiple different annotations are supported for flexibility of data generated.

Supported annotations include:

## Supported Annoations
### GenAddress
Generate parts of a physical address. The part of the address to generate is specified by the annotation parameter, and valid values are:
* BUILDING_NUMBER: The number of a building
* CITY: A city
* COUNTRY: The full names of a country
* COUNTRY_CODE: The 2 letter code of the country
* FULL_ADDRESS: All parts of an address consolidated onto one line. 
* LATITUDE: A valid latitude value
* LONGITUDE: A valid longitude value
* SECONDARY: Apartment / Suite number
* STATE: State for the address
* STATE_ABBR: A shorter version of a state
* STREET_ADDRESS: A building number followed by a street name
* STREET_ADDRESS_NUMBER: Location on the street
* STREET_NAME: THe name of a street
* STREET_PREFIX: 
* STREET_SUFFIX: Alley, Avenue, Branch, etc. The type of the street
* TIMEZONE: A valid timezone
* ZIPCODE: A valid zipcode.

### GenBrowser
Generate the name of a browser or a user agent string similar to those coming from a real browser depending on the parameter

### GenBytes
Generate binary data to be mapped to a `byte[]` type. Use `GenHexString` to get a string representation of a byte array. The parameters control how many bytes are generated:

| Parameter value | Bytes Generated |
| :-- | :-- |
|`length`|The exact number of bytes to generates. Can be zero for an empty byte array|
|`minLength` and `maxLength`|Generate between `minLength` and `maxLength` bytes, both inclusive. `minLength` must be less than or equal to `maxLength`

### GenDate
Generate a random date in the defined range. Two parameters are required, `start` and `end`. The format of these parameters are strings and include dates and offsets. The date components can be:
* "now" - the current date and time
* dates in formats "dd/MM/yy", "dd/MM/yyyy", "dd/MM/yy-hh:mm", "dd/MM/yyyy-hh:mm", "dd/MM/yy-hh:mm:ss" or "dd/MM/yyyy-hh:mm:ss"

Offsets can also be applied onto these dates too. Offsets consist of a number followed by a unit, for example `- 30 days`, or `+10d`. 

Example date parameters would be `now - 100 years`, `now +10days`, `31/12/2000`, `1/1/2001 + 18h + 12m`

So to specify a date in a range from 100 years ago to 18 years ago, you could specify:

```
@GenDate(start = "now - 100y", end = "now - 18y")
```

A `percentNull` can be specified if some of the date should not have values assigned. This will default to zero.

This annotation can be used on fields of type `long`, `Date`, `LocalDate`, `LocalDateTime`, or `String`.

### GenEmail
Generate a valid email address. 

### GenEnum
This is only valid for fields that are an enum type, A random value from that enum will be mapped to the field.

### GenExclude
This doesn't generate any value, but rather prevents a value being assigned. This is used in conjunction with the class level annotations like `GenString`.

### GenExpression
This can be used either at the field level or class level. If used at the class level, it will apply to all string fields which do not have any other annotation. 

This annotation can be applied to either an integer or a string field. However, if the field is an integer type and the expression can only be evaluated to a String, an exception will be thrown.

Inside the expression, you can use strings enclosed in single quotes, like `Bob`. Strings can be concatenated with `&` so "`Hello` & ` World`" would yield `Hello World`. Numeric operations are:
* Addition (+)
* Subtraction (-)
* Multiplication (*)
* Division (/)
* Modulo (%)
* Power (^)

Other functions include `NOW()` which returns the current timestamp in milliseconds, and `DATE(timestamp, [format])` which  Formats a timestamp as a string

This annotation takes a map of values which can be specified in the calling code that can be referenced by prefixing with `$`. By default, `Key` is specified in the map which is a number used to generate the object. So if the format of your key should be `Person-1234` for example, this could be achieved by:

``` java
@GenExpression("`Person-` & $Key")
private String id;
```

