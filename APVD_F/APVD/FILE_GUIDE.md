# APVD Frontend - File Guide

## Complete File Listing

### 📄 Documentation Files
Located: `APVD_F/APVD/`

1. **README.md** - Main project documentation (comprehensive guide)
   - Features overview
   - Installation instructions
   - Project structure
   - Technology stack
   - Deployment options

2. **QUICKSTART.md** - 5-minute quick start guide
   - Prerequisites
   - Installation
   - Running the app
   - Quick testing
   - Troubleshooting

3. **FRONTEND_SETUP.md** - Complete setup guide
   - Feature checklist
   - Installation steps
   - Project structure details
   - Configuration options
   - API documentation
   - Deployment instructions

4. **FEATURES.md** - Detailed feature documentation
   - Feature walkthrough by page
   - User flows
   - Data visualization details
   - Security features
   - Tips & tricks

5. **IMPLEMENTATION_SUMMARY.md** - Implementation checklist
   - Complete feature list
   - Requirements verification
   - File statistics
   - Quality assurance

---

### 🔧 Source Code Files

#### Pages (6 files)
Location: `src/pages/`

1. **HomePage.jsx** (520 lines)
   - Hero section with animations
   - Feature showcase (6 cards)
   - Call-to-action buttons
   - Responsive layout

2. **LoginPage.jsx** (90 lines)
   - User ID/Email input
   - Password input
   - Error handling
   - Sign Up link

3. **RegisterPage.jsx** (180 lines)
   - Multi-field registration form
   - Role-based field visibility
   - Validation logic
   - Department handling

4. **StudentDashboard.jsx** (480 lines)
   - Attendance tab with charts
   - Results tab with marks
   - Courses tab with selection
   - Data visualization

5. **FacultyDashboard.jsx** (530 lines)
   - Attendance management
   - Marks entry and management
   - Student course management
   - Add student functionality

6. **AdminDashboard.jsx** (640 lines)
   - All faculty features
   - Add faculty functionality
   - Cross-department management
   - Organization-wide view

#### Components (2 files)
Location: `src/components/`

1. **Navigation.jsx** (90 lines)
   - Header with logo
   - Navigation menu
   - Theme toggle
   - Mobile hamburger menu
   - User info display

2. **ProtectedRoute.jsx** (35 lines)
   - Role-based access control
   - Loading state
   - Route protection

#### Services (3 files)
Location: `src/services/`

1. **api.js** (85 lines)
   - Axios configuration
   - JWT token injection
   - API endpoints
   - Error handling
   - All API categories:
     - Authentication
     - User Management
     - Marks Management
     - Attendance
     - Courses
     - Grades

2. **AuthContext.jsx** (75 lines)
   - Authentication state
   - Login/Register/Logout functions
   - useAuth hook
   - User session management

3. **ThemeContext.jsx** (40 lines)
   - Theme state (dark/light)
   - Toggle functionality
   - useTheme hook
   - Persistent storage

#### Styles (6 files)
Location: `src/styles/`

1. **Global.css** (90 lines)
   - CSS custom properties
   - Light mode variables
   - Dark mode variables
   - Base styles
   - Utility classes

2. **Navigation.css** (130 lines)
   - Navbar styling
   - Mobile menu
   - Theme transitions
   - Responsive design

3. **HomePage.css** (330 lines)
   - Hero section animations
   - Floating card animations
   - Feature cards styling
   - 8+ keyframe animations

4. **AuthPages.css** (250 lines)
   - Login/Register form styles
   - Input styling
   - Error messages
   - Responsive layout

5. **Dashboard.css** (490 lines)
   - Tab system
   - Statistics cards
   - Charts containers
   - Table styling
   - Result cards
   - Filter sections

6. **DashboardExtended.css** (350 lines)
   - Two-column layout
   - Students list
   - Mark forms
   - Mobile responsiveness

#### Main Files (4 files)
Location: `src/`

1. **App.jsx** (75 lines)
   - Main application component
   - Router configuration
   - Route definitions
   - Provider setup

2. **App.css** (10 lines)
   - App-level styles
   - CSS imports

3. **main.jsx** (11 lines)
   - React entry point
   - DOM mounting

4. **index.css** (35 lines)
   - Base CSS reset
   - Font definitions
   - HTML/body styles

---

### 📦 Configuration Files

**package.json**
- React 19.2.0
- React DOM 19.2.0
- React Router 7.2.0
- Axios 1.7.9
- Chart.js 4.4.8
- react-chartjs-2 5.3.0
- Vite 7.2.4
- ESLint configuration

**vite.config.js**
- Vite configuration with React plugin
- Development server settings

**index.html**
- HTML template
- Root div for React mounting
- Meta tags

---

### 📊 Statistics

**Total Files Created:** 23
- Documentation: 5
- Pages: 6
- Components: 2
- Services: 3
- Styles: 6
- Main: 4 (including config)

**Total Code Lines:** ~5,400
- JavaScript/JSX: ~2,500 lines
- CSS: ~1,640 lines
- Documentation: ~1,300 lines

**Documentation:** ~2,500 lines

---

## 🔍 How to Find Things

### Looking for...

**User Authentication?**
- Logic: `src/services/AuthContext.jsx`
- API: `src/services/api.js` → authAPI
- Pages: `src/pages/LoginPage.jsx`, `src/pages/RegisterPage.jsx`

**Student Features?**
- Page: `src/pages/StudentDashboard.jsx`
- API calls: `src/services/api.js` → marksAPI, attendanceAPI, courseAPI
- Styles: `src/styles/Dashboard.css`

**Faculty Features?**
- Page: `src/pages/FacultyDashboard.jsx`
- API calls: `src/services/api.js` → userAPI, attendanceAPI, marksAPI
- Styles: `src/styles/Dashboard.css`, `src/styles/DashboardExtended.css`

**Admin Features?**
- Page: `src/pages/AdminDashboard.jsx`
- API calls: `src/services/api.js` → userAPI
- Styles: `src/styles/Dashboard.css`

**Theme System?**
- Logic: `src/services/ThemeContext.jsx`
- CSS: `src/styles/Global.css`
- Component: `src/components/Navigation.jsx` (toggle button)

**Charts?**
- Page: `src/pages/StudentDashboard.jsx`
- Page: `src/pages/FacultyDashboard.jsx`
- Styles: `src/styles/Dashboard.css`

**Routing?**
- Main: `src/App.jsx` (route definitions)
- Protection: `src/components/ProtectedRoute.jsx`
- Navigation: `src/components/Navigation.jsx` (links)

**Form Validation?**
- Register: `src/pages/RegisterPage.jsx`
- Login: `src/pages/LoginPage.jsx`
- Marks: `src/pages/FacultyDashboard.jsx`
- Student: `src/pages/FacultyDashboard.jsx`

**API Integration?**
- All APIs: `src/services/api.js`
- Context: `src/services/AuthContext.jsx`
- Usage: All pages and components

**Styling/CSS?**
- Global: `src/styles/Global.css` (variables)
- Component: `src/styles/[component].css` (specific styles)
- Dark mode: Each CSS file has `[data-theme='dark']` rules

**Mobile Responsive?**
- Each CSS file has `@media (max-width: 768px)` queries
- Navigation: `src/components/Navigation.jsx` (hamburger menu)

---

## 🚀 Quick Navigation

### Getting Started
1. Read: `QUICKSTART.md` (5 minutes)
2. Read: `FRONTEND_SETUP.md` (installation)
3. Run: `npm install` && `npm run dev`

### Understanding Features
1. Read: `README.md` (overview)
2. Read: `FEATURES.md` (details)
3. Explore: Corresponding page files

### Modifying Code
1. Pages: `src/pages/[PageName].jsx`
2. Styles: `src/styles/[ComponentName].css`
3. APIs: `src/services/api.js`
4. Auth: `src/services/AuthContext.jsx`

### Deployment
1. Build: `npm run build`
2. Deploy: `dist/` folder to hosting
3. Configure: Backend URL in `src/services/api.js`

---

## ✅ Verification Checklist

Check these files to verify implementation:

- [ ] `src/pages/HomePage.jsx` - Home page with animation
- [ ] `src/pages/LoginPage.jsx` - Login with backend connection
- [ ] `src/pages/RegisterPage.jsx` - Register with all fields
- [ ] `src/pages/StudentDashboard.jsx` - Student features with 3 tabs
- [ ] `src/pages/FacultyDashboard.jsx` - Faculty features with 4 tabs
- [ ] `src/pages/AdminDashboard.jsx` - Admin features with 5 tabs
- [ ] `src/components/Navigation.jsx` - Common navbar with theme toggle
- [ ] `src/components/ProtectedRoute.jsx` - Route protection
- [ ] `src/services/api.js` - All API calls
- [ ] `src/services/AuthContext.jsx` - Authentication logic
- [ ] `src/services/ThemeContext.jsx` - Theme logic
- [ ] `src/styles/Global.css` - Theme variables
- [ ] `src/styles/Dashboard.css` - Dashboard styles
- [ ] `QUICKSTART.md` - Quick start guide
- [ ] `FEATURES.md` - Feature documentation
- [ ] `README.md` - Main documentation

---

## 📞 Support

- For setup help: See `QUICKSTART.md`
- For feature details: See `FEATURES.md`
- For troubleshooting: See `FRONTEND_SETUP.md`
- For code structure: See this file

---

**Last Updated:** February 10, 2026
**Frontend Version:** 1.0.0
**Status:** ✅ Complete & Production Ready
