package com.aerospike.generator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class Generator {

    public interface MonitorCallback {
        String addExtraInfo();
    }
    
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

    public <T> Generator generate(long startId, long endId, int threads, Class<T> clazz, Callback<T> callback) {
        return this.generate(startId, endId, threads, clazz, null, callback);
    }
    
    public <T> Generator generate(long startId, long endId, int threads, Class<T> clazz, Factory<T> factory, Callback<T> callback) {
        Factory<T> factoryToUse = factory == null ? new DefaultConstructorFactory<T>(clazz) : factory;
        ValueCreator<T> valueCreator = ValueCreatorCache.getInstance().get(clazz);
        int threadsToUse = threads <= 0 ? Runtime.getRuntime().availableProcessors() : threads;
        executor = Executors.newFixedThreadPool(threadsToUse);
        startRecord = startId;
        endRecord = endId;
        started.set(startId);
        
        for (int i = 0; i < threadsToUse; i++) {
            executor.submit(() -> {
                while (true) {
                    long id = started.getAndIncrement();
                    if (id > endRecord) {
                        break;
                    }
                    try {
                        // The map must be mutable, so cannot use Map.of
                        Map<String, Object> params = new HashMap<>();
                        params.put("Key", id);
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
        return this;
    }
    
    public MonitorStats getMontiorStats() {
        return new MonitorStats(startRecord, endRecord, started.get(), success.get(), errors.get());
    }

    public void monitor() throws InterruptedException {
        this.monitor(null);
    }
    public void monitor(MonitorCallback extraInfo) throws InterruptedException {
        long now = System.currentTimeMillis();
        while (!isComplete()) {
            Thread.sleep(1000);
            MonitorStats stats = getMontiorStats();
            String extraInfoStr = extraInfo == null ? "" : extraInfo.addExtraInfo();
            System.out.printf("[%,dms] %,d successful, %,d failed, %,.1f%% done %s\n",
                    (System.currentTimeMillis() - now), stats.getSuccessCount(), stats.getFailureCount(), 
                    100.0*(stats.getSuccessCount() + stats.getFailureCount())/(1+stats.getEndRecord()-stats.getStartRecord()),
                    extraInfoStr);
        }
    }

    
    public boolean isComplete() {
        return executor == null || executor.isTerminated();
    }
}
