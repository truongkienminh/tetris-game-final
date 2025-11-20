# ------------------------------
# 1. Build stage (Java 21 + Maven)
# ------------------------------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper & pom
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Cấp quyền thực thi cho mvnw
RUN chmod +x mvnw

# Download dependencies (cache)
RUN ./mvnw dependency:go-offline -B

# Copy source code & build jar
COPY src src
RUN ./mvnw package -DskipTests -B

# ------------------------------
# 2. Runtime stage
# ------------------------------
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy jar từ build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java","-jar","/app/app.jar"]
