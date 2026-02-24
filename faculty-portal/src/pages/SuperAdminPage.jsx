import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import API from '../api/api';

const roles = [
  'SUPER_ADMIN',
  'FACULTY_ADMIN',
  'COURSE_ADMIN',
  'VENUE_ADMIN',
  'DEPARTMENT_ADMIN',
  'HOD',
  'FACULTY',
];

export default function SuperAdminPage() {
  const { user, logout } = useAuth();
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('FACULTY');
  const [status, setStatus] = useState('');

  const updateRole = async (e) => {
    e.preventDefault();
    setStatus('');
    try {
      await API.put(`/super-admin/users/${encodeURIComponent(email)}/role`, { role });
      setStatus('Role updated.');
    } catch (err) {
      setStatus(err?.response?.data?.detail ?? 'Failed to update role.');
    }
  };

  return (
    <div style={{ padding: 24, fontFamily: 'system-ui, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>Super Admin</h2>
        <div>
          <span style={{ marginRight: 12 }}>{user?.email}</span>
          <button onClick={logout}>Logout</button>
        </div>
      </div>

      <div style={{ marginTop: 16, maxWidth: 520 }}>
        <h3>Assign role by email</h3>
        <form onSubmit={updateRole} style={{ display: 'grid', gap: 10 }}>
          <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="user@college.edu" required />
          <select value={role} onChange={(e) => setRole(e.target.value)}>
            {roles.map((r) => (
              <option key={r} value={r}>
                {r}
              </option>
            ))}
          </select>
          <button type="submit">Update role</button>
        </form>
        {status ? <p style={{ marginTop: 10 }}>{status}</p> : null}
      </div>
    </div>
  );
}

