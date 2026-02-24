import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { notificationApi, timetableApi, workloadApi } from '../api/api';

export default function HodDashboard() {
  const { user, logout } = useAuth();
  const [status, setStatus] = useState('');
  const [notifications, setNotifications] = useState([]);
  const [form, setForm] = useState({
    departmentId: user?.departmentId ?? '',
    yearOfStudy: 1,
    section: 'A',
  });
  const [activeTimetable, setActiveTimetable] = useState(null);

  const refresh = async () => {
    try {
      const n = await notificationApi.latest();
      setNotifications(n.data);
    } catch {
      // ignore
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  const loadActive = async () => {
    setStatus('');
    try {
      const res = await timetableApi.getActive(Number(form.departmentId), Number(form.yearOfStudy), form.section);
      setActiveTimetable(res.data);
    } catch (err) {
      setActiveTimetable(null);
      setStatus(err?.response?.data?.detail ?? 'Failed to load active timetable.');
    }
  };

  const generate = async () => {
    setStatus('');
    try {
      const res = await workloadApi.generateNextWeek(Number(form.departmentId), Number(form.yearOfStudy), form.section);
      setStatus(`Generated for week starting ${res.data.weekStartDate}. Created=${res.data.assignmentsCreated}, Unassigned=${res.data.slotsUnassigned}`);
      refresh();
    } catch (err) {
      setStatus(err?.response?.data?.detail ?? 'Failed to generate next week work.');
    }
  };

  return (
    <div style={{ padding: 24, fontFamily: 'system-ui, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>HOD Dashboard</h2>
        <div>
          <span style={{ marginRight: 12 }}>{user?.email}</span>
          <button onClick={logout}>Logout</button>
        </div>
      </div>

      <div style={{ display: 'grid', gap: 16, marginTop: 16, gridTemplateColumns: '1fr 1fr' }}>
        <div style={{ padding: 12, border: '1px solid #ddd', borderRadius: 8 }}>
          <h3 style={{ marginTop: 0 }}>Workload generation</h3>
          <div style={{ display: 'grid', gap: 8 }}>
            <input
              value={form.departmentId}
              onChange={(e) => setForm((s) => ({ ...s, departmentId: e.target.value }))}
              placeholder="Department ID"
            />
            <input
              type="number"
              min={1}
              value={form.yearOfStudy}
              onChange={(e) => setForm((s) => ({ ...s, yearOfStudy: e.target.value }))}
              placeholder="Year of study"
            />
            <input value={form.section} onChange={(e) => setForm((s) => ({ ...s, section: e.target.value }))} placeholder="Section" />
            <div style={{ display: 'flex', gap: 8 }}>
              <button onClick={loadActive}>Load active timetable</button>
              <button onClick={generate}>Generate next week</button>
            </div>
          </div>
          {status ? <p style={{ marginTop: 10 }}>{status}</p> : null}
          {activeTimetable ? (
            <div style={{ marginTop: 10, fontSize: 13 }}>
              <div>
                Active timetable: v{activeTimetable.versionNo}, semester {activeTimetable.semester}
              </div>
              <div>Entries: {activeTimetable.entries?.length ?? 0}</div>
            </div>
          ) : null}
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

