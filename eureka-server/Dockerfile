# Use Eclipse Temurin JDK runtime as a parent image
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the built JAR file from target directory
COPY target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8761

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]