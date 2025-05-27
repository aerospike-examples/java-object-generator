package com.aerospike.generator.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.aerospike.generator.ValueCreator;
import com.aerospike.generator.ValueCreatorCache;

public class GenObjectProcessor<T> implements Processor {

    private final ValueCreator<T> valueCreator;
    private final Constructor<T> constructor;
    private final int percentNull;
    
    @SuppressWarnings("unchecked")
    public GenObjectProcessor(GenObject genObject, FieldType fieldType, Field field) {
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
        Class<T> clazz = (Class<T>) field.getType();
        this.valueCreator = (ValueCreator<T>) ValueCreatorCache.getInstance().get(clazz);
        try {
            this.constructor = (Constructor<T>) clazz.getConstructor();
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("Class " + clazz + " does not have the required no-arg constructor");
        }
        this.percentNull = Math.min(100, Math.max(0, genObject.percentNull()));
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        try {
            if (ThreadLocalRandom.current().nextInt(101) < this.percentNull) {
                return null;
            }
            T result = this.constructor.newInstance();
            this.valueCreator.populate(result, params);
            return result;
        } catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
    @Override
    public boolean supports(FieldType fieldType) {
        return fieldType == FieldType.OBJECT;
    }
}
