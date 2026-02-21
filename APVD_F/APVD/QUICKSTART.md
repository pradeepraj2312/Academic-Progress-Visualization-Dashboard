# APVD Frontend - Quick Start Guide

## ⚡ 5-Minute Quick Start

### 1. **Prerequisites Check**
```bash
# Check Node.js installation
node --version  # Should be 16+
npm --version   # Should be 8+
```

### 2. **Install Dependencies**
```bash
cd APVD_F/APVD
npm install
```

### 3. **Start Development Server**
```bash
npm run dev
```

**Output:**
```
  VITE v7.2.4  ready in XXX ms

  ➜  Local:   http://localhost:5173/
  ➜  Press h to show help
```

### 4. **Open in Browser**
- Click the link: http://localhost:5173/
- Or manually navigate to: `http://localhost:5173`

### 5. **Test the Application**

**Create Test Account:**
1. Click "Sign Up" button
2. Fill in the form:
   - Name: "Test Student"
   - Email: "student@test.com"
   - Mobile: "9876543210"
   - Role: "Student"
   - Department: "CSE"
   - Semester: "1"
3. Click "Sign Up"
4. Click "Sign In"
5. Enter Email and Password
6. Click "Sign In"

**Explore Dashboard:**
- You're now in Student Dashboard
- Click through tabs: Attendance, Results, Courses
- Try switching themes with moon/sun icon

---

## 🔗 Backend API Connection

### Ensure Backend is Running
```bash
# In a separate terminal, from APVD_B folder
cd ../APVD_B/apvd
mvn spring-boot:run
```

Backend should be running on: `http://localhost:8080`

### Verify Connection
1. Open browser DevTools (F12)
2. Go to Network tab
3. Perform login
4. Check if requests to `localhost:8080` are successful (200 status)

---

## 📁 Project Structure Quick Reference

```
APVD_F/APVD/
├── src/
│   ├── pages/           ← Homepage, Login, Register, Dashboards
│   ├── components/      ← Navigation, ProtectedRoute
│   ├── services/        ← API calls, Auth, Theme
│   ├── styles/          ← CSS files (one per feature)
│   ├── App.jsx          ← Main app with routing
│   └── main.jsx         ← React entry point
│
├── public/              ← Static assets
├── package.json         ← Dependencies
├── index.html           ← HTML template
└── vite.config.js       ← Vite configuration
```

---

## 🎯 Key File Locations

| Feature | File |
|---------|------|
| **Home Page** | `src/pages/HomePage.jsx` |
| **Login** | `src/pages/LoginPage.jsx` |
| **Register** | `src/pages/RegisterPage.jsx` |
| **Student Dashboard** | `src/pages/StudentDashboard.jsx` |
| **Faculty Dashboard** | `src/pages/FacultyDashboard.jsx` |
| **Admin Dashboard** | `src/pages/AdminDashboard.jsx` |
| **API Integration** | `src/services/api.js` |
| **Authentication** | `src/services/AuthContext.jsx` |
| **Theme Toggle** | `src/services/ThemeContext.jsx` |
| **Navigation** | `src/components/Navigation.jsx` |
| **Route Protection** | `src/components/ProtectedRoute.jsx` |

---

## ✅ Checklist After Setup

- [ ] Backend is running on port 8080
- [ ] Frontend is running on port 5173
- [ ] Can navigate to http://localhost:5173
- [ ] Home page loads with animation
- [ ] Can create a new account
- [ ] Can login with created account
- [ ] Dashboard displays correctly
- [ ] Theme toggle works (dark/light)
- [ ] Logout button works and redirects to login

---

## 🚀 Available Commands

```bash
# Development
npm run dev          # Start dev server

# Build
npm run build        # Create production build
npm run preview      # Preview production build

# Linting
npm run lint         # Check code style

# Other
npm install          # Install dependencies
npm update           # Update all packages
```

---

## 🐛 Troubleshooting

### Port Already in Use
```bash
# If port 5173 is busy, use a different port
npm run dev -- --port 3000
```

### Dependencies Installation Fails
```bash
# Clear npm cache and try again
npm cache clean --force
npm install
```

### Backend Connection Error
```
Error: Request failed with status code 404
```
**Solution:** Verify backend is running on http://localhost:8080

### Module Not Found
```
Cannot find module '@components/...'
```
**Solution:** Check file paths in import statements (use relative paths like `./components/`)

### Blank Page on Load
1. Press F12 to open DevTools
2. Check Console tab for errors
3. Check Network tab for failed requests
4. Hard refresh: Ctrl+Shift+R (Windows) or Cmd+Shift+R (Mac)

---

## 📚 Next Steps

### Customization
1. **Change Colors**: Edit `src/styles/Global.css` (--primary-color)
2. **Add New Pages**: Create file in `src/pages/`, add route in `App.jsx`
3. **Change API URL**: Edit `src/services/api.js` (API_BASE_URL)
4. **Add Features**: Create components, add to appropriate pages

### Deployment
1. Build project: `npm run build`
2. Deploy `dist/` folder to:
   - Vercel (automatic)
   - Netlify (automatic)
   - GitHub Pages
   - Any static hosting

### Learning Resources
- React Docs: https://react.dev
- React Router: https://reactrouter.com
- Chart.js: https://www.chartjs.org
- Vite: https://vitejs.dev

---

## 📞 Support

### Check These First
1. Is backend running? (http://localhost:8080)
2. Are all dependencies installed? (npm install)
3. Is port 5173 available?
4. Check browser console for errors (F12)

### Common Fixes
- Clear browser cache: Ctrl+Shift+Delete
- Restart dev server: Ctrl+C, then npm run dev
- Check network requests: F12 → Network tab
- Verify backend API: http://localhost:8080/api/auth/verify

---

## 🎉 You're All Set!

The APVD frontend is ready to use. Start exploring and building amazing academic features!

**Quick Links:**
- 🏠 [Home Page](http://localhost:5173)
- 📚 [Full Documentation](./FRONTEND_SETUP.md)
- 🎯 [Feature Guide](./FEATURES.md)
- 🔧 [Backend Configuration](../APVD_B/apvd/BACKEND_API_DOCUMENTATION.md)

---

**Happy coding! 🚀**
