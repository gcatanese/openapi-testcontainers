# openapi-testcontainers

Testcontainers module for OpenAPI: from an OpenAPI specification creates a [Testcontainers](https://www.testcontainers.org/) module to simplify ~~Integration~~ Contract Testing.

Write your tests against the API specifications.

## Overview

The OpenAPI Testcontainers extension allows developers to create on-the-fly a mock version of the API being tested.
The API container is loaded when the Junit tests start and can be used to test the different endpoints and payloads.


## Usage

Define Testcontainers [dependency](https://www.testcontainers.org/#prerequisites) 

Create the rule to create the Docker container from your `myOpenapiFile.yaml`  
```
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
```
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

## How it works

## Standalone mock

Run the mock server standalone

```docker
docker build --build-arg openapifile=/path/to/myOpenapiFile.yaml -t openapi-testcontainers .

docker run --rm -d -p 8080:8080 --name openapi-testcontainers-app openapi-testcontainers
```
The API is reachable
```shell
curl http://localhost
```

