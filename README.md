# FileStore

This is a simple web based file server, used to store a local file on a remote server and also perform search and query operations on those files.

## Build and run


This example requires a database.
Instructions for H2 can be found here: https://www.h2database.com/html/cheatSheet.html


With JDK17+
```bash
mvn package
java -jar target/fileserver.jar
```

## Exercise the application
```
curl -X GET http://localhost:8080/v1/files
[]
```



## Building a Native Image

The generation of native binaries requires an installation of GraalVM 22.1.0+.

In order to produce a native binary, you must run the H2 Database as a separate process
and use a network connection for access. The simplest way to do this is by starting a Docker
container as follows:

```
docker run -d -p 1521:1521 -p 81:81 -e H2_OPTIONS='-ifNotExists' --name=h2 oscarfonts/h2
```

The resulting container will listen to port 1521 for network connections.
Switch property `javax.sql.DataSource.test.dataSource.url` in `microprofile-config.properties`
to use a TCP connection:

```
javax.sql.DataSource.test.dataSource.url=jdbc:h2:tcp://localhost:1521/test
```

Next, uncomment the following dependency in your project's pom file:

```
<dependency>
    <groupId>io.helidon.integrations.db</groupId>
    <artifactId>h2</artifactId>
</dependency>
```

With all these changes, re-build your project and verify that all tests are passing.
Finally, you can build a native binary using Maven as follows:

```
mvn -Pnative-image install -DskipTests
```

The generation of the executable binary may take several minutes to complete
depending on your hardware and operating system --with Linux typically outperforming other
platforms. When completed, the executable file will be available
under the `target` directory and be named after the artifact ID you have chosen during the
project generation phase.



## Try metrics

```
# Prometheus Format
curl -s -X GET http://localhost:8080/metrics
# TYPE base:gc_g1_young_generation_count gauge
. . .

# JSON Format
curl -H 'Accept: application/json' -X GET http://localhost:8080/metrics
{"base":...
. . .
```



## Try health

```
curl -s -X GET http://localhost:8080/health
{"outcome":"UP",...

```


## Tracing

### Set up Jaeger

First, you need to run the Jaeger tracer. Helidon will communicate with this tracer at runtime.

Run Jaeger within a docker container:
```
docker run -d --name jaeger\
   -e COLLECTOR_ZIPKIN_HOST_PORT=:9411\
   -e COLLECTOR_OTLP_ENABLED=true\
   -p 6831:6831/udp\
   -p 6832:6832/udp\
   -p 5778:5778\
   -p 16686:16686\
   -p 4317:4317\   
   -p 4318:4318\
   -p 14250:14250\
   -p 14268:14268\
   -p 14269:14269\
   -p 9411:9411\
   jaegertracing/all-in-one:1.38
```

### View Tracing Using Jaeger UI

Jaeger provides a web-based UI at http://localhost:16686, where you can see a visual representation of
the same data and the relationship between spans within a trace.


## Building the Docker Image
```
docker build -t fileserver .
```

## Running the Docker Image

```
docker run --rm -p 8080:8080 fileserver:latest
```

Exercise the application as described above.
                                

## Deploy the application to Kubernetes

```
kubectl cluster-info                        # Verify which cluster
kubectl get pods                            # Verify connectivity to cluster
kubectl create -f app.yaml                  # Deploy application
kubectl get pods                            # Wait for quickstart pod to be RUNNING
kubectl get service  fileserver         # Get service info
```

Note the PORTs. You can now exercise the application as you did before but use the second
port number (the NodePort) instead of 8080.

After you’re done, cleanup.

```
kubectl delete -f app.yaml
```
                                
