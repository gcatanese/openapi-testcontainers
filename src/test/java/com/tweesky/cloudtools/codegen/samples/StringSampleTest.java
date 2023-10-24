package com.tweesky.cloudtools.codegen.samples;

import io.swagger.v3.oas.models.media.StringSchema;
import org.testng.annotations.Test;

import static org.junit.Assert.assertEquals;

public class StringSampleTest {

    // generate value for given key
    @Test
    public void getValue() {
        String key = "country";
        StringSchema value = new StringSchema().type("string");
        assertEquals("NL", new StringSample(value).getValue(key));
    }

    // get first enum as value
    @Test
    public void getValueFromEnum() {
        String key = "country";
        StringSchema value = new StringSchema().type("string").addEnumItem("UK").addEnumItem("NL");
        assertEquals("UK", new StringSample(value).getValue(key));
    }

}
