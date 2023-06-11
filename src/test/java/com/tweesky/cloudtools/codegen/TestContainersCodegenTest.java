package com.tweesky.cloudtools.codegen;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.EmailSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestContainersCodegenTest {

    @Test
    public void generateByContractId() throws IOException {
        File output = Files.createTempDirectory("test").toFile();
        output.deleteOnExit();

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("test-containers")
                .setInputSpec("src/test/resources/test-containers/specByContractId.yaml")
                .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

        DefaultGenerator generator = new DefaultGenerator();
        List<File> files = generator.opts(configurator.toClientOptInput()).generate();
        files.forEach(File::deleteOnExit);

        TestUtils.assertFileContains(Paths.get(output + "/main.go"),
                "package main");

        TestUtils.assertFileExists(Paths.get(output + "/api/api_basic.go"));
        TestUtils.assertFileContains(Paths.get(output + "/api/api_basic.go"),
                "request: post-user-request-200 response: post-user-response-200");

    }

    @Test
    public void generateByRef() throws IOException {
        File output = Files.createTempDirectory("test").toFile();
        output.deleteOnExit();

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("test-containers")
                .setInputSpec("src/test/resources/test-containers/specByRef.yaml")
                .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

        DefaultGenerator generator = new DefaultGenerator();
        List<File> files = generator.opts(configurator.toClientOptInput()).generate();
        files.forEach(File::deleteOnExit);
        files.forEach(System.out::println);

        TestUtils.assertFileContains(Paths.get(output + "/main.go"),
                "package main");

        TestUtils.assertFileExists(Paths.get(output + "/api/api_basic.go"));
        TestUtils.assertFileContains(Paths.get(output + "/api/api_basic.go"),
                "request: post-user response: post-user-200");

    }

    @Test
    public void generateFromSchema() throws IOException {
        File output = Files.createTempDirectory("test").toFile();
        output.deleteOnExit();

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("test-containers")
                .setInputSpec("src/test/resources/test-containers/specNoExamples.yaml")
                .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

        DefaultGenerator generator = new DefaultGenerator();
        List<File> files = generator.opts(configurator.toClientOptInput()).generate();
        files.forEach(File::deleteOnExit);

        TestUtils.assertFileContains(Paths.get(output + "/main.go"),
                "package main");

        log(output + "/api/api_basic.go");

        TestUtils.assertFileExists(Paths.get(output + "/api/api_basic.go"));
        // check generated email address
        TestUtils.assertFileContains(Paths.get(output + "/api/api_basic.go"),
                "\"user@example.com\"");

    }

    @Test
    public void getOperation() throws IOException {
        File output = Files.createTempDirectory("test").toFile();
        output.deleteOnExit();

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("test-containers")
                .setInputSpec("src/test/resources/test-containers/get-operation.yaml")
                .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

        DefaultGenerator generator = new DefaultGenerator();
        List<File> files = generator.opts(configurator.toClientOptInput()).generate();
        files.forEach(File::deleteOnExit);

        TestUtils.assertFileContains(Paths.get(output + "/main.go"),
                "package main");

        log(output + "/api/api_basic.go");

        TestUtils.assertFileExists(Paths.get(output + "/api/api_basic.go"));
        // check generated email address
        TestUtils.assertFileContains(Paths.get(output + "/api/api_basic.go"),
                "\"user@example.com\"");

    }

    @Test
    public void generateByName() throws IOException {
        File output = Files.createTempDirectory("test").toFile();
        output.deleteOnExit();

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("test-containers")
                .setInputSpec("src/test/resources/test-containers/specByName.yaml")
                .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

        DefaultGenerator generator = new DefaultGenerator();
        List<File> files = generator.opts(configurator.toClientOptInput()).generate();
        files.forEach(File::deleteOnExit);

        TestUtils.assertFileContains(Paths.get(output + "/main.go"),
                "package main");

        log(output + "/api/api_basic.go");

        TestUtils.assertFileExists(Paths.get(output + "/api/api_basic.go"));
        TestUtils.assertFileContains(Paths.get(output + "/api/api_basic.go"),
                "// request: get-user-valid response: get-user-valid");

        TestUtils.assertFileContains(Paths.get(output + "/api/api_basic.go"),
                "// request: new-user response: basic");
    }


    @Test
    public void extractExampleByName() {
        String str = "#/components/examples/get-user-basic";

        assertEquals("get-user-basic", new TestContainersCodegen().extractNameFromRef(str));
    }

    @Test
    public void formatJson() {

        final String EXPECTED = "{\n  \"id\" : 1,\n  \"city\" : \"Amsterdam\"\n}";
        final String JSON = "{\"id\":1,\"city\":\"Amsterdam\"}";

        assertEquals(EXPECTED, new TestContainersCodegen().formatJson(JSON));

    }

    @Test
    public void convertObjectNodeToJson() {

        final String EXPECTED = "{\n  \"id\" : 1,\n  \"city\" : \"Amsterdam\"\n}";

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode city = mapper.createObjectNode();

        city.put("id", 1);
        city.put("city", "Amsterdam");

        assertEquals(EXPECTED, new TestContainersCodegen().convertToJson(city));

    }

    @Test
    public void convertLinkedHashMapToJson() {
        String EXPECTED = "{\n" +
                "  \"key\" : \"abcdefghijklmnopqrstuvwxyz\",\n" +
                "  \"key2\" : 0,\n" +
                "  \"key3\" : \"user@example.com\",\n" +
                "  \"key4\" : [ ]\n" +
                "}";

        LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>(
                ImmutableMap.of(
                        "key", new StringSchema(),
                        "key2", new IntegerSchema(),
                        "key3", new EmailSchema(),
                        "key4", new ArraySchema()
                        ));

        String json = new TestContainersCodegen().convertToJson(linkedHashMap);
        assertEquals(EXPECTED, json);
    }

    @Test
    public void getInputSpecFilename() {

        String INPUT_SPEC = "/dir/subdir/myopenapi.yaml";
        String EXPECTED = "myopenapi.yaml";
        assertEquals(EXPECTED, new TestContainersCodegen().getInputSpecFilename(INPUT_SPEC));
    }


    private void log(String filename) throws IOException {
        System.out.println(new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8));
    }

}
