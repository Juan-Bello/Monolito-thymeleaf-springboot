FROM eclipse-temurin:21-jdk-jammy

# Install Maven dependencies needed for wrapper
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy everything
COPY . .

# Normalize line endings of mvnw (fix Windows CRLF -> LF) and make executable
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

EXPOSE 8080

# Run the app using Maven Wrapper
CMD ["./mvnw", "spring-boot:run"]
