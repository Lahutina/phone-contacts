FROM maven:3.6.0-jdk-11-slim
RUN mvn clean package

FROM openjdk:17.0.2-jdk-slim-buster
COPY target/PhoneContacts-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

