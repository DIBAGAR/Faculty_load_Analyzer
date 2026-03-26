import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import {
    Box, Typography, Paper, Grid, Select, MenuItem, FormControl, InputLabel,
    Table, TableHead, TableRow, TableCell, TableBody, TableContainer,
    Chip, CircularProgress, ToggleButtonGroup, ToggleButton, Card, CardContent,
    IconButton, Alert
} from '@mui/material';
import { CalendarMonth, Person, ArrowBackIos, ArrowForwardIos } from '@mui/icons-material';
import { startOfWeek, addDays, format, isSameDay } from 'date-fns';

const HOURS = [1, 2, 3, 4, 5, 6, 7];
const DAYS_LABELS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

const typeStyle = (type) => type === 'LAB'
    ? { bg: 'rgba(233,69,96,0.09)', border: '#e94560', chip: '#e94560' }
    : { bg: 'rgba(15,52,96,0.07)', border: '#0f3460', chip: '#0f3460' };

// ── Reusable weekly grid ─────────────────────────────────────────────────────
const WeekGrid = ({ weekDates, assignments, showFaculty = false }) => {
    const getCell = (dayIdx, hour) => {
        const date = weekDates[dayIdx];
        return assignments.filter(a => a.hour === hour && isSameDay(new Date(a.assignDate), date));
    };

    return (
        <TableContainer sx={{ maxHeight: '72vh' }}>
            <Table stickyHeader size="small" sx={{ minWidth: 980 }}>
                <TableHead>
                    <TableRow>
                        {/* Top-left corner */}
                        <TableCell sx={{
                            bgcolor: '#f1f5f9', fontWeight: 800, color: '#0f3460',
                            minWidth: 110, borderRight: '2px solid #cbd5e1', fontSize: '0.8rem'
                        }}>
                            Day / Hour
                        </TableCell>
                        {/* Hour columns */}
                        {HOURS.map(h => (
                            <TableCell key={h} align="center" sx={{
                                bgcolor: '#f1f5f9', fontWeight: 800, color: '#0f3460',
                                minWidth: 145, borderRight: '2px solid #cbd5e1', fontSize: '0.8rem'
                            }}>
                                Hour {h}
                            </TableCell>
                        ))}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {/* Day rows */}
                    {DAYS_LABELS.map((day, dIdx) => (
                        <TableRow key={day}>
                            <TableCell sx={{
                                fontWeight: 800, color: '#0f3460',
                                bgcolor: isSameDay(weekDates[dIdx], new Date()) ? '#eff6ff' : '#f8fafc',
                                borderRight: '2px solid #cbd5e1', borderBottom: '2px solid #e2e8f0',
                                fontSize: '0.78rem'
                            }}>
                                {day}
                                <Typography display="block" variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
                                    {format(weekDates[dIdx], 'dd MMM')}
                                </Typography>
                            </TableCell>
                            {HOURS.map(hour => {
                                const cells = getCell(dIdx, hour);
                                const first = cells[0];
                                const slotType = first?.timetableSlot?.slotType || first?.slotType;
                                const c = first ? typeStyle(slotType) : null;
                                return (
                                    <TableCell key={hour} align="center" sx={{
                                        p: 0.75, borderRight: '2px solid #e2e8f0',
                                        borderBottom: '2px solid #e2e8f0', verticalAlign: 'top',
                                        bgcolor: slotType === 'LAB' ? 'rgba(233,69,96,0.04)' : '#fff'
                                    }}>
                                        {cells.length > 0 ? (
                                            <Box sx={{
                                                border: `2px solid ${c.border}`, bgcolor: c.bg,
                                                borderRadius: 2, p: 0.75, minHeight: 90,
                                                display: 'flex', flexDirection: 'column', gap: 0.4,
                                                alignItems: 'center'
                                            }}>
                                                <Typography sx={{ fontWeight: 900, fontSize: '0.72rem', color: '#0f3460', lineHeight: 1.2 }}>
                                                    {first.course?.courseCode || first.timetableSlot?.course?.courseCode}
                                                </Typography>
                                                <Typography sx={{ fontSize: '0.62rem', color: '#475569', lineHeight: 1.2 }}>
                                                    {first.course?.courseName || first.timetableSlot?.course?.courseName}
                                                </Typography>
                                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.25, justifyContent: 'center' }}>
                                                    <Chip label={slotType || 'THEORY'} size="small" sx={{
                                                        height: 16, fontSize: '0.55rem', fontWeight: 800,
                                                        bgcolor: c.chip, color: '#fff'
                                                    }} />
                                                    {first.venue && (
                                                        <Chip label={first.venue.venueName} size="small" sx={{
                                                            height: 16, fontSize: '0.55rem', fontWeight: 700,
                                                            bgcolor: '#f0fdf4', color: '#166534'
                                                        }} />
                                                    )}
                                                </Box>
                                                {showFaculty && cells.map((a, ai) => (
                                                    <Chip key={ai} label={a.faculty?.name} size="small" sx={{
                                                        height: 18, fontSize: '0.6rem', fontWeight: 700,
                                                        bgcolor: a.reassigned ? '#fef3c7' : '#dbeafe',
                                                        color: a.reassigned ? '#92400e' : '#1e40af',
                                                        maxWidth: '100%'
                                                    }} />
                                                ))}
                                                {!showFaculty && first.reassigned && (
                                                    <Chip label="Reassigned" size="small" color="warning"
                                                        sx={{ height: 16, fontSize: '0.55rem', fontWeight: 800 }} />
                                                )}
                                            </Box>
                                        ) : (
                                            <Box sx={{ color: '#e2e8f0', pt: 2, fontSize: '0.75rem' }}>—</Box>
                                        )}
                                    </TableCell>
                                );
                            })}
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
};

// ── Main Page ────────────────────────────────────────────────────────────────
const HodWorkViewPage = () => {
    const deptId = localStorage.getItem('deptId');
    const [viewMode, setViewMode] = useState('timetable');
    const [weekOffset, setWeekOffset] = useState(0);
    const [timetables, setTimetables] = useState([]);
    const [ttSlots, setTtSlots] = useState([]);   // slots for selected TT (for timetable-view context)
    const [faculty, setFaculty] = useState([]);
    const [selectedTT, setSelectedTT] = useState('');
    const [selectedFaculty, setSelectedFaculty] = useState('');
    const [assignments, setAssignments] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    // Week date range
    const weekDates = (() => {
        const start = startOfWeek(addDays(new Date(), weekOffset * 7), { weekStartsOn: 1 });
        return DAYS_LABELS.map((_, i) => addDays(start, i));
    })();
    const startStr = format(weekDates[0], 'yyyy-MM-dd');
    const endStr   = format(weekDates[5], 'yyyy-MM-dd');

    // Load timetable list and faculty list once
    useEffect(() => {
        if (!deptId) return;
        Promise.all([
            api.get('/faculty/dept-timetables'),
            api.get(`/faculty/dept/${deptId}`)
        ]).then(([ttRes, fRes]) => {
            setTimetables(ttRes.data || []);
            setFaculty(fRes.data || []);
            if (ttRes.data?.length > 0) setSelectedTT(ttRes.data[0].id);
        }).catch(console.error);
    }, [deptId]);

    // Load timetable slots whenever selected timetable changes (for context labels)
    useEffect(() => {
        if (!selectedTT) return;
        api.get(`/faculty/timetables/${selectedTT}/slots`)
            .then(r => setTtSlots(r.data || []))
            .catch(console.error);
    }, [selectedTT]);

    // Load work assignments for the week
    const fetchAssignments = useCallback(async () => {
        if (!deptId) return;
        setLoading(true); setError('');
        try {
            const params = { deptId, startDate: startStr, endDate: endStr };
            if (viewMode === 'faculty' && selectedFaculty) params.facultyId = selectedFaculty;
            const res = await api.get('/hod/work/view', { params });
            setAssignments(res.data || []);
        } catch {
            setError('Failed to load work assignments for this week.');
        } finally {
            setLoading(false);
        }
    }, [deptId, startStr, endStr, viewMode, selectedFaculty]);

    useEffect(() => { fetchAssignments(); }, [fetchAssignments]);

    // Filter assignments for timetable view — only assignments whose slot belongs to selected timetable
    const ttSlotIds = new Set(ttSlots.map(s => s.id));
    const timetableAssignments = assignments.filter(a =>
        a.timetableSlot && ttSlotIds.has(a.timetableSlot.id)
    );

    const selTT = timetables.find(t => t.id === selectedTT);
    const selFaculty = faculty.find(f => f.id === Number(selectedFaculty));

    // Week label
    const weekLabel = weekOffset === 0
        ? `This Week  (${format(weekDates[0], 'dd MMM')} – ${format(weekDates[5], 'dd MMM yyyy')})`
        : weekOffset < 0
            ? `${Math.abs(weekOffset)} Week${Math.abs(weekOffset) > 1 ? 's' : ''} Ago  (${format(weekDates[0], 'dd MMM')} – ${format(weekDates[5], 'dd MMM yyyy')})`
            : `${weekOffset} Week${weekOffset > 1 ? 's' : ''} Ahead  (${format(weekDates[0], 'dd MMM')} – ${format(weekDates[5], 'dd MMM yyyy')})`;

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 6 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 } }}>
                <Typography variant="h4" fontWeight="800" mb={0.5} color="#0f3460">Work Assignment View</Typography>
                <Typography variant="body2" color="text.secondary" mb={3}>
                    Review assigned work for any week — by section timetable or by individual faculty.
                </Typography>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                {/* ── Controls ── */}
                <Paper sx={{ p: 2.5, mb: 3, borderRadius: 3 }}>
                    <Grid container spacing={2} alignItems="center">

                        {/* View Toggle */}
                        <Grid item>
                            <ToggleButtonGroup value={viewMode} exclusive size="small"
                                onChange={(_, v) => { if (v) { setViewMode(v); setWeekOffset(0); } }}>
                                <ToggleButton value="timetable">
                                    <CalendarMonth sx={{ mr: 0.5 }} fontSize="small" />Timetable View
                                </ToggleButton>
                                <ToggleButton value="faculty">
                                    <Person sx={{ mr: 0.5 }} fontSize="small" />Faculty View
                                </ToggleButton>
                            </ToggleButtonGroup>
                        </Grid>

                        {/* Week Navigator — ±5 weeks */}
                        <Grid item>
                            <Box display="flex" alignItems="center" gap={1}>
                                <IconButton size="small" onClick={() => setWeekOffset(p => p - 1)}
                                    disabled={weekOffset <= -5} sx={{ bgcolor: '#f1f5f9' }}>
                                    <ArrowBackIos fontSize="small" sx={{ ml: 0.5 }} />
                                </IconButton>
                                <Chip label={weekLabel}
                                    sx={{ fontWeight: 700, bgcolor: '#0f3460', color: '#fff', px: 1, maxWidth: 340 }} />
                                <IconButton size="small" onClick={() => setWeekOffset(p => p + 1)}
                                    disabled={weekOffset >= 5} sx={{ bgcolor: '#f1f5f9' }}>
                                    <ArrowForwardIos fontSize="small" />
                                </IconButton>
                            </Box>
                        </Grid>

                        {/* Dropdown — timetable or faculty */}
                        <Grid item xs={12} md={4}>
                            {viewMode === 'timetable' ? (
                                <FormControl fullWidth size="small">
                                    <InputLabel>Select Section Timetable</InputLabel>
                                    <Select value={selectedTT} label="Select Section Timetable"
                                        onChange={e => setSelectedTT(e.target.value)}>
                                        {timetables.map(t => (
                                            <MenuItem key={t.id} value={t.id}>
                                                Year {t.section?.year} – Sem {t.section?.semester} – Sec {t.section?.sectionName}&nbsp;({t.timetableLabel})
                                            </MenuItem>
                                        ))}
                                    </Select>
                                </FormControl>
                            ) : (
                                <FormControl fullWidth size="small">
                                    <InputLabel>Select Faculty</InputLabel>
                                    <Select value={selectedFaculty} label="Select Faculty"
                                        onChange={e => setSelectedFaculty(e.target.value)}>
                                        <MenuItem value=""><em>— Select a faculty member —</em></MenuItem>
                                        {faculty.map(f => (
                                            <MenuItem key={f.id} value={f.id}>{f.name}</MenuItem>
                                        ))}
                                    </Select>
                                </FormControl>
                            )}
                        </Grid>
                    </Grid>
                </Paper>

                {/* ── Content ── */}
                {loading ? (
                    <Box display="flex" justifyContent="center" py={6}><CircularProgress /></Box>
                ) : viewMode === 'timetable' ? (
                    <Card sx={{ borderRadius: 3, overflow: 'hidden' }}>
                        {/* ── Timetable Card Header ── */}
                        <Box sx={{
                            p: 2.5, bgcolor: '#0f3460',
                            display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap'
                        }}>
                            <CalendarMonth sx={{ color: '#fff', fontSize: 32 }} />
                            <Box>
                                <Typography variant="h6" fontWeight="900" sx={{ color: '#fff', lineHeight: 1.2 }}>
                                    Section {selTT?.section?.sectionName || '—'}
                                </Typography>
                                <Box sx={{ display: 'flex', gap: 0.75, mt: 0.5, flexWrap: 'wrap' }}>
                                    <Chip label={`Year ${selTT?.section?.year}`} size="small"
                                        sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: '#fff', fontWeight: 800, height: 20, fontSize: '0.7rem' }} />
                                    <Chip label={`Sem ${selTT?.section?.semester}`} size="small"
                                        sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: '#fff', fontWeight: 800, height: 20, fontSize: '0.7rem' }} />
                                    <Chip label={selTT?.timetableLabel || ''} size="small"
                                        sx={{ bgcolor: '#e94560', color: '#fff', fontWeight: 800, height: 20, fontSize: '0.7rem' }} />
                                    <Chip label={weekLabel} size="small"
                                        sx={{ bgcolor: 'rgba(255,255,255,0.12)', color: '#fff', fontWeight: 700, height: 20, fontSize: '0.65rem' }} />
                                </Box>
                            </Box>
                        </Box>
                        <CardContent sx={{ p: 0 }}>
                            {timetableAssignments.length === 0 && !loading ? (
                                <Box sx={{ p: 4, textAlign: 'center', color: '#94a3b8' }}>
                                    No work assignments found for this timetable in the selected week.
                                </Box>
                            ) : (
                                <WeekGrid weekDates={weekDates} assignments={timetableAssignments} showFaculty={true} />
                            )}
                        </CardContent>
                    </Card>
                ) : selectedFaculty ? (
                    <Card sx={{ borderRadius: 3, overflow: 'hidden' }}>
                        {/* ── Faculty Card Header ── */}
                        <Box sx={{
                            p: 2.5, bgcolor: '#0f3460',
                            display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap'
                        }}>
                            <Person sx={{ color: '#fff', fontSize: 32 }} />
                            <Box>
                                <Typography variant="h6" fontWeight="900" sx={{ color: '#fff', lineHeight: 1.2 }}>
                                    {selFaculty?.name}
                                </Typography>
                                <Box sx={{ display: 'flex', gap: 0.75, mt: 0.5, flexWrap: 'wrap' }}>
                                    <Chip label={`${assignments.length} assignment(s)`} size="small"
                                        sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: '#fff', fontWeight: 800, height: 20, fontSize: '0.7rem' }} />
                                    <Chip label={weekLabel} size="small"
                                        sx={{ bgcolor: 'rgba(255,255,255,0.12)', color: '#fff', fontWeight: 700, height: 20, fontSize: '0.65rem' }} />
                                </Box>
                            </Box>
                        </Box>
                        <CardContent sx={{ p: 0 }}>
                            {assignments.length === 0 ? (
                                <Box sx={{ p: 4, textAlign: 'center', color: '#94a3b8' }}>
                                    No work assignments found for {selFaculty?.name} this week.
                                </Box>
                            ) : (
                                <WeekGrid weekDates={weekDates} assignments={assignments} showFaculty={false} />
                            )}
                        </CardContent>
                    </Card>
                ) : (
                    <Paper sx={{ p: 5, textAlign: 'center', borderRadius: 3 }}>
                        <Person sx={{ fontSize: 48, color: '#cbd5e1', mb: 1 }} />
                        <Typography color="text.secondary">Select a faculty member from the dropdown to view their weekly schedule.</Typography>
                    </Paper>
                )}
            </Box>
        </Box>
    );
};

export default HodWorkViewPage;
