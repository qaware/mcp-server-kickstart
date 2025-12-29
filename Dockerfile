# Multi-stage build for smaller final image
FROM gradle:8.14.2-jdk17 AS builder

WORKDIR /app

# Copy gradle files first for better caching
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle/

# Copy source code
COPY src src/
COPY log4j2.properties ./
COPY stopswords.txt ./

# Build the fat jar
RUN ./gradlew fatJar --no-daemon

# Runtime stage with minimal JRE
FROM eclipse-temurin:17-jre

WORKDIR /data

# Copy the built jar from builder stage to /opt (not /app to avoid volume mount conflicts)
COPY --from=builder /app/build/libs/mcp-server-kickstart-all-*.jar /opt/mcp-server.jar

# Default to running with knowledge tool - can be overridden
ENTRYPOINT ["java", "-jar", "/opt/mcp-server.jar"]
CMD ["--stdio", "com.qaware.mcp.tools.knowledge.McpKnowledgeTool"]
