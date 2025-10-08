package com.aerospike.generator.annotations;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.aerospike.generator.ValueCreator;
import com.aerospike.generator.ValueCreatorCache;
import com.aerospike.generator.annotations.GenString.StringType;

public class GenListProcessor<T> implements Processor {

    private final int minItems;
    private final int maxItems;
    private final int items;
    private final ValueCreator<T> valueCreator;
    private final int percentNull;
    private final Class<?>[] subclasses;
    private final boolean isArray;
    private final Class<?> elementType;
    private final Processor processor;
    
    public GenListProcessor(GenList genList, FieldType fieldType, Field field) {
        this(genList.subclasses(), genList.percentNull(), genList.minItems(), genList.maxItems(), 
                genList.items(), fieldType, field);
    }
    @SuppressWarnings("unchecked")
    public GenListProcessor(Class<?>[] subclasses, int percentNull, int minItems, int maxItems, 
            int items, FieldType fieldType, Field field) {
        
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

        
        this.isArray = field.getType().isArray();
        Class<?> elementType = null;
        if (isArray) {
            elementType = field.getType().getComponentType();
        }
        else {
            // This is a List type, check it's parameterized
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                elementType = (Class<?>) pt.getActualTypeArguments()[0];
            }
            
        }
        if (elementType == null) {
            throw new IllegalArgumentException(String.format("Field %s of class %s is %s but the type it contains could not be determined",
                    field.getName(), field.getDeclaringClass().getName(), isArray ? "an array" : "a list"));
        }
        
        if (String.class.isAssignableFrom(elementType)) {
            processor = new GenStringProcessor(StringType.WORDS, 1, 5, -1, null, FieldType.STRING);
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
        
        this.elementType = elementType;
        if (processor == null) {
            this.valueCreator = (ValueCreator<T>) ValueCreatorCache.getInstance().get(elementType);
            this.valueCreator.requiresConstructor();
        
            for (Class<?> subclass : subclasses) {
                if (!elementType.isAssignableFrom(subclass)) {
                    throw new IllegalArgumentException(String.format("Class % is listed as a subclass on field %s of class %s, but is not a subclass",
                            subclass.getName(), field.getName(), elementType.getName()));
                }
                if (Modifier.isAbstract(subclass.getModifiers())) {
                    throw new IllegalArgumentException(String.format("Class % is listed as a subclass on field %s of class %s, but is abstract. Only concrete classes can be listed",
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
        Object objs = Array.newInstance(elementType, length);
        for (int i = 0; i < length; i++) {
            Object thisObject;
            pushParamKey(params, i); 
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
            Array.set(objs, i, thisObject);
            popParamKey(params);
        }
        if (isArray ) {
            return objs;
        }
        else {
            // Do NOT use
            // Arrays.asList(objs); 
            // here! This gives a list of size one which contains an array.
            List<Object> list = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                list.add(Array.get(objs, i));
            }
            return list;
        }
    }
    public boolean supports(FieldType fieldType) {
        return fieldType == FieldType.OBJECT;
    }
}
