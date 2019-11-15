
# Breakfast Coding 2019

This example implements a REST service using Helidon MicroProfile
and shows HTTP Verbs (GET, POST, PUT, DELETE, and PATCH )used
together with eTags, caching, content-type based versioning and
idempotency.

## Prerequisites

1. Maven 3.5 or newer
2. Java SE 11 or newer
3. Docker 18 or newer (if you want to build and run docker images)
4. Kubernetes minikube v0.33 or newer (if you want to deploy to Kubernetes)
   or access to a Kubernetes 1.10 or newer cluster
5. Kubectl 1.12 or newer for deploying to Kubernetes

Verify prerequisites
```
java -version
mvn --version
docker --version
minikube version
kubectl version --short
```

## Build

```
mvn verify
```

## Start the application

```
java -jar target/breakfastcoding.jar
```

## Exercise the application

```
curl -X GET http://localhost:8080/meet
{"message":"Hello World!"}

curl -X GET http://localhost:8080/meet/Joe
{"message":"Hello Joe!"}

curl -X PUT http://localhost:8080/meet-representation/Hola
{"gretting":"Hola"}

curl -X GET http://localhost:8080/meet/Jose
{"message":"Hola Jose!"}

curl -i -H "Accept-Language: da" -H "Accept: application/hal+json" -X GET http://localhost:8080/greetings/hallihalleje

```

## calling seeing the headers etc.
```
curl -i -H "Accept-Language: da" -H "Accept: application/hal+json" -X GET http://localhost:8080/greetings/hallo
HTTP/1.1 200 OK
Cache-Control: no-transform, max-age=60
Content-Type: application/hal+json;p=greeting;v=4
Date: Thu, 14 Nov 2019 13:16:45 +0100
ETag: "c8de8162"
Last-Modified: Tue, 06 Aug 2019 06:46:40 GMT
X-Log-Token: e97b3c14-6aac-4419-a34c-7fe60efa5614
transfer-encoding: chunked
connection: keep-alive

{"_links":{"self":{"href":"/greetings/hallo","templated":false,"type":"application/hal+json;p=greeting","name":"Danish Greeting Hallo","title":"Dansk Hilsen Hallo","hreflang":"da","seen":"2019-11-14T12:03:53.819646Z"}},"greeting":"Hallo!","language":"Dansk","country":"Danmark","native":{"language":"Dansk","country":"Danmark"}}


```

## calling something that have moved and follow the redirect
```
curl -i -L -H "Accept-Language: da" -H "Accept: application/hal+json" -X GET http://localhost:8080/greetings/hallihalleje
HTTP/1.1 301 Moved Permanently
Content-Type: application/hal+json
Date: Thu, 14 Nov 2019 13:19:01 +0100
Location: http://[0:0:0:0:0:0:0:1]:8080/greetings/hallo
X-Log-Token: f8271958-9ae7-4553-8539-8f4c52a3dec3
transfer-encoding: chunked
connection: keep-alive

HTTP/1.1 200 OK
Cache-Control: no-transform, max-age=60
Content-Type: application/hal+json;p=greeting;v=4
Date: Thu, 14 Nov 2019 13:19:01 +0100
ETag: "c8de8162"
Last-Modified: Tue, 06 Aug 2019 06:46:40 GMT
X-Log-Token: fc7028ce-dcf6-4122-a24f-6a7ce7762c6c
transfer-encoding: chunked
connection: keep-alive

{"_links":{"self":{"href":"/greetings/hallo","templated":false,"type":"application/hal+json;p=greeting","name":"Danish Greeting Hallo","title":"Dansk Hilsen Hallo","hreflang":"da","seen":"2019-11-14T12:03:53.819646Z"}},"greeting":"Hallo!","language":"Dansk","country":"Danmark","native":{"language":"Dansk","country":"Danmark"}}

```

## Try health and metrics

```
curl -s -X GET http://localhost:8080/health
{"outcome":"UP",...
. . .

# Prometheus Format
curl -s -X GET http://localhost:8080/metrics
# TYPE base:gc_g1_young_generation_count gauge
. . .

# JSON Format
curl -H 'Accept: application/json' -X GET http://localhost:8080/metrics
{"base":...
. . .

```

## Build the Docker Image

```
docker build -t breakfastcoding target
```

## Start the application with Docker

```
docker run --rm -p 8080:8080 breakfastcoding:latest
```

Exercise the application as described above

## Deploy the application to Kubernetes

```
kubectl cluster-info                         # Verify which cluster
kubectl get pods                             # Verify connectivity to cluster
kubectl create -f target/app.yaml            # Deploy application
kubectl get service breakfastcoding          # Verify deployed service
```
