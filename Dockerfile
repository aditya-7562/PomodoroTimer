# Use OpenJDK 21
FROM eclipse-temurin:21-jdk

# Create app directory
WORKDIR /app

# Copy the Maven wrapper files and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Build dependencies first to leverage Docker cache
RUN ./mvnw dependency:go-offline

# Copy the rest of the application
COPY . .

# Package the app
RUN ./mvnw clean package -DskipTests

# Run the app
CMD ["java", "-jar", "target/pomodoro-timer-0.0.1-SNAPSHOT.jar"]
