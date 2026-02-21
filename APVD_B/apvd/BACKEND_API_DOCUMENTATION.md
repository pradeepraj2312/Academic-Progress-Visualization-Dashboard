# Academic Progress Visualization Dashboard - Backend API Documentation

## Overview
This is a comprehensive backend system for managing academic progress, student marks, and attendance for an educational institution.

## Technology Stack
- **Framework**: Spring Boot 4.0.2
- **Database**: MySQL
- **Language**: Java 25
- **Security**: JWT (JSON Web Tokens)
- **Build Tool**: Maven

## Database Setup

Before running the application, create the MySQL database:

```sql
CREATE DATABASE apvd_db;
```

The application will automatically create tables using Hibernate (ddl-auto=update).

## Features Implemented

### 1. User Management
- **User Registration** with roles: STUDENT, FACULTY, ADMIN
- **User Login** with JWT authentication
- **Automatic Role-Based User Registration**: Users are automatically added to their respective tables (Students, Faculty, Admin)
- **User ID Generation**: Unique user IDs in format APVD1001, APVD1002, etc.

### 2. Student Management
- Add students (by Admin or Faculty)
- Store student details: username, email, enrollment number, department, semester
- Retrieve student information

### 3. Marks Management
- **Store Student Marks**: 6 subjects per semester
- **Automatic SGPA Calculation**: Calculated based on marks
- **Update Marks**: Faculty can update individual subject marks
- **Retrieve Marks**: By student, semester, or date range

### 4. Attendance Management
- **Mark Attendance**: Faculty can mark attendance as Present/Absent
- **Attendance Tracking**: Date-based attendance records
- **Attendance Percentage**: Calculate attendance percentage for date range
- **Attendance History**: View all attendance records

### 5. Role-Based Access Control
- **ADMIN**: Full system access, can add students, faculty, and admins
- **FACULTY**: Can add students, update marks, and mark attendance
- **STUDENT**: Can view their own marks and attendance

## API Endpoints

### Authentication Endpoints (Public)
```
POST /api/auth/register          - Register a new user
POST /api/auth/login             - Login with userID or email
GET  /api/auth/verify            - Verify JWT token
```

### User Management Endpoints
```
POST /api/users/admin/add-student        - Admin adds a student
POST /api/users/admin/add-faculty        - Admin adds a faculty
POST /api/users/faculty/add-student      - Faculty adds a student
GET  /api/users/role/{role}              - Get all users by role
GET  /api/users/{userId}                 - Get user by userId
GET  /api/users/email/{email}            - Get user by email
PUT  /api/users/{userId}                 - Update user (Admin/Faculty)
DELETE /api/users/{userId}               - Delete user (Admin only)
```

### Student Marks Endpoints
```
POST /api/marks/save                                    - Save/Update student marks
GET  /api/marks/{userId}                               - Get all marks for student
GET  /api/marks/{userId}/semester/{semester}          - Get marks for specific semester
GET  /api/marks/semester/{semester}                   - Get all marks for a semester
GET  /api/marks/email/{userEmail}                     - Get marks by email
PUT  /api/marks/{userId}/semester/{semester}/subject/{subjectNumber}  - Update specific subject mark
DELETE /api/marks/{marksId}                           - Delete marks (Admin only)
```

### Attendance Endpoints
```
POST /api/attendance/mark                              - Mark attendance
PUT  /api/attendance/{attendanceId}                    - Update attendance record
GET  /api/attendance/{userId}                          - Get all attendance records
GET  /api/attendance/{userId}/range                   - Get attendance for date range
GET  /api/attendance/date/{date}                      - Get attendance for specific date
GET  /api/attendance/{userId}/percentage              - Get attendance percentage
DELETE /api/attendance/{attendanceId}                 - Delete attendance (Admin only)
```

## Database Models

### Users Table
```
- internalId (PK)
- userId (unique) - APVD1001 format
- username
- userEmail (unique)
- userPassword (encrypted)
- mobile
- role (STUDENT, FACULTY, ADMIN)
- createdAt, updatedAt
```

### Students Table
```
- id (PK)
- userId (FK to Users)
- username
- userEmail
- mobile
- enrollmentNumber
- department
- semester
- createdAt, updatedAt
```

### Faculty Table
```
- id (PK)
- userId (FK to Users)
- username
- userEmail
- mobile
- department
- qualification
- specialization
- createdAt, updatedAt
```

### Admin Table
```
- id (PK)
- userId (FK to Users)
- username
- userEmail
- mobile
- department
- createdAt, updatedAt
```

### StudentMarks Table
```
- id (PK)
- userId (FK)
- username
- userEmail
- semester
- subject1Mark to subject6Mark
- totalMarks (auto-calculated)
- sgpa (auto-calculated)
- createdAt, updatedAt
```

### StudentAttendance Table
```
- id (PK)
- userId (FK)
- username
- userEmail
- attendanceDate
- status (PRESENT, ABSENT)
- remarks
- createdAt, updatedAt
```

## Request/Response Examples

### Register User
**Request:**
```json
{
  "username": "john_doe",
  "userEmail": "john@example.com",
  "userPassword": "password123",
  "mobile": "9876543210",
  "role": "STUDENT",
  "enrollmentNumber": "ENG2023001",
  "department": "Engineering",
  "semester": 1
}
```

**Response:**
```json
{
  "userId": "APVD1001",
  "username": "john_doe",
  "userEmail": "john@example.com",
  "mobile": "9876543210",
  "role": "STUDENT",
  "createdAt": "2024-02-09T10:30:00",
  "updatedAt": "2024-02-09T10:30:00"
}
```

### Login
**Request:**
```json
{
  "userIdOrEmail": "APVD1001",
  "password": "password123"
}
```

**Response:**
```json
{
  "userId": "APVD1001",
  "username": "john_doe",
  "userEmail": "john@example.com",
  "mobile": "9876543210",
  "role": "STUDENT",
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

### Save Student Marks
**Request:**
```json
{
  "userId": "APVD1001",
  "semester": 1,
  "subject1Mark": 85,
  "subject2Mark": 90,
  "subject3Mark": 78,
  "subject4Mark": 88,
  "subject5Mark": 92,
  "subject6Mark": 80
}
```

**Response:**
```json
{
  "id": 1,
  "userId": "APVD1001",
  "username": "john_doe",
  "userEmail": "john@example.com",
  "semester": 1,
  "subject1Mark": 85,
  "subject2Mark": 90,
  "subject3Mark": 78,
  "subject4Mark": 88,
  "subject5Mark": 92,
  "subject6Mark": 80,
  "totalMarks": 513,
  "sgpa": 8.55,
  "createdAt": "2024-02-09T10:30:00",
  "updatedAt": "2024-02-09T10:30:00"
}
```

### Mark Attendance
**Request:**
```json
{
  "userId": "APVD1001",
  "attendanceDate": "2024-02-09",
  "status": "PRESENT",
  "remarks": "Present in class"
}
```

**Response:**
```json
{
  "id": 1,
  "userId": "APVD1001",
  "username": "john_doe",
  "userEmail": "john@example.com",
  "attendanceDate": "2024-02-09",
  "status": "PRESENT",
  "remarks": "Present in class",
  "createdAt": "2024-02-09T10:30:00",
  "updatedAt": "2024-02-09T10:30:00"
}
```

## Configuration

### application.properties
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/apvd_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Server Port
server.port=8080

# JWT Configuration
jwt.secret=MyVerySecureSecretKeyForJWTTokenGenerationAndValidation12345678
jwt.expiration=86400000

# CORS Configuration
spring.web.cors.allowed-origins=http://localhost:5173,http://localhost:3000
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*
spring.web.cors.allow-credentials=true
spring.web.cors.max-age=3600
```

## Running the Application

1. **Update application.properties** with your MySQL credentials

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

4. **Access the API**:
   ```
   http://localhost:8080/api/auth/login
   ```

## Authentication

All protected endpoints require JWT token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

## Security Features

- Password encryption using BCrypt
- JWT-based authentication
- Role-based access control (RBAC)
- CORS configuration for frontend integration
- Automatic timestamp tracking (createdAt, updatedAt)

## Error Handling

All API errors return appropriate HTTP status codes:
- `200`: Success
- `201`: Created
- `400`: Bad Request
- `401`: Unauthorized
- `403`: Forbidden
- `404`: Not Found
- `500`: Internal Server Error

## Future Enhancements

- Course management system
- Assignment tracking
- Email notifications
- Advanced dashboard analytics
- Bulk upload for marks and attendance
- Grade distribution analysis
- Performance trend analysis

## Support

For issues or questions, please contact the development team.
