# # syntax=docker/dockerfile:1

# FROM eclipse-temurin:17-jdk-jammy

# WORKDIR /app

# COPY .mvn/ .mvn
# COPY mvnw pom.xml ./
# RUN ./mvnw dependency:resolve

# COPY src ./src

# CMD ["./mvnw", "spring-boot:run"]


# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk-jammy

ENV HOME=/app

ENV APP_NAME=appserver

WORKDIR $HOME

ADD ./target/*.jar $HOME/$APP_NAME.jar
ADD ./pom.xml $HOME/pom.xml
ENTRYPOINT java -jar -Dspring.profiles.active=docker $APP_NAME.jar
