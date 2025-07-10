# Pomodoro Timer Web Application

## Overview
Pomodoro Timer is a web-based productivity application that implements the Pomodoro Technique - a time management method that uses a timer to break work into intervals, traditionally 25 minutes in length, separated by short breaks. This application helps users manage their work sessions, track productivity, and maintain focus.

## Features
- **Customizable Timer**: Focus sessions (25 minutes) and break sessions
- **Task Management**: Add descriptions to your Pomodoro sessions
- **Session Tracking**: Record completed sessions and view productivity statistics
- **User Authentication**: Secure login and registration system
- **User Profiles**: Personalized dashboard and settings
- **Responsive Design**: Works on desktop and mobile devices
- **Audio Notifications**: Sound alerts when sessions end

## Technologies Used

### Backend
- **Java 21**: Core programming language
- **Spring Boot 3.2.3**: Application framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database access and ORM
- **Hibernate**: Object-relational mapping
- **SQLite**: Development database
- **PostgreSQL**: Production database option
- **Lombok**: Reduces boilerplate code
- **Maven**: Dependency management and build tool

### Frontend
- **Thymeleaf**: Server-side Java template engine
- **Tailwind CSS**: Utility-first CSS framework
- **JavaScript**: Client-side functionality
- **Tone.js**: Audio feedback library

## Project Structure
```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/pomodoro/app/
│   │   │       ├── config/        # Application configuration
│   │   │       ├── controller/    # MVC controllers
│   │   │       ├── dto/           # Data transfer objects
│   │   │       ├── model/         # Entity models
│   │   │       ├── repository/    # Data access layer
│   │   │       └── service/       # Business logic
│   │   └── resources/
│   │       ├── static/            # CSS, JavaScript
│   │       ├── templates/         # Thymeleaf templates
│   │       └── application.properties # App configuration
└── pom.xml                        # Maven dependencies
```

## Setup Instructions

### Prerequisites
- Java Development Kit (JDK) 21
- Maven 3.8+ (or use the included Maven wrapper)
- Git (optional)

### Local Development Setup

1. **Clone the repository** (if using Git):
   ```
   git clone <repository-url>
   cd pomodoro_prod
   ```

2. **Build the application**:
   ```
   ./mvnw clean install
   ```
   Or with Maven installed:
   ```
   mvn clean install
   ```

3. **Run the application**:
   ```
   ./mvnw spring-boot:run
   ```
   Or with Maven installed:
   ```
   mvn spring-boot:run
   ```

4. **Access the application**:
   Open your browser and navigate to `http://localhost:8080`

### Database Configuration

The application uses SQLite for development by default. The database file will be created automatically in the project root directory.

To use PostgreSQL (for production):

1. Update the `application-prod.properties` file with your PostgreSQL connection details
2. Run the application with the production profile:
   ```
   ./mvnw spring-boot:run -Dspring.profiles.active=prod
   ```

## Usage

1. Register a new account or login with existing credentials
2. Navigate to the Timer page
3. Enter a task description (optional)
4. Start a focus session
5. Work until the timer ends
6. Take a break when prompted
7. View your productivity statistics on the dashboard

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgements

- The Pomodoro Technique was developed by Francesco Cirillo
- Built with Spring Boot and modern web technologies