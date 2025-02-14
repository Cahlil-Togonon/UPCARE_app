FROM maven:3.8.6-openjdk-11-slim as builder

WORKDIR /app

COPY . .
# RUN mvn dependency:go-offline
# RUN mvn clean package

EXPOSE 9098
ENTRYPOINT ["java", "-jar", "/app/target/gh_configured_router-0.0.1-SNAPSHOT.jar"]


# FROM openjdk:17-jdk-slim

# WORKDIR /app

# COPY --from=builder /app/target/gh_configured_router-0.0.1-SNAPSHOT.jar app.jar

# EXPOSE 9098

# ENTRYPOINT ["java", "-jar", "app.jar"]