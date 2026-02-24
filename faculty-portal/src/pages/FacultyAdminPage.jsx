import React, { useEffect, useMemo, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { courseApi, departmentApi, facultyApi } from '../api/api';

export default function FacultyAdminPage() {
  const { user, logout } = useAuth();
  const [departments, setDepartments] = useState([]);
  const [courses, setCourses] = useState([]);
  const [status, setStatus] = useState('');

  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    departmentId: '',
    designation: 'ASSISTANT_PROFESSOR_1',
    primaryCourseIds: [],
    additionalCourseIds: [],
  });

  useEffect(() => {
    (async () => {
      try {
        const [d, c] = await Promise.all([departmentApi.list(), courseApi.list()]);
        setDepartments(d.data);
        setCourses(c.data);
      } catch (e) {
        setStatus(e?.response?.data?.detail ?? 'Failed to load reference data.');
      }
    })();
  }, []);

  const courseOptions = useMemo(() => courses.map((c) => ({ id: c.id, label: `${c.courseCode} - ${c.name}` })), [courses]);

  const submit = async (e) => {
    e.preventDefault();
    setStatus('');
    try {
      const payload = {
        ...form,
        departmentId: Number(form.departmentId),
      };
      await facultyApi.create(payload);
      setStatus('Faculty created.');
    } catch (err) {
      setStatus(err?.response?.data?.detail ?? 'Failed to create faculty.');
    }
  };

  return (
    <div style={{ padding: 24, fontFamily: 'system-ui, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>Faculty Admin</h2>
        <div>
          <span style={{ marginRight: 12 }}>{user?.email}</span>
          <button onClick={logout}>Logout</button>
        </div>
      </div>

      <div style={{ marginTop: 16, maxWidth: 720 }}>
        <h3>Create faculty</h3>
        <form onSubmit={submit} style={{ display: 'grid', gap: 10 }}>
          <input value={form.name} onChange={(e) => setForm((s) => ({ ...s, name: e.target.value }))} placeholder="Name" required />
          <input value={form.email} onChange={(e) => setForm((s) => ({ ...s, email: e.target.value }))} placeholder="Email" required />
          <input type="password" value={form.password} onChange={(e) => setForm((s) => ({ ...s, password: e.target.value }))} placeholder="Initial password" required />

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

          <select value={form.designation} onChange={(e) => setForm((s) => ({ ...s, designation: e.target.value }))}>
            <option value="ASSISTANT_PROFESSOR_1">Assistant Professor 1</option>
            <option value="ASSISTANT_PROFESSOR_2">Assistant Professor 2</option>
            <option value="ASSISTANT_PROFESSOR_3">Assistant Professor 3</option>
            <option value="ASSOCIATE_PROFESSOR_1">Associate Professor 1</option>
            <option value="ASSOCIATE_PROFESSOR_2">Associate Professor 2</option>
            <option value="ASSOCIATE_PROFESSOR_3">Associate Professor 3</option>
          </select>

          <label>
            Primary courses known (IDs)
            <select
              multiple
              value={form.primaryCourseIds}
              onChange={(e) => setForm((s) => ({ ...s, primaryCourseIds: Array.from(e.target.selectedOptions).map((o) => Number(o.value)) }))}
              size={6}
            >
              {courseOptions.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.label}
                </option>
              ))}
            </select>
          </label>

          <label>
            Additional courses known (IDs)
            <select
              multiple
              value={form.additionalCourseIds}
              onChange={(e) => setForm((s) => ({ ...s, additionalCourseIds: Array.from(e.target.selectedOptions).map((o) => Number(o.value)) }))}
              size={6}
            >
              {courseOptions.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.label}
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

