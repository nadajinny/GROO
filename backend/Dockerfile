FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=default

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
