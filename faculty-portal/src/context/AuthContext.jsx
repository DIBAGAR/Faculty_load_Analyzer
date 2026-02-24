import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { authApi } from '../api/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });

  const INACTIVITY_LIMIT = 10 * 60 * 1000; // 10 minutes

  const logout = useCallback(() => {
    try {
      authApi.logout();
    } catch {
      // ignore
    }
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
    window.location.assign('/login');
  }, []);

  useEffect(() => {
    if (!user) return;
    let timer;
    const resetTimer = () => {
      clearTimeout(timer);
      timer = setTimeout(() => {
        alert('Session expired. Please login again.');
        logout();
      }, INACTIVITY_LIMIT);
    };
    const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart'];
    events.forEach((name) => window.addEventListener(name, resetTimer));
    resetTimer();
    return () => {
      events.forEach((name) => window.removeEventListener(name, resetTimer));
      clearTimeout(timer);
    };
  }, [user, logout]);

  const login = ({ token: newToken, user: newUser }) => {
    localStorage.setItem('token', newToken);
    localStorage.setItem('user', JSON.stringify(newUser));
    setToken(newToken);
    setUser(newUser);
  };

  return <AuthContext.Provider value={{ token, user, login, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}

