package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;

import com.aerospike.generator.annotations.ExpressionParser.Node;

public class GenExpressionProcessor implements Processor {

    private final FieldType fieldType;
    private final ExpressionParser parser;
    private final Node abstractSyntaxTree;
    private final boolean returnString;
    
    public GenExpressionProcessor(GenExpression range, FieldType fieldType, Field field) {
        this(range.value(), fieldType);
    }
    
    public GenExpressionProcessor(String value, FieldType fieldType) {
        this.fieldType = fieldType;
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
        this.parser = new ExpressionParser();
        this.abstractSyntaxTree = parser.parseExpression(value);
        this.returnString = (fieldType == FieldType.STRING);
        
        if (!returnString) {
            // Give a quick test to ensure a number is returned
            parser.evaluate(abstractSyntaxTree, Map.of("Key", 1L), false);
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
        default:
            return result;
        }
    }
    
    public boolean supports(FieldType fieldType) {
        switch (fieldType) {
        case INTEGER:
        case LONG:
        case STRING:
            return true;
        default: 
            return false;
        }
    }
}
