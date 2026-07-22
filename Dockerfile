# syntax=docker/dockerfile:1

# Dockerfile único para los 7 servicios: el stage de build compila el reactor
# Maven completo y es idéntico para todas las imágenes, así BuildKit lo
# construye una sola vez. MODULE selecciona qué jar va al runtime.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
ARG MODULE
RUN addgroup -S spring && adduser -S spring -G spring
USER spring
COPY --from=build /workspace/${MODULE}/target/${MODULE}-*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
