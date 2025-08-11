package com.aerospike.generator.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Evaluates annotation references in expressions by dynamically creating annotation proxies
 * and using their corresponding processors.
 */
public class AnnotationEvaluator {
    
    /**
     * Evaluates an annotation reference by creating a dynamic annotation proxy and using its processor.
     * 
     * @param annotationName The name of the annotation (e.g., "GenNumber", "GenString")
     * @param parameters The parameters for the annotation
     * @param globalParams The global parameter map for resolving parameter references
     * @param returnString Whether to return the result as a string
     * @return The generated value
     */
    public static Object evaluate(String annotationName, Map<String, Object> parameters, Map<String, Object> globalParams, boolean returnString) {
        try {
            // Map annotation names to their classes and processors
            AnnotationInfo info = getAnnotationInfo(annotationName);
            if (info == null) {
                throw new IllegalArgumentException("Unsupported annotation: @" + annotationName);
            }
            
            // Resolve parameter references in the annotation parameters
            Map<String, Object> resolvedParameters = resolveParameterReferences(parameters, globalParams);
            
            // Create a dynamic annotation proxy
            Object annotation = createAnnotationProxy(info.annotationClass, resolvedParameters);
            
            // Create the processor and evaluate
            Constructor<?> processorConstructor = info.processorClass.getConstructor(
                info.annotationClass, FieldType.class, Field.class);
            
            // Create a dummy field for the processor
            Field dummyField = createDummyField();
            
            Processor processor = (Processor) processorConstructor.newInstance(
                annotation, info.fieldType, dummyField);
            
            return processor.process(parameters);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate annotation @" + annotationName, e);
        }
    }
    
    private static AnnotationInfo getAnnotationInfo(String annotationName) {
        switch (annotationName) {
            case "GenNumber":
                return new AnnotationInfo(GenNumber.class, GenNumberProcessor.class, FieldType.INTEGER);
            case "GenString":
                return new AnnotationInfo(GenString.class, GenStringProcessor.class, FieldType.STRING);
            case "GenBoolean":
                return new AnnotationInfo(GenBoolean.class, GenBooleanProcessor.class, FieldType.BOOLEAN);
            case "GenUuid":
                return new AnnotationInfo(GenUuid.class, GenUuidProcessor.class, FieldType.UUID);
            case "GenDate":
                return new AnnotationInfo(GenDate.class, GenDateProcessor.class, FieldType.DATE);
            case "GenEmail":
                return new AnnotationInfo(GenEmail.class, GenEmailProcessor.class, FieldType.STRING);
            case "GenName":
                return new AnnotationInfo(GenName.class, GenNameProcessor.class, FieldType.STRING);
            case "GenPhoneNumber":
                return new AnnotationInfo(GenPhoneNumber.class, GenPhoneNumberProcessor.class, FieldType.STRING);
            case "GenIpV4":
                return new AnnotationInfo(GenIpV4.class, GenIpV4Processor.class, FieldType.STRING);
            case "GenBrowser":
                return new AnnotationInfo(GenBrowser.class, GenBrowserProcessor.class, FieldType.STRING);
            case "GenAddress":
                return new AnnotationInfo(GenAddress.class, GenAddressProcessor.class, FieldType.STRING);
            case "GenHexString":
                return new AnnotationInfo(GenHexString.class, GenHexStringProcessor.class, FieldType.STRING);
            case "GenEnum":
                return new AnnotationInfo(GenEnum.class, GenEnumProcessor.class, FieldType.ENUM);
            case "GenList":
                return new AnnotationInfo(GenList.class, GenListProcessor.class, FieldType.LIST);
            case "GenObject":
                return new AnnotationInfo(GenObject.class, GenObjectProcessor.class, FieldType.OBJECT);
            case "GenOneOf":
                return new AnnotationInfo(GenOneOf.class, GenOneOfProcessor.class, FieldType.STRING);
            case "GenRange":
                return new AnnotationInfo(GenRange.class, GenRangeProcessor.class, FieldType.STRING);
            case "GenBytes":
                return new AnnotationInfo(GenBytes.class, GenBytesProcessor.class, FieldType.BYTES);
            default:
                return null;
        }
    }
    
    private static Object createAnnotationProxy(Class<?> annotationClass, Map<String, Object> parameters) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                
                // Handle annotationType() method
                if ("annotationType".equals(methodName)) {
                    return annotationClass;
                }
                
                // Handle toString(), hashCode(), equals() methods
                if ("toString".equals(methodName)) {
                    return "@" + annotationClass.getSimpleName() + "(" + parameters + ")";
                }
                
                if ("hashCode".equals(methodName)) {
                    return parameters.hashCode();
                }
                
                if ("equals".equals(methodName)) {
                    return proxy == args[0];
                }
                
                // Handle annotation attribute methods
                if (parameters.containsKey(methodName)) {
                    Object value = parameters.get(methodName);
                    
                    // Handle enum types
                    if (method.getReturnType().isEnum()) {
                        if (value instanceof String) {
                            try {
                                return Enum.valueOf((Class<? extends Enum>) method.getReturnType(), (String) value);
                            } catch (IllegalArgumentException e) {
                                throw new IllegalArgumentException("Invalid enum value '" + value + "' for " + method.getReturnType().getSimpleName());
                            }
                        }
                    }
                    
                    // Handle primitive type conversions
                    Class<?> returnType = method.getReturnType();
                    if (returnType == int.class || returnType == Integer.class) {
                        if (value instanceof Long) {
                            return ((Long) value).intValue();
                        } else if (value instanceof Number) {
                            return ((Number) value).intValue();
                        }
                    } else if (returnType == long.class || returnType == Long.class) {
                        if (value instanceof Number) {
                            return ((Number) value).longValue();
                        }
                    } else if (returnType == double.class || returnType == Double.class) {
                        if (value instanceof Number) {
                            return ((Number) value).doubleValue();
                        }
                    } else if (returnType == float.class || returnType == Float.class) {
                        if (value instanceof Number) {
                            return ((Number) value).floatValue();
                        }
                    }
                    
                    return value;
                }
                
                // Return default value for the method
                return method.getDefaultValue();
            }
        };
        
        return Proxy.newProxyInstance(
            annotationClass.getClassLoader(),
            new Class<?>[] { annotationClass },
            handler
        );
    }
    
    private static Field createDummyField() {
        try {
            return String.class.getDeclaredField("value");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to create dummy field", e);
        }
    }
    
    /**
     * Resolves parameter references within annotation parameters.
     * 
     * @param annotationParams The annotation parameters that may contain ParameterReference objects
     * @param globalParams The global parameter map to resolve references against
     * @return A new map with resolved parameter references
     */
    private static Map<String, Object> resolveParameterReferences(Map<String, Object> annotationParams, Map<String, Object> globalParams) {
        Map<String, Object> resolved = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : annotationParams.entrySet()) {
            Object value = entry.getValue();
            
            if (value instanceof ExpressionParser.ParameterReference) {
                // Resolve the parameter reference
                String paramName = ((ExpressionParser.ParameterReference) value).getParameterName();
                Object resolvedValue = globalParams.get(paramName);
                if (resolvedValue == null) {
                    throw new IllegalArgumentException("Parameter reference '$" + paramName + "' not found in parameter map");
                }
                resolved.put(entry.getKey(), resolvedValue);
            } else {
                // Keep the original value
                resolved.put(entry.getKey(), value);
            }
        }
        
        return resolved;
    }
    
    private static class AnnotationInfo {
        final Class<?> annotationClass;
        final Class<?> processorClass;
        final FieldType fieldType;
        
        AnnotationInfo(Class<?> annotationClass, Class<?> processorClass, FieldType fieldType) {
            this.annotationClass = annotationClass;
            this.processorClass = processorClass;
            this.fieldType = fieldType;
        }
    }
    

} 