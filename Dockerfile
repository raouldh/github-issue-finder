FROM eclipse-temurin:21-jdk-alpine AS builder

ENV GRADLE_USER_HOME=/home/gradle/.gradle \
    JAVA_TOOL_OPTIONS="-Xmx512m -XX:+ExitOnOutOfMemoryError"

WORKDIR /workspace/app

COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts build.gradle.kts ./
RUN chmod +x gradlew

RUN ./gradlew --no-daemon build -x test || true

COPY src src
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre-alpine AS runtime

ENV APP_USER=app \
    APP_HOME=/app \
    JAVA_TOOL_OPTIONS="-Xmx512m -XX:+ExitOnOutOfMemoryError"

RUN apk add --no-cache curl \
    && addgroup -S ${APP_USER} \
    && adduser -S ${APP_USER} -G ${APP_USER}

WORKDIR ${APP_HOME}

COPY --from=builder /workspace/app/build/libs/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health | grep '"status":"UP"' || exit 1

USER ${APP_USER}

ENTRYPOINT ["java","-jar","/app/app.jar"]