# APVD Project Architecture Diagram

This diagram is generated from the current source code in both projects:
- `APVD_F/APVD` (React + Vite frontend)
- `APVD_B/apvd` (Spring Boot backend)

## 1) System Component Architecture

```mermaid
flowchart LR
    U[User Browser]

    subgraph FE[Frontend - React + Vite (APVD_F/APVD)]
      APP[App.jsx Router]
      NAV[Navigation + ProtectedRoute]
      AUTHCTX[AuthContext]
      THEME[ThemeContext]
      PAGES[Pages: Home/Login/Register\nStudent/Faculty/Admin Dashboards]
      APIJS[services/api.js\nAxios + JWT interceptors]
    end

    subgraph BE[Backend - Spring Boot (APVD_B/apvd)]
      SEC[Security Layer\nSecurityConfig + JwtAuthenticationFilter\nJwtTokenProvider]
      CTRL[Controllers\nAuth/User/Course/Department\nStudentCourse/Attendance/Marks/Grade]
      SVC[Services\nAuth/User/Course/Department\nStudentCourse/Attendance/Marks/Grade]
      REPO[Repositories (Spring Data JPA)]
      EXH[GlobalExceptionHandler]
    end

    DB[(MySQL\napvd_db)]

    U --> FE
    APP --> NAV
    APP --> PAGES
    NAV --> AUTHCTX
    APP --> THEME
    PAGES --> APIJS
    AUTHCTX --> APIJS

    APIJS -->|HTTP REST + Bearer JWT| SEC
    SEC --> CTRL
    CTRL --> SVC
    CTRL --> EXH
    SVC --> REPO
    REPO --> DB
```

## 2) Authentication & Authorization Flow

```mermaid
sequenceDiagram
    participant C as Client (React)
    participant A as AuthController
    participant AS as AuthService
    participant J as JwtTokenProvider
    participant F as JwtAuthenticationFilter
    participant X as Protected APIs

    C->>A: POST /api/auth/login
    A->>AS: validate credentials
    AS->>J: generate JWT (userId, role, expiry)
    A-->>C: token + user payload

    C->>C: Store token in localStorage
    C->>X: API request with Authorization: Bearer <token>
    F->>J: validate token
    J-->>F: userId + role
    F->>X: set SecurityContext authentication
    X-->>C: protected resource response
```

## 3) Core Domain Model (Logical)

```mermaid
erDiagram
    USERS ||--o| STUDENT : role-based profile
    USERS ||--o| FACULTY : role-based profile
    USERS ||--o| ADMIN : role-based profile

    STUDENT ||--o{ STUDENT_ATTENDANCE : has
    STUDENT ||--o{ STUDENT_MARKS : has
    STUDENT ||--o{ STUDENT_COURSE : selects
    STUDENT ||--o{ GRADE : receives

    FACULTY ||--o{ COURSE : owns_or_handles
    DEPARTMENT ||--o{ COURSE : contains

    COURSE ||--o{ STUDENT_COURSE : selected_in
    COURSE ||--o{ GRADE : graded_for

    USERS {
      string userId PK
      string userEmail
      string userPassword
      string role
    }
    STUDENT {
      string userId FK
      string department
      int yearOfStudying
    }
    FACULTY {
      string userId FK
      string department
    }
    ADMIN {
      string userId FK
      string department
    }
    DEPARTMENT {
      long departmentId PK
      string departmentName
      string departmentCode
    }
    COURSE {
      long courseId PK
      string courseCode
      string department
      int semester
      string courseStatus
      string facultyUserId
    }
    STUDENT_COURSE {
      long id PK
      string studentUserId
      long courseId
      int semester
      string courseStatus
    }
    STUDENT_ATTENDANCE {
      long id PK
      string userId
      date attendanceDate
      string session
      string status
    }
    STUDENT_MARKS {
      long id PK
      string userId
      int semester
      double totalMarks
      double sgpa
    }
    GRADE {
      long gradeId PK
      string studentUserId
      long courseId
      int semester
      string letterGrade
      double gradePoint
    }
```

## 4) Role-based UI to API Mapping

```mermaid
flowchart TD
    S[Student Dashboard] --> SA[/attendance APIs/]
    S --> SC[/student-courses APIs/]
    S --> SM[/marks + grades APIs/]

    F[Faculty Dashboard] --> FA[/attendance mark/update APIs/]
    F --> FM[/marks save/update APIs/]
    F --> FU[/users/faculty/add-student/]

    AD[Admin Dashboard] --> AU[/users admin add/update/delete APIs/]
    AD --> AC[/courses add/update/delete + reassign-faculty/]
    AD --> ADEP[/departments add/update/delete APIs/]
    AD --> AAT[/attendance + results management APIs/]
```

## Notes
- Frontend routing and role guards are in `src/App.jsx` and `src/components/ProtectedRoute.jsx`.
- JWT is attached by Axios interceptor in `src/services/api.js`.
- Backend security is enforced via `SecurityConfig` + `JwtAuthenticationFilter`.
- Runtime business errors are normalized by `GlobalExceptionHandler`.
