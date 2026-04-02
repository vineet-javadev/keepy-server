# --- Stage 1: Build Stage ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only the pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime Stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy only the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Render provides a $PORT environment variable. 
# We tell Spring Boot to listen on that port.
ENV PORT=8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["sh", "-c", "java -Xmx512m -jar app.jar --server.port=${PORT}"]
