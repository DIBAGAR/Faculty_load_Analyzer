import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authApi } from '../api/api';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await authApi.login({ email, password });
      const data = response.data; // { token, user: { ... } }
      login(data);

      switch (data.user.role) {
        case 'SUPER_ADMIN':
          navigate('/super-admin');
          break;
        case 'FACULTY_ADMIN':
          navigate('/admin/faculty');
          break;
        case 'COURSE_ADMIN':
          navigate('/admin/course');
          break;
        case 'VENUE_ADMIN':
          navigate('/admin/venue');
          break;
        case 'DEPARTMENT_ADMIN':
          navigate('/admin/department');
          break;
        case 'HOD':
          navigate('/hod-dashboard');
          break;
        case 'FACULTY':
          navigate('/faculty-dashboard');
          break;
        default:
          navigate('/login');
      }
    } catch {
      alert('Invalid credentials. Please try again.');
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h1>Faculty Load Analyzer</h1>
        <p>Sign in to continue</p>
        <form onSubmit={handleLogin}>
          <input type="email" placeholder="College Email" onChange={(e) => setEmail(e.target.value)} required />
          <input type="password" placeholder="Password" onChange={(e) => setPassword(e.target.value)} required />
          <button type="submit">Sign In</button>
        </form>
        <button className="forgot-btn" onClick={() => alert('Please contact Super Admin to reset password')}>
          Forgot Password?
        </button>
      </div>
    </div>
  );
}

