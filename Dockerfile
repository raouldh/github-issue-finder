FROM ghcr.io/graalvm/graalvm-community:21 AS builder

ENV GRADLE_USER_HOME=/home/gradle/.gradle \
    JAVA_TOOL_OPTIONS="-Xmx2g -XX:+ExitOnOutOfMemoryError"

RUN gu install native-image || true

WORKDIR /workspace/app

COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts build.gradle.kts ./
RUN chmod +x gradlew

RUN ./gradlew --no-daemon build -x test || true

COPY src src
RUN ./gradlew --no-daemon nativeCompile -x test

FROM debian:bookworm-slim AS runtime

ENV APP_USER=app \
    APP_HOME=/app

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl grep \
    && rm -rf /var/lib/apt/lists/* \
    && useradd -r -s /sbin/nologin ${APP_USER}

WORKDIR ${APP_HOME}

COPY --from=builder /workspace/app/build/native/nativeCompile/github-issue-finder /app/app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health | grep '"status":"UP"' || exit 1

USER ${APP_USER}

ENTRYPOINT ["/app/app"]