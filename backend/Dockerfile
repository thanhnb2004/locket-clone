# ---- Build stage ----
FROM eclipse-temurin:25-jdk AS build

ARG MAVEN_VERSION=3.9.9
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
       | tar -xz -C /opt \
    && ln -s "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven \
    && rm -rf /var/lib/apt/lists/*
ENV PATH="/opt/maven/bin:${PATH}"

WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Build the application
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:25-jdk AS runtime

WORKDIR /app
RUN useradd -r -u 1001 locket
COPY --from=build /app/target/*.jar app.jar
RUN chown -R locket:locket /app
USER locket

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
