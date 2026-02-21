import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../services/AuthContext';
import { FiBarChart2, FiUsers, FiSmartphone, FiTarget, FiLock } from 'react-icons/fi';
import { MdTrendingUp, MdSchool } from 'react-icons/md';
import '../styles/HomePage.css';

const HomePage = () => {
  const { isAuthenticated, user } = useAuth();

  return (
    <div className="home-page">
      <div className="hero-section">
        <div className="hero-content">
          <h1 className="hero-title">
            <span className="title-line">Academic</span>
            <span className="title-line">Progress</span>
            <span className="title-line">Visualization</span>
            <span className="title-line">Dashboard</span>
          </h1>

          <p className="hero-subtitle">
            Track your academic journey with comprehensive insights and analytics
          </p>

          <div className="hero-buttons">
            {!isAuthenticated ? (
              <>
                <Link to="/login" className="btn btn-primary">
                  Sign In
                </Link>
                <Link to="/register" className="btn btn-secondary">
                  Create Account
                </Link>
              </>
            ) : (
              <>
                {user?.role === 'STUDENT' && (
                  <Link to="/student-dashboard" className="btn btn-primary">
                    Go to Dashboard
                  </Link>
                )}
                {user?.role === 'FACULTY' && (
                  <Link to="/faculty-dashboard" className="btn btn-primary">
                    Go to Dashboard
                  </Link>
                )}
                {user?.role === 'ADMIN' && (
                  <Link to="/admin-dashboard" className="btn btn-primary">
                    Go to Dashboard
                  </Link>
                )}
              </>
            )}
          </div>
        </div>

        <div className="hero-animation">
          <div className="floating-card">
            <div className="card-content">
              <FiBarChart2 size={32} />
              <p>Analytics</p>
            </div>
          </div>
          <div className="floating-card">
            <div className="card-content">
              <MdTrendingUp size={32} />
              <p>Progress</p>
            </div>
          </div>
          <div className="floating-card">
            <div className="card-content">
              <MdSchool size={32} />
              <p>Learning</p>
            </div>
          </div>
        </div>
      </div>

      <div className="features-section">
        <h2>Why Choose APVD?</h2>
        <div className="features-grid">
          <div className="feature-card">
            <div className="feature-icon"><FiBarChart2 size={32} /></div>
            <h3>Real-time Analytics</h3>
            <p>Get instant insights into your academic performance with detailed analytics</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon"><MdTrendingUp size={32} /></div>
            <h3>Progress Tracking</h3>
            <p>Monitor your academic progress with comprehensive tracking and visualization</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon"><FiUsers size={32} /></div>
            <h3>Multi-Role Support</h3>
            <p>Designed for students, faculty, and administrators with role-specific dashboards</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon"><FiSmartphone size={32} /></div>
            <h3>Responsive Design</h3>
            <p>Access your dashboard from any device with our responsive design</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon"><FiTarget size={32} /></div>
            <h3>Goal Setting</h3>
            <p>Set and track your academic goals with visual progress indicators</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon"><FiLock size={32} /></div>
            <h3>Secure & Private</h3>
            <p>Your data is protected with industry-standard security measures</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
