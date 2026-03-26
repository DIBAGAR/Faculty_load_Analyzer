import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import {
    AppBar, Toolbar, Typography, IconButton, Drawer, List, ListItem, ListItemButton, ListItemIcon,
    ListItemText, Box, Avatar, Badge, Menu, MenuItem, Divider
} from '@mui/material';
import {
    Menu as MenuIcon, Dashboard, People, School, Room, CalendarMonth,
    Assignment, NotificationsActive, ExitToApp, AdminPanelSettings, EventNote,
} from '@mui/icons-material';
import { Button } from '@mui/material';
import { ROLES } from '../utils/constants';

const Navbar = ({ notifCount = 0 }) => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [anchorEl, setAnchorEl] = useState(null);

    const handleLogout = () => { logout(); navigate('/login'); };

    const menuItems = React.useMemo(() => {
        if (!user) return [];
        const items = [];
        const r = user.role;

        if (r === ROLES.SUPER_ADMIN) {
            items.push({ text: 'Dashboard', icon: <Dashboard />, path: '/super-admin' });
            items.push({ text: 'Manage Admins', icon: <AdminPanelSettings />, path: '/super-admin/admins' });
        }
        if (r === ROLES.FACULTY_ADMIN || r === ROLES.SUPER_ADMIN) {
            items.push({ text: 'Faculty Management', icon: <People />, path: '/faculty-admin' });
            items.push({ text: 'HOD Approvals', icon: <Assignment />, path: '/faculty-admin/leaves' });
        }
        if (r === ROLES.DEPARTMENT_ADMIN || r === ROLES.SUPER_ADMIN)
            items.push({ text: 'Departments', icon: <School />, path: '/departments' });
        if (r === ROLES.COURSE_ADMIN || r === ROLES.SUPER_ADMIN)
            items.push({ text: 'Courses', icon: <School />, path: '/courses' });
        if (r === ROLES.VENUE_ADMIN || r === ROLES.SUPER_ADMIN)
            items.push({ text: 'Venues', icon: <Room />, path: '/venues' });
        if (r === ROLES.HOD || r === ROLES.TEMP_HOD) {
            items.push({ text: 'HOD Dashboard', icon: <Dashboard />, path: '/hod' });
            items.push({ text: 'Timetables', icon: <CalendarMonth />, path: '/hod/timetables' });
            items.push({ text: 'Work Generation', icon: <Assignment />, path: '/hod/work' });
            items.push({ text: 'Course Mapping', icon: <EventNote />, path: '/hod/mappings' });
            items.push({ text: 'Faculty Attendance', icon: <People />, path: '/hod/attendance' });
            items.push({ text: 'Work View', icon: <Assignment />, path: '/hod/work-view' });
        }
        if ([ROLES.FACULTY, ROLES.HOD, ROLES.TEMP_HOD].includes(r)) {
            items.push({ text: 'My Dashboard', icon: <Dashboard />, path: '/faculty' });
            items.push({ text: 'Leave', icon: <EventNote />, path: '/faculty/leave' });
            items.push({ text: 'Dept Timetable', icon: <CalendarMonth />, path: '/faculty/timetable' });
        }
        items.push({ text: 'Notifications', icon: <NotificationsActive />, path: '/notifications' });
        return items;
    }, [user]);

    return (
        <>
            <AppBar position="fixed" sx={{
                background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)',
                boxShadow: '0 4px 20px rgba(0,0,0,0.3)'
            }}>
                <Toolbar sx={{ color: '#ffffff' }}>
                    <IconButton color="inherit" onClick={() => setDrawerOpen(true)} sx={{ mr: 2, color: '#ffffff' }}>
                        <MenuIcon />
                    </IconButton>
                    <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 700, letterSpacing: 1, color: '#ffffff' }}>
                        Faculty Load Analyzer
                    </Typography>

                    {(user?.role === ROLES.FACULTY_ADMIN || user?.role === ROLES.SUPER_ADMIN) && (
                        <Button color="inherit" onClick={() => navigate('/faculty-admin/leaves')} sx={{ fontWeight: 700, mr: 2, display: { xs: 'none', sm: 'block' } }}>
                            HOD Approvals
                        </Button>
                    )}
                    <IconButton color="inherit" onClick={() => navigate('/notifications')} sx={{ color: '#ffffff' }}>
                        <Badge badgeContent={notifCount} color="error">
                            <NotificationsActive />
                        </Badge>
                    </IconButton>
                    <IconButton onClick={(e) => setAnchorEl(e.currentTarget)} sx={{ ml: 1 }}>
                        <Avatar sx={{ bgcolor: '#e94560', width: 34, height: 34, color: '#ffffff' }}>
                            {user?.name?.[0] || 'U'}
                        </Avatar>
                    </IconButton>
                    <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
                        <MenuItem disabled><b>{user?.name}</b></MenuItem>
                        <MenuItem disabled sx={{ fontSize: 12 }}>{user?.role?.replace('_', ' ')}</MenuItem>
                        <Divider />
                        <MenuItem onClick={handleLogout}><ExitToApp sx={{ mr: 1 }} /> Logout</MenuItem>
                    </Menu>
                </Toolbar>
            </AppBar>

            <Drawer open={drawerOpen} onClose={() => setDrawerOpen(false)}>
                <Box sx={{ width: 260, pt: 2 }}>
                    <Typography variant="h6" align="center" sx={{ mb: 2, fontWeight: 700, color: '#1a1a2e' }}>
                        Navigation
                    </Typography>
                    <Divider />
                    <List>
                        {menuItems.map((item, idx) => (
                            <ListItem key={idx} disablePadding>
                                <ListItemButton component={Link} to={item.path}
                                    onClick={() => setDrawerOpen(false)}
                                    sx={{ '&:hover': { bgcolor: '#e8eaf6' } }}>
                                    <ListItemIcon sx={{ color: '#0f3460' }}>{item.icon}</ListItemIcon>
                                    <ListItemText primary={item.text} />
                                </ListItemButton>
                            </ListItem>
                        ))}
                    </List>
                </Box>
            </Drawer>
            <Toolbar /> {/* Spacer */}
        </>
    );
};

export default Navbar;
