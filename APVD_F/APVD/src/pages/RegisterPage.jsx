import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../services/AuthContext';
import { departmentAPI } from '../services/api';
import { FiBookOpen } from 'react-icons/fi';
import '../styles/AuthPages.css';

const RegisterPage = () => {
  const [formData, setFormData] = useState({
    userId: '',
    username: '',
    userEmail: '',
    userPassword: '',
    confirmPassword: '',
    mobile: '',
    role: 'STUDENT',
    department: '',
    enrollmentNumber: '',
    yearOfStudying: 1,
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [departments, setDepartments] = useState([]);
  const { register } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchDepartments();
  }, []);

  const fetchDepartments = async () => {
    try {
      const response = await departmentAPI.getAllDepartments();
      setDepartments(response.data || []);
    } catch (err) {
      console.error('Failed to fetch departments:', err);
    }
  };

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

    // Validation
    if (!formData.userId || !formData.username || !formData.userEmail || !formData.userPassword || !formData.mobile) {
      setError('Please fill in all required fields');
      setIsLoading(false);
      return;
    }

    if (formData.userPassword !== formData.confirmPassword) {
      setError('Passwords do not match');
      setIsLoading(false);
      return;
    }

    if (formData.userPassword.length < 6) {
      setError('Password must be at least 6 characters long');
      setIsLoading(false);
      return;
    }

    try {
      const registrationData = {
        userId: formData.userId,
        username: formData.username,
        userEmail: formData.userEmail,
        userPassword: formData.userPassword,
        mobile: formData.mobile,
        role: formData.role,
        department: formData.role === 'ADMIN' ? 'ADMIN' : formData.department,
      };

      // Add student-specific fields
      if (formData.role === 'STUDENT') {
        registrationData.enrollmentNumber = formData.enrollmentNumber || `ENR${Date.now()}`;
        registrationData.yearOfStudying = formData.yearOfStudying;
      }

      await register(registrationData);
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-card large-card">
          <h2 className="auth-title">Create Account</h2>
          <p className="auth-subtitle">Join APVD to track your academic progress</p>

          {error && <div className="error-message">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="userId">User ID *</label>
                <input
                  type="text"
                  id="userId"
                  name="userId"
                  placeholder="Enter a unique User ID (e.g., APVD1001)"
                  value={formData.userId}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="username">Full Name *</label>
                <input
                  type="text"
                  id="username"
                  name="username"
                  placeholder="Enter your full name"
                  value={formData.username}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="userEmail">Email *</label>
                <input
                  type="email"
                  id="userEmail"
                  name="userEmail"
                  placeholder="Enter your email"
                  value={formData.userEmail}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="mobile">Mobile Number *</label>
                <input
                  type="tel"
                  id="mobile"
                  name="mobile"
                  placeholder="Enter your mobile number"
                  value={formData.mobile}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="role">Role *</label>
                <select
                  id="role"
                  name="role"
                  value={formData.role}
                  onChange={handleChange}
                  required
                >
                  <option value="STUDENT">Student</option>
                  <option value="FACULTY">Faculty</option>
                  <option value="ADMIN">Admin</option>
                </select>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="userPassword">Password *</label>
                <input
                  type="password"
                  id="userPassword"
                  name="userPassword"
                  placeholder="Enter password (min 6 characters)"
                  value={formData.userPassword}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="confirmPassword">Confirm Password *</label>
                <input
                  type="password"
                  id="confirmPassword"
                  name="confirmPassword"
                  placeholder="Confirm your password"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="department">Department *</label>
                {formData.role === 'ADMIN' ? (
                  <input
                    type="text"
                    disabled
                    value="ADMIN"
                    placeholder="Admin Department"
                  />
                ) : (
                  <select
                    id="department"
                    name="department"
                    value={formData.department}
                    onChange={handleChange}
                    required={formData.role !== 'ADMIN'}
                  >
                    <option value="">-- Select Department --</option>
                    {departments.map((dept) => (
                      <option key={dept.departmentId} value={dept.departmentName}>
                        {dept.departmentName} {dept.departmentCode ? `(${dept.departmentCode})` : ''}
                      </option>
                    ))}
                  </select>
                )}
              </div>

              {formData.role === 'STUDENT' && (
                <div className="form-group">
                  <label htmlFor="yearOfStudying">Year of Studying</label>
                  <select
                    id="yearOfStudying"
                    name="yearOfStudying"
                    value={formData.yearOfStudying}
                    onChange={handleChange}
                  >
                    {[1, 2, 3, 4].map((year) => (
                      <option key={year} value={year}>
                        Year {year}
                      </option>
                    ))}
                  </select>
                </div>
              )}
            </div>

            {formData.role === 'STUDENT' && (
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="enrollmentNumber">Enrollment Number</label>
                  <input
                    type="text"
                    id="enrollmentNumber"
                    name="enrollmentNumber"
                    placeholder="Enter your enrollment number"
                    value={formData.enrollmentNumber}
                    onChange={handleChange}
                  />
                </div>
              </div>
            )}

            <button
              type="submit"
              className="btn-submit"
              disabled={isLoading}
            >
              {isLoading ? 'Creating Account...' : 'Sign Up'}
            </button>
          </form>

          <p className="auth-footer">
            Already have an account? <Link to="/login">Sign In</Link>
          </p>
        </div>

        <div className="auth-illustration">
          <div className="illustration-content">
            <FiBookOpen size={48} style={{ marginBottom: '16px' }} />
            <h3>Start Learning</h3>
            <p>Join thousands of students tracking their academic progress</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
