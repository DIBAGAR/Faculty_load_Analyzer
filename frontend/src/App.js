import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import SuperAdminPage from './pages/SuperAdminPage';
import FacultyAdminPage from './pages/FacultyAdminPage';
import DepartmentPage from './pages/DepartmentPage';
import CoursePage from './pages/CoursePage';
import VenuePage from './pages/VenuePage';
import HodDashboardPage from './pages/HodDashboardPage';
import TimetablePage from './pages/TimetablePage';
import WorkGenerationPage from './pages/WorkGenerationPage';
import CourseMappingPage from './pages/CourseMappingPage';
import FacultyDashboardPage from './pages/FacultyDashboardPage';
import LeavePage from './pages/LeavePage';
import HodLeaveApprovalPage from './pages/HodLeaveApprovalPage';
import HodAttendancePage from './pages/HodAttendancePage';
import DeptTimetablePage from './pages/DeptTimetablePage';
import HodWorkViewPage from './pages/HodWorkViewPage';
import NotificationsPage from './pages/NotificationsPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import IdleTimeout from './components/IdleTimeout';
import { ROLES } from './utils/constants';
import { createTheme, ThemeProvider, CssBaseline } from '@mui/material';
import '@fontsource/inter/400.css';
import '@fontsource/inter/700.css';

const theme = createTheme({
  typography: {
    fontFamily: 'Inter, Roboto, sans-serif',
    h1: { color: '#0f3460', fontWeight: 800 },
    h2: { color: '#0f3460', fontWeight: 800 },
    h3: { color: '#0f3460', fontWeight: 800 },
    h4: { color: '#0f3460', fontWeight: 800, letterSpacing: '-0.5px' },
    h5: { color: '#0f3460', fontWeight: 700, letterSpacing: '-0.5px' },
    h6: { color: '#0f3460', fontWeight: 700 },
  },
  palette: {
    primary: { main: '#0f3460' },
    secondary: { main: '#e94560' },
    background: { default: '#f4f7fe' },
    text: { primary: '#0f172a', secondary: '#475569' }
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: { textTransform: 'none', borderRadius: 10, fontWeight: 700, padding: '8px 20px', boxShadow: 'none', '&:hover': { boxShadow: '0 4px 15px rgba(0,0,0,0.1)' } }
      }
    },
    MuiCard: {
      styleOverrides: {
        root: { borderRadius: 16, boxShadow: '0 10px 30px rgba(0,0,0,0.05)', border: '1px solid rgba(0,0,0,0.02)' }
      }
    },
    MuiPaper: {
      styleOverrides: {
        root: { borderRadius: 16, boxShadow: '0 4px 20px rgba(0,0,0,0.03)' }
      }
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-root': {
            backgroundColor: '#0f3460',
            color: '#ffffff',
            fontWeight: 600,
            textTransform: 'uppercase',
            fontSize: '0.8rem',
            letterSpacing: '0.5px'
          }
        }
      }
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:nth-of-type(even)': { backgroundColor: '#fafbfd' },
          '&:hover': { backgroundColor: '#f0f4f8', transition: 'background-color 0.2s' }
        }
      }
    },
    MuiTableCell: {
      styleOverrides: {
        root: { borderBottom: '1px solid #edf2f7', padding: '16px' }
      }
    }
  }
});

const AppRoutes = () => {
  const { user, loading } = useAuth();
  if (loading) return null;

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route path="/" element={user ? <Navigate to={getHome(user.role)} replace /> : <Navigate to="/login" replace />} />

      <Route path="/super-admin" element={<ProtectedRoute allowedRoles={[ROLES.SUPER_ADMIN]}><SuperAdminPage /></ProtectedRoute>} />
      <Route path="/super-admin/admins" element={<ProtectedRoute allowedRoles={[ROLES.SUPER_ADMIN]}><SuperAdminPage /></ProtectedRoute>} />
      <Route path="/faculty-admin" element={<ProtectedRoute allowedRoles={[ROLES.FACULTY_ADMIN, ROLES.SUPER_ADMIN]}><FacultyAdminPage /></ProtectedRoute>} />
      <Route path="/faculty-admin/leaves" element={<ProtectedRoute allowedRoles={[ROLES.FACULTY_ADMIN, ROLES.SUPER_ADMIN]}><HodLeaveApprovalPage /></ProtectedRoute>} />

      <Route path="/departments" element={<ProtectedRoute allowedRoles={[ROLES.DEPARTMENT_ADMIN, ROLES.SUPER_ADMIN]}><DepartmentPage /></ProtectedRoute>} />
      <Route path="/courses" element={<ProtectedRoute allowedRoles={[ROLES.COURSE_ADMIN, ROLES.SUPER_ADMIN]}><CoursePage /></ProtectedRoute>} />
      <Route path="/venues" element={<ProtectedRoute allowedRoles={[ROLES.VENUE_ADMIN, ROLES.SUPER_ADMIN]}><VenuePage /></ProtectedRoute>} />
      <Route path="/hod" element={<ProtectedRoute allowedRoles={[ROLES.HOD, ROLES.TEMP_HOD, ROLES.SUPER_ADMIN]}><HodDashboardPage /></ProtectedRoute>} />
      <Route path="/hod/timetables" element={<ProtectedRoute allowedRoles={[ROLES.HOD, ROLES.TEMP_HOD, ROLES.SUPER_ADMIN]}><TimetablePage /></ProtectedRoute>} />
      <Route path="/hod/work" element={<ProtectedRoute allowedRoles={[ROLES.HOD, ROLES.TEMP_HOD, ROLES.SUPER_ADMIN]}><WorkGenerationPage /></ProtectedRoute>} />
      <Route path="/hod/mappings" element={<ProtectedRoute allowedRoles={[ROLES.HOD, ROLES.TEMP_HOD, ROLES.SUPER_ADMIN]}><CourseMappingPage /></ProtectedRoute>} />
      <Route path="/hod/attendance" element={<ProtectedRoute allowedRoles={[ROLES.HOD, ROLES.TEMP_HOD, ROLES.SUPER_ADMIN]}><HodAttendancePage /></ProtectedRoute>} />
      <Route path="/hod/work-view" element={<ProtectedRoute allowedRoles={[ROLES.HOD, ROLES.TEMP_HOD, ROLES.SUPER_ADMIN]}><HodWorkViewPage /></ProtectedRoute>} />
      <Route path="/hod/performance" element={<ProtectedRoute allowedRoles={[ROLES.HOD, ROLES.TEMP_HOD, ROLES.SUPER_ADMIN]}><HodDashboardPage /></ProtectedRoute>} />
      <Route path="/faculty" element={<ProtectedRoute allowedRoles={[ROLES.FACULTY, ROLES.HOD, ROLES.TEMP_HOD, ROLES.SUPER_ADMIN]}><FacultyDashboardPage /></ProtectedRoute>} />
      <Route path="/faculty/leave" element={<ProtectedRoute allowedRoles={[ROLES.FACULTY, ROLES.HOD, ROLES.TEMP_HOD, ROLES.FACULTY_ADMIN, ROLES.SUPER_ADMIN]}><LeavePage /></ProtectedRoute>} />
      <Route path="/faculty/timetable" element={<ProtectedRoute allowedRoles={[ROLES.FACULTY, ROLES.HOD, ROLES.TEMP_HOD, ROLES.SUPER_ADMIN]}><DeptTimetablePage /></ProtectedRoute>} />
      <Route path="/notifications" element={<ProtectedRoute><NotificationsPage /></ProtectedRoute>} />

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

const getHome = (role) => {
  switch (role) {
    case ROLES.SUPER_ADMIN: return '/super-admin';
    case ROLES.FACULTY_ADMIN: return '/faculty-admin';
    case ROLES.DEPARTMENT_ADMIN: return '/departments';
    case ROLES.COURSE_ADMIN: return '/courses';
    case ROLES.VENUE_ADMIN: return '/venues';
    case ROLES.HOD: case ROLES.TEMP_HOD: return '/hod';
    case ROLES.FACULTY: return '/faculty';
    default: return '/login';
  }
};

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <AuthProvider>
          <IdleTimeout>
            <AppRoutes />
          </IdleTimeout>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
