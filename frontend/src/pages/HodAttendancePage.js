import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import { useAuth } from '../contexts/AuthContext';
import {
    Box, Typography, Table, TableHead, TableRow, TableCell,
    TableBody, Paper, TableContainer, Chip, CircularProgress, Alert,
    TextField, Divider, Grid
} from '@mui/material';

const statusConfig = {
    PRESENT:         { label: 'Present',        color: 'success' },
    ON_DUTY:         { label: 'On Duty',        color: 'warning' },
    ON_LEAVE:        { label: 'On Leave',       color: 'error' },
    NON_WORKING_DAY: { label: 'Holiday / No Work', color: 'default' },
};

const HodAttendancePage = () => {
    const { user } = useAuth();
    const [stats, setStats] = useState([]);
    const [loadingStats, setLoadingStats] = useState(true);
    const [error, setError] = useState('');

    // Day-wise state
    const [selectedDate, setSelectedDate] = useState('');
    const [dailyData, setDailyData] = useState([]);
    const [loadingDaily, setLoadingDaily] = useState(false);
    const [dailyError, setDailyError] = useState('');

    const rawDeptId = localStorage.getItem('deptId');
    const deptId = user?.deptId || (rawDeptId !== 'undefined' && rawDeptId !== 'null' ? rawDeptId : null);

    useEffect(() => {
        if (deptId) {
            fetchAttendance();
        } else {
            setLoadingStats(false);
            setError('Department ID not found. Please relogin.');
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [deptId]);

    const fetchAttendance = async () => {
        try {
            setLoadingStats(true);
            const res = await api.get(`/hod/attendance/${deptId}`);
            setStats(res.data);
            setError('');
        } catch (err) {
            setError('Failed to load attendance statistics.');
        } finally {
            setLoadingStats(false);
        }
    };

    const fetchDailyAttendance = async (date) => {
        if (!date || !deptId) return;
        try {
            setLoadingDaily(true);
            setDailyError('');
            const res = await api.get(`/hod/attendance/${deptId}/daily?date=${date}`);
            setDailyData(res.data);
        } catch (err) {
            setDailyError('Failed to fetch daily attendance for selected date.');
            setDailyData([]);
        } finally {
            setLoadingDaily(false);
        }
    };

    const handleDateChange = (e) => {
        const val = e.target.value;
        setSelectedDate(val);
        if (val) fetchDailyAttendance(val);
        else setDailyData([]);
    };

    const presentCount  = dailyData.filter(d => d.status === 'PRESENT' || d.status === 'ON_DUTY').length;
    const absentCount   = dailyData.filter(d => d.status === 'ON_LEAVE').length;

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 6 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 } }}>
                <Typography variant="h4" sx={{ fontWeight: 800, mb: 4, color: '#0f3460' }}>
                    Faculty Attendance
                </Typography>

                {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

                {/* ── Overall Stats Table ── */}
                <Typography variant="h6" sx={{ fontWeight: 700, mb: 2, color: '#0f3460' }}>
                    Overall Attendance Summary
                </Typography>

                {loadingStats ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', p: 5 }}>
                        <CircularProgress />
                    </Box>
                ) : (
                    <TableContainer component={Paper} className="glass" sx={{ borderRadius: 3, overflow: 'auto', mb: 5 }}>
                        <Table size="small" sx={{ minWidth: 800 }}>
                            <TableHead sx={{ bgcolor: '#0f3460' }}>
                                <TableRow>
                                    {['Faculty Name', 'Roll No', 'Working Days', 'Present Days', 'Leave Days', 'On Duty Days', 'Attendance %'].map(h => (
                                        <TableCell key={h} sx={{ color: '#fff', fontWeight: 700, py: 1.5, textAlign: h.includes('Days') || h.includes('%') ? 'center' : 'left' }}>
                                            {h}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {stats.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={7} align="center" sx={{ color: '#475569', py: 3 }}>
                                            No faculty data available. Make sure work has been generated and attendance date is set.
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    stats.map(s => (
                                        <TableRow key={s.facultyId} sx={{ '&:hover': { bgcolor: 'rgba(0,0,0,0.04)' } }}>
                                            <TableCell sx={{ color: '#1e293b', fontWeight: 600 }}>{s.facultyName}</TableCell>
                                            <TableCell sx={{ color: '#475569' }}>{s.rollNumber}</TableCell>
                                            <TableCell align="center" sx={{ color: '#0f3460', fontWeight: 700 }}>{s.totalWorkingDays}</TableCell>
                                            <TableCell align="center">
                                                <Chip label={s.presentDays} size="small" color="success" sx={{ fontWeight: 700, minWidth: 40 }} />
                                            </TableCell>
                                            <TableCell align="center">
                                                <Chip label={s.leaveDays} size="small" color="error" sx={{ fontWeight: 700, minWidth: 40 }} />
                                            </TableCell>
                                            <TableCell align="center">
                                                <Chip label={s.onDutyDays} size="small" color="warning" sx={{ fontWeight: 700, minWidth: 40 }} />
                                            </TableCell>
                                            <TableCell align="center">
                                                <Typography sx={{
                                                    fontWeight: 800,
                                                    color: s.percentage >= 75 ? '#16c79a' : s.percentage >= 50 ? '#f59e0b' : '#e94560'
                                                }}>
                                                    {s.percentage}%
                                                </Typography>
                                            </TableCell>
                                        </TableRow>
                                    ))
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )}

                <Divider sx={{ mb: 4 }} />

                {/* ── Day-wise Attendance ── */}
                <Typography variant="h6" sx={{ fontWeight: 700, mb: 2, color: '#0f3460' }}>
                    Day-wise Attendance Check
                </Typography>

                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3, flexWrap: 'wrap' }}>
                    <TextField
                        label="Select Date"
                        type="date"
                        value={selectedDate}
                        onChange={handleDateChange}
                        InputLabelProps={{ shrink: true }}
                        size="small"
                        sx={{ minWidth: 200 }}
                    />
                    {dailyData.length > 0 && (
                        <Box sx={{ display: 'flex', gap: 2 }}>
                            <Chip label={`✅ Present / On Duty: ${presentCount}`} color="success" sx={{ fontWeight: 700, px: 1 }} />
                            <Chip label={`❌ On Leave: ${absentCount}`} color="error" sx={{ fontWeight: 700, px: 1 }} />
                        </Box>
                    )}
                </Box>

                {dailyError && <Alert severity="warning" sx={{ mb: 2 }}>{dailyError}</Alert>}

                {loadingDaily ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                        <CircularProgress size={32} />
                    </Box>
                ) : selectedDate && dailyData.length > 0 ? (
                    <Grid container spacing={2}>
                        {dailyData.map(d => {
                            const cfg = statusConfig[d.status] || statusConfig.PRESENT;
                            return (
                                <Grid item xs={12} sm={6} md={4} lg={3} key={d.facultyId}>
                                    <Paper
                                        className="glass"
                                        sx={{
                                            p: 2, borderRadius: 3,
                                            borderLeft: `5px solid ${
                                                d.status === 'PRESENT' ? '#16c79a' :
                                                d.status === 'ON_DUTY' ? '#f59e0b' :
                                                d.status === 'ON_LEAVE' ? '#e94560' : '#94a3b8'
                                            }`,
                                            transition: 'transform 0.2s',
                                            '&:hover': { transform: 'translateY(-2px)', boxShadow: '0 8px 20px rgba(0,0,0,0.08)' }
                                        }}
                                    >
                                        <Typography sx={{ fontWeight: 700, color: '#1e293b', fontSize: '0.95rem' }}>{d.facultyName}</Typography>
                                        <Typography sx={{ color: '#64748b', fontSize: '0.8rem', mb: 1 }}>Roll: {d.rollNumber}</Typography>
                                        <Chip label={cfg.label} color={cfg.color} size="small" sx={{ fontWeight: 700 }} />
                                    </Paper>
                                </Grid>
                            );
                        })}
                    </Grid>
                ) : selectedDate ? (
                    <Alert severity="info">No faculty data found for this date.</Alert>
                ) : (
                    <Alert severity="info" sx={{ bgcolor: 'rgba(15,52,96,0.05)', color: '#0f3460' }}>
                        Select a date above to see which faculty were present or absent that day.
                    </Alert>
                )}
            </Box>
        </Box>
    );
};

export default HodAttendancePage;
