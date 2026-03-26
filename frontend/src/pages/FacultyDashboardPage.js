import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import { useAuth } from '../contexts/AuthContext';
import {
    Box, Typography, Card, CardContent, Grid, Chip, Table, TableHead, TableRow,
    TableCell, TableBody, Paper, IconButton, TableContainer
} from '@mui/material';
import { ArrowBackIos, ArrowForwardIos, Timer, Assessment, Business, Person } from '@mui/icons-material';
import { addDays, format, startOfWeek, isSameDay } from 'date-fns';

const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
const HOURS = [1, 2, 3, 4, 5, 6, 7];

const FacultyDashboardPage = () => {
    const { user } = useAuth();
    const [data, setData] = useState(null);
    const [weekOffset, setWeekOffset] = useState(0);

    useEffect(() => { fetchData(); }, []);

    const fetchData = async () => {
        try {
            const res = await api.get('/faculty/dashboard');
            setData(res.data);
            if (res.data?.faculty?.departmentId) localStorage.setItem('deptId', res.data.faculty.departmentId);
        } catch (e) { console.error(e); }
    };

    const generateWeekGrid = () => {
        const today = new Date();
        const startOfThisWeek = startOfWeek(today, { weekStartsOn: 1 });
        const weekStart = addDays(startOfThisWeek, weekOffset * 7);

        const weekDates = DAYS.map((_, i) => addDays(weekStart, i));

        return (
            <TableContainer component={Paper} elevation={0} sx={{ borderRadius: 3, overflow: 'auto', maxHeight: '75vh', display: 'flex', flexDirection: 'column', border: '1px solid #e2e8f0', boxShadow: '0 4px 20px rgba(0,0,0,0.05)' }}>
                <Box sx={{ bgcolor: '#ffffff', color: '#0f3460', p: 2, textAlign: 'center', position: 'sticky', top: 0, zIndex: 10, borderBottom: '3px solid #0f3460' }}>
                    <Typography variant="h6" sx={{ fontWeight: 900, letterSpacing: 1, color: '#0f3460' }}>
                        {format(weekDates[0], 'MMMM dd')} — {format(weekDates[5], 'MMMM dd, yyyy')}
                    </Typography>
                </Box>
                <Box sx={{ overflowX: 'auto', p: { xs: 0.5, md: 3 }, flexGrow: 1 }}>
                    <Table size="small" sx={{ minWidth: 1000 }}>
                        <TableHead>
                            <TableRow>
                                <TableCell sx={{ fontWeight: 800, p: { xs: 1, md: 2 }, minWidth: 80, fontSize: { xs: '0.8rem', md: '1rem' } }}>Day</TableCell>
                                {HOURS.map(h => <TableCell key={h} align="center" sx={{ fontWeight: 800, p: { xs: 1, md: 2 }, minWidth: 140, fontSize: { xs: '0.8rem', md: '1rem' } }}>Hour {h}</TableCell>)}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {DAYS.map((dayName, dIdx) => {
                                const currentDate = weekDates[dIdx];
                                return (
                                    <TableRow key={dayName}>
                                        <TableCell sx={{ fontWeight: 700, p: { xs: 1, md: 2 }, fontSize: { xs: '0.75rem', md: '1rem' }, borderRight: '1px solid rgba(0,0,0,0.05)' }}>{format(currentDate, 'EEEE')}</TableCell>
                                        {HOURS.map(hour => {
                                            const assignment = data?.workHistory?.find(w =>
                                                w.hour === hour && isSameDay(new Date(w.assignDate), currentDate)
                                            );
                                            return (
                                                <TableCell key={hour} align="center" sx={{ p: 0.5, border: '1px solid rgba(0,0,0,0.05)' }}>
                                                    {assignment ? (
                                                        <Box sx={{
                                                            bgcolor: assignment.reassigned ? '#fef3c7' : '#e0f2fe',
                                                            p: 1, borderRadius: 2, border: `2px solid ${assignment.reassigned ? '#f59e0b' : '#38bdf8'}`,
                                                            color: '#0f172a', display: 'flex', flexDirection: 'column', gap: 0.5,
                                                            minHeight: { xs: '80px', md: '100px' }, justifyContent: 'center', transition: 'transform 0.2s', '&:hover': { transform: 'scale(1.02)' }
                                                        }}>
                                                            <Typography sx={{ fontWeight: 800, fontSize: { xs: '0.75rem', md: '0.9rem' }, lineHeight: 1.2 }}>
                                                                {assignment.timetableSlot?.course?.courseCode || 'Work'}
                                                            </Typography>
                                                            <Typography sx={{ fontSize: { xs: '0.65rem', md: '0.75rem' }, lineHeight: 1.1, color: '#334155', fontWeight: 600 }}>
                                                                {assignment.timetableSlot?.course?.courseName || ''}
                                                            </Typography>
                                                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 'auto', pt: 0.5 }}>
                                                                <Chip label={assignment.venue?.venueName || '-'} size="small" sx={{ height: 18, fontSize: '0.6rem', fontWeight: 600, bgcolor: 'rgba(255,255,255,0.7)' }} />
                                                                {assignment.timetableSlot?.slotType && (
                                                                    <Chip label={assignment.timetableSlot.slotType === 'LAB' ? 'Lab' : 'Theory'}
                                                                        color={assignment.timetableSlot.slotType === 'LAB' ? 'error' : 'primary'}
                                                                        variant="outlined" size="small" sx={{ height: 18, fontSize: '0.6rem', fontWeight: 700, bgcolor: '#fff' }} />
                                                                )}
                                                            </Box>
                                                        </Box>
                                                    ) : (
                                                        <Box sx={{ bgcolor: '#f8fafc', p: 1, borderRadius: 2, fontSize: { xs: '0.7rem', md: '0.95rem' }, color: '#94a3b8', minHeight: { xs: '80px', md: '100px' }, display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700 }}>Free</Box>
                                                    )}
                                                </TableCell>
                                            );
                                        })}
                                    </TableRow>
                                );
                            })}
                        </TableBody>
                    </Table>
                </Box>
            </TableContainer>
        );
    };

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 4 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 } }}>
                <Typography variant="h4" sx={{ fontWeight: 800, mb: 3, color: '#0f3460' }}>My Dashboard</Typography>

                {data?.faculty && (
                    <Grid container spacing={3} sx={{ mb: 4 }}>
                        {[
                            { label: 'My Monthly Hours', value: data.faculty.currentMonthHours || 0, icon: <Timer />, color: '#0f3460' },
                            { label: 'Dept Average', value: data.deptDashboard?.averageHours?.toFixed(1) || 0, icon: <Assessment />, color: '#e94560' },
                            { label: 'Attendance', value: data.attendance?.percentage != null ? `${data.attendance.percentage}%` : 'N/A', icon: <Assessment />, color: '#f59e0b', isText: true },
                            { label: 'Department', value: data.faculty.departmentName, icon: <Business />, color: '#533483', isText: true },
                            { label: 'Role', value: data.faculty.role?.replace('_', ' '), icon: <Person />, color: '#16c79a', isText: true }
                        ].map((stat, i) => (
                            <Grid item xs={12} sm={6} md={4} key={i}>
                                <Card sx={{
                                    borderRadius: 4, bgcolor: '#ffffff', border: '1px solid #e2e8f0',
                                    borderLeft: `6px solid ${stat.color}`, height: '100%',
                                    boxShadow: '0 4px 12px rgba(0,0,0,0.03)', transition: 'transform 0.2s',
                                    '&:hover': { transform: 'translateY(-3px)', boxShadow: '0 8px 24px rgba(0,0,0,0.08)' }
                                }}>
                                    <CardContent sx={{ p: 3, display: 'flex', alignItems: 'center', gap: 2.5 }}>
                                        <Box sx={{ color: stat.color, display: 'flex', bgcolor: `${stat.color}15`, p: 1.5, borderRadius: 3 }}>
                                            {React.cloneElement(stat.icon, { sx: { fontSize: 32 } })}
                                        </Box>
                                        <Box>
                                            <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 700, textTransform: 'uppercase', letterSpacing: 1, display: 'block', mb: 0.5 }}>
                                                {stat.label}
                                            </Typography>
                                            <Typography variant={stat.isText ? "body1" : "h3"} sx={{ color: '#1e293b', fontWeight: 900, lineHeight: 1.1 }}>
                                                {stat.value}
                                            </Typography>
                                        </Box>
                                    </CardContent>
                                </Card>
                            </Grid>
                        ))}
                    </Grid>
                )}


                {/* Work History Grids */}
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2, flexWrap: 'wrap', gap: 2 }}>
                    <Typography variant="h5" sx={{ fontWeight: 800, color: '#0f3460' }}>Weekly Schedule Mapping</Typography>
                    <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                        <IconButton onClick={() => setWeekOffset(prev => Math.max(-5, prev - 1))} disabled={weekOffset <= -5} sx={{ bgcolor: 'rgba(0,0,0,0.04)' }}>
                            <ArrowBackIos fontSize="small" sx={{ ml: 1 }} />
                        </IconButton>
                        <Chip label={weekOffset === 0 ? "Current Week" : weekOffset < 0 ? `${Math.abs(weekOffset)} Weeks Ago` : `${weekOffset} Weeks Ahead`} sx={{ fontWeight: 700, bgcolor: '#0f3460', color: '#fff', fontSize: '1rem', py: 2.5 }} />
                        <IconButton onClick={() => setWeekOffset(prev => Math.min(5, prev + 1))} disabled={weekOffset >= 5} sx={{ bgcolor: 'rgba(0,0,0,0.04)' }}>
                            <ArrowForwardIos fontSize="small" />
                        </IconButton>
                    </Box>
                </Box>

                <Box sx={{ pb: 4 }}>
                    {generateWeekGrid()}
                </Box>
            </Box>
        </Box>
    );
};

export default FacultyDashboardPage;
