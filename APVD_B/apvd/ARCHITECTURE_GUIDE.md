# APVD Backend - Architecture & Field Mapping

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (React/Vite)                    │
│                    http://localhost:5173                        │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP/REST
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                 Spring Boot Backend API                         │
│                  http://localhost:8080                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           Spring Security + JWT Authentication          │  │
│  │  (JwtTokenProvider, JwtAuthenticationFilter)            │  │
│  └──────────────────────────────────────────────────────────┘  │
│           ▲                                                     │
│           │                                                     │
│  ┌────────┴─────────────────────────────────────────────────┐  │
│  │                REST Controllers                         │  │
│  │  ├─ AuthController (Login/Register)                     │  │
│  │  ├─ UserController (User Management)                    │  │
│  │  ├─ StudentMarksController (Marks CRUD)                 │  │
│  │  └─ StudentAttendanceController (Attendance)            │  │
│  └────────────────────────┬─────────────────────────────────┘  │
│                           │                                     │
│  ┌────────────────────────▼─────────────────────────────────┐  │
│  │              Service Layer (Business Logic)             │  │
│  │  ├─ AuthService (Authentication)                        │  │
│  │  ├─ UserService (User Operations)                       │  │
│  │  ├─ StudentMarksService (Marks Logic)                   │  │
│  │  ├─ StudentAttendanceService (Attendance Logic)         │  │
│  │  └─ StudentService (Student Operations)                 │  │
│  └────────────────────────┬─────────────────────────────────┘  │
│                           │                                     │
│  ┌────────────────────────▼─────────────────────────────────┐  │
│  │          Repository Layer (Data Access)                 │  │
│  │  ├─ UserRepository (JpaRepository)                       │  │
│  │  ├─ StudentRepository (JpaRepository)                    │  │
│  │  ├─ FacultyRepository (JpaRepository)                    │  │
│  │  ├─ AdminRepository (JpaRepository)                      │  │
│  │  ├─ StudentMarksRepository (JpaRepository)               │  │
│  │  └─ StudentAttendanceRepository (JpaRepository)          │  │
│  └────────────────────────┬─────────────────────────────────┘  │
│                           │                                     │
└───────────────────────────┼─────────────────────────────────────┘
                            │ JDBC/Hibernate
                            ▼
        ┌───────────────────────────────────┐
        │       MySQL Database              │
        │  apvd_db                         │
        │  ├─ users                        │
        │  ├─ students                     │
        │  ├─ faculty                      │
        │  ├─ admin                        │
        │  ├─ student_marks               │
        │  └─ student_attendance          │
        └───────────────────────────────────┘
```

## 📊 Entity Relationship Diagram

```
┌─────────────────────────┐
│       USERS             │
├─────────────────────────┤
│ internalId (PK)        │
│ userId (UK) - APVD★★★★ │
│ username               │
│ userEmail (UK)         │
│ userPassword           │
│ mobile                 │
│ role (ENUM)            │
│ createdAt              │
│ updatedAt              │
└──────┬──────────────────┘
       │
       │ 1:1 relationship (when role is STUDENT)
       ▼
┌──────────────────────────┐
│     STUDENTS             │
├──────────────────────────┤
│ id (PK)                 │
│ userId (FK) ────────────┼─────► users.userId
│ username                │
│ userEmail               │
│ mobile                  │
│ enrollmentNumber        │
│ department              │
│ semester                │
│ createdAt               │
│ updatedAt               │
└──────┬───────────────────┘
       │
       │ 1:M relationship
       └─────────────┬────────────────────────┐
                     ▼                        ▼
        ┌────────────────────────┐ ┌──────────────────────┐
        │   STUDENT_MARKS        │ │  STUDENT_ATTENDANCE  │
        ├────────────────────────┤ ├──────────────────────┤
        │ id (PK)               │ │ id (PK)             │
        │ userId (FK)           │ │ userId (FK)         │
        │ username              │ │ username            │
        │ userEmail             │ │ userEmail           │
        │ semester              │ │ attendanceDate      │
        │ subject1Mark          │ │ status (ENUM)       │
        │ subject2Mark          │ │ remarks             │
        │ subject3Mark          │ │ createdAt           │
        │ subject4Mark          │ │ updatedAt           │
        │ subject5Mark          │ └──────────────────────┘
        │ subject6Mark          │
        │ totalMarks (AUTO)     │
        │ sgpa (AUTO)           │
        │ createdAt             │
        │ updatedAt             │
        └────────────────────────┘

       │ 1:1 relationship (when role is FACULTY)
       ▼
┌──────────────────────────┐
│      FACULTY             │
├──────────────────────────┤
│ id (PK)                 │
│ userId (FK)             │
│ username                │
│ userEmail               │
│ mobile                  │
│ department              │
│ qualification           │
│ specialization          │
│ createdAt               │
│ updatedAt               │
└──────────────────────────┘

       │ 1:1 relationship (when role is ADMIN)
       ▼
┌──────────────────────────┐
│      ADMIN               │
├──────────────────────────┤
│ id (PK)                 │
│ userId (FK)             │
│ username                │
│ userEmail               │
│ mobile                  │
│ department              │
│ createdAt               │
│ updatedAt               │
└──────────────────────────┘
```

## 🔄 API Flow Diagram

### Registration Flow
```
Frontend
   │
   └─► POST /api/auth/register
       {username, email, password, role, ...}
       │
       ▼
   AuthController.register()
       │
       ▼
   AuthService.register()
       │
       ├─► UserRepository.save()  → Create user with role
       │   (Generate userId: APVDN+increment)
       │
       ├─► if role == STUDENT
       │   └─► StudentRepository.save()
       │
       ├─► if role == FACULTY
       │   └─► FacultyRepository.save()
       │
       └─► if role == ADMIN
           └─► AdminRepository.save()
       │
       ▼
   Return UserResponseDTO
       │
       ▼
Frontend (Store userId)
```

### Login Flow
```
Frontend
   │
   └─► POST /api/auth/login
       {userIdOrEmail, password}
       │
       ▼
   AuthController.login()
       │
       ▼
   AuthService.login()
       │
       ├─► UserRepository.findByUserIdOrUserEmail()
       │
       ├─► Verify password with BCrypt
       │   ├─ Match: Proceed
       │   └─ No Match: Return error
       │
       ├─► JwtTokenProvider.generateToken()
       │   └─ Create JWT with:
       │      - userId (subject)
       │      - email (claim)
       │      - role (claim)
       │      - exp (24 hours from now)
       │
       ▼
   Return LoginResponseDTO with token
       │
       ▼
Frontend (Store token in localStorage)
```

### Mark Update Flow
```
Frontend (Faculty)
   │
   └─► POST /api/marks/save
       {userId, semester, subject1Mark, ..., subject6Mark}
       │
       ▼
   StudentMarksController.saveOrUpdateMarks()
       │
       ├─► Check JWT Authorization
       │   └─ Must have FACULTY or ADMIN role
       │
       ▼
   StudentMarksService.saveOrUpdateMarks()
       │
       ├─► StudentRepository.findByUserId()
       │   └─ Verify student exists
       │
       ├─► StudentMarksRepository.findByUserIdAndSemester()
       │   ├─ Exists: Update record
       │   └─ Not exists: Create new record
       │
       ├─► Calculate totalMarks (sum of all 6 subjects)
       │
       ├─► Calculate SGPA
       │   └─ Formula: (totalMarks / 600) * 10
       │
       ├─► Save StudentMarks entity
       │   └─ @PreUpdate triggers calculation
       │
       ▼
   Return StudentMarksDTO with calculated fields
       │
       ▼
Frontend (Display on dashboard)
```

### Attendance Flow
```
Frontend (Faculty)
   │
   └─► POST /api/attendance/mark
       {userId, attendanceDate, status}
       │
       ▼
   StudentAttendanceController.markAttendance()
       │
       ├─► Check JWT Authorization
       │   └─ Must have FACULTY or ADMIN role
       │
       ▼
   StudentAttendanceService.markAttendance()
       │
       ├─► StudentRepository.findByUserId()
       │   └─ Verify student exists
       │
       ├─► Create StudentAttendance entity
       │   ├─ userId, username, userEmail
       │   ├─ attendanceDate
       │   └─ status (PRESENT/ABSENT)
       │
       ├─► StudentAttendanceRepository.save()
       │
       ▼
   Return StudentAttendanceDTO
       │
       ▼
Frontend (Update attendance calendar)
```

### Attendance Percentage Flow
```
Frontend (Any User)
   │
   └─► GET /api/attendance/{userId}/percentage?startDate=X&endDate=Y
       │
       ▼
   StudentAttendanceController.getAttendancePercentage()
       │
       ├─► Check JWT Authorization
       │
       ▼
   StudentAttendanceService.getAttendancePercentage()
       │
       ├─► StudentAttendanceRepository.findByUserIdAndAttendanceDateBetween()
       │   └─ Get all records in date range
       │
       ├─► Count PRESENT records
       │
       ├─► Calculate total days in range
       │
       ├─► Calculate percentage
       │   └─ Formula: (presentDays / totalDays) * 100
       │
       ▼
   Return attendance statistics
       │
       ▼
Frontend (Display percentage and chart)
```

## 📋 Field Mapping Reference

### Registration to User & Student

```
Registration Request
├─ username               ──► Users.username / Student.username
├─ userEmail              ──► Users.userEmail / Student.userEmail
├─ userPassword           ──► Users.userPassword (encrypted)
├─ mobile                 ──► Users.mobile / Student.mobile
├─ role                   ──► Users.role
├─ enrollmentNumber       ──► Student.enrollmentNumber (if student)
├─ department             ──► Student.department (if student)
└─ semester               ──► Student.semester (if student)

Generated
├─ userId (APVD★★★★)     ──► Users.userId
├─ createdAt              ──► Users.createdAt + Student.createdAt
└─ updatedAt              ──► Users.updatedAt + Student.updatedAt
```

### Marks Calculation Flow

```
Input Marks
├─ subject1Mark (0-100)
├─ subject2Mark (0-100)
├─ subject3Mark (0-100)
├─ subject4Mark (0-100)
├─ subject5Mark (0-100)
└─ subject6Mark (0-100)

Auto-Calculated
├─ totalMarks = sum(all 6 subjects)
│  └─ Example: 88+92+85+90+87+89 = 531
│
└─ sgpa = (totalMarks / 600) * 10
   └─ Example: (531 / 600) * 10 = 8.85
```

### Attendance Summary

```
Daily Record
├─ attendanceDate        ──► Date (YYYY-MM-DD)
├─ status               ──► PRESENT / ABSENT
└─ remarks              ──► Optional notes

Period Analysis (Date Range)
├─ Total Days = Days between startDate and endDate
├─ Present Count = SUM(status = PRESENT)
├─ Percentage = (Present Count / Total Days) * 100
└─ Example:
   startDate: 2024-02-01, endDate: 2024-02-28
   Total Days: 28
   Present Days: 26
   Percentage: (26/28) * 100 = 92.86%
```

## 🔐 JWT Token Structure

```
Header
├─ alg: HS512 (HMAC SHA-512)
└─ typ: JWT

Payload (Claims)
├─ sub (subject): userId (APVD1001)
├─ email: userEmail
├─ role: UserRole (STUDENT, FACULTY, ADMIN)
├─ iat: Issued At (timestamp)
└─ exp: Expiration (current_time + 24 hours)

Signature
└─ HMAC-SHA512(base64(header) + "." + base64(payload), secret)
```

## 🔄 Request/Response Lifecycle

```
1. Client Request
   ├─ Method: GET/POST/PUT/DELETE
   ├─ URL: /api/endpoint
   ├─ Headers: { Authorization: "Bearer TOKEN", ... }
   └─ Body: JSON (if POST/PUT)

2. Spring Security Gateway
   ├─ JwtAuthenticationFilter intercepts
   ├─ Extracts token from Authorization header
   └─ Validates token with JwtTokenProvider

3. Controller Handler
   ├─ Routes to appropriate method
   ├─ Validates @Valid annotations
   └─ Calls service layer

4. Service Layer
   ├─ Implements business logic
   ├─ Calls repositories
   ├─ Calculates values (SGPA, percentages)
   └─ Handles transactions

5. Repository Layer
   ├─ Executes JPA queries
   ├─ Manages database operations
   └─ Returns entities or persists changes

6. Return Response
   ├─ Map entity to DTO
   ├─ Set HTTP status code
   └─ Return JSON response to client
```

## 📊 Data Validation

### Registration Validation
```
@NotBlank - username, email, password, mobile, role
@Email    - userEmail must be valid email format
Length    - password, mobile length constraints
Uniqueness - userEmail, userId must be unique in DB
```

### Marks Validation
```
@NotNull      - all marks fields required
@Min(0)       - marks cannot be negative
@Max(100)     - marks cannot exceed 100
Range Check   - semester 1-8
```

### Attendance Validation
```
@NotNull      - userId, date, status required
LocalDate     - must be valid date format
Status Enum   - only PRESENT or ABSENT
```

---

This architecture ensures:
- ✅ Separation of concerns (Controller → Service → Repository)
- ✅ Type safety with DTOs
- ✅ Automatic calculations at persistence
- ✅ Secure JWT-based authentication
- ✅ Scalable database design
- ✅ Clear data flow and transformations
