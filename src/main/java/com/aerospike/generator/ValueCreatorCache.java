package com.aerospike.generator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ValueCreatorCache {
    private final Map<Class<?>, ValueCreator<?>> valueCreators = new ConcurrentHashMap<>();
    
    private final static ValueCreatorCache INSTANCE = new ValueCreatorCache();
    private ValueCreatorCache() {}
    
    public static ValueCreatorCache getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    private synchronized <T> ValueCreator<T> getValueCreator(Class<T> clazz) {
        return (ValueCreator<T>) valueCreators.get(clazz);
    }
    
    private synchronized <T> void addToCache(Class<T> clazz, ValueCreator<T> valueCreator) {
        this.valueCreators.put(clazz, valueCreator);
    }
    
    public <T> ValueCreator<T> get(Class<T> clazz) {
        if (clazz == null || clazz.equals(Object.class)) {
            return null;
        }
        
        ValueCreator<T> result = getValueCreator(clazz);
        if (result == null) {
            result = new ValueCreator<>(clazz);
            addToCache(clazz, result);
        }
        return result;
    }
}
