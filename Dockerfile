# ---- Build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Resolve dependencies first so they are cached until pom.xml changes.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --system --no-create-home appuser
COPY --from=build /app/target/finance.jar app.jar
USER appuser

# prod profile: Secure session cookie (Render serves over https).
# Render injects PORT; application.properties uses server.port=${PORT:8080}.
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
