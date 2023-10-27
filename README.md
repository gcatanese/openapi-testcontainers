# OpenAPI Testcontainers

Testcontainers module for OpenAPI: from an OpenAPI specification creates a [Testcontainers](https://www.testcontainers.org/) module to simplify ~~Integration~~ Contract Testing.

Write your tests against the API specifications (check the [sample](https://github.com/gcatanese/openapi-testcontainers-demo)).

## Overview

The OpenAPI Testcontainers extension allows developers to create on-the-fly a lightweight instance of the API consumed by the application.
The API container is loaded when the Junit tests start and can be used to mock the different endpoints and payloads.

This repository does not contain source code, but only a README on how to create and run TestContainers with an OpenAPI specification.  
It integrates the [OpenAPI Native Mock Server](https://github.com/gcatanese/openapi-native-mock-server) to generate the mock server.

Check out the [Contract Testing with OpenAPI](https://medium.com/geekculture/contract-testing-with-openapi-42267098ddc7) article
to understand the challenges and solutions of Contract Testing with the OpenAPI standard.


## Usage

Define Testcontainers [dependency](https://www.testcontainers.org/#prerequisites) 

Add the rule to create the Docker container from the OpenAPI file (`myOpenapiFile.yaml`)  
```java
    boolean deleteOnExit = false;
    
    @Rule
    public GenericContainer container = new GenericContainer(
            new ImageFromDockerfile("my-test-container", deleteOnExit)
                    .withFileFromFile("openapi.yaml", new File("myOpenapiFile.yaml"))
                    .withFileFromFile("Dockerfile", new File("Dockerfile"))
    )
            .withExposedPorts(8080);

```
Write your tests
```java
    @Test
    public void testGet() throws Exception {

        var client = HttpClient.newHttpClient();
        
        // test /users/1000
        var uri = "http://" + container.getHost() + ":" + container.getFirstMappedPort() + "/users/1000";

        var request = HttpRequest.newBuilder(URI.create(uri))
                .header("accept", "application/json").GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

```

