import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../services/AuthContext';
import { useTheme } from '../services/ThemeContext';
import { FiBarChart2, FiSun, FiMoon } from 'react-icons/fi';
import '../styles/Navigation.css';

const Navigation = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const { isDark, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
    setMobileMenuOpen(false);
  };

  const handleNavClick = () => {
    setMobileMenuOpen(false);
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-logo" onClick={handleNavClick}>
          <FiBarChart2 style={{ marginRight: '8px' }} />
          APVD
        </Link>

        <button
          className="mobile-menu-icon"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
        >
          <span></span>
          <span></span>
          <span></span>
        </button>

        <ul className={`nav-menu ${mobileMenuOpen ? 'active' : ''}`}>
          <li className="nav-item">
            <Link
              to="/"
              className={`nav-link ${location.pathname === '/' ? 'active' : ''}`}
              onClick={handleNavClick}
            >
              Home
            </Link>
          </li>

          {!isAuthenticated ? (
            <>
              <li className="nav-item">
                <Link
                  to="/register"
                  className={`nav-link ${location.pathname === '/register' ? 'active' : ''}`}
                  onClick={handleNavClick}
                >
                  Sign Up
                </Link>
              </li>
              <li className="nav-item">
                <Link
                  to="/login"
                  className={`nav-link ${location.pathname === '/login' ? 'active' : ''}`}
                  onClick={handleNavClick}
                >
                  Sign In
                </Link>
              </li>
            </>
          ) : (
            <>
              <li className="nav-item">
                <span className="nav-user">
                  {user?.username} ({user?.role})
                </span>
              </li>
              <li className="nav-item">
                <button className="nav-link logout-btn" onClick={handleLogout}>
                  Logout
                </button>
              </li>
            </>
          )}

          <li className="nav-item">
            <button className="theme-toggle" onClick={toggleTheme} title="Toggle theme">
              {isDark ? <FiSun size={20} /> : <FiMoon size={20} />}
            </button>
          </li>
        </ul>
      </div>
    </nav>
  );
};

export default Navigation;
