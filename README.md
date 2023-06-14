# OpenAPI Testcontainers

Testcontainers module for OpenAPI: from an OpenAPI specification creates a [Testcontainers](https://www.testcontainers.org/) module to simplify ~~Integration~~ Contract Testing.

Write your tests against the API specifications.

## Overview

The OpenAPI Testcontainers extension allows developers to create on-the-fly a mock version of the API being tested.
The API container is loaded when the Junit tests start and can be used to test the different endpoints and payloads.


## Usage

Define Testcontainers [dependency](https://www.testcontainers.org/#prerequisites) 

Add the rule to create the Docker container from your own OpenAPI file (`myOpenapiFile.yaml`)  
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

The module uses the [OpenAPI Generator](https://github.com/OpenAPITools/openapi-generator) to genereate a lightweight 
mock server based on the OpenAPI specification. The request and response examples are found and matched to define the 
interactions (contract) between the consumer and the producer of the API.  

The following strategies are applied (in order) to match requests with responses:
* **match by Contract tag**: do requests and examples use the vendor extension `x-contract-id`? In this case match a request with a response example that has the same value
* **match by Ref name**: match `$ref` request example with a corresponding `$ref` response example, for example 
`create-users-example` would match `create-users-example-200` to define a successful `200` scenario
* **match by Example name**: find request and response examples that have the same name
* **generate from Schema**: fallback strategy (when no matching is found): generate the response from the Schema and
ensure every request has at least a response.

Check out the [Contract Testing with OpenAPI](https://medium.com/geekculture/contract-testing-with-openapi-42267098ddc7) article
to understand challenges and solutions of Contract Testing with the OpenAPI standard.



## Standalone mock

Run the mock server standalone

```docker
docker build --build-arg openapifile=/path/to/myOpenapiFile.yaml -t openapi-testcontainers .

docker run --rm -d -p 8080:8080 --name openapi-testcontainers-app openapi-testcontainers

```
Access the index page
```shell
curl http://localhost:8080/index/
```



