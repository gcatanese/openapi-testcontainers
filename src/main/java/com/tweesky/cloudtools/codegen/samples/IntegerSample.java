package com.tweesky.cloudtools.codegen.samples;

public class IntegerSample implements SampleValue {

    private Object schema;

    public IntegerSample(Object schema) {
        this.schema = schema;
    }

    @Override
    public String getValue(String key) {
        return "0";
    }
}
