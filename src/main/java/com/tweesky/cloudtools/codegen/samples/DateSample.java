package com.tweesky.cloudtools.codegen.samples;

public class DateSample implements SampleValue {

    private Object schema;

    public DateSample(Object schema) {
        this.schema = schema;
    }

    @Override
    public String getValue(String key) {
        return "01/01/2000";
    }
}
