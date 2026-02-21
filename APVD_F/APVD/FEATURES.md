# APVD Frontend - Feature Guide

## 🎯 Complete Feature Walkthrough

### 1. Home Page
The home page serves as the landing page with:

#### Hero Section
- Animated title: "Academic Progress Visualization Dashboard"
- Call-to-action buttons for Sign In/Sign Up or Dashboard navigation
- Floating animated cards showing key features

#### Features Showcase
Six feature cards highlighting:
- 📊 Real-time Analytics
- 📈 Progress Tracking
- 👥 Multi-Role Support
- 📱 Responsive Design
- 🎯 Goal Setting
- 🔒 Secure & Private

**Navigation:**
- Unauthenticated users: See "Sign In" and "Sign Up" buttons
- Authenticated users: See "Go to Dashboard" button based on their role

---

### 2. Registration (Sign Up)

#### Form Fields
1. **Full Name** - User's complete name
2. **Email** - Unique email address
3. **Mobile Number** - Contact number
4. **Role Selection** - Choose between:
   - Student
   - Faculty
   - Admin

#### Role-Specific Fields
- **For Students:**
  - Department (e.g., "CSE", "ECE", "EEE")
  - Semester (Dropdown: 1-8)
  - Enrollment Number (Optional)

- **For Faculty/Admin:**
  - Department (Manual entry)
  - Auto-set to "ADMIN" if Admin role is selected

#### Password Requirements
- Minimum 6 characters
- Must confirm password (matching)
- Password field is masked for security

#### Response
- System generates unique User ID: `APVD1001`, `APVD1002`, etc.
- Automatically creates user account
- User is redirected to login page

---

### 3. Login (Sign In)

#### Input Options
1. **User ID** - Format: `APVD1001` (generated during registration)
2. **Email** - Email address used during registration
3. **Password** - Account password

#### After Login
- JWT token is stored in localStorage
- User is redirected based on role:
  - Student → Student Dashboard
  - Faculty → Faculty Dashboard
  - Admin → Admin Dashboard
- User info displayed in navigation bar

---

### 4. Student Dashboard

#### Tab 1: Attendance 📊

**Statistics Cards:**
- Attendance Percentage (%)
- Total Days Attended
- Absent Days
- Shows real-time data

**Visual Elements:**
- **Line Chart:** Shows attendance trend for last 7 days
- **Doughnut Chart:** Pie chart showing Present vs Absent ratio

**Attendance History Table:**
- Date column (formatted locale-specific)
- Status column with color-coded badges
  - 🟢 Green: PRESENT
  - 🔴 Red: ABSENT
- Remarks column (if any)

#### Tab 2: Results 📈

**Semester Selector:**
- Dropdown to view marks for Semester 1-8
- Data updates when semester changes

**Statistics Cards:**
- **CGPA** - Cumulative Grade Point Average (all semesters)
- **Total Marks** - Sum of all subject marks (current semester)
- **SGPA** - Semester Grade Point Average (current semester)

**Visual Elements:**
- **Bar Chart:** Subject-wise marks comparison (6 bars for 6 subjects)
- **Color-coded bars:** Each subject has different color

**Detailed Results Grid:**
- 6 result cards (one for each subject)
- Shows marks out of 100
- Gradient background for visual appeal

#### Tab 3: Courses 📚

**Course Selection Info:**
- Selected courses count: X/7
- Maximum limit: 5 core + 2 elective = 7 total

**Selected Courses Section:**
- List of already selected courses
- Course type badge (Core/Elective)
- Shows empty state if no courses selected

**Available Courses Section:**
- Grid of all available courses
- Each course card shows:
  - Course name
  - Course code
  - Faculty name
  - Course type badge
  - Select button (disabled if already selected)
- Color-coded badges:
  - 🔵 Blue: Core course
  - 🟡 Yellow: Elective course

**Course Selection Logic:**
- Can select up to 7 courses total
- Mix of core and elective courses
- Once selected, button shows "Selected" (disabled state)

---

### 5. Faculty Dashboard

#### Tab 1: Attendance 📊

**Left Panel: Students List**
- Scrollable list of all students in faculty's department
- Click to select a student
- Shows student name, email, and department

**Right Panel: Student Details & Actions**

**Statistics Cards:**
- Total Classes (total attendance records)
- Present (count of present days)
- Absent (count of absent days)
- Attendance Percentage (%)

**Action Buttons:**
- **Mark Present** - Mark selected student as present today
- **Mark Absent** - Mark selected student as absent today

**Attendance History:**
- Table showing all attendance records
- Date and Status columns
- Color-coded status badges

#### Tab 2: Results 📈

**Left Panel: Students List**
- Same as attendance tab

**Right Panel: Marks Management**

**Semester Selector:**
- Dropdown to choose semester (1-8)

**Marks Entry Grid:**
- 6 input fields (Subject 1-6)
- Number input type with min=0, max=100
- Save Marks button

**Marks Display Section:**
- Shows student's marks history
- Semester-wise breakdown
- SGPA for each semester
- 6 mark items per semester showing subject marks

#### Tab 3: Courses 📚
- Placeholder for course management
- Select student to manage their courses

#### Tab 4: Add Student ➕

**Form Fields:**
1. Full Name (required)
2. Email (required)
3. Password (required)
4. Mobile Number (required)
5. Enrollment Number (optional)
6. Semester (dropdown 1-8)
7. Department (auto-filled with faculty's department)

**Submission:**
- Creates new student account
- Auto-assigns to faculty's department
- Shows success message
- Refreshes student list

---

### 6. Admin Dashboard

#### All Features From Faculty Dashboard Plus:

#### Tab 4: Add Student ➕

**Form Fields:**
1. Full Name (required)
2. Email (required)
3. Password (required)
4. Mobile Number (required)
5. Enrollment Number (optional)
6. Semester (dropdown 1-8)
7. **Department (required)** - Admin can enter any department

**Key Difference from Faculty:**
- Can add students to any department
- Department field is editable (not auto-filled)

#### Tab 5: Add Faculty 👨‍🏫

**Form Fields:**
1. Full Name (required)
2. Email (required)
3. Password (required)
4. Mobile Number (required)
5. Department (required)

**Submission:**
- Creates faculty account with specified department
- Faculty can then manage students in that department
- Admin gets success notification

---

## 🎨 Theme System

### Light Mode (Default)
- White background with blue accents
- Dark text on light background
- Clean, professional appearance

### Dark Mode
- Dark background (#0f0f0f) with light text
- Blue accents adjusted for dark background
- Reduces eye strain in low-light conditions

### Theme Toggle
- Moon icon (🌙) in navigation - Click to enable dark mode
- Sun icon (☀️) in navigation - Click to enable light mode
- Theme preference persists across sessions
- Smooth transition between themes

---

## 🔒 Security Features

1. **JWT Authentication**
   - Token stored in localStorage
   - Sent with every API request
   - Auto-logout on token expiration (401 response)

2. **Protected Routes**
   - Role-based access control (RBAC)
   - Auth context checks user authentication
   - Redirects to login if not authenticated
   - Redirects non-role users to home

3. **Password Security**
   - Password field masked during input
   - Min 6 characters validation
   - Confirm password matching
   - Backend uses BCrypt for hashing

---

## 🚀 User Flows

### New User Registration Flow
```
1. Click "Sign Up" on home page
2. Fill registration form
3. Select role and department (role-specific)
4. Submit form
5. System generates User ID
6. Redirected to login page
7. Login with User ID/Email and password
8. Auto-redirected to appropriate dashboard
```

### Existing User Login Flow
```
1. Click "Sign In" on home page
2. Enter User ID or Email
3. Enter password
4. Click "Sign In"
5. Auto-redirected to role-specific dashboard
```

### Student View Attendance Flow
```
1. Login as student
2. Go to Student Dashboard
3. Click "Attendance" tab
4. View statistics cards
5. Analyze line chart (7-day trend)
6. Review doughnut chart (Present vs Absent)
7. Scroll down for detailed history table
```

### Faculty Mark Attendance Flow
```
1. Login as faculty
2. Go to Faculty Dashboard
3. Click "Attendance" tab
4. Click student from left panel
5. View student statistics
6. Click "Mark Present" or "Mark Absent"
7. View updated attendance history
```

### Faculty Enter Marks Flow
```
1. Login as faculty
2. Go to Faculty Dashboard
3. Click "Results" tab
4. Click student from left panel
5. Select semester using dropdown
6. Enter marks for all 6 subjects
7. Click "Save Marks"
8. View marks history below
```

---

## 📊 Data Visualization

### Charts Used
1. **Line Chart** - Shows attendance trend over time
2. **Bar Chart** - Compares subject-wise marks
3. **Doughnut Chart** - Shows ratio of Present to Absent

### When Charts Display
- Charts appear when data is available
- "No data available" message shown if no records
- Charts are responsive and scale with container
- Legend shows data categories

---

## 🔄 Real-time Updates

The dashboard doesn't auto-refresh, but:
- Clicking different students refreshes their data
- Changing semester refreshes marks data
- Switching tabs may require re-fetch
- Manual refresh (F5) gets latest data

---

## ⚠️ Error Handling

### Common Errors & Solutions

1. **Login Failed**
   - Double-check User ID/Email spelling
   - Verify password is correct
   - Ensure account exists

2. **Cannot Mark Attendance**
   - Select a student first
   - Check backend connection
   - Verify user has permission

3. **Marks Not Saving**
   - Enter valid numbers (0-100)
   - Select a student first
   - Check all subject fields
   - Verify backend is running

4. **Courses Not Loading**
   - Ensure student department is specified
   - Check if courses exist for department
   - Refresh the page

5. **Dashboard Not Loading**
   - Verify authentication token exists
   - Check user role matches route
   - Ensure backend API is running
   - Check network tab for API errors

---

## 💡 Tips & Tricks

1. **Quick Navigation**
   - Click logo to go home anytime
   - Use back button in browser to go back
   - Click home in navbar to reset

2. **Theme Switching**
   - Switch between themes without logging out
   - Theme applies instantly
   - Works on all pages

3. **Efficient Marks Entry**
   - Tab key moves between fields
   - Enter marks in order (Subject 1 to 6)
   - All fields support keyboard input

4. **Attendance Tracking**
   - Check attendance percentage regularly
   - Use trend chart to identify patterns
   - Maintain good attendance for better grades

5. **Course Selection**
   - Carefully select core courses for your specialization
   - Choose electives from other departments for diversity
   - Cannot change courses after selection (by design)

---

## 🎓 Educational Value

The APVD system helps in:
- **Progress Tracking** - Monitor academic performance
- **Data Visualization** - Understand trends through charts
- **Goal Setting** - Use CGPA as a goal metric
- **Decision Making** - Choose courses wisely
- **Accountability** - Track attendance regularly

---

**Last Updated:** February 10, 2026
**Frontend Version:** 1.0.0
