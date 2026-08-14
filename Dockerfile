FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

COPY . .

# Build executable Spring Boot jar for rtnt-server.
RUN ./gradlew :rtnt-server:bootJar --no-daemon

FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY --from=builder /app/rtnt-server/build/libs/*-exec.jar /app/app.jar

ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
