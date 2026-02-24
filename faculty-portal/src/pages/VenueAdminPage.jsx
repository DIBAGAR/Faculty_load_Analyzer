import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { departmentApi, venueApi } from '../api/api';

export default function VenueAdminPage() {
  const { user, logout } = useAuth();
  const [departments, setDepartments] = useState([]);
  const [status, setStatus] = useState('');
  const [form, setForm] = useState({ departmentId: '', name: '', code: '', type: 'ACADEMIC' });

  useEffect(() => {
    (async () => {
      try {
        const d = await departmentApi.list();
        setDepartments(d.data);
      } catch (e) {
        setStatus(e?.response?.data?.detail ?? 'Failed to load departments.');
      }
    })();
  }, []);

  const submit = async (e) => {
    e.preventDefault();
    setStatus('');
    try {
      await venueApi.create({ ...form, departmentId: Number(form.departmentId) });
      setStatus('Venue created.');
    } catch (err) {
      setStatus(err?.response?.data?.detail ?? 'Failed to create venue.');
    }
  };

  return (
    <div style={{ padding: 24, fontFamily: 'system-ui, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>Venue Admin</h2>
        <div>
          <span style={{ marginRight: 12 }}>{user?.email}</span>
          <button onClick={logout}>Logout</button>
        </div>
      </div>

      <div style={{ marginTop: 16, maxWidth: 640 }}>
        <h3>Create venue</h3>
        <form onSubmit={submit} style={{ display: 'grid', gap: 10 }}>
          <select value={form.departmentId} onChange={(e) => setForm((s) => ({ ...s, departmentId: e.target.value }))} required>
            <option value="" disabled>
              Select department
            </option>
            {departments.map((d) => (
              <option key={d.id} value={d.id}>
                {d.name}
              </option>
            ))}
          </select>
          <input value={form.name} onChange={(e) => setForm((s) => ({ ...s, name: e.target.value }))} placeholder="Venue name" required />
          <input value={form.code} onChange={(e) => setForm((s) => ({ ...s, code: e.target.value }))} placeholder="Code (e.g. LAB-1)" required />
          <select value={form.type} onChange={(e) => setForm((s) => ({ ...s, type: e.target.value }))}>
            <option value="ACADEMIC">Academic</option>
            <option value="LAB">Lab</option>
          </select>
          <button type="submit">Create</button>
        </form>
        {status ? <p style={{ marginTop: 10 }}>{status}</p> : null}
      </div>
    </div>
  );
}

