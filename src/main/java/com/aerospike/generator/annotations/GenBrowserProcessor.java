package com.aerospike.generator.annotations;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.aerospike.generator.annotations.GenBrowser.BrowserType;
import com.github.javafaker.Faker;
import com.github.javafaker.Internet;
import com.github.javafaker.Internet.UserAgent;

public class GenBrowserProcessor implements Processor {
    private final Internet internet = Faker.instance().internet();
    private final BrowserType type;
    
    public GenBrowserProcessor(GenBrowser browser, FieldType fieldType, Field field) {
        this(browser.value(), fieldType);
    }
    
    public GenBrowserProcessor(BrowserType type, FieldType fieldType) {
        this.type = type;
        if (!supports(fieldType) ) {
            throw new IllegalArgumentException("Unsupported field type " + fieldType);
        }
    }
    
    @Override
    public Object process(Map<String, Object> params) {
        switch (type) {
        case USERAGENT:
            return internet.userAgentAny();
        case NAME:
        default:
            UserAgent[] userAgents = Internet.UserAgent.values();
            return userAgents[ThreadLocalRandom.current().nextInt(userAgents.length)].toString();
        }
    }
    public boolean supports(FieldType fieldType) {
        return fieldType == FieldType.STRING;
    }
}
