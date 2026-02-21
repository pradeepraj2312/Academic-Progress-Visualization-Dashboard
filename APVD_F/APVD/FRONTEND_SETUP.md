# APVD Frontend - Complete Setup Guide

Welcome to the Academic Progress Visualization Dashboard (APVD) frontend! This is a comprehensive React-based application with full support for student, faculty, and admin dashboards.

## 🚀 Features Implemented

### ✅ Common Features
- **Responsive Navigation Bar** with home, sign up/login, logout, and dark/light theme toggle
- **Beautiful Home Page** with animated hero section and features showcase
- **Theme Support** - Dark and Light modes with persistent storage
- **Authentication** - JWT-based login/register with role-based access control

### ✅ Authentication Pages
- **Login Page** - Sign in with User ID or Email
- **Register Page** - Complete registration with role selection (Student/Faculty/Admin)
  - Department selection (Auto-set to "ADMIN" for admins)
  - Dynamic fields based on role selection

### ✅ Student Dashboard
- **Attendance Tab**
  - View attendance history with status badges
  - Attendance percentage calculation
  - Line chart for attendance trends (last 7 days)
  - Doughnut chart for attendance status distribution
  
- **Results Tab**
  - Semester-wise marks display
  - CGPA calculation and display
  - SGPA per semester
  - Bar chart showing subject-wise marks
  - Detailed result cards for each subject
  
- **Courses Tab**
  - View available courses by department
  - Select up to 7 courses (5 core + 2 elective)
  - Display selected courses separately
  - Course type badges (Core/Elective)

### ✅ Faculty Dashboard
- **Attendance Management**
  - View all students in department
  - Mark attendance (Present/Absent) for selected student
  - View student attendance history
  - Statistics card showing attendance metrics
  
- **Results Management**
  - Add/update marks for all students
  - Semester selector
  - Input fields for all 6 subjects
  - SGPA automatic calculation
  - View student marks history
  
- **Courses Management**
  - View student courses (ready for implementation)
  
- **Add Student**
  - Faculty can add students to their department
  - Auto-populate department field

### ✅ Admin Dashboard
- **All Faculty Features** plus:
  - Manage all students across all departments
  - **Add Faculty Tab** - Create new faculty accounts with department assignment

## 📦 Installation & Setup

### Prerequisites
- Node.js 16+ (recommended)
- npm or yarn package manager
- Backend API running on `http://localhost:8080`

### Step 1: Install Dependencies
```bash
cd APVD_F/APVD
npm install
```

### Step 2: Configure Backend URL
The API base URL is configured in `src/services/api.js`:
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

Update this if your backend runs on a different URL.

### Step 3: Run Development Server
```bash
npm run dev
```

The application will be available at `http://localhost:5173`

### Step 4: Build for Production
```bash
npm run build
npm run preview
```

## 🏗️ Project Structure

```
src/
├── components/
│   ├── Navigation.jsx          # Common navigation bar
│   └── ProtectedRoute.jsx      # Route protection component
├── pages/
│   ├── HomePage.jsx            # Home page
│   ├── LoginPage.jsx           # Login page
│   ├── RegisterPage.jsx        # Registration page
│   ├── StudentDashboard.jsx    # Student dashboard
│   ├── FacultyDashboard.jsx    # Faculty dashboard
│   └── AdminDashboard.jsx      # Admin dashboard
├── services/
│   ├── api.js                  # API calls and axios config
│   ├── AuthContext.jsx         # Authentication context
│   └── ThemeContext.jsx        # Theme management context
├── styles/
│   ├── Global.css              # Global styles and theme variables
│   ├── Navigation.css          # Navigation bar styles
│   ├── HomePage.css            # Home page styles with animations
│   ├── AuthPages.css           # Login/Register page styles
│   ├── Dashboard.css           # Dashboard tab and chart styles
│   └── DashboardExtended.css   # Additional dashboard layouts
├── App.jsx                     # Main app component with routing
├── App.css                     # App-level styles
├── main.jsx                    # React entry point
└── index.css                   # Base CSS
```

## 🔐 Authentication Flow

1. **Register** → Create account with role selection
2. **Login** → Enter User ID/Email and password
3. **JWT Token** → Stored in localStorage
4. **Protected Routes** → Role-based access control
5. **Auto-logout** → When token expires (401 response)

## 🎨 Theme System

The application uses CSS custom properties for theming:
- Light mode (default): Clean white interface
- Dark mode: Dark background with light text
- Theme toggle in navigation bar (☀️/🌙)
- Persistent theme preference (localStorage)

### Adding New Theme Variables
In `src/styles/Global.css`, update the CSS variables:
```css
:root {
  --primary-color: #2563eb;
  --page-bg: #f9fafb;
  /* More variables... */
}

[data-theme='dark'] {
  --page-bg: #0f0f0f;
  /* Dark mode overrides... */
}
```

## 📊 Charts & Visualizations

The application uses **Chart.js** with **react-chartjs-2** for data visualization:
- **Line Chart** - Attendance trends
- **Bar Chart** - Subject-wise marks
- **Doughnut Chart** - Attendance status distribution

## 🔗 API Integration

All API calls are handled through `src/services/api.js`. The service layer includes:

### Authentication APIs
- `authAPI.register()` - Register new user
- `authAPI.login()` - Login user
- `authAPI.verify()` - Verify token

### User Management APIs
- `userAPI.addStudent()` - Add student (Admin)
- `userAPI.addFaculty()` - Add faculty (Admin)
- `userAPI.getUsersByRole()` - Get users by role

### Marks APIs
- `marksAPI.saveMarks()` - Save/update marks
- `marksAPI.getStudentMarks()` - Get student marks
- `marksAPI.getSemesterMarks()` - Get marks by semester

### Attendance APIs
- `attendanceAPI.markAttendance()` - Mark attendance
- `attendanceAPI.getStudentAttendance()` - Get attendance records
- `attendanceAPI.getAttendancePercentage()` - Get percentage

### Courses APIs
- `courseAPI.getAllCourses()` - Get all courses
- `courseAPI.getCoursesByDepartment()` - Get dept courses
- `courseAPI.selectCourse()` - Select course
- `courseAPI.getStudentCourses()` - Get selected courses

### Grades APIs
- `gradesAPI.getStudentGrades()` - Get all grades
- `gradesAPI.calculateCGPA()` - Calculate CGPA

## 🧪 Testing the Application

### Test Accounts (Use registered accounts from backend)

1. **Student Account**
   - Login with student credentials
   - View attendance and results
   - Select courses

2. **Faculty Account**
   - Login with faculty credentials
   - Mark student attendance
   - Enter marks
   - Add new students

3. **Admin Account**
   - Access all features
   - Add students and faculty
   - Manage all department resources

## 🛠️ Troubleshooting

### Backend Connection Issues
- Ensure backend is running on `http://localhost:8080`
- Check CORS configuration in backend
- Verify token is being sent in requests

### Authentication Issues
- Clear localStorage: `localStorage.clear()`
- Check token expiration
- Re-login with credentials

### Chart Not Displaying
- Ensure data is available (no empty responses)
- Check Chart.js registration in component
- Verify canvas element is present

### Styling Issues
- Hard refresh browser (Ctrl+Shift+R)
- Clear browser cache
- Check theme attributes in DevTools

## 📱 Responsive Design

The application is fully responsive with breakpoints for:
- Desktop (1200px+)
- Tablet (768px - 1199px)
- Mobile (< 768px)

Mobile menu appears at 768px with hamburger icon.

## 🚀 Deployment

### Frontend Deployment (Vercel)
```bash
npm run build
# Deploy the dist/ folder
```

### Environment Variables (.env)
```
VITE_API_URL=https://your-backend-api.com/api
```

## 🤝 Contributing

To add new features:
1. Create components in `src/components/`
2. Create pages in `src/pages/`
3. Add styles in `src/styles/`
4. Update routes in `App.jsx`

## 📄 License

This project is part of the APVD system. All rights reserved.

## 🆘 Support

For issues or questions:
1. Check API documentation in backend
2. Verify network requests in DevTools
3. Check console for error messages
4. Ensure all dependencies are installed

---

**Happy coding! 🎉**
