import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { departmentApi } from '../api/api';

export default function DepartmentAdminPage() {
  const { user, logout } = useAuth();
  const [status, setStatus] = useState('');
  const [form, setForm] = useState({ name: '', code: '' });

  const submit = async (e) => {
    e.preventDefault();
    setStatus('');
    try {
      await departmentApi.create(form);
      setStatus('Department created.');
    } catch (err) {
      setStatus(err?.response?.data?.detail ?? 'Failed to create department.');
    }
  };

  return (
    <div style={{ padding: 24, fontFamily: 'system-ui, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>Department Admin</h2>
        <div>
          <span style={{ marginRight: 12 }}>{user?.email}</span>
          <button onClick={logout}>Logout</button>
        </div>
      </div>

      <div style={{ marginTop: 16, maxWidth: 640 }}>
        <h3>Create department</h3>
        <form onSubmit={submit} style={{ display: 'grid', gap: 10 }}>
          <input value={form.name} onChange={(e) => setForm((s) => ({ ...s, name: e.target.value }))} placeholder="Department name" required />
          <input value={form.code} onChange={(e) => setForm((s) => ({ ...s, code: e.target.value }))} placeholder="Code (optional)" />
          <button type="submit">Create</button>
        </form>
        {status ? <p style={{ marginTop: 10 }}>{status}</p> : null}
      </div>
    </div>
  );
}

