# java-object-generator
This repository allows Java POJOs to have sample data generated for them. Control of the generated data is done by annotating the business objects, and multiple different annotations are supported for flexibility of data generated.

## Installing
This repository does not currently reside on maven central, so needs to be installed on a local maven repsoitory.

This can be achieved by:

```
cd java-object-generator
mvn clean package
mvn install:install-file -Dfile=target/java-object-generator-0.9.jar -DgroupId=com.aerospike -DartifactId=java-object-generator -Dverions=0.9 -Dpackaging=jar
```

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

Inside the expression, you can use strings enclosed in single quotes, like `Bob`. Strings can be concatenated with `&` so `'Hello' & 'World'` would yield `'Hello World'`. Numeric operations are:
* Addition (+)
* Subtraction (-)
* Multiplication (*)
* Division (/)
* Modulo (%)
* Power (^)

Other functions include `NOW()` which returns the current timestamp in milliseconds, and `DATE(timestamp, [format])` which  Formats a timestamp as a string

This annotation takes a map of values which can be specified in the calling code that can be referenced by prefixing with `$`. By default, `Key` is specified in the map which is a number used to generate the object. So if the format of your key should be `Person-1234` for example, this could be achieved by:

``` java
@GenExpression("'Person-' & $Key")
private String id;
```

## Example
Given the annotations discussed above, a fully marked up class might be:

```java
@Data
@NoArgConstructor
@GenString(type = StringType.WORDS, length = 2)
public class Member {
    public static enum MembershipLevel {
        BRONZE,
        SILVER,
        GOLD,
        PLATINUM
    }
    
    @GenExpression("'Person-' & $Key")
    private String id;
    @GenName(NameType.FIRST)
    private String firstName;
    @GenName(NameType.LAST)
    private String lastName;
    @GenEmail
    private String email;
    @GenDate(start = "now - 85years", end = "now - 18 years")
    private Date dob;
    @GenDate(start = "now - 6 years", end = "now")
    private Date dateJoined;
    @GenEnum
    private MembershipLevel level;
    @GenString(type = StringType.SENTENCES, minLength = 1, maxLength = 5)
    private String description;
    @GenAddress(AddressPart.STREET_ADDRESS)
    private String line1;
    @GenExclude     // do not generate a value for this string
    private String line2;
    @GenAddress(AddressPart.CITY)
    private String city;
    @GenAddress(AddressPart.COUNTRY)
    private String country;
    @GenAddress(AddressPart.STATE)
    private String state;
    @GenString(type = StringType.LETTERIFY, format = "???-???")
    private String licensePlate;
    @GenOneOf("Apple:10, Walmart:15, Safeway:8, Wing Stop, company1-10")
    private String employer;
    @GenUuid
    private String extId;
    @GenDate(start = "now-30d", end = "now-5m")
    private long lastUpdate;
    @GenRange(start = 0, end = 10)
    int familyMembers;
    @GenIpV4
    String ipAddress;
    
    // Dummy strings to be filled by the default 
    String dummy1;
    String dummy2;
    String dummy3;
}
```

A sample `Member` generated with this code (and reformatted for readability) would be:
```
Member [
	id=Person-1, 
	firstName=Dorian, l
	astName=Fritsch, 
	email=lavina.collier@yahoo.com, 
	dob=Wed Jun 12 03:08:09 AEST 1996, 
	dateJoined=Thu Sep 22 13:04:35 AEST 2022, 
	level=SILVER, 
	description=Accusamus omnis et eos eos unde. Aut doloremque illo., 
	line1=9887 Tasha Cliffs, 
	line2=null,
	city=North Nakia, 
	country=Mauritania, 
	state=Washington, 
	licensePlate=hfu-han, 
	employer=Walmart, 
	extId=9f86a927-1fe4-4d5c-b14f-729a396eac4c, 
	lastUpdate=1745586904478, 
	familyMembers=2, 
	ipAddress=241.226.77.11, 
	dummy1=quis quod, 
	dummy2=ullam eum, 
	dummy3=sint minima
]
```

## Generating Objects
To populate data into a single object, a `ValueCreator` is needed. This class is thread-safe and should be re-used whenever possible as it performs introspection on the class passed to the constructor to determine what annotations apply to which fields. Once a `ValueCreator` for the appropriate type has been created, simply call `populate` passing the object to set the values on, and either a parameter map to be used in `GenExpression`s or just a `long` as the key.

So the person shown in the example above could be generated with the code:

```java
ValueCreator<Memeber> valueCreator = new ValueCreator(Member.class);
Member member = valueCreator.populate(new Member(), 1);
```

To generate many instances of a class, the `Generator` class can be used. The constructor takes an optional array of classes, and will pre-instantiate an appropriate `ValueCreator` class for each of these classes. If you refer to a class which isn't pre-instantiated, a new `ValueCreator` will be created on the fly.

Then all you need to do is invoke `generate`. This takes the start id, the end id, the number of threads to use, the class of the objects to generate, an optional factory to construct the object (needed if the class to generate doesn't have a no argument constructor), and a callback which will be invoked with each fully-populated class.

An example to generate 10 Members would be:

```java
public static void main(String[] args) throws Exception{
    Generator generator = new Generator(Member.class);
    generator.generate(1, 10, 1, Member.class, 
            (member) -> System.out.println(member.toString()));
    generator.monitor();
}
```

`generate` returns once the generation threads are started off. If you want to track the progress you can either roll your own, using `Generator.isComplete()` to determine when the generation has completed, and `getMonitorStats()` to get the current stats of the generation. However, it is often easier to just use the `montitor()` method, which will block the thread until generation is complete, dumping stats to the console once a second.

