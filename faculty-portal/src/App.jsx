import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './pages/Login';
import SuperAdminPage from './pages/SuperAdminPage';
import FacultyAdminPage from './pages/FacultyAdminPage';
import CourseAdminPage from './pages/CourseAdminPage';
import VenueAdminPage from './pages/VenueAdminPage';
import DepartmentAdminPage from './pages/DepartmentAdminPage';
import HodDashboard from './pages/HodDashboard';
import FacultyDashboard from './pages/FacultyDashboard';

const ProtectedRoute = ({ children, role }) => {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (role && user.role !== role) return <Navigate to="/login" replace />;
  return children;
};

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />

        <Route
          path="/super-admin"
          element={
            <ProtectedRoute role="SUPER_ADMIN">
              <SuperAdminPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/faculty"
          element={
            <ProtectedRoute role="FACULTY_ADMIN">
              <FacultyAdminPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/course"
          element={
            <ProtectedRoute role="COURSE_ADMIN">
              <CourseAdminPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/venue"
          element={
            <ProtectedRoute role="VENUE_ADMIN">
              <VenueAdminPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/department"
          element={
            <ProtectedRoute role="DEPARTMENT_ADMIN">
              <DepartmentAdminPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/hod-dashboard"
          element={
            <ProtectedRoute role="HOD">
              <HodDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/faculty-dashboard"
          element={
            <ProtectedRoute role="FACULTY">
              <FacultyDashboard />
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    </AuthProvider>
  );
}

