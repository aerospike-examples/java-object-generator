package com.aerospike.generator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A high-level generator for creating and populating test data objects in bulk.
 * 
 * <p>This class provides a fluent API for generating large numbers of test objects
 * with configurable threading, monitoring, and callback processing. It uses
 * {@link ValueCreator} instances internally to populate objects based on their
 * annotations.</p>
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * Generator generator = new Generator(Member.class);
 * generator.generate(1, 1000, Member.class, 
 *     (member) -> System.out.println(member.toString()));
 * generator.monitor();
 * }</pre>
 * 
 * <p>The generator is thread-safe and can process multiple objects concurrently
 * using a configurable thread pool. It automatically handles object creation,
 * population, and error tracking.</p>
 */
public class Generator {

    /**
     * Callback interface for providing additional monitoring information during generation.
     * 
     * <p>This callback is invoked periodically during monitoring to allow custom
     * statistics or information to be displayed alongside the default progress output.</p>
     */
    public interface MonitorCallback {
        /**
         * Returns additional information to be displayed during monitoring.
         * 
         * @return A string containing additional monitoring information, or an empty string
         */
        String addExtraInfo();
    }
    
    /**
     * Callback interface for processing each generated object.
     * 
     * <p>This callback is invoked for each successfully generated object, allowing
     * custom processing such as saving to a database, writing to a file, or performing
     * validation.</p>
     * 
     * @param <T> The type of object being generated
     */
    public interface Callback<T> {
        /**
         * Processes a generated object.
         * 
         * <p>This method is called for each successfully generated object. Any exceptions
         * thrown by this method will be caught and counted as errors.</p>
         * 
         * @param t The generated object to process
         */
        void process(T t);
    }
    
    /**
     * Factory interface for creating object instances.
     * 
     * <p>By default, the generator uses reflection to create objects via their
     * no-argument constructor. This interface allows custom object creation logic,
     * such as using builders, dependency injection, or other factory patterns.</p>
     * 
     * @param <T> The type of object to create
     */
    public interface Factory<T> {
        /**
         * Creates a new instance of type T.
         * 
         * <p>The provided id can be used to seed the object creation process or
         * to ensure unique instances.</p>
         * 
         * @param id A unique identifier for this object instance
         * @return A new instance of type T
         */
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
    
    /**
     * Statistics about the current generation process.
     * 
     * <p>This class provides snapshot statistics about the generation progress,
     * including the range of records being generated, current position, and
     * success/failure counts.</p>
     */
    public static class MonitorStats {
        private long startRecord;
        private long endRecord;
        private long currentRecord;
        private long successCount;
        private long failureCount;
        
        /**
         * Creates a new MonitorStats instance.
         * 
         * @param startRecord The starting record ID
         * @param endRecord The ending record ID (inclusive)
         * @param currentRecord The current record ID being processed
         * @param successCount The number of successfully generated objects
         * @param failureCount The number of failed generations
         */
        public MonitorStats(long startRecord, long endRecord, long currentRecord, long successCount,
                long failureCount) {
            super();
            this.startRecord = startRecord;
            this.endRecord = endRecord;
            this.currentRecord = currentRecord;
            this.successCount = successCount;
            this.failureCount = failureCount;
        }
        
        /**
         * Gets the starting record ID for this generation run.
         * 
         * @return The starting record ID
         */
        public long getStartRecord() {
            return startRecord;
        }
        
        /**
         * Gets the ending record ID for this generation run (inclusive).
         * 
         * @return The ending record ID
         */
        public long getEndRecord() {
            return endRecord;
        }
        
        /**
         * Gets the current record ID being processed.
         * 
         * @return The current record ID
         */
        public long getCurrentRecord() {
            return currentRecord;
        }
        
        /**
         * Gets the number of successfully generated objects.
         * 
         * @return The success count
         */
        public long getSuccessCount() {
            return successCount;
        }
        
        /**
         * Gets the number of failed generations.
         * 
         * @return The failure count
         */
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
    
    /**
     * Creates a new Generator instance and pre-initializes ValueCreator caches for the specified classes.
     * 
     * <p>Pre-initializing the cache can improve performance by avoiding lazy initialization
     * during the first generation run. This is especially useful when generating large
     * numbers of objects.</p>
     * 
     * @param seedClasses The classes to pre-initialize in the ValueCreator cache
     */
    public Generator(Class<?> ...seedClasses) {
        for (Class<?> thisClazz : seedClasses) {
            ValueCreatorCache.getInstance().get(thisClazz);
        }
    }

    /**
     * Generates objects using default thread count (number of available processors).
     * 
     * @param <T> The type of object to generate
     * @param startId The starting ID for generated objects
     * @param endId The ending ID for generated objects (inclusive)
     * @param clazz The class of objects to generate
     * @param callback The callback to process each generated object
     * @return This generator instance for method chaining
     */
    public <T> Generator generate(long startId, long endId, Class<T> clazz, Callback<T> callback) {
        return this.generate(startId, endId, 0, clazz, null, null, callback);
    }

    /**
     * Generates objects with custom parameters using default thread count.
     * 
     * @param <T> The type of object to generate
     * @param startId The starting ID for generated objects
     * @param endId The ending ID for generated objects (inclusive)
     * @param clazz The class of objects to generate
     * @param paramMap Additional parameters to pass to the value creator (e.g., for GenExpression)
     * @param callback The callback to process each generated object
     * @return This generator instance for method chaining
     */
    public <T> Generator generate(long startId, long endId, Class<T> clazz, Map<String, Object> paramMap, Callback<T> callback) {
        return this.generate(startId, endId, 0, clazz, null, paramMap, callback);
    }

    /**
     * Generates objects with a specified number of threads.
     * 
     * @param <T> The type of object to generate
     * @param startId The starting ID for generated objects
     * @param endId The ending ID for generated objects (inclusive)
     * @param threads The number of threads to use (0 or less uses available processors)
     * @param clazz The class of objects to generate
     * @param callback The callback to process each generated object
     * @return This generator instance for method chaining
     */
    public <T> Generator generate(long startId, long endId, int threads, Class<T> clazz, Callback<T> callback) {
        return this.generate(startId, endId, threads, clazz, null, null, callback);
    }
    
    /**
     * Generates objects with custom parameters and thread count.
     * 
     * @param <T> The type of object to generate
     * @param startId The starting ID for generated objects
     * @param endId The ending ID for generated objects (inclusive)
     * @param threads The number of threads to use (0 or less uses available processors)
     * @param clazz The class of objects to generate
     * @param paramMap Additional parameters to pass to the value creator (e.g., for GenExpression)
     * @param callback The callback to process each generated object
     * @return This generator instance for method chaining
     */
    public <T> Generator generate(long startId, long endId, int threads, Class<T> clazz, Map<String, Object> paramMap, Callback<T> callback) {
        return this.generate(startId, endId, threads, clazz, null, paramMap, callback);
    }
    
    /**
     * Generates objects with full control over factory, parameters, and threading.
     * 
     * <p>This is the main generation method that all other overloads delegate to.
     * It creates a thread pool, generates objects in parallel, and processes them
     * through the provided callback.</p>
     * 
     * @param <T> The type of object to generate
     * @param startId The starting ID for generated objects
     * @param endId The ending ID for generated objects (inclusive)
     * @param threads The number of threads to use (0 or less uses available processors)
     * @param clazz The class of objects to generate
     * @param factory Custom factory for creating object instances (null uses default constructor)
     * @param paramMap Additional parameters to pass to the value creator (e.g., for GenExpression)
     * @param callback The callback to process each generated object
     * @return This generator instance for method chaining
     */
    public <T> Generator generate(long startId, long endId, int threads, Class<T> clazz, 
            Factory<T> factory, Map<String, Object> paramMap,Callback<T> callback) {
        
        Factory<T> factoryToUse = factory == null ? new DefaultConstructorFactory<T>(clazz) : factory;
        ValueCreator<T> valueCreator = ValueCreatorCache.getInstance().get(clazz);
        
        int threadsToUse = threads <= 0 ? Runtime.getRuntime().availableProcessors() : threads;
        this.started.set(0);
        this.success.set(0);
        this.errors.set(0);
        executor = Executors.newFixedThreadPool(threadsToUse);
        startRecord = startId;
        endRecord = endId;
        started.set(startId);
        
        for (int i = 0; i < threadsToUse; i++) {
            executor.submit(() -> {
                Map<String, Object> params = paramMap != null ? new HashMap<>(paramMap) : new HashMap<>();
                while (true) {
                    long id = started.getAndIncrement();
                    if (id > endRecord) {
                        break;
                    }
                    try {
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
    
    /**
     * Gets the current monitoring statistics for the generation process.
     * 
     * <p>This method provides a snapshot of the current generation state,
     * including progress, success, and failure counts.</p>
     * 
     * @return A MonitorStats object containing current statistics
     */
    public MonitorStats getMontiorStats() {
        return new MonitorStats(startRecord, endRecord, started.get(), success.get(), errors.get());
    }

    /**
     * Monitors the generation progress, printing statistics every second until completion.
     * 
     * <p>This method blocks until all objects have been generated and processed.
     * It prints progress information including elapsed time, success/failure counts,
     * and completion percentage.</p>
     * 
     * @throws InterruptedException if the monitoring thread is interrupted
     */
    public void monitor() throws InterruptedException {
        this.monitor(null);
    }
    
    /**
     * Monitors the generation progress with custom additional information.
     * 
     * <p>This method blocks until all objects have been generated and processed.
     * It prints progress information including elapsed time, success/failure counts,
     * completion percentage, and any additional information provided by the callback.</p>
     * 
     * @param extraInfo A callback that provides additional monitoring information
     * @throws InterruptedException if the monitoring thread is interrupted
     */
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

    
    /**
     * Checks if the generation process has completed.
     * 
     * <p>Returns true if the executor has been shut down and all tasks have finished.
     * This can be used to poll for completion status without blocking.</p>
     * 
     * @return true if generation is complete, false otherwise
     */
    public boolean isComplete() {
        return executor == null || executor.isTerminated();
    }
}
