FROM openjdk:11.0.14.1-oraclelinux8
EXPOSE 8080 5005
COPY ./target /opt/target
WORKDIR /opt/target

#ENV _JAVA_OPTIONS '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005'

CMD ["java","-jar","kalah-0.0.1-SNAPSHOT.jar"]
