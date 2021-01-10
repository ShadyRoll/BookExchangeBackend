FROM openjdk:11-jre-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY files /files
ENTRYPOINT ["java","-jar","/app.jar"]
