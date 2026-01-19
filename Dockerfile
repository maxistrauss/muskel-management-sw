# Multi-Stage Build für Spring Boot mit Tailwind CSS
# Stage 1: Build Stage mit Node.js und Gradle
FROM gradle:8.5-jdk21 AS builder

# Node.js installieren (für Tailwind CSS)
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Arbeitsverzeichnis setzen
WORKDIR /app

# Gradle Wrapper und Build-Dateien kopieren
COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY package.json .
COPY package-lock.json* .
COPY tailwind.config.js .

#gradle
RUN gradle clean --no-daemon
# Abhängigkeiten downloaden (für besseres Caching)
RUN gradle dependencies --no-daemon || true

# Quellcode kopieren
COPY src src
COPY data data

# Tailwind CSS Abhängigkeiten installieren
RUN npm install tailwindcss

# Tailwind CSS kompilieren direkt in src (wird ins JAR kopiert)
RUN npx tailwindcss -i src/main/resources/static/css/input.css -o src/main/resources/static/css/output.css

# Gradle Clean und Projekt bauen

RUN gradle bootJar --no-daemon

# Stage 2: Runtime Stage (minimal)
FROM eclipse-temurin:21-jre-alpine

# Metadaten
LABEL maintainer="muskelmanagement"
LABEL description="Muskel Management Spring Boot Application"

# Arbeitsverzeichnis setzen
WORKDIR /app

# Non-root User erstellen für Sicherheit
RUN addgroup -S spring && adduser -S spring -G spring

#jannis changes
COPY --from=builder /app/data /app/data
# Verzeichnisse erstellen und Berechtigungen setzen (als root)
RUN mkdir -p /app/data && \
    chown -R spring:spring /app

# JAR aus Build Stage kopieren
COPY --from=builder --chown=spring:spring /app/build/libs/*.jar app.jar

# Jetzt zum spring User wechseln
USER spring:spring

# Port exponieren (Standard Spring Boot Port)
EXPOSE 8080

# Health Check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Application starten
ENTRYPOINT ["java", "-jar", "app.jar"]
