# ---- Stage 1: build the jar ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copy pom.xml first and download dependencies. Docker caches this step,
# so later builds skip the download unless pom.xml changes.
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src/ src/
RUN mvn -q package -DskipTests

# ---- Stage 2: run the jar (small image, no Maven or source code) ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]