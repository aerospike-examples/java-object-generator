package com.aerospike.generator;

import java.awt.List;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.aerospike.generator.annotations.FieldType;
import com.aerospike.generator.annotations.GenAddress;
import com.aerospike.generator.annotations.GenAddress.AddressPart;
import com.aerospike.generator.annotations.GenAddressProcessor;
import com.aerospike.generator.annotations.GenBoolean;
import com.aerospike.generator.annotations.GenBooleanProcessor;
import com.aerospike.generator.annotations.GenBrowser;
import com.aerospike.generator.annotations.GenBrowser.BrowserType;
import com.aerospike.generator.annotations.GenBrowserProcessor;
import com.aerospike.generator.annotations.GenBytes;
import com.aerospike.generator.annotations.GenBytesProcessor;
import com.aerospike.generator.annotations.GenDate;
import com.aerospike.generator.annotations.GenDateProcessor;
import com.aerospike.generator.annotations.GenEmail;
import com.aerospike.generator.annotations.GenEmailProcessor;
import com.aerospike.generator.annotations.GenEnum;
import com.aerospike.generator.annotations.GenEnumProcessor;
import com.aerospike.generator.annotations.GenExclude;
import com.aerospike.generator.annotations.GenExcludeProcessor;
import com.aerospike.generator.annotations.GenExpression;
import com.aerospike.generator.annotations.GenExpressionProcessor;
import com.aerospike.generator.annotations.GenHexString;
import com.aerospike.generator.annotations.GenHexStringProcessor;
import com.aerospike.generator.annotations.GenIpV4;
import com.aerospike.generator.annotations.GenIpV4Processor;
import com.aerospike.generator.annotations.GenList;
import com.aerospike.generator.annotations.GenListProcessor;
import com.aerospike.generator.annotations.GenMagic;
import com.aerospike.generator.annotations.GenMagicProcessor;
import com.aerospike.generator.annotations.GenName;
import com.aerospike.generator.annotations.GenNameProcessor;
import com.aerospike.generator.annotations.GenNumber;
import com.aerospike.generator.annotations.GenNumberProcessor;
import com.aerospike.generator.annotations.GenObject;
import com.aerospike.generator.annotations.GenObjectProcessor;
import com.aerospike.generator.annotations.GenOneOf;
import com.aerospike.generator.annotations.GenOneOfProcessor;
import com.aerospike.generator.annotations.GenRange;
import com.aerospike.generator.annotations.GenRangeProcessor;
import com.aerospike.generator.annotations.GenString;
import com.aerospike.generator.annotations.GenString.StringType;
import com.aerospike.generator.annotations.GenStringProcessor;
import com.aerospike.generator.annotations.GenUuid;
import com.aerospike.generator.annotations.GenUuidProcessor;
import com.aerospike.generator.annotations.Processor;

/**
 * A {@code ValueCreator} is used to populate test data into objects of a single class. It is thread-safe and should be reused across
 * different instantiations of that class.
 * @param <T>
 */
public class ValueCreator<T> {
    
    private final Map<Field, Processor> fieldProcessors = new ConcurrentHashMap<>();
    private final ValueCreator<Object> superclazz;
    private final Constructor<T> constructor;
    private final Class<T> clazz;

    public ValueCreator(Class<T> clazz) {
        this.clazz = clazz;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // Note: Deliberately allowing setting of final fields
            if (!Modifier.isTransient(field.getModifiers())) {
                addProcessorForField(field);
            }
        }
        Class<?> mySuperClazz = clazz.getSuperclass();
        if (mySuperClazz != null) {
            String superclassPackage = mySuperClazz.getPackageName();
            if (superclassPackage != null && !(superclassPackage.startsWith("java.lang") || superclassPackage.startsWith("java.util"))) {
                superclazz = (ValueCreator<Object>) ValueCreatorCache.getInstance().get(mySuperClazz);
            }
            else {
                superclazz = null;
            }
        }
        else {
            superclazz = null;
        }
        Constructor<T> theConstructor = null;
        try {
            theConstructor = (Constructor<T>) clazz.getConstructor();
        } catch (NoSuchMethodException | SecurityException e) {
        }
        this.constructor = theConstructor;
    }
    
    public void requiresConstructor() {
        if (this.constructor == null) {
            throw new IllegalArgumentException("Class " + clazz + " does not have the required no-arg constructor");
        }
    }
    
    public T create() {
        requiresConstructor();
        try {
            return this.constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
    
    public T createAndPopulate(Map<String, Object> params) {
        T obj = create();
        try {
            return populate(obj, params);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
    private <P extends Annotation> boolean checkAndUse(boolean alreadyFound, Field field, FieldType fieldType, Class<P> annotation, Class<? extends Processor> processor) {
        if (alreadyFound) {
            return true;
        }
        P gen = field.getAnnotation(annotation);
        if (gen != null) {
            Constructor<? extends Processor> constructor;
            try {
                constructor = processor.getConstructor(annotation, FieldType.class, Field.class);
                Processor proc = constructor.newInstance(gen, fieldType, field);
                fieldProcessors.put(field,  proc);
                return true;
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    
    private void addProcessorForField(Field field) {
        Class<?> clazz = field.getDeclaringClass();
        FieldType fieldType = mapFieldType(field);
        boolean found = false;
        found = checkAndUse(found, field, fieldType, GenAddress.class, GenAddressProcessor.class);
        found = checkAndUse(found, field, fieldType, GenBoolean.class, GenBooleanProcessor.class);
        found = checkAndUse(found, field, fieldType, GenBrowser.class, GenBrowserProcessor.class);
        found = checkAndUse(found, field, fieldType, GenBytes.class, GenBytesProcessor.class);
        found = checkAndUse(found, field, fieldType, GenDate.class, GenDateProcessor.class);
        found = checkAndUse(found, field, fieldType, GenEmail.class, GenEmailProcessor.class);
        found = checkAndUse(found, field, fieldType, GenEnum.class, GenEnumProcessor.class);
        found = checkAndUse(found, field, fieldType, GenExclude.class, GenExcludeProcessor.class);
        found = checkAndUse(found, field, fieldType, GenExpression.class, GenExpressionProcessor.class);
        found = checkAndUse(found, field, fieldType, GenHexString.class, GenHexStringProcessor.class);
        found = checkAndUse(found, field, fieldType, GenIpV4.class, GenIpV4Processor.class);
        found = checkAndUse(found, field, fieldType, GenList.class, GenListProcessor.class);
        found = checkAndUse(found, field, fieldType, GenName.class, GenNameProcessor.class);
        found = checkAndUse(found, field, fieldType, GenNumber.class, GenNumberProcessor.class);
        found = checkAndUse(found, field, fieldType, GenObject.class, GenObjectProcessor.class);
        found = checkAndUse(found, field, fieldType, GenOneOf.class, GenOneOfProcessor.class);
        found = checkAndUse(found, field, fieldType, GenRange.class, GenRangeProcessor.class);
        found = checkAndUse(found, field, fieldType, GenString.class, GenStringProcessor.class);
        found = checkAndUse(found, field, fieldType, GenUuid.class, GenUuidProcessor.class);
        
        
        if (!found) {
            GenMagic genMagic = clazz.getAnnotation(GenMagic.class);
            if (genMagic != null) {
                Processor processor = new GenMagicProcessor(genMagic, fieldType, field);
                fieldProcessors.put(field, processor);
                found = true;
            }
        }
        if (!found) {
            GenString genString = clazz.getAnnotation(GenString.class);
            if (genString != null && field.getType().isAssignableFrom(String.class)) {
                Processor processor = new GenStringProcessor(genString, fieldType, field);
                fieldProcessors.put(field, processor);
                found = true;
            }
        }
        if (!found) {
            GenExpression genExpr = clazz.getAnnotation(GenExpression.class);
            if (genExpr != null && field.getType().isAssignableFrom(String.class)) {
                Processor processor = new GenExpressionProcessor(genExpr, fieldType, field);
                fieldProcessors.put(field, processor);
                found = true;
            }
        }
        if (found) {
            field.setAccessible(true);
        }
    }
    
    private FieldType mapFieldType(Field field) {
        Class<?> clazz = field.getType();
        if (clazz.isEnum()) {
            return FieldType.ENUM;
        }
        else if (Boolean.TYPE.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz)) {
            return FieldType.BOOLEAN;
        }
        else if (Double.TYPE.isAssignableFrom(clazz) || Double.class.isAssignableFrom(clazz)) {
            return FieldType.DOUBLE;
        }
        else if (Float.TYPE.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz)) {
            return FieldType.FLOAT;
        }
        else if (Integer.TYPE.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz)) {
            return FieldType.INTEGER;
        }
        else if (Long.TYPE.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)) {
            return FieldType.LONG;
        }
        else if (String.class.isAssignableFrom(clazz)) {
            return FieldType.STRING;
        }
        else if (Date.class.isAssignableFrom(clazz)) {
            return FieldType.DATE;
        }
        else if (LocalDate.class.isAssignableFrom(clazz)) {
            return FieldType.LOCALDATE;
        }
        else if (LocalDateTime.class.isAssignableFrom(clazz)) {
            return FieldType.LOCALDATETIME;
        }
        else if (UUID.class.isAssignableFrom(clazz)) {
            return FieldType.UUID;
        }
        else if (clazz.isArray()) {
            Class<?> elementType = clazz.getComponentType();
            if (Byte.class.equals(elementType) ||  Byte.TYPE.equals(elementType)) {
                return FieldType.BYTES;
            }
            else {
                return FieldType.LIST;
            }
        }
        else if (List.class.isAssignableFrom(clazz)) {
            return FieldType.LIST;
        }
        else if (!clazz.isPrimitive()) {
            return FieldType.OBJECT;
        }
        return null;
    }
    public T populate(T object, long key) throws IllegalArgumentException, IllegalAccessException { 
        return this.populate(object, Map.of("Key", key));
    }
    
    public T populate(T object, Map<String, Object> params) throws IllegalArgumentException, IllegalAccessException {
        for (Field field : fieldProcessors.keySet()) {
            field.set(object, fieldProcessors.get(field).process(params));
        }
        if (superclazz != null) {
            superclazz.populate(object, params);
        }
        return object;
    }
    
    private static final int ITERATORS = 10000;
    private static long time(Processor processor) {
        long now = System.nanoTime();
        for (int i = 0; i < ITERATORS; i++) {
            processor.process(Map.of("Key", i));
        }
        return (System.nanoTime() - now)/1000;
    }
    
    private static void benchmark(Processor processor, String name) {
        long time = time(processor);
        System.out.printf("%,11dus total, %,8dus per iteration - %s\n", time, time / ITERATORS, name);
    }
    public static void main(String[] args) {
        benchmark(new GenOneOfProcessor("flipkart:78, swiggy:78, amazon:78, merchant[1-100]", FieldType.STRING), "GenOneOf");
        benchmark(new GenEmailProcessor(null, FieldType.STRING, null), "GenEmail");
        benchmark(new GenExpressionProcessor("'Trans-' & $Key", FieldType.STRING), "GenExpression");
        benchmark(new GenUuidProcessor(null, FieldType.STRING, null), "GenUuid");
        benchmark(new GenStringProcessor(StringType.REGEXIFY, 0,0,0, "[a-z]{6}:\\d{1,7}-\\d{3}-\\d{2}", FieldType.STRING), "GenString - REGEXIFY");
        benchmark(new GenStringProcessor(StringType.LETTERIFY, 0,0,0, "???-???", FieldType.STRING), "GenString - LETTERIFY");
        benchmark(new GenStringProcessor(StringType.WORDS, 2,4,-1, null, FieldType.STRING), "GenString - WORDS");
        benchmark(new GenAddressProcessor(AddressPart.COUNTRY, FieldType.STRING), "GenAddress");
        benchmark(new GenIpV4Processor(null, FieldType.STRING, null), "GenIpV4");
        benchmark(new GenBrowserProcessor(BrowserType.USERAGENT, FieldType.STRING), "GenBrowser");
        benchmark(new GenDateProcessor("now-30d", "now-3m", 10, FieldType.STRING), "GenDate");
        
    }
}
