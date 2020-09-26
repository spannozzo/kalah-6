# Kalah 6

Creation of a rest game with docker, spring boot, swagger, open API 3, and mongodb.

## Description

Mancala / Kalah REST game with six tokens for each pit, instead of four of the classic versions.

## Getting Started

### Dependencies

Docker
Mongodb
Maven
java version 11

### Installing
```
docker-compose up
```
and the application will start. You can also run in local, by running docker db container outside from docker-compose:

```
docker-compose down
```
```
docker run --rm -d --name=mongodb -p 27017:27017 -e MONGODB_USERNAME=root -e MONGODB_PASSWORD=1234 -e MONGODB_DATABASE=kalah bitnami/mongodb:latest
```
If you want to run the tests you have to use a second docker mongodb container, specific for tests:

```
docker run --rm -d --name=mongodb_test -p 27018:27017 -e MONGODB_USERNAME=root -e MONGODB_PASSWORD=1234 -e MONGODB_DATABASE=kalah_test bitnami/mongodb:latest
```
You can run tests with 

```
mvn test
```
or just build the application with 

```
mvn clean package -DskipTests
```
This will generate a jar in the target folder so, you will be able to run it with

```
java -jar .\target\kalah-0.0.1-SNAPSHOT.jar
```
### Executing program
Once the application is running properly on your local, you can access to swagger ui from

```
localhost:8080/swagger-ui-custom.html
```
and you can download the open document file from

```
http://localhost:8080/api-docs.yml
```
this YAML file can be imported for generating Postman documentation, on Postman use import button and select open api file, then next. Postman will generate a collection of documented API call. 
A copy of this file is already in the resource folder of the application (./src/main/resources).

## Version History

* 0.0.1-SNAPSHOT
    * Initial Release


