# Dockerfile for FIVUCSAS Client Apps - Desktop Application
# Multi-stage build for optimal image size
#
# This Dockerfile creates a containerized environment for running the Desktop application
# Follows Docker best practices and security guidelines

# Stage 1: Build Stage
FROM gradle:8.14-jdk21 AS builder

LABEL maintainer="FIVUCSAS Team"
LABEL description="FIVUCSAS Facial Identity Verification - Desktop App Builder"

# Set working directory
WORKDIR /app

# Copy Gradle configuration files first (for layer caching)
COPY gradle/ gradle/
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts build.gradle.kts ./

# Copy source code
COPY shared/ shared/
COPY desktopApp/ desktopApp/
COPY androidApp/ androidApp/
COPY docs/ docs/
COPY *.md ./

# Build the application
RUN ./gradlew :desktopApp:packageDistributionForCurrentOS --no-daemon

# Stage 2: Runtime Stage
FROM openjdk:21-jdk-slim

LABEL maintainer="FIVUCSAS Team"
LABEL description="FIVUCSAS Facial Identity Verification - Desktop App Runtime"
LABEL version="1.0.0"

# Install runtime dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libfreetype6 \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

# Create application user (security best practice)
RUN groupadd -r fivucsas && useradd -r -g fivucsas -m -s /bin/bash fivucsas

# Set working directory
WORKDIR /home/fivucsas/app

# Copy built application from builder stage
COPY --from=builder --chown=fivucsas:fivucsas /app/desktopApp/build/compose/binaries/main/app/ ./

# Copy environment configuration
COPY --chown=fivucsas:fivucsas .env.example .env

# Switch to non-root user
USER fivucsas

# Expose any ports if needed (uncomment and adjust as needed)
# EXPOSE 8080

# Health check (adjust as needed for your application)
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD ps aux | grep -q '[j]ava' || exit 1

# Set environment variables
ENV APP_HOME=/home/fivucsas/app
ENV JAVA_OPTS="-Xmx2g -Xms512m"

# Run the application
CMD ["java", "-jar", "desktop-app.jar"]

# Alternative: Run with JVM options
# CMD ["sh", "-c", "java $JAVA_OPTS -jar desktop-app.jar"]

# Build instructions:
# docker build -t fivucsas-desktop:latest .
#
# Run instructions:
# docker run -it --rm \
#   -e DISPLAY=$DISPLAY \
#   -v /tmp/.X11-unix:/tmp/.X11-unix \
#   --name fivucsas-desktop \
#   fivucsas-desktop:latest
#
# For GUI applications, you may need to allow X11 forwarding:
# xhost +local:docker
# (Remember to revoke with: xhost -local:docker)
