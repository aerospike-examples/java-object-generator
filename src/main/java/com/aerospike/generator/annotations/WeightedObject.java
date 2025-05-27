package com.aerospike.generator.annotations;

class WeightedObject {
    private final int weight;
    private final Object value;
    public WeightedObject(Object obj, int weight) {
        this.weight = weight;
        this.value = obj;
    }
    
    public Object getValue() {
        return value;
    }
    public int getWeight() {
        return weight;
    }
}