import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { facultyApi, notificationApi } from '../api/api';

export default function FacultyDashboard() {
  const { user, logout } = useAuth();
  const [status, setStatus] = useState('');
  const [notifications, setNotifications] = useState([]);
  const [leaves, setLeaves] = useState([]);
  const [form, setForm] = useState({
    fromDate: '',
    toDate: '',
    leaveType: 'CASUAL',
    hourNumber: '',
    isEmergency: false,
  });

  const refresh = async () => {
    try {
      const [n, l] = await Promise.all([notificationApi.latest(), facultyApi.myLeaves()]);
      setNotifications(n.data);
      setLeaves(l.data);
    } catch {
      // ignore
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  const submitLeave = async (e) => {
    e.preventDefault();
    setStatus('');
    try {
      const payload = {
        ...form,
        hourNumber: form.hourNumber ? Number(form.hourNumber) : null,
      };
      await facultyApi.submitLeave(payload);
      setStatus('Leave submitted.');
      refresh();
    } catch (err) {
      setStatus(err?.response?.data?.detail ?? 'Failed to submit leave.');
    }
  };

  return (
    <div style={{ padding: 24, fontFamily: 'system-ui, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>Faculty Dashboard</h2>
        <div>
          <span style={{ marginRight: 12 }}>{user?.email}</span>
          <button onClick={logout}>Logout</button>
        </div>
      </div>

      <div style={{ display: 'grid', gap: 16, marginTop: 16, gridTemplateColumns: '1fr 1fr' }}>
        <div style={{ padding: 12, border: '1px solid #ddd', borderRadius: 8 }}>
          <h3 style={{ marginTop: 0 }}>Submit leave</h3>
          <form onSubmit={submitLeave} style={{ display: 'grid', gap: 10 }}>
            <input type="date" value={form.fromDate} onChange={(e) => setForm((s) => ({ ...s, fromDate: e.target.value }))} required />
            <input type="date" value={form.toDate} onChange={(e) => setForm((s) => ({ ...s, toDate: e.target.value }))} required />
            <select value={form.leaveType} onChange={(e) => setForm((s) => ({ ...s, leaveType: e.target.value }))}>
              <option value="CASUAL">Casual</option>
              <option value="SICK">Sick</option>
              <option value="EMERGENCY">Emergency</option>
              <option value="OTHER">Other</option>
            </select>
            <input
              type="number"
              min={1}
              max={7}
              value={form.hourNumber}
              onChange={(e) => setForm((s) => ({ ...s, hourNumber: e.target.value }))}
              placeholder="Hour number (optional)"
            />
            <label>
              <input
                type="checkbox"
                checked={form.isEmergency}
                onChange={(e) => setForm((s) => ({ ...s, isEmergency: e.target.checked }))}
              />{' '}
              Emergency leave (auto-redistribute)
            </label>
            <button type="submit">Submit</button>
          </form>
          {status ? <p style={{ marginTop: 10 }}>{status}</p> : null}

          <h4 style={{ marginTop: 16 }}>My leaves</h4>
          <div style={{ display: 'grid', gap: 8 }}>
            {leaves.map((l) => (
              <div key={l.id} style={{ padding: 8, border: '1px solid #eee', borderRadius: 6 }}>
                <div>
                  {l.fromDate} → {l.toDate} {l.hourNumber ? `(Hour ${l.hourNumber})` : ''} ({l.leaveType})
                </div>
                <div style={{ opacity: 0.7 }}>Status: {l.status}</div>
              </div>
            ))}
            {leaves.length === 0 ? <div style={{ opacity: 0.7 }}>No leave requests.</div> : null}
          </div>
        </div>

        <div style={{ padding: 12, border: '1px solid #ddd', borderRadius: 8 }}>
          <h3 style={{ marginTop: 0 }}>Notifications</h3>
          <button onClick={refresh}>Refresh</button>
          <div style={{ marginTop: 10, display: 'grid', gap: 8 }}>
            {notifications.map((n) => (
              <div key={n.id} style={{ padding: 8, border: '1px solid #eee', borderRadius: 6 }}>
                <div style={{ fontSize: 13, opacity: 0.7 }}>{new Date(n.createdAt).toLocaleString()}</div>
                <div>{n.message}</div>
              </div>
            ))}
            {notifications.length === 0 ? <div style={{ opacity: 0.7 }}>No notifications.</div> : null}
          </div>
        </div>
      </div>
    </div>
  );
}

