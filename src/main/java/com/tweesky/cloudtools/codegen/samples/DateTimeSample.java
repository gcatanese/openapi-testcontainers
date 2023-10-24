package com.tweesky.cloudtools.codegen.samples;

public class DateTimeSample implements SampleValue {

    private Object schema;

    public DateTimeSample(Object schema) {
        this.schema = schema;
    }

    @Override
    public String getValue(String key) {
        return "01/01/2000 h00:00:00";
    }
}
