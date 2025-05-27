package com.aerospike.generator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class Generator {

    public interface Callback<T> {
        void process(T t);
    }
    
    public interface Factory<T> {
        T create(long id);
    }
    
    private static class DefaultConstructorFactory<T> implements Factory<T> {
        private final Constructor<T> constructor;
        private final String className;
        public DefaultConstructorFactory(Class<T> clazz) {
            this.className = clazz.getName();
            try {
                this.constructor = clazz.getDeclaredConstructor();
            } catch (NoSuchMethodException | SecurityException e) {
                throw new IllegalArgumentException(String.format("Class %s does not have a no-argument constructor, so a factory must be provided", className));
            }
        }
        
        @Override
        public T create(long id) {
            try {
                return constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new IllegalArgumentException("Could not instantiate class " + className, e);
            }
        }
    }
    
    public static class MonitorStats {
        private long startRecord;
        private long endRecord;
        private long currentRecord;
        private long successCount;
        private long failureCount;
        
        public MonitorStats(long startRecord, long endRecord, long currentRecord, long successCount,
                long failureCount) {
            super();
            this.startRecord = startRecord;
            this.endRecord = endRecord;
            this.currentRecord = currentRecord;
            this.successCount = successCount;
            this.failureCount = failureCount;
        }
        public long getStartRecord() {
            return startRecord;
        }
        public long getEndRecord() {
            return endRecord;
        }
        public long getCurrentRecord() {
            return currentRecord;
        }
        public long getSuccessCount() {
            return successCount;
        }
        public long getFailureCount() {
            return failureCount;
        }
    }
    private long startRecord;
    private long endRecord;
    private AtomicLong started = new AtomicLong(0);
    private AtomicLong success = new AtomicLong(0);
    private AtomicLong errors = new AtomicLong(0);
    private ExecutorService executor = null;
    
    public Generator(Class<?> ...seedClasses) {
        for (Class<?> thisClazz : seedClasses) {
            ValueCreatorCache.getInstance().get(thisClazz);
        }
    }

    public <T> void generate(long startId, long endId, int threads, Class<T> clazz, Callback<T> callback) {
        this.generate(startId, endId, threads, clazz, null, callback);
    }
    
    public <T> void generate(long startId, long endId, int threads, Class<T> clazz, Factory<T> factory, Callback<T> callback) {
        Factory<T> factoryToUse = factory == null ? new DefaultConstructorFactory<T>(clazz) : factory;
        ValueCreator<T> valueCreator = ValueCreatorCache.getInstance().get(clazz);
        executor = Executors.newFixedThreadPool(threads);
        startRecord = startId;
        endRecord = endId;
        started.set(startId);
        
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                while (true) {
                    long id = started.getAndIncrement();
                    if (id > endRecord) {
                        break;
                    }
                    try {
                        Map<String, Object> params = Map.of("Key", id);
                        T object = factoryToUse.create(id);
                        valueCreator.populate(object, params);
                        callback.process(object);
                        success.incrementAndGet();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        errors.incrementAndGet();
                    }
                }
                
            });
        }
        
        executor.shutdown();
    }
    
    public MonitorStats getMontiorStats() {
        return new MonitorStats(startRecord, endRecord, started.get(), success.get(), errors.get());
    }
    
    public void monitor() throws InterruptedException {
        long now = System.currentTimeMillis();
        while (!isComplete()) {
            Thread.sleep(1000);
            MonitorStats stats = getMontiorStats();
            System.out.printf("[%,dms] %,d successful, %,d failed, %,.1f%% done\n",
                    (System.currentTimeMillis() - now), stats.getSuccessCount(), stats.getFailureCount(), 
                    100.0*(stats.getSuccessCount() + stats.getFailureCount())/(1+stats.getEndRecord()-stats.getStartRecord()));
        }
    }

    
    public boolean isComplete() {
        return executor == null || executor.isTerminated();
    }
}
