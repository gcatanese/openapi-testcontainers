FROM openjdk:17.0.2-jdk-slim

ADD openapi/cli/openapi-generator-cli.jar /openapi/bin/openapi-generator-cli.jar
ADD target/openapi-testcontainers.jar /openapi/bin/openapi-testcontainers.jar

ARG openapifile=openapi/openapi.yaml
ADD $openapifile /openapi/openapi.yaml

RUN java -cp /openapi/bin/openapi-testcontainers.jar:/openapi/bin/openapi-generator-cli.jar \
  org.openapitools.codegen.OpenAPIGenerator generate -g com.tweesky.cloudtools.codegen.TestContainersCodegen \
   -i /openapi/openapi.yaml -o /openapi/go-server

FROM golang:1.19
WORKDIR /go/src
COPY --from=0 /openapi/go-server .

RUN go mod tidy
RUN go build -o build .

FROM ubuntu AS runtime
ENV GIN_MODE=release
COPY --from=1 /go/src/build /build

EXPOSE 8080/tcp
ENTRYPOINT ["./build"]
