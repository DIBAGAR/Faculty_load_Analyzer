import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import { useAuth } from '../contexts/AuthContext';
import {
    Box, Typography, Card, CardContent, Grid, Chip, Table, TableHead, TableRow,
    TableCell, TableBody, Paper, MenuItem, Select, FormControl, InputLabel, TextField,
    TableContainer
} from '@mui/material';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js';
import { Bar } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const todayStr = new Date().toISOString().split('T')[0]; // YYYY-MM-DD

const HodDashboardPage = () => {
    const { user } = useAuth();
    const [dashboard, setDashboard] = useState(null);
    const [performance, setPerformance] = useState(null);
    const [attendanceMap, setAttendanceMap] = useState({});   // facultyId -> percentage
    const [todayStatusMap, setTodayStatusMap] = useState({}); // facultyId -> status
    const [filterType, setFilterType] = useState('all');
    const [nameFilter, setNameFilter] = useState('');
    const rawDeptId = localStorage.getItem('deptId');
    const deptId = user?.deptId || (rawDeptId !== 'undefined' && rawDeptId !== 'null' ? rawDeptId : null);

    useEffect(() => {
        const fetchDeptId = async () => {
            try {
                const res = await api.get('/faculty/dashboard');
                const did = res.data?.faculty?.departmentId;
                if (did) {
                    localStorage.setItem('deptId', did);
                    fetchData(did);
                }
            } catch (e) { console.error(e); }
        };
        if (deptId) fetchData(deptId);
        else fetchDeptId();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const fetchData = async (did) => {
        try {
            const [d, p] = await Promise.all([
                api.get(`/hod/dashboard/${did}`),
                api.get(`/hod/performance/${did}`)
            ]);
            setDashboard(d.data);
            setPerformance(p.data);
        } catch (e) { console.error(e); }

        // Fetch attendance data separately (non-blocking)
        try {
            const [attRes, dailyRes] = await Promise.all([
                api.get(`/hod/attendance/${did}`),
                api.get(`/hod/attendance/${did}/daily?date=${todayStr}`)
            ]);
            const aMap = {};
            (attRes.data || []).forEach(s => { aMap[s.facultyId] = s.percentage; });
            setAttendanceMap(aMap);

            const tMap = {};
            (dailyRes.data || []).forEach(s => { tMap[s.facultyId] = s.status; });
            setTodayStatusMap(tMap);
        } catch (e) { console.warn('Attendance fetch skipped:', e.message); }
    };

    const filteredFaculty = dashboard?.faculty?.filter(f => {
        if (nameFilter && !f.name.toLowerCase().includes(nameFilter.toLowerCase()) && !f.rollNumber?.toLowerCase().includes(nameFilter.toLowerCase())) return false;
        if (filterType === 'below' && f.monthlyHours >= (dashboard?.averageHours || 0)) return false;
        if (filterType === 'above' && f.monthlyHours < (dashboard?.averageHours || 0)) return false;
        return true;
    }) || [];

    const chartData = performance ? {
        labels: performance.chartData?.map(d => d.name) || [],
        datasets: [{
            label: 'Monthly Hours',
            data: performance.chartData?.map(d => d.hours) || [],
            backgroundColor: performance.chartData?.map(d =>
                d.hours < (performance.rangeThreshold || 0) ? 'rgba(233,69,96,0.7)' : 'rgba(22,199,154,0.7)'
            ) || [],
            borderRadius: 6,
        }]
    } : null;

    const statusChip = (facultyId) => {
        const s = todayStatusMap[facultyId];
        if (!s || s === 'NON_WORKING_DAY') return <Chip label="Holiday" size="small" sx={{ fontWeight: 600, height: 20, fontSize: '0.65rem' }} />;
        if (s === 'PRESENT')  return <Chip label="Present" size="small" color="success" sx={{ fontWeight: 700, height: 20, fontSize: '0.65rem' }} />;
        if (s === 'ON_DUTY')  return <Chip label="On Duty" size="small" color="warning" sx={{ fontWeight: 700, height: 20, fontSize: '0.65rem' }} />;
        if (s === 'ON_LEAVE') return <Chip label="On Leave" size="small" color="error"   sx={{ fontWeight: 700, height: 20, fontSize: '0.65rem' }} />;
        return <Chip label="Unknown" size="small" sx={{ fontWeight: 600, height: 20, fontSize: '0.65rem' }} />;
    };

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 4 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 } }}>
                <Typography variant="h4" sx={{ fontWeight: 800, mb: 3, color: '#0f3460' }}>HOD Dashboard</Typography>

                {dashboard && (
                    <Grid container spacing={3} sx={{ mb: 4 }}>
                        {[
                            { label: 'Total Faculty', value: dashboard.faculty?.length || 0, color: '#0f3460' },
                            { label: 'Avg Monthly Hours', value: dashboard.averageHours?.toFixed(1) || 0, color: '#e94560' },
                            { label: 'Below Range', value: performance?.belowRangeCount || 0, color: '#533483' },
                            { label: 'Performance', value: `${performance?.performancePercent?.toFixed(1) || 0}%`, color: '#16c79a' }
                        ].map((stat, i) => (
                            <Grid item xs={12} sm={6} md={3} key={i}>
                                <Card sx={{
                                    borderRadius: 4, bgcolor: '#ffffff', border: '1px solid #e2e8f0',
                                    borderLeft: `6px solid ${stat.color}`, height: '100%',
                                    boxShadow: '0 4px 12px rgba(0,0,0,0.03)', transition: 'transform 0.2s',
                                    '&:hover': { transform: 'translateY(-3px)', boxShadow: '0 8px 24px rgba(0,0,0,0.08)' }
                                }}>
                                    <CardContent sx={{ p: 3 }}>
                                        <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 700, textTransform: 'uppercase', letterSpacing: 1, display: 'block', mb: 0.5 }}>
                                            {stat.label}
                                        </Typography>
                                        <Typography variant="h3" sx={{ color: '#1e293b', fontWeight: 900, lineHeight: 1.1 }}>
                                            {stat.value}
                                        </Typography>
                                    </CardContent>
                                </Card>
                            </Grid>
                        ))}
                    </Grid>
                )}

                {/* Performance Chart */}
                {chartData && (
                    <Paper className="glass" sx={{ p: 3, borderRadius: 3, mb: 4 }}>
                        <Typography variant="h6" sx={{ fontWeight: 800, mb: 2, color: '#0f3460' }}>Faculty Workload Distribution</Typography>
                        <Box sx={{ height: { xs: 300, md: 400 } }}>
                            <Bar data={chartData} options={{
                                responsive: true, maintainAspectRatio: false,
                                indexAxis: window.innerWidth < 768 ? 'y' : 'x',
                                plugins: { legend: { display: false }, tooltip: { callbacks: { label: (c) => `${c.raw} hours` } } },
                                scales: { y: { beginAtZero: true } }
                            }} />
                        </Box>
                    </Paper>
                )}

                {/* Faculty Table */}
                <Box sx={{ display: 'flex', gap: 2, mb: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                    <TextField size="small" label="Search Name/Roll" value={nameFilter} onChange={e => setNameFilter(e.target.value)} />
                    <FormControl size="small" sx={{ minWidth: 150 }}>
                        <InputLabel>Filter</InputLabel>
                        <Select value={filterType} label="Filter" onChange={e => setFilterType(e.target.value)}>
                            <MenuItem value="all">All</MenuItem>
                            <MenuItem value="below">Below Average</MenuItem>
                            <MenuItem value="above">Above Average</MenuItem>
                        </Select>
                    </FormControl>
                    <Typography variant="caption" sx={{ color: '#64748b', fontStyle: 'italic' }}>
                        Showing today ({todayStr}) status
                    </Typography>
                </Box>

                <TableContainer component={Paper} className="glass" sx={{ borderRadius: 3, overflow: 'auto' }}>
                    <Table size="small" sx={{ minWidth: 700 }}>
                        <TableHead sx={{ bgcolor: '#0f3460' }}>
                            <TableRow>
                                {['Name', 'Roll No', 'Monthly Hours', 'Attendance %', 'Role', "Today's Status"].map(h =>
                                    <TableCell key={h} sx={{ color: '#ffffff', fontWeight: 700, py: 1.5 }}>{h}</TableCell>
                                )}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {filteredFaculty.length === 0 ? (
                                <TableRow><TableCell colSpan={6} align="center" sx={{ color: '#475569', py: 3 }}>No faculty found.</TableCell></TableRow>
                            ) : (
                                filteredFaculty.map(f => (
                                    <TableRow key={f.id} sx={{ '&:hover': { bgcolor: 'rgba(0,0,0,0.04)' }, bgcolor: f.monthlyHours < (dashboard?.averageHours || 0) ? 'rgba(233,69,96,0.05)' : 'inherit' }}>
                                        <TableCell sx={{ color: '#1e293b', fontWeight: 600, fontSize: { xs: '0.75rem', md: '0.875rem' } }}>{f.name}</TableCell>
                                        <TableCell sx={{ color: '#475569', fontSize: { xs: '0.75rem', md: '0.875rem' } }}>{f.rollNumber}</TableCell>
                                        <TableCell>
                                            <Chip label={`${f.monthlyHours} hrs`} size="small" sx={{ fontWeight: 600, height: 20, fontSize: '0.65rem' }}
                                                color={f.monthlyHours < (dashboard?.averageHours || 0) ? 'error' : 'success'} />
                                        </TableCell>
                                        <TableCell>
                                            {attendanceMap[f.id] !== undefined
                                                ? <Chip label={`${attendanceMap[f.id]}%`} size="small" sx={{ fontWeight: 700, height: 20, fontSize: '0.65rem', bgcolor: attendanceMap[f.id] >= 75 ? '#dcfce7' : attendanceMap[f.id] >= 50 ? '#fef3c7' : '#fee2e2', color: attendanceMap[f.id] >= 75 ? '#166534' : attendanceMap[f.id] >= 50 ? '#b45309' : '#991b1b' }} />
                                                : <Typography sx={{ color: '#94a3b8', fontSize: '0.75rem' }}>—</Typography>
                                            }
                                        </TableCell>
                                        <TableCell>
                                            <Chip label={f.role?.replace('_', ' ')} size="small" sx={{ fontWeight: 600, height: 20, fontSize: '0.65rem' }} />
                                        </TableCell>
                                        <TableCell>{statusChip(f.id)}</TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
            </Box>
        </Box>
    );
};

export default HodDashboardPage;
