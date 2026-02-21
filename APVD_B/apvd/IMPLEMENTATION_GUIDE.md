# Implementation Guide - APVD Backend

## Complete Workflow Examples

### Scenario 1: Admin Creates a Student

#### Step 1: Admin Registration
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "admin_user",
  "userEmail": "admin@institution.edu",
  "userPassword": "SecurePassword123!",
  "mobile": "9876543210",
  "role": "ADMIN",
  "department": "Administration"
}
```

#### Step 2: Admin Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "userIdOrEmail": "APVD1001",
  "password": "SecurePassword123!"
}
```

Response:
```json
{
  "userId": "APVD1001",
  "username": "admin_user",
  "userEmail": "admin@institution.edu",
  "mobile": "9876543210",
  "role": "ADMIN",
  "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJBUFZEMTAwMSIsImVtYWlsIjoiYWRtaW5AaW5zdGl0dXRpb24uZWR1Iiwicm9sZSI6IkFETUlOIiwiaWF0IjoxNzA3NDc1NDAwLCJleHAiOjE3MDc1NjE4MDB9.xxxx",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

#### Step 3: Admin Adds Student
```bash
POST /api/users/admin/add-student
Authorization: Bearer <token_from_step2>
Content-Type: application/json

{
  "username": "john_student",
  "userEmail": "john.student@student.edu",
  "userPassword": "StudentPass123!",
  "mobile": "9123456789",
  "role": "STUDENT",
  "enrollmentNumber": "CS2023001",
  "department": "Computer Science",
  "semester": 1
}
```

Response:
```json
{
  "userId": "APVD1002",
  "username": "john_student",
  "userEmail": "john.student@student.edu",
  "mobile": "9123456789",
  "role": "STUDENT",
  "createdAt": "2024-02-09T10:35:00",
  "updatedAt": "2024-02-09T10:35:00"
}
```

---

### Scenario 2: Faculty Updates Student Marks and Attendance

#### Step 1: Faculty Registration
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "prof_smith",
  "userEmail": "smith@institution.edu",
  "userPassword": "FacultyPass123!",
  "mobile": "9998887776",
  "role": "FACULTY",
  "department": "Computer Science",
  "qualification": "Ph.D.",
  "specialization": "Artificial Intelligence"
}
```

#### Step 2: Faculty Login and Get Token
```bash
POST /api/auth/login
Content-Type: application/json

{
  "userIdOrEmail": "APVD1003",
  "password": "FacultyPass123!"
}
```

#### Step 3: Faculty Updates Student Marks
```bash
POST /api/marks/save
Authorization: Bearer <faculty_token>
Content-Type: application/json

{
  "userId": "APVD1002",
  "semester": 1,
  "subject1Mark": 88,
  "subject2Mark": 92,
  "subject3Mark": 85,
  "subject4Mark": 90,
  "subject5Mark": 87,
  "subject6Mark": 89
}
```

Response:
```json
{
  "id": 1,
  "userId": "APVD1002",
  "username": "john_student",
  "userEmail": "john.student@student.edu",
  "semester": 1,
  "subject1Mark": 88,
  "subject2Mark": 92,
  "subject3Mark": 85,
  "subject4Mark": 90,
  "subject5Mark": 87,
  "subject6Mark": 89,
  "totalMarks": 531,
  "sgpa": 8.85,
  "createdAt": "2024-02-09T10:40:00",
  "updatedAt": "2024-02-09T10:40:00"
}
```

#### Step 4: Faculty Marks Attendance for Multiple Days
```bash
POST /api/attendance/mark
Authorization: Bearer <faculty_token>
Content-Type: application/json

{
  "userId": "APVD1002",
  "attendanceDate": "2024-02-09",
  "status": "PRESENT",
  "remarks": "Present in Database lecture"
}
```

Mark absent:
```bash
POST /api/attendance/mark
Authorization: Bearer <faculty_token>
Content-Type: application/json

{
  "userId": "APVD1002",
  "attendanceDate": "2024-02-08",
  "status": "ABSENT",
  "remarks": "Sick leave"
}
```

#### Step 5: Faculty Updates Individual Subject Mark
```bash
PUT /api/marks/APVD1002/semester/1/subject/1?marks=92
Authorization: Bearer <faculty_token>
Content-Type: application/json
```

---

### Scenario 3: Student Views Their Marks and Attendance

#### Step 1: Student Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "userIdOrEmail": "john.student@student.edu",
  "password": "StudentPass123!"
}
```

#### Step 2: View Their Marks
```bash
GET /api/marks/APVD1002
Authorization: Bearer <student_token>
```

Response:
```json
[
  {
    "id": 1,
    "userId": "APVD1002",
    "username": "john_student",
    "userEmail": "john.student@student.edu",
    "semester": 1,
    "subject1Mark": 92,
    "subject2Mark": 92,
    "subject3Mark": 85,
    "subject4Mark": 90,
    "subject5Mark": 87,
    "subject6Mark": 89,
    "totalMarks": 535,
    "sgpa": 8.92,
    "createdAt": "2024-02-09T10:40:00",
    "updatedAt": "2024-02-09T10:50:00"
  }
]
```

#### Step 3: View Their Attendance
```bash
GET /api/attendance/APVD1002
Authorization: Bearer <student_token>
```

Response:
```json
[
  {
    "id": 2,
    "userId": "APVD1002",
    "username": "john_student",
    "userEmail": "john.student@student.edu",
    "attendanceDate": "2024-02-09",
    "status": "PRESENT",
    "remarks": "Present in Database lecture",
    "createdAt": "2024-02-09T10:45:00",
    "updatedAt": "2024-02-09T10:45:00"
  },
  {
    "id": 1,
    "userId": "APVD1002",
    "username": "john_student",
    "userEmail": "john.student@student.edu",
    "attendanceDate": "2024-02-08",
    "status": "ABSENT",
    "remarks": "Sick leave",
    "createdAt": "2024-02-09T10:42:00",
    "updatedAt": "2024-02-09T10:42:00"
  }
]
```

#### Step 4: View Attendance Percentage
```bash
GET /api/attendance/APVD1002/percentage?startDate=2024-02-01&endDate=2024-02-28
Authorization: Bearer <student_token>
```

Response:
```json
{
  "userId": "APVD1002",
  "attendancePercentage": 95.45,
  "presentDays": 21,
  "startDate": "2024-02-01",
  "endDate": "2024-02-28"
}
```

---

### Scenario 4: Faculty Adds Student and Updates to Multiple Semesters

#### Step 1: Faculty Adds Student
```bash
POST /api/users/faculty/add-student
Authorization: Bearer <faculty_token>
Content-Type: application/json

{
  "username": "sarah_student",
  "userEmail": "sarah@student.edu",
  "userPassword": "SarahPass123!",
  "mobile": "9111111111",
  "enrollmentNumber": "CS2023002",
  "department": "Computer Science",
  "semester": 2
}
```

#### Step 2: Faculty Adds Semester 2 Marks
```bash
POST /api/marks/save
Authorization: Bearer <faculty_token>
Content-Type: application/json

{
  "userId": "APVD1004",
  "semester": 2,
  "subject1Mark": 90,
  "subject2Mark": 88,
  "subject3Mark": 92,
  "subject4Mark": 85,
  "subject5Mark": 91,
  "subject6Mark": 89
}
```

#### Step 3: Faculty Retrieves All Marks for Semester 2
```bash
GET /api/marks/semester/2
Authorization: Bearer <faculty_token>
```

---

## Database Query Examples

### View All Users
```sql
SELECT * FROM users ORDER BY created_at DESC;
```

### View All Students with Marks
```sql
SELECT 
    s.user_id,
    s.username,
    s.user_email,
    sm.semester,
    sm.total_marks,
    sm.sgpa
FROM students s
LEFT JOIN student_marks sm ON s.user_id = sm.user_id
ORDER BY s.user_id;
```

### View Attendance Summary
```sql
SELECT 
    user_id,
    username,
    COUNT(*) as total_days,
    SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as present_days,
    ROUND(SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as percentage
FROM student_attendance
WHERE user_id = 'APVD1002'
    AND attendance_date BETWEEN '2024-02-01' AND '2024-02-28'
GROUP BY user_id, username;
```

### View Faculty's Added Students
```sql
SELECT 
    u.user_id,
    u.username,
    u.user_email,
    s.enrollment_number,
    s.department,
    s.semester
FROM users u
JOIN students s ON u.user_id = s.user_id
WHERE u.role = 'STUDENT'
ORDER BY u.created_at DESC;
```

### Top 5 Students by SGPA
```sql
SELECT 
    sm.user_id,
    sm.username,
    sm.semester,
    MAX(sm.sgpa) as highest_sgpa
FROM student_marks sm
GROUP BY sm.user_id, sm.username, sm.semester
ORDER BY highest_sgpa DESC
LIMIT 5;
```

---

## Error Responses

### Invalid Credentials
```json
HTTP 401 Unauthorized
{
  "error": "Invalid credentials"
}
```

### User Already Exists
```json
HTTP 400 Bad Request
{
  "error": "Email already exists"
}
```

### Student Not Found
```json
HTTP 404 Not Found
{
  "error": "Student not found with userId: APVD9999"
}
```

### Insufficient Permissions
```json
HTTP 403 Forbidden
{
  "error": "Access Denied"
}
```

---

## Integration with Frontend

### React/Vite Example
```javascript
// Login
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    userIdOrEmail: 'APVD1001',
    password: 'password123'
  })
});

const data = await response.json();
localStorage.setItem('token', data.token);

// Fetch Student Marks
const marksResponse = await fetch('http://localhost:8080/api/marks/APVD1001', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  }
});

const marks = await marksResponse.json();
console.log(marks);
```

---

## Testing Checklist

- [ ] User registration with all roles
- [ ] User login with userID and email
- [ ] Admin adds student/faculty
- [ ] Faculty adds student
- [ ] Faculty updates marks
- [ ] Faculty marks attendance
- [ ] Faculty updates individual subject mark
- [ ] Student views marks
- [ ] Student views attendance
- [ ] Attendance percentage calculation
- [ ] SGPA calculation
- [ ] User deletion
- [ ] Role-based access control

---

## Performance Optimization Tips

1. Add database indexes on frequently queried fields:
```sql
CREATE INDEX idx_user_id ON student_marks(user_id);
CREATE INDEX idx_attendance_date ON student_attendance(attendance_date);
CREATE INDEX idx_semester ON student_marks(semester);
```

2. Use pagination for list endpoints
3. Cache frequently accessed data
4. Use eager loading for related entities
