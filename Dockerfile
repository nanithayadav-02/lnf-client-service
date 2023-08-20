FROM amazoncorretto:17-alpine-jdk
ARG JAR_FILE=target/*.jar
RUN mkdir /opt/app
COPY ${JAR_FILE} /opt/app/lnf-client-service.jar
EXPOSE 8081
ENTRYPOINT  ["java", "-jar", "/opt/app/lnf-client-service.jar"]