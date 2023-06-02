package com.tweesky.cloudtools.codegen;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestContainersCodegenTest {

    @Test
    public void generate() throws IOException {
        File output = Files.createTempDirectory("test").toFile();
        output.deleteOnExit();

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("test-containers")
                .setInputSpec("src/test/resources/test-containers/specByContractId.yaml")
                .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

        DefaultGenerator generator = new DefaultGenerator();
        List<File> files = generator.opts(configurator.toClientOptInput()).generate();
        files.forEach(File::deleteOnExit);

        TestUtils.assertFileExists(Paths.get(output + "/api/BasicApi.go"));
        TestUtils.assertFileContains(Paths.get(output + "/api/BasicApi.go"),
                "request: post-user-request-200 response: post-user-response-200");

        log(output + "/api/BasicApi.go");

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

        TestUtils.assertFileExists(Paths.get(output + "/api/BasicApi.go"));
        TestUtils.assertFileContains(Paths.get(output + "/api/BasicApi.go"),
                "request: post-user response: post-user-200");


        log(output + "/api/BasicApi.go");

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

        log(output + "/api/BasicApi.go");
        TestUtils.assertFileExists(Paths.get(output + "/api/BasicApi.go"));
        TestUtils.assertFileContains(Paths.get(output + "/api/BasicApi.go"),
                "request: post-user response: post-user-200");

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


    private void log(String filename) throws IOException {
        System.out.println(new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8));
    }



}
