package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.aerospike.generator.annotations.ExpressionParser.Node;

public class GenExpressionProcessor implements Processor {

    private final FieldType fieldType;
    private final ExpressionParser parser;
    private final Node abstractSyntaxTree;
    private final boolean returnString;
    
    public GenExpressionProcessor(GenExpression genExpression, FieldType fieldType, Field field) {
        this(genExpression.value(), fieldType);
    }
    
    public GenExpressionProcessor(String value, FieldType fieldType) {
        this.fieldType = fieldType;
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
        this.parser = new ExpressionParser();
        this.abstractSyntaxTree = parser.parseExpression(value);
        this.returnString = (fieldType == FieldType.STRING || fieldType == FieldType.UUID);
        
        if (!returnString) {
            // Give a quick test to ensure a number is returned, but only if no object properties are involved
            try {
                parser.evaluate(abstractSyntaxTree, Map.of("Key", 1L), false);
            } catch (IllegalArgumentException e) {
                // If the expression contains object properties, skip the test evaluation
                // as the object won't be available during construction
                if (!e.getMessage().contains("Object 'obj' not found")) {
                    throw e;
                }
            }
        }
    }

    @Override
    public Object process(Map<String, Object> parameterMap) {
        Object result = parser.evaluate(abstractSyntaxTree, parameterMap, returnString);
        switch (fieldType) {
        case INTEGER:
            return (int)(long)result;
        case LONG:
            return (long)result;
        case UUID:
            return UUID.fromString((String)result);
        case DATE:
            return new Date((long)result);
        default:
            return result;
        }
    }
    
    public boolean supports(FieldType fieldType) {
        switch (fieldType) {
        case INTEGER:
        case LONG:
        case STRING:
        case UUID:
        case DATE:
            return true;
        default:
            return false;
        }
    }
    
    @Override
    public boolean isDeferred() {
        return true;
    }
}
