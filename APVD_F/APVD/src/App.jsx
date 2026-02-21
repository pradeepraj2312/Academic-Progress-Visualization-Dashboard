import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './services/AuthContext';
import { ThemeProvider } from './services/ThemeContext';
import Navigation from './components/Navigation';
import ProtectedRoute from './components/ProtectedRoute';

// Pages
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import StudentDashboard from './pages/StudentDashboard';
import FacultyDashboard from './pages/FacultyDashboard';
import AdminDashboard from './pages/AdminDashboard';

// Styles
import './styles/Global.css';
import './App.css';

function AppContent() {
  return (
    <>
      <Navigation />
      <main>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected Routes */}
          <Route
            path="/student-dashboard"
            element={
              <ProtectedRoute requiredRole="STUDENT">
                <StudentDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/faculty-dashboard"
            element={
              <ProtectedRoute requiredRole="FACULTY">
                <FacultyDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin-dashboard"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminDashboard />
              </ProtectedRoute>
            }
          />

          {/* Catch all - Redirect to home */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </>
  );
}

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <Router>
          <AppContent />
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
