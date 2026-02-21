import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../services/AuthContext';
import { MdSchool } from 'react-icons/md';
import '../styles/AuthPages.css';

const LoginPage = () => {
  const [formData, setFormData] = useState({
    userIdOrEmail: '',
    password: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      const result = await login(formData.userIdOrEmail, formData.password);

      // Redirect based on role
      if (result.role === 'STUDENT') {
        navigate('/student-dashboard');
      } else if (result.role === 'FACULTY') {
        navigate('/faculty-dashboard');
      } else if (result.role === 'ADMIN') {
        navigate('/admin-dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-card">
          <h2 className="auth-title">Sign In</h2>
          <p className="auth-subtitle">Welcome back to APVD</p>

          {error && <div className="error-message">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="userIdOrEmail">User ID or Email</label>
              <input
                type="text"
                id="userIdOrEmail"
                name="userIdOrEmail"
                placeholder="Enter your User ID (APVD1001) or Email"
                value={formData.userIdOrEmail}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="password">Password</label>
              <input
                type="password"
                id="password"
                name="password"
                placeholder="Enter your password"
                value={formData.password}
                onChange={handleChange}
                required
              />
            </div>

            <button
              type="submit"
              className="btn-submit"
              disabled={isLoading}
            >
              {isLoading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          <p className="auth-footer">
            Don't have an account? <Link to="/register">Sign Up</Link>
          </p>
        </div>

        <div className="auth-illustration">
          <div className="illustration-content">
            <MdSchool size={48} style={{ marginBottom: '16px' }} />
            <h3>Academic Excellence</h3>
            <p>Track your progress and achieve your academic goals</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
