package com.tweesky.cloudtools.codegen.samples;

public class EmailSample implements SampleValue {

    private Object schema;

    public EmailSample(Object schema) {
        this.schema = schema;
    }

    @Override
    public String getValue(String key) {
        return "user@example.com";
    }
}
