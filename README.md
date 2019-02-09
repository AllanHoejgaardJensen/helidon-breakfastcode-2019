
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
kubectl create -f target/app.yaml               # Deploy application
kubectl get service breakfastcoding  # Verify deployed service
```


