# Etapa 1: compilación con Maven y Java 21
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B -Dmaven.wagon.http.retryHandler.count=10
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B -Dmaven.wagon.http.retryHandler.count=10

# Etapa 2: imagen de ejecución (SIN Maven)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/target/*.jar app.jar
USER spring
# catalog: 8082 | notify: 8083 | report: 8084
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]