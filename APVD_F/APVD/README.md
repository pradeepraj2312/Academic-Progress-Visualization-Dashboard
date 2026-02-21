# APVD - Academic Progress Visualization Dashboard

## Frontend Application

A modern, responsive React-based frontend for the Academic Progress Visualization Dashboard with comprehensive student, faculty, and admin management features.

## 🌟 Features Overview

### ✨ Key Features

**For All Users:**
- 🎨 Dark/Light theme toggle
- 📱 Fully responsive design
- 🔐 JWT-based authentication
- 🛡️ Role-based access control (RBAC)
- ⚡ Fast and interactive UI with animations

**Home Page:**
- Animated hero section with floating cards
- Features showcase with 6 key benefits
- Call-to-action buttons for Sign In/Up
- Professional, modern design

**Authentication:**
- User registration with role selection
- Login with User ID or Email
- Form validation and error handling
- Persistent authentication across sessions

**Student Dashboard:**
- 📊 Attendance tracking with line/doughnut charts
- 📈 Results view with CGPA and SGPA calculations
- 📚 Course selection (5 Core + 2 Elective)
- 🎯 Detailed performance analytics

**Faculty Dashboard:**
- 👥 Student attendance management
- 📝 Marks entry and management
- 📊 Attendance analytics by department
- ➕ Add new students

**Admin Dashboard:**
- 🔑 Full system control
- 👨‍🎓 Student management
- 👨‍🏫 Faculty management
- 📊 Organization-wide analytics

## 🚀 Getting Started

### Quick Start (5 minutes)
See [QUICKSTART.md](./QUICKSTART.md) for immediate setup instructions.

### Complete Setup Guide
See [FRONTEND_SETUP.md](./FRONTEND_SETUP.md) for detailed installation and configuration.

### Feature Documentation
See [FEATURES.md](./FEATURES.md) for comprehensive feature walkthrough.

## 📋 Prerequisites

- **Node.js** 16+ (recommended 18+)
- **npm** 8+ or **yarn** 3+
- **Backend API** running on http://localhost:8080
- **Modern web browser** (Chrome, Firefox, Safari, Edge)

## 🔧 Installation

```bash
# Navigate to project directory
cd APVD_F/APVD

# Install dependencies
npm install

# Start development server
npm run dev

# Open browser to
http://localhost:5173
```

## 📦 Project Structure

```
APVD_F/APVD/
├── src/
│   ├── pages/                  # Page components
│   │   ├── HomePage.jsx
│   │   ├── LoginPage.jsx
│   │   ├── RegisterPage.jsx
│   │   ├── StudentDashboard.jsx
│   │   ├── FacultyDashboard.jsx
│   │   └── AdminDashboard.jsx
│   │
│   ├── components/             # Reusable components
│   │   ├── Navigation.jsx
│   │   └── ProtectedRoute.jsx
│   │
│   ├── services/               # API and context
│   │   ├── api.js             # Axios configuration & API calls
│   │   ├── AuthContext.jsx    # Authentication state
│   │   └── ThemeContext.jsx   # Theme state
│   │
│   ├── styles/                # CSS files
│   │   ├── Global.css
│   │   ├── Navigation.css
│   │   ├── HomePage.css
│   │   ├── AuthPages.css
│   │   ├── Dashboard.css
│   │   └── DashboardExtended.css
│   │
│   ├── App.jsx                # Main app component
│   ├── main.jsx               # React entry point
│   └── index.css              # Base styles
│
├── public/                    # Static assets
├── index.html                 # HTML template
├── vite.config.js             # Vite config
├── package.json               # Dependencies
│
├── QUICKSTART.md              # Quick start guide (5 min)
├── FRONTEND_SETUP.md          # Complete setup guide
└── FEATURES.md                # Feature documentation
```

## 🎨 Technology Stack

- **Frontend Framework:** React 19.2.0
- **Routing:** React Router 7.2.0
- **HTTP Client:** Axios 1.7.9
- **Charts:** Chart.js 4.4.8 + react-chartjs-2
- **Build Tool:** Vite 7.2.4
- **CSS:** Custom CSS with CSS variables
- **Authentication:** JWT (JSON Web Tokens)

## 🔐 Authentication Flow

1. **Register** → Create account with role (Student/Faculty/Admin)
2. **Verify** → System generates unique User ID
3. **Login** → Enter User ID/Email + Password
4. **Token** → JWT stored in localStorage
5. **Protected Routes** → Role-based access control
6. **Token Refresh** → Automatic in requests
7. **Logout** → Clear token and redirect

## 🎯 User Roles & Features

### Student
- View personal attendance and attendance trends
- View semester-wise results and CGPA
- Select courses (5 core + 2 elective)
- Analyze performance through charts

### Faculty
- Mark student attendance
- Enter and manage student marks
- View department attendance statistics
- Add new students to department
- Manage course assignments

### Admin
- All faculty capabilities
- Add faculty accounts
- Manage all students across departments
- Organization-wide analytics
- Full system access

## 🌐 API Integration

The frontend communicates with the backend through RESTful APIs:

- **Base URL:** http://localhost:8080/api
- **Authentication:** Bearer token in Authorization header
- **Format:** JSON
- **Error Handling:** Automatic 401 redirect on token expiration

### API Categories
- Authentication (login, register, verify)
- User Management (add/get/update users)
- Marks Management (save/get/update marks)
- Attendance (mark, get, calculate percentage)
- Courses (get, select, view)
- Grades (get, calculate CGPA)

See `src/services/api.js` for all available endpoints.

## 🎨 Theme System

The application supports light and dark themes:

**Light Mode (Default)**
- Clean white interface
- Dark text
- Professional blue accents

**Dark Mode**
- Dark background (#0f0f0f)
- Light text (#e0e0e0)
- Adjusted colors for visibility
- Reduces eye strain

**Toggle**
- Click sun/moon icon in navigation
- Theme persists across sessions
- Smooth transition between modes

## 📊 Charts & Visualizations

**Chart Types:**
- Line Chart: Attendance trends (7-day rolling window)
- Bar Chart: Subject-wise marks comparison
- Doughnut Chart: Attendance status distribution

**Libraries:**
- Chart.js 4.4.8
- react-chartjs-2 5.3.0

## 🔄 State Management

**Context API:**
- `AuthContext` - User authentication and profile
- `ThemeContext` - Theme preference (light/dark)

**Local Storage:**
- `authToken` - JWT for API authentication
- `user` - Current user information
- `theme` - Theme preference (light/dark)

## 📱 Responsive Design

**Breakpoints:**
- **Desktop:** 1200px and above
- **Tablet:** 768px - 1199px
- **Mobile:** Below 768px

**Mobile Menu:**
- Hamburger menu icon at 768px
- Smooth open/close animation
- Full navigation in mobile menu

## ✨ Animations

**Page Load:**
- Fade-in animations
- Staggered slide-in effects
- Smooth transitions

**Interactive Elements:**
- Button hover effects
- Chart animations
- Tab transitions
- Theme toggle smoothness

## 🚀 Build & Deployment

### Development Build
```bash
npm run dev
```

### Production Build
```bash
npm run build
npm run preview  # Preview before deployment
```

### Deployment Options
1. **Vercel** (Recommended)
   - Automatic deployments from Git
   - Zero configuration
   - Free tier available

2. **Netlify**
   - Drag-and-drop deploy
   - CI/CD integration
   - Free tier available

3. **GitHub Pages**
   - Free static hosting
   - Git integration
   - Custom domain support

4. **Traditional Hosting**
   - Upload `dist/` folder
   - Configure server for SPA routing
   - Set appropriate cache headers

## 🛠️ Development Commands

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run linter
npm run lint

# Install dependencies
npm install

# Update dependencies
npm update
```

## ⚙️ Configuration

### API Base URL
Edit `src/services/api.js`:
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

### Colors & Theming
Edit `src/styles/Global.css`:
```css
:root {
  --primary-color: #2563eb;
  --danger-color: #dc2626;
  /* More variables... */
}
```

### Chart Colors
Edit relevant dashboard component:
```javascript
backgroundColor: ['#3b82f6', '#10b981', '#f59e0b'];
```

## 🐛 Troubleshooting

### Common Issues

**Backend Connection Error**
- Ensure backend is running on http://localhost:8080
- Check CORS headers in backend
- Verify network connectivity

**Login Fails**
- Double-check User ID/Email
- Verify password is correct
- Check if account exists in backend

**Charts Not Displaying**
- Ensure data is available (no empty responses)
- Check browser console for errors
- Verify Chart.js is properly imported

**Styling Issues**
- Hard refresh browser (Ctrl+Shift+R)
- Clear browser cache
- Check DevTools for CSS conflicts

**Mobile Layout Issues**
- Check viewport meta tag in HTML
- Verify CSS media queries
- Test in DevTools mobile view

See [FRONTEND_SETUP.md](./FRONTEND_SETUP.md#-troubleshooting) for more solutions.

## 📖 Documentation

- **[QUICKSTART.md](./QUICKSTART.md)** - 5-minute quick start
- **[FRONTEND_SETUP.md](./FRONTEND_SETUP.md)** - Complete setup & installation
- **[FEATURES.md](./FEATURES.md)** - Detailed feature guide
- **[Backend API Docs](../APVD_B/apvd/BACKEND_API_DOCUMENTATION.md)** - Backend endpoints

## 🤝 Contributing

To add new features:
1. Create components in `src/components/`
2. Create pages in `src/pages/`
3. Add styles in `src/styles/`
4. Update routing in `App.jsx`
5. Test thoroughly
6. Document changes in FEATURES.md

## 📄 License

This project is part of the Academic Progress Visualization Dashboard (APVD) system. All rights reserved.

## 🆘 Support & Help

**For Issues:**
1. Check console for error messages (F12)
2. Verify network requests (Network tab)
3. Check documentation in FEATURES.md
4. Review troubleshooting section above

**Useful Resources:**
- React Documentation: https://react.dev
- React Router: https://reactrouter.com
- Axios: https://axios-http.com
- Chart.js: https://www.chartjs.org
- Vite: https://vitejs.dev

## 📈 Project Statistics

- **Total Components:** 6 main pages + 2 reusable components
- **Total API Endpoints:** 40+
- **Lines of Code:** 2000+ (frontend)
- **CSS Rules:** 1000+ (with theme support)
- **Development Time:** Complete system ready

## 🎓 Educational Features

This project demonstrates:
- ✅ Modern React patterns
- ✅ Context API for state management
- ✅ JWT authentication flow
- ✅ RESTful API integration
- ✅ Chart.js data visualization
- ✅ Responsive design principles
- ✅ Dark mode implementation
- ✅ Role-based access control
- ✅ Form validation
- ✅ Error handling

## 🚀 Future Enhancements

Potential improvements:
- [ ] Real-time notifications
- [ ] Advanced filtering & search
- [ ] Bulk operations
- [ ] Export to PDF/Excel
- [ ] Email notifications
- [ ] Performance analytics
- [ ] Predictive analytics
- [ ] Mobile app (React Native)
- [ ] Progressive Web App (PWA)
- [ ] Offline support

## 📞 Contact & Support

For issues, questions, or suggestions, please refer to the comprehensive documentation or contact the development team.

---

**Version:** 1.0.0  
**Last Updated:** February 10, 2026  
**Status:** ✅ Production Ready

**Happy coding! 🎉**
