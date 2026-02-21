# Quick Start Guide - APVD Backend

## Prerequisites
- Java 25 or higher
- MySQL Server
- Maven (or use mvnw)

## Setup Steps

### 1. Database Setup
```sql
-- Create database
CREATE DATABASE apvd_db;

-- Use the database
USE apvd_db;
```

### 2. Update Configuration
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/apvd_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Build the Project
```bash
cd apvd
./mvnw clean install
```

### 4. Run the Application
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## Testing the API

### 1. Register a Student
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student1",
    "userEmail": "student1@example.com",
    "userPassword": "password123",
    "mobile": "9876543210",
    "role": "STUDENT",
    "enrollmentNumber": "CS2023001",
    "department": "Computer Science",
    "semester": 1
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userIdOrEmail": "APVD1001",
    "password": "password123"
  }'
```

Response will include a JWT token.

### 3. Add Marks (Faculty)
```bash
curl -X POST http://localhost:8080/api/marks/save \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "userId": "APVD1001",
    "semester": 1,
    "subject1Mark": 85,
    "subject2Mark": 90,
    "subject3Mark": 78,
    "subject4Mark": 88,
    "subject5Mark": 92,
    "subject6Mark": 80
  }'
```

### 4. Mark Attendance
```bash
curl -X POST http://localhost:8080/api/attendance/mark \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "userId": "APVD1001",
    "attendanceDate": "2024-02-09",
    "status": "PRESENT"
  }'
```

### 5. Get Student Marks
```bash
curl -X GET http://localhost:8080/api/marks/APVD1001 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 6. Get Attendance
```bash
curl -X GET http://localhost:8080/api/attendance/APVD1001 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Default Test Users

After the application starts, you can create test users:

### Admin User
```json
{
  "username": "admin",
  "userEmail": "admin@example.com",
  "userPassword": "admin123",
  "mobile": "9876543210",
  "role": "ADMIN"
}
```

### Faculty User
```json
{
  "username": "faculty1",
  "userEmail": "faculty1@example.com",
  "userPassword": "faculty123",
  "mobile": "9876543210",
  "role": "FACULTY",
  "department": "Computer Science",
  "qualification": "M.Tech",
  "specialization": "AI"
}
```

## Project Structure
```
apvd/
├── src/
│   ├── main/
│   │   ├── java/com/jd/apvd/
│   │   │   ├── controller/      # REST Controllers
│   │   │   ├── service/         # Business Logic
│   │   │   ├── entity/          # Database Models
│   │   │   ├── repository/      # Data Access Layer
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── security/        # JWT & Security
│   │   │   └── ApvdApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── BACKEND_API_DOCUMENTATION.md
```

## Troubleshooting

### JAVA_HOME Error
Set JAVA_HOME environment variable:
```bash
export JAVA_HOME=/path/to/java
```

### Database Connection Error
- Verify MySQL is running
- Check credentials in application.properties
- Ensure database exists

### Port Already in Use
Change port in application.properties:
```properties
server.port=8081
```

### Build Errors
Clean and rebuild:
```bash
./mvnw clean compile
```

## API Documentation
See `BACKEND_API_DOCUMENTATION.md` for complete API documentation with examples.

## Features Included
✅ User Registration & Login with JWT
✅ Role-Based Access Control (RBAC)
✅ Student Marks Management
✅ Attendance Tracking
✅ Automatic SGPA Calculation
✅ Faculty Operations
✅ Admin Operations
✅ CORS Support

## Next Steps
1. Integrate with frontend (React/Vite)
2. Set up environment-specific configurations
3. Configure CI/CD pipelines
4. Add logging and monitoring
5. Implement email notifications
