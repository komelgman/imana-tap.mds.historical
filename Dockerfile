ARG MAVEN_VERSION=3.9.11
ARG JAVA_VERSION=25

FROM maven:${MAVEN_VERSION}-eclipse-temurin-${JAVA_VERSION} AS builder
WORKDIR /app

ARG JAVA_VERSION=25
ENV JAVA_VERSION=${JAVA_VERSION}

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

COPY src/ ./src/
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

RUN mkdir -p target/extracted
RUN java -Djarmode=tools -jar target/application.jar extract --destination target/extracted

RUN jdeps --ignore-missing-deps -q \
    --recursive \
    --multi-release ${JAVA_VERSION} \
    --print-module-deps \
    --class-path 'target/extracted/lib/*' \
    target/extracted/application.jar > deps.info

RUN jlink \
    --add-modules $(cat deps.info),jdk.jdwp.agent,jdk.jdi,jdk.management.agent,jdk.attach,jdk.management \
    --compress zip-9 \
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
    PATH=/jre/bin:$PATH

ENTRYPOINT ["java", "-jar", "application.jar"]