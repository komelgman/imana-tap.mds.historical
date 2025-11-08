FROM maven:3.9.11-eclipse-temurin-25 AS builder
WORKDIR /app

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

COPY src/ ./src/
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B

RUN mkdir -p target/extracted
RUN java -Djarmode=tools -jar target/application.jar extract --destination target/extracted

RUN jdeps --ignore-missing-deps -q \
    --recursive \
    --multi-release 25 \
    --print-module-deps \
    --class-path 'target/extracted/lib/*' \
    target/extracted/application.jar > deps.info

RUN jlink \
    --verbose \
    --add-modules $(cat deps.info) \
    --compress 2 \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --output /custom_jre

FROM gcr.io/distroless/base-debian12:nonroot
WORKDIR /app

COPY --from=builder /custom_jre /jre
COPY --from=builder /app/target/extracted/lib/ ./lib/
COPY --from=builder /app/target/extracted/application.jar ./application.jar

ENV JAVA_HOME=/jre \
    PATH=/jre/bin:$

ENTRYPOINT ["java", "-jar", "application.jar"]
