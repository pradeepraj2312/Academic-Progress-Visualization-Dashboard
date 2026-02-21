import React, { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from './api';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    const token = localStorage.getItem('authToken');

    if (storedUser && token) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (userIdOrEmail, password) => {
    setLoading(true);
    setError(null);
    try {
      const response = await authAPI.login({
        userIdOrEmail,
        password,
      });

      const { token, tokenType, ...userData } = response.data;
      localStorage.setItem('authToken', token);
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);

      return userData;
    } catch (err) {
      const message = err.response?.data?.message || 'Login failed';
      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData) => {
    setLoading(true);
    setError(null);
    try {
      const response = await authAPI.register(userData);
      return response.data;
    } catch (err) {
      const message = err.response?.data?.message || 'Registration failed';
      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
    setUser(null);
    setError(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        error,
        login,
        register,
        logout,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
