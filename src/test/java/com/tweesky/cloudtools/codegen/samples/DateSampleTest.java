package com.tweesky.cloudtools.codegen.samples;

import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.testng.annotations.Test;

import static org.junit.Assert.assertEquals;

public class DateSampleTest {

    @Test
    public void getValue() {
        String key = "created";
        DateSchema value = new DateSchema().type("date");
        assertEquals("01/01/2000", new DateSample(value).getValue(key));
    }

}
