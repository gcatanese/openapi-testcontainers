# openapi-testcontainers

Testcontainers module for OpenAPI

## Overview

From an OpenAPI specification creates a Testcontainers module to simplify ~~Integration~~ Contract Testing.

## Usage

* [Run with Docker](#run-with-docker)
* [Build from source](#run-from-source)

### Run with Docker

Run with the pre-built image passing `-i` inputspec (path of the OpenAPI spec file) and `-o` output dir (location
of the generated file i.e ./postman/gen).

It supports the following commands:
* `generate`: create the postman.json file
* `push`: create postman.json and push to your postman.com default `My Workspace`.
  This uses the [Postman API](https://www.postman.com/postman/workspace/postman-public-workspace/folder/12959542-c705956d-1005-4fbc-803c-b6b985242a85?ctx=documentation)
  and requires a valid API key from Postman's integrations [dashboard](https://web.postman.co/settings/me/api-keys).

```docker
# generate only
docker run -v $(pwd):/usr/src/app \
   -it --rm --name postmanv2-container gcatanese/openapi-generator-postman-v2 generate \
   -i src/test/resources/SampleProject.yaml \
   -o tmp 

# generate only (with additional parameters)
docker run -v $(pwd):/usr/src/app \
   -it --rm --name postmanv2-container gcatanese/openapi-generator-postman-v2 generate \
   -i src/test/resources/SampleProject.yaml \
   -o tmp \
   --additional-properties folderStrategy=Tags,postmanVariables=MY_VAR1-ANOTHERVAR


# generate and push to Postman.com
# note: require POSTMAN API KEY
docker run -v $(pwd):/usr/src/app \
   -e POSTMAN_API_KEY=YOUR_POSTMAN_API_KEY \
   -it --rm --name postmanv2-container gcatanese/openapi-generator-postman-v2 push \
   -i src/test/resources/SampleProject.yaml \
   -o tmp \
   --additional-properties folderStrategy=Tags,postmanVariables=MY_VAR1-ANOTHERVAR      
```

### Run from source

Clone and build [OpenAPI Generator](https://github.com/OpenAPITools/openapi-generator) CLI

Build `postman-v2` from source

Run OpenAPI Generator adding `postman-v2` jar file in the class path and specifying the `PostmanV2Generator` generator:
```shell
java -cp target/openapi-generator-postman-v2.jar:/openapi-generator/modules/openapi-generator-cli/target/openapi-generator-cli.jar \
  org.openapitools.codegen.OpenAPIGenerator generate -g com.tweesky.cloudtools.codegen.PostmanV2Generator \
  -i src/test/resources/BasicJson.json -o output
```
