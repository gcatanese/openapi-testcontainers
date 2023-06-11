package com.tweesky.cloudtools.codegen;

/*
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.*;
import org.joda.time.DateTime;
import org.openapitools.codegen.*;
import org.openapitools.codegen.languages.AbstractGoCodegen;
import org.openapitools.codegen.meta.GeneratorMetadata;
import org.openapitools.codegen.meta.Stability;
import org.openapitools.codegen.meta.features.*;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.OperationMap;
import org.openapitools.codegen.model.OperationsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TestContainersCodegen extends AbstractGoCodegen {

    private final Logger LOGGER = LoggerFactory.getLogger(TestContainersCodegen.class);

    protected int serverPort = 8080;
    protected String projectName = "openapi-test-containers";
    protected String apiPath = "api";

    protected String packageName = "openapi";

    public static final String CONTRACT_ID_EXTENSION = "contractIdExtension";
    public static final String CONTRACT_ID_EXTENSION_DEFAULT_VALUE = "x-contract-id";

    protected String contractIdExtension = CONTRACT_ID_EXTENSION_DEFAULT_VALUE;

    public static final String JSON_ESCAPE_DOUBLE_QUOTE = "\"";
    public static final String JSON_ESCAPE_NEW_LINE = "";

    public TestContainersCodegen() {
        super();

        generatorMetadata = GeneratorMetadata.newBuilder(generatorMetadata)
                .stability(Stability.EXPERIMENTAL)
                .build();

        modifyFeatureSet(features -> features
                .includeDocumentationFeatures(DocumentationFeature.Readme)
                .wireFormatFeatures(EnumSet.of(WireFormatFeature.JSON, WireFormatFeature.XML))
                .securityFeatures(EnumSet.noneOf(
                        SecurityFeature.class
                ))
                .excludeGlobalFeatures(
                        GlobalFeature.XMLStructureDefinitions,
                        GlobalFeature.Callbacks,
                        GlobalFeature.LinkObjects,
                        GlobalFeature.ParameterStyling
                )
                .excludeSchemaSupportFeatures(
                        SchemaSupportFeature.Polymorphism
                )
                .excludeParameterFeatures(
                        ParameterFeature.Cookie
                )
        );

        outputFolder = "generated-code/test-containers";

        /*
         * Models.  You can write model files using the modelTemplateFiles map.
         * if you want to create one template for file, you can do so here.
         * for multiple files for model, just put another entry in the `modelTemplateFiles` with
         * a different extension
         */
        modelTemplateFiles.put(
                "model.mustache",
                ".go");

        /*
         * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
         * as with models, add multiple entries with different extensions for multiple files per
         * class
         */
        apiTemplateFiles.put(
                "controller-api.mustache",   // the template to use
                ".go");       // the extension for each file to write

        /*
         * Template Location.  This is the location which templates will be read from.  The generator
         * will use the resource stream to attempt to read the templates.
         */
        embeddedTemplateDir = templateDir = "test-containers";

        /*
         * Reserved words.  Override this with reserved words specific to your language
         */
        setReservedWordsLowerCase(
                Arrays.asList(
                        // data type
                        "string", "bool", "uint", "uint8", "uint16", "uint32", "uint64",
                        "int", "int8", "int16", "int32", "int64", "float32", "float64",
                        "complex64", "complex128", "rune", "byte", "uintptr",

                        "break", "default", "func", "interface", "select",
                        "case", "defer", "go", "map", "struct",
                        "chan", "else", "goto", "package", "switch",
                        "const", "fallthrough", "if", "range", "type",
                        "continue", "for", "import", "return", "var", "error", "nil")
                // Added "error" as it's used so frequently that it may as well be a keyword
        );

        CliOption optServerPort = new CliOption("serverPort", "The network port the generated server binds to");
        optServerPort.setType("int");
        optServerPort.defaultValue(Integer.toString(serverPort));
        cliOptions.add(optServerPort);

        cliOptions.add(CliOption.newString(CONTRACT_ID_EXTENSION, "name of the extension that defines the Contract Id"));

    }

    @Override
    public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
        objs = super.postProcessOperationsWithModels(objs, allModels);

        OperationMap operations = objs.getOperations();
        List<CodegenOperation> operationList = operations.getOperation();


        for (CodegenOperation codegenOperation : operationList) {
            List<Interaction> items = new ArrayList<>();

            if (codegenOperation.path != null) {
                codegenOperation.path = codegenOperation.path.replaceAll("\\{(.*?)\\}", ":$1");
            }

            items.addAll(getInteractions(codegenOperation));

            if (!items.isEmpty()) {
                codegenOperation.vendorExtensions.put("items", items);
            }

            // add default response
            codegenOperation.vendorExtensions.put("fallback", getDefault(codegenOperation));

        }
        return objs;
    }

    @Override
    public void processOpts() {
        super.processOpts();

        additionalProperties.put("buildTimestamp", getBuildTimestamp());

        /*
         * Additional Properties.  These values can be passed to the templates and
         * are available in models, apis, and supporting files
         */

        if (additionalProperties.containsKey("serverPort")) {
            this.serverPort = Integer.parseInt((String) additionalProperties.get("serverPort"));
        } else {
            additionalProperties.put("serverPort", serverPort);
        }

        if (additionalProperties().containsKey(CONTRACT_ID_EXTENSION)) {
            contractIdExtension = additionalProperties().get(CONTRACT_ID_EXTENSION).toString();
        }

        additionalProperties.put("apiPath", apiPath);
        additionalProperties.put("packageName", packageName);

        /*
         * Supporting Files.  You can write single files for the generator with the
         * entire object tree available.  If the input file has a suffix of `.mustache
         * it will be processed by the template engine.  Otherwise, it will be copied
         */
        supportingFiles.add(new SupportingFile("openapi.mustache", "api", "openapi.yaml"));
        supportingFiles.add(new SupportingFile("main.mustache", "", "main.go"));
        supportingFiles.add(new SupportingFile("Dockerfile.mustache", "", "Dockerfile"));
        supportingFiles.add(new SupportingFile("routers.mustache", apiPath, "routers.go"));
        supportingFiles.add(new SupportingFile("README.mustache", apiPath, "README.md")
                .doNotOverwrite());
        supportingFiles.add(new SupportingFile("go.mod.mustache", "go.mod"));
        supportingFiles.add(new SupportingFile("templates/index.mustache", "", "templates/index.html"));
    }


    @Override
    public String apiPackage() {
        return apiPath;
    }

    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     * @see CodegenType
     */
    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public GeneratorLanguage generatorLanguage() {
        return GeneratorLanguage.GO;
    }

    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -g flag.
     *
     * @return the friendly name for the generator
     */
    @Override
    public String getName() {
        return "test-containers";
    }

    /**
     * Returns human-friendly help for the generator.  Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    @Override
    public String getHelp() {
        return "Generates a Go server from an OpenAPI-Generator.";
    }

    /**
     * Location to write api files.  You can use the apiPackage() as defined when the class is
     * instantiated
     */
    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String modelFileFolder() {
        return outputFolder + File.separator + apiPackage().replace('.', File.separatorChar);
    }

    List<Interaction> getInteractions(CodegenOperation codegenOperation) {
        List<Interaction> interactions = new ArrayList<>();

        if (!codegenOperation.getHasBodyParam()) {
            List<Interaction> interactionsFromParameters = getFromParameters(codegenOperation);
            interactions.addAll(interactionsFromParameters);

        } else {
            List<Interaction> interactionsFromRequestExamples = getFromRequestExamples(codegenOperation);
            interactions.addAll(interactionsFromRequestExamples);
        }

        return interactions;
    }

    private List<Interaction> getFromRequestExamples(CodegenOperation codegenOperation) {

        List<Interaction> interactions = new ArrayList<>();

        // loop through request examples
        if (codegenOperation.bodyParam.getContent().get("application/json") != null &&
                codegenOperation.bodyParam.getContent().get("application/json").getExamples() != null) {
            for (Map.Entry<String, Example> entry : codegenOperation.bodyParam.getContent().get("application/json").getExamples().entrySet()) {

                Example requestExample = getRequestExample(entry);

                if (requestExample != null) {
                    // found request example
                    String requestExampleContractId = getContractId(requestExample);
                    String requestExampleRef = entry.getValue().get$ref();
                    String requestExampleName = entry.getKey();

                    // find by contractId
                    InteractionResponse responseItem = getInteractionResponseByContractId(codegenOperation, requestExampleContractId);
                    if (responseItem == null) {
                        // find by matching $ref
                        responseItem = getInteractionResponseByRef(requestExampleRef);
                    }
                    if(responseItem == null) {
                        // find by matching example name
                        responseItem = getInteractionResponseByName(codegenOperation, requestExampleName);
                    }

                    if (responseItem != null) {
                        // found response example
                        Interaction item = new Interaction();
                        item.setRequestBody(getJsonFromExample(requestExample));
                        item.setStatusCode(responseItem.getStatusCode());
                        item.setResponseBody(responseItem.getBody());
                        if(requestExampleRef != null) {
                            item.setRequestExampleName(extractNameFromRef(requestExampleRef));
                        } else {
                            item.setRequestExampleName(requestExampleName);
                        }
                        item.setResponseExampleName(responseItem.getName());

                        interactions.add(item);
                    }
                }

            }
        }

        return interactions;
    }

    private List<Interaction> getFromParameters(CodegenOperation codegenOperation) {

        List<Interaction> interactions = new ArrayList<>();

        for (CodegenParameter codegenParameter : codegenOperation.allParams) {
            // loop through parameters
            if (codegenParameter.examples != null) {
                for (Map.Entry<String, Example> entry : codegenParameter.examples.entrySet()) {

                    Example requestExample = null;

                    if (entry.getValue().getValue() != null) {
                        requestExample = entry.getValue();
                    }

                    if (requestExample != null) {
                        // found request example
                        String requestExampleContractId = getContractId(requestExample);
                        String requestExampleRef = entry.getValue().get$ref();
                        String requestExampleName = entry.getKey();

                        // find by contractId
                        InteractionResponse responseItem = getInteractionResponseByContractId(codegenOperation, requestExampleContractId);
                        if (responseItem == null) {
                            // find by matching $ref
                            responseItem = getInteractionResponseByRef(requestExampleRef);
                        }
                        if(responseItem == null) {
                            // find by matching example name
                            responseItem = getInteractionResponseByName(codegenOperation, requestExampleName);
                        }

                        if (responseItem != null) {
                            // found response example
                            Interaction item = new Interaction();
                            item.setParameterName(codegenParameter.paramName);
                            item.setParameterValue(String.valueOf(requestExample.getValue()));
                            item.setStatusCode(responseItem.getStatusCode());
                            item.setResponseBody(responseItem.getBody());
                            if(requestExampleRef != null) {
                                item.setRequestExampleName(extractNameFromRef(requestExampleRef));
                            } else {
                                item.setRequestExampleName(requestExampleName);
                            }
                            item.setResponseExampleName(responseItem.getName());

                            interactions.add(item);
                        }
                    }

                }
            }
        }

        return interactions;
    }

    private Interaction getDefault(CodegenOperation codegenOperation) {
        Interaction interaction = new Interaction();

        InteractionResponse interactionResponse = getInteractionResponseBySchema(codegenOperation);

        if(interactionResponse != null) {
            interaction.setStatusCode(interactionResponse.getStatusCode());
            interaction.setResponseBody(interactionResponse.getBody());
        } else {
            // empty response body
            interaction.setStatusCode("200");
            interaction.setResponseBody("");
        }

        return interaction;
    }

    private Example getRequestExample(Map.Entry<String, Example> entry) {
        Example requestExample = null;

        if (entry.getValue().get$ref() != null) {
            // get by $ref
            requestExample = getExampleByRef(entry.getValue().get$ref());
        } else if (entry.getValue().getValue() != null) {
            // get inline example
            requestExample = entry.getValue();
        }

        return requestExample;
    }

    Example getExampleByRef(String ref) {
        return this.openAPI.getComponents().getExamples().get(extractNameFromRef(ref));
    }

    Schema getSchemaByRef(String ref) {
        return this.openAPI.getComponents().getSchemas().get(extractNameFromRef(ref));
    }


    InteractionResponse getInteractionResponseByRef(String ref) {
        InteractionResponse response = null;

        if(ref != null) {
            String name = extractNameFromRef(ref);

            String[] codes = {"200", "201", "400", "401", "403", "404", "422", "500"};

            for (String code : codes) {
                for (Map.Entry<String, Example> entry : this.openAPI.getComponents().getExamples().entrySet()) {
                    String nameWithStatusCode = name + "-" + code;  // ie post-user-200
                    if (nameWithStatusCode.equalsIgnoreCase(entry.getKey())) {
                        response = new InteractionResponse();
                        response.setName(nameWithStatusCode);
                        response.setBody(getJsonFromExample(entry.getValue()));
                        response.setStatusCode(code);
                        break;
                    }
                }

            }
        }
        return response;
    }

    // generate InteractionResponse from schema definition
    InteractionResponse getInteractionResponseBySchema(CodegenOperation codegenOperation) {
        InteractionResponse response = null;

        // loop through responses
        for (CodegenResponse codegenResponse : codegenOperation.responses) {
            if (codegenResponse.code.equalsIgnoreCase("200")) {
                // generate from schema
                if (codegenResponse.schema != null) {
                    Schema schema = (Schema) codegenResponse.schema;
                    if (schema.get$ref() != null) {
                        schema = getSchemaByRef(schema.get$ref());

                        response = new InteractionResponse();
                        response.setName("<schema>");
                        response.setBody(convertToJson((LinkedHashMap<String, Object>) schema.getProperties()));
                        response.setStatusCode(codegenResponse.code);
                        break;
                    }
                }
            }
        }

        return response;
    }

    String getContractId(Example example) {
        String ret = null;
        if (example.getExtensions() != null
                && example.getExtensions().get(contractIdExtension) != null) {
            ret = (String) example.getExtensions().get(contractIdExtension);
        }

        return ret;
    }


    InteractionResponse getInteractionResponseByContractId(CodegenOperation codegenOperation, String contractId) {

        InteractionResponse response = null;

        // loop through responses
        for (CodegenResponse codegenResponse : codegenOperation.responses) {
            if (codegenResponse.getContent() != null && codegenResponse.getContent().get("application/json") != null &&
                    codegenResponse.getContent().get("application/json").getExamples() != null) {
                for (Map.Entry<String, Example> respExample : codegenResponse.getContent().get("application/json").getExamples().entrySet()) {
                    // loop through response examples
                    Example e = null;
                    String respExampleName = null;
                    if (respExample.getValue().get$ref() != null) {
                        // example by $ref
                        e = getExampleByRef(respExample.getValue().get$ref());
                        respExampleName = extractNameFromRef(respExample.getValue().get$ref());
                    } else if (respExample.getValue() != null) {
                        // inline example
                        e = respExample.getValue();
                        respExampleName = "<inline>";
                    }
                    if (e != null) {

                        String responseExampleContractId = getContractId(e);

                        if (contractId != null) {
                            // search by contracId
                            if (contractId.equalsIgnoreCase(responseExampleContractId)) {
                                // found matching response example
                                response = new InteractionResponse();
                                response.setBody(getJsonFromExample(e));
                                response.setName(respExampleName);
                                response.setStatusCode(codegenResponse.code);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return response;

    }


    InteractionResponse getInteractionResponseByName(CodegenOperation codegenOperation, String name) {

        InteractionResponse response = null;

        // loop through responses
        for (CodegenResponse codegenResponse : codegenOperation.responses) {
            if (codegenResponse.getContent() != null && codegenResponse.getContent().get("application/json") != null &&
                    codegenResponse.getContent().get("application/json").getExamples() != null) {
                for (Map.Entry<String, Example> respExample : codegenResponse.getContent().get("application/json").getExamples().entrySet()) {
                    // loop through response examples
                    if(respExample.getKey().equalsIgnoreCase(name)) {
                        // found matching response example name
                        Example e = null;
                        String respExampleName = name;
                        if (respExample.getValue().get$ref() != null) {
                            // example by $ref
                            e = getExampleByRef(respExample.getValue().get$ref());
                        } else if (respExample.getValue() != null) {
                            // inline example
                            e = respExample.getValue();
                        }

                        if (e != null) {
                            // found matching response example
                            response = new InteractionResponse();
                            response.setBody(getJsonFromExample(e));
                            response.setName(respExampleName);
                            response.setStatusCode(codegenResponse.code);
                            break;
                        }

                    }
                }
            }
        }

        return response;

    }

    public String getBuildTimestamp() {
        LocalDateTime timestamp = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return timestamp.format(format);
    }

    // Supporting helpers

    // from $ref: '#/components/examples/post-user' returns 'post-user'
    public String extractNameFromRef(String ref) {
        return ref.substring(ref.lastIndexOf("/") + 1);
    }

    public String getJsonFromExample(Example example) {
        String ret = "";

        if (example == null) {
            return ret;
        }

        if (example.getValue() instanceof ObjectNode) {
            ret = convertToJson((ObjectNode) example.getValue());
        } else if (example.getValue() instanceof LinkedHashMap) {
            ret = convertToJson((LinkedHashMap) example.getValue());
        }

        return ret;
    }

    // array of attributes from JSON payload (ignore commas within quotes)
    public String[] getAttributes(String json) {
        return json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    public String convertToJson(ObjectNode objectNode) {
        return formatJson(objectNode.toString());
    }

    // convert to JSON (string) escaping and formatting
    public String convertToJson(LinkedHashMap<String, Object> linkedHashMap) {
        String json = traverseMap(linkedHashMap, "");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode actualObj = objectMapper.readTree(json);
            json = actualObj.toPrettyString();

        } catch (Exception e) {
            LOGGER.warn("Error formatting JSON", e);
            json = "";
        }

        return json;

    }

    public String formatJson(String json) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode actualObj = objectMapper.readTree(json);
            json = actualObj.toPrettyString();

        } catch (JsonProcessingException e) {
            LOGGER.warn("Error formatting JSON", e);
            json = "";
        }

        return json;
    }

    // traverse recursively
    private String traverseMap(LinkedHashMap<String, Object> linkedHashMap, String ret) {

        ret = ret + "{" + " ";

        int numVars = linkedHashMap.entrySet().size();
        int counter = 1;

        for (Map.Entry<String, Object> mapElement : linkedHashMap.entrySet()) {
            String key = mapElement.getKey();
            Object value = mapElement.getValue();

            if (value instanceof String) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        JSON_ESCAPE_DOUBLE_QUOTE + value + JSON_ESCAPE_DOUBLE_QUOTE;
            } else if (value instanceof StringSchema) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        JSON_ESCAPE_DOUBLE_QUOTE + "abcdefghijklmnopqrstuvwxyz" + JSON_ESCAPE_DOUBLE_QUOTE;
            } else if (value instanceof Integer) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        value;
            } else if (value instanceof IntegerSchema) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        "0";
            } else if (value instanceof EmailSchema) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        JSON_ESCAPE_DOUBLE_QUOTE + "user@example.com" + JSON_ESCAPE_DOUBLE_QUOTE;
            } else if (value instanceof Date) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        JSON_ESCAPE_DOUBLE_QUOTE + "01/01/2000" + JSON_ESCAPE_DOUBLE_QUOTE;
            } else if (value instanceof DateSchema) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        JSON_ESCAPE_DOUBLE_QUOTE + "01/01/2000" + JSON_ESCAPE_DOUBLE_QUOTE;
            } else if (value instanceof DateTime) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        JSON_ESCAPE_DOUBLE_QUOTE + "01/01/2000 h00:00:00" + JSON_ESCAPE_DOUBLE_QUOTE;
            } else if (value instanceof DateTimeSchema) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        JSON_ESCAPE_DOUBLE_QUOTE + "01/01/2000 h00:00:00" + JSON_ESCAPE_DOUBLE_QUOTE;
            } else if (value instanceof BooleanSchema) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        "true";
            } else if (value instanceof ArraySchema) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        "[]";
            } else if (value instanceof ByteArraySchema) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        "[]";
            } else if (value instanceof MapSchema) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        "[]";
            } else if (value instanceof Schema) {
                String ref = ((Schema)value).get$ref();
                Schema schema = getSchemaByRef(ref);
                if(schema.getProperties() != null) {
                    String in = ret + "\"" + key + JSON_ESCAPE_DOUBLE_QUOTE + ": ";
                    ret = traverseMap(((LinkedHashMap<String, Object>) schema.getProperties()), in);
                } else {
                    ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": \"<none>\"";
                }
            } else if (value instanceof LinkedHashMap) {
                String in = ret + "\"" + key + JSON_ESCAPE_DOUBLE_QUOTE + ": ";
                ret = traverseMap(((LinkedHashMap<String, Object>) value), in);
            } else {
                LOGGER.warn("Value type unrecognised: " + value.getClass());
            }

            if (counter < numVars) {
                // add comma unless last attribute
                ret = ret + "," + " ";
            }
            counter++;
        }

        ret = ret + JSON_ESCAPE_NEW_LINE + "}";

        return ret;
    }

    // Interaction between consumer and provider
    public class Interaction {

        private String statusCode;
        private String parameterName;
        private String parameterValue;
        private String requestBody;
        private String responseBody;
        private String requestExampleName;
        private String responseExampleName;

        public String getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(String statusCode) {
            this.statusCode = statusCode;
        }

        public String getParameterName() {
            return parameterName;
        }

        public void setParameterName(String parameterName) {
            this.parameterName = parameterName;
        }

        public String getParameterValue() {
            return parameterValue;
        }

        public void setParameterValue(String parameterValue) {
            this.parameterValue = parameterValue;
        }

        public String getRequestBody() {
            return requestBody;
        }

        public void setRequestBody(String requestBody) {
            this.requestBody = requestBody;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }


        public String getRequestExampleName() {
            return requestExampleName;
        }

        public void setRequestExampleName(String requestExampleName) {
            this.requestExampleName = requestExampleName;
        }

        public String getResponseExampleName() {
            return responseExampleName;
        }

        public void setResponseExampleName(String responseExampleName) {
            this.responseExampleName = responseExampleName;
        }
    }

    class InteractionResponse {
        private String statusCode;
        private String body;

        private String name;

        public String getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(String statusCode) {
            this.statusCode = statusCode;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


}

