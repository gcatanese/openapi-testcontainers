package com.tweesky.cloudtools.codegen.samples;

public class BooleanSample implements SampleValue {

    private Object schema;

    public BooleanSample(Object schema) {
        this.schema = schema;
    }

    @Override
    public String getValue(String key) {
        return "true";
    }
}
