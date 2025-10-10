package com.aerospike.generator.annotations;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.aerospike.generator.ValueCreator;
import com.aerospike.generator.ValueCreatorCache;
import com.aerospike.generator.annotations.GenString.StringType;

public class GenSetProcessor<T> implements Processor {

    private final int minItems;
    private final int maxItems;
    private final int items;
    private final ValueCreator<T> valueCreator;
    private final int percentNull;
    private final Class<?>[] subclasses;
    private final Class<?> elementType;
    private final Processor processor;
    
    public GenSetProcessor(GenSet genSet, FieldType fieldType, Field field) {
        this(genSet.subclasses(), genSet.percentNull(), genSet.minItems(), genSet.maxItems(), 
                genSet.items(), genSet.stringLength(), genSet.minStringLength(), genSet.maxStringLength(),
                genSet.stringType(), genSet.stringPattern(), genSet.stringOptions(), fieldType, field);
    }
    
    @SuppressWarnings("unchecked")
    public GenSetProcessor(Class<?>[] subclasses, int percentNull, int minItems, int maxItems, 
            int items, FieldType fieldType, Field field) {
        this(subclasses, percentNull, minItems, maxItems, items, -1, 3, 10, 
                GenString.StringType.WORDS, "", "", fieldType, field);
    }
    
    @SuppressWarnings("unchecked")
    public GenSetProcessor(Class<?>[] subclasses, int percentNull, int minItems, int maxItems, 
            int items, int stringLength, int minStringLength, int maxStringLength,
            GenString.StringType stringType, String stringPattern, String stringOptions, 
            FieldType fieldType, Field field) {
        
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
        this.percentNull = Math.min(100, Math.max(0, percentNull));
        if (subclasses == null) {
            subclasses = new Class<?>[0];
        }
        this.subclasses = subclasses;
        this.minItems = minItems;
        this.maxItems = maxItems;
        this.items = items;
        
        if (items < 0 && (minItems < 0 || maxItems < 0)) {
            throw new IllegalArgumentException("Either items must be specified or both minItems and maxItems");
        }
        if (items >= 0) {
            if (minItems >= 0 || maxItems >= 0) {
                throw new IllegalArgumentException("Neither minItems nor maxItems can be specified if items is specified");
            }
        }
        else if (minItems < 0 || maxItems < 0) {
            throw new IllegalArgumentException("Both minItems and maxItems must be specified, or just specify items");
        }

        // Determine element type from Set<?>
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            this.elementType = (Class<?>) pt.getActualTypeArguments()[0];
        } else {
            throw new IllegalArgumentException(String.format("Field %s of class %s is a Set but the type it contains could not be determined",
                    field.getName(), field.getDeclaringClass().getName()));
        }
        
        if (String.class.isAssignableFrom(elementType)) {
            // Use flexible string generation based on parameters
            if (!stringOptions.isEmpty()) {
                // Use GenOneOfProcessor for predefined options
                processor = new GenOneOfProcessor(stringOptions, FieldType.STRING);
            } else if (!stringPattern.isEmpty()) {
                // Use GenStringProcessor with custom pattern
                processor = new GenStringProcessor(StringType.REGEXIFY, stringLength, stringLength, -1, stringPattern, FieldType.STRING);
            } else {
                // Use GenStringProcessor with specified type and length
                if (stringLength >= 0) {
                    processor = new GenStringProcessor(stringType, -1, -1, stringLength, "", FieldType.STRING);
                } else {
                    processor = new GenStringProcessor(stringType, minStringLength, maxStringLength, -1, "", FieldType.STRING);
                }
            }
        }
        else if (Long.class.isAssignableFrom(elementType)) {
            processor = new GenNumberProcessor(0, 100, FieldType.LONG);
        }
        else if (Integer.class.isAssignableFrom(elementType)) {
            processor = new GenNumberProcessor(0, 100, FieldType.INTEGER);
        }
        else if (Double.class.isAssignableFrom(elementType)) {
            processor = new GenNumberProcessor(0, 1000, FieldType.DOUBLE);
        }
        else if (Float.class.isAssignableFrom(elementType)) {
            processor = new GenNumberProcessor(0, 1000, FieldType.FLOAT);
        }
        else if (Boolean.class.isAssignableFrom(elementType)) {
            processor = new GenBooleanProcessor(null, FieldType.BOOLEAN);
        }
        else {
            processor = null;
        }
        
        if (processor == null) {
            this.valueCreator = (ValueCreator<T>) ValueCreatorCache.getInstance().get(elementType);
            this.valueCreator.requiresConstructor();
        
            for (Class<?> subclass : subclasses) {
                if (!elementType.isAssignableFrom(subclass)) {
                    throw new IllegalArgumentException(String.format("Class %s is listed as a subclass on field %s of class %s, but is not a subclass",
                            subclass.getName(), field.getName(), elementType.getName()));
                }
                if (Modifier.isAbstract(subclass.getModifiers())) {
                    throw new IllegalArgumentException(String.format("Class %s is listed as a subclass on field %s of class %s, but is abstract. Only concrete classes can be listed",
                            subclass.getName(), field.getName(), elementType.getName()));
                }
                // Make sure this subclass in in the cache
                ValueCreatorCache.getInstance().get(subclass);
            }
        }
        else {
            this.valueCreator = null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private String pushParamKey(Map<String, Object> params, int index) {
        List<String> keys = (List<String>) params.get("__KeyStack");
        if (keys == null) {
            keys = new ArrayList<>();
            params.put("__KeyStack", keys);
        }
        String thisKeyPrefix = (keys.size() == 0) ? "Key" : keys.get(keys.size()-1);
        String newKey = thisKeyPrefix + "." + this.elementType.getSimpleName();
        keys.add(newKey);
        params.put(newKey, index);
        return newKey;
    }
    
    @SuppressWarnings("unchecked")
    private void popParamKey(Map<String, Object> params) {
        List<String> keys = (List<String>) params.get("__KeyStack");
        String thisKey = keys.remove(keys.size()-1);
        params.remove(thisKey);
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        if (ThreadLocalRandom.current().nextInt(101) < this.percentNull) {
            return null;
        }
        int length = Processor.getLengthToGenerate(items, minItems, maxItems);
        
        // Generate items and add to set (Set automatically handles duplicates)
        Set<Object> set = new HashSet<>();
        int attempts = 0;
        int maxAttempts = length * 10; // Prevent infinite loops
        
        while (set.size() < length && attempts < maxAttempts) {
            Object thisObject;
            pushParamKey(params, set.size()); 
            if (processor != null) {
                thisObject = processor.process(params);
            }
            else if (subclasses.length == 0) {
                thisObject = this.valueCreator.createAndPopulate(params, false);
            }
            else {
                Class<?> subclass = subclasses[ThreadLocalRandom.current().nextInt(subclasses.length)];
                ValueCreator<?> creator = ValueCreatorCache.getInstance().get(subclass);
                thisObject = creator.createAndPopulate(params);
            }
            set.add(thisObject);
            popParamKey(params);
            attempts++;
        }
        
        return set;
    }
    
    public boolean supports(FieldType fieldType) {
        return fieldType == FieldType.SET;
    }
}
