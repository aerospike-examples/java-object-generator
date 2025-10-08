package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.aerospike.generator.ValueCreator;
import com.aerospike.generator.ValueCreatorCache;

public class GenObjectProcessor<T> implements Processor {

    private final ValueCreator<T> valueCreator;
    private final int percentNull;
    private final Class<?>[] subclasses;
    
    public GenObjectProcessor(GenObject genObject, FieldType fieldType, Field field) {
        this(genObject.subclasses(), genObject.percentNull(), fieldType, field);
    }
    @SuppressWarnings("unchecked")
    public GenObjectProcessor(Class<?>[] subclasses, int percentNull, FieldType fieldType, Field field) {
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
        Class<T> clazz = (Class<T>) field.getType();
        this.valueCreator = (ValueCreator<T>) ValueCreatorCache.getInstance().get(clazz);
        this.valueCreator.requiresConstructor();
        this.percentNull = Math.min(100, Math.max(0, percentNull));
        if (subclasses == null) {
            subclasses = new Class<?>[0];
        }
        this.subclasses = subclasses;
        for (Class<?> subclass : subclasses) {
            if (!clazz.isAssignableFrom(subclass)) {
                throw new IllegalArgumentException(String.format("Class % is listed as a subclass on field %s of class %s, but is not a subclass",
                        subclass.getName(), field.getName(), clazz.getName()));
            }
            if (Modifier.isAbstract(subclass.getModifiers())) {
                throw new IllegalArgumentException(String.format("Class % is listed as a subclass on field %s of class %s, but is abstract. Only concrete classes can be listed",
                        subclass.getName(), field.getName(), clazz.getName()));
            }
            // Make sure this subclass in in the cache
            ValueCreatorCache.getInstance().get(subclass);
        }
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        if (ThreadLocalRandom.current().nextInt(101) < this.percentNull) {
            return null;
        }
        if (subclasses.length == 0) {
            return this.valueCreator.createAndPopulate(params, false);
        }
        else {
            Class<?> subclass = subclasses[ThreadLocalRandom.current().nextInt(subclasses.length)];
            ValueCreator<?> creator = ValueCreatorCache.getInstance().get(subclass);
            return creator.createAndPopulate(params);
        }
    }
    public boolean supports(FieldType fieldType) {
        return fieldType == FieldType.OBJECT;
    }
}
