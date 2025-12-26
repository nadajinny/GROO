FROM gradle:8.14-jdk17 AS builder
WORKDIR /workspace

COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle src src

RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=default

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
