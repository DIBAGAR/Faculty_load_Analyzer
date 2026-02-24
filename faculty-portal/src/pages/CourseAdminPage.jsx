import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { courseApi, departmentApi } from '../api/api';

export default function CourseAdminPage() {
  const { user, logout } = useAuth();
  const [departments, setDepartments] = useState([]);
  const [status, setStatus] = useState('');
  const [form, setForm] = useState({ courseCode: '', name: '', credit: 3, departmentIds: [] });

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
      await courseApi.create({ ...form, credit: Number(form.credit) });
      setStatus('Course created.');
    } catch (err) {
      setStatus(err?.response?.data?.detail ?? 'Failed to create course.');
    }
  };

  return (
    <div style={{ padding: 24, fontFamily: 'system-ui, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>Course Admin</h2>
        <div>
          <span style={{ marginRight: 12 }}>{user?.email}</span>
          <button onClick={logout}>Logout</button>
        </div>
      </div>

      <div style={{ marginTop: 16, maxWidth: 640 }}>
        <h3>Create course</h3>
        <form onSubmit={submit} style={{ display: 'grid', gap: 10 }}>
          <input value={form.courseCode} onChange={(e) => setForm((s) => ({ ...s, courseCode: e.target.value }))} placeholder="COURSE101" required />
          <input value={form.name} onChange={(e) => setForm((s) => ({ ...s, name: e.target.value }))} placeholder="Course name" required />
          <input type="number" value={form.credit} onChange={(e) => setForm((s) => ({ ...s, credit: e.target.value }))} min={1} max={10} />
          <label>
            Departments
            <select
              multiple
              value={form.departmentIds}
              onChange={(e) => setForm((s) => ({ ...s, departmentIds: Array.from(e.target.selectedOptions).map((o) => Number(o.value)) }))}
              size={6}
            >
              {departments.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name}
                </option>
              ))}
            </select>
          </label>
          <button type="submit">Create</button>
        </form>
        {status ? <p style={{ marginTop: 10 }}>{status}</p> : null}
      </div>
    </div>
  );
}

