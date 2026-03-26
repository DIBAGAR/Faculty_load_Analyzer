import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import {
    Box, Typography, Card, CardContent, CircularProgress,
    Alert, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper,
    Select, MenuItem, FormControl, InputLabel, Chip
} from '@mui/material';

const DAYS = [
    { id: 1, name: 'MONDAY' },
    { id: 2, name: 'TUESDAY' },
    { id: 3, name: 'WEDNESDAY' },
    { id: 4, name: 'THURSDAY' },
    { id: 5, name: 'FRIDAY' },
    { id: 6, name: 'SATURDAY' }
];
const HOURS = [1, 2, 3, 4, 5, 6, 7];

const DeptTimetablePage = () => {
    const [timetables, setTimetables] = useState([]);
    const [selectedTimetable, setSelectedTimetable] = useState(null);
    const [slots, setSlots] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchTimetables();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const fetchTimetables = async () => {
        try {
            const res = await api.get('/faculty/dept-timetables');
            setTimetables(res.data);
            if (res.data.length > 0) {
                setSelectedTimetable(res.data[0]);
                fetchSlots(res.data[0].id);
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to fetch department timetables.');
        } finally {
            setLoading(false);
        }
    };

    const fetchSlots = async (id) => {
        try {
            setLoading(true);
            const res = await api.get(`/faculty/timetables/${id}/slots`);
            setSlots(res.data);
        } catch (err) {
            setError('Failed to fetch timetable slots.');
        } finally {
            setLoading(false);
        }
    };

    const handleTimetableChange = (e) => {
        const tt = timetables.find(t => t.id === e.target.value);
        setSelectedTimetable(tt);
        fetchSlots(tt.id);
    };

    const getSlot = (dayId, hour) => {
        return slots.find(s => s.dayOfWeek === dayId && s.hour === hour);
    };

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 4 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 } }}>
                <Typography variant="h4" sx={{ fontWeight: 800, mb: 1, color: '#0f3460' }}>
                    Department Active Timetables
                </Typography>
                <Typography variant="body1" sx={{ color: '#475569', mb: 3 }}>
                    Reference all active schedules across your department.
                </Typography>

                {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

                {loading && !timetables.length ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', p: 5 }}><CircularProgress /></Box>
                ) : timetables.length === 0 ? (
                    <Paper className="glass" sx={{ p: 4, textAlign: 'center' }}>
                        <Typography variant="h6" color="textSecondary">No active timetables found for your department.</Typography>
                    </Paper>
                ) : (
                    <>
                        <Card className="glass" sx={{ mb: 3, p: 2 }}>
                            <FormControl sx={{ minWidth: 300 }}>
                                <InputLabel>Select Timetable</InputLabel>
                                <Select
                                    value={selectedTimetable?.id || ''}
                                    label="Select Timetable"
                                    onChange={handleTimetableChange}
                                >
                                    {timetables.map(t => (
                                        <MenuItem key={t.id} value={t.id}>
                                            Year {t.section?.year} - Sem {t.section?.semester} - Sec {t.section?.sectionName}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                        </Card>

                        {selectedTimetable && (
                            <Card className="glass">
                                <CardContent sx={{ p: 0 }}>
                                    <Box sx={{ p: 3, bgcolor: '#0f3460', color: 'white' }}>
                                        <Typography variant="h5" sx={{ fontWeight: 700 }}>
                                            Section: {selectedTimetable.section?.sectionName}
                                        </Typography>
                                        <Typography variant="body2" sx={{ opacity: 0.9 }}>
                                            Year {selectedTimetable.section?.year} &bull; Semester {selectedTimetable.section?.semester} &bull; {selectedTimetable.timetableLabel}
                                        </Typography>
                                    </Box>

                                    <TableContainer sx={{ maxHeight: '70vh' }}>
                                        <Table stickyHeader sx={{ minWidth: 1200 }}>
                                            <TableHead>
                                                <TableRow>
                                                    <TableCell sx={{ bgcolor: '#f1f5f9', fontWeight: 800, color: '#0f3460', borderRight: '1px solid #e2e8f0', minWidth: 120 }}>Day / Hour</TableCell>
                                                    {HOURS.map(h => (
                                                        <TableCell key={h} align="center" sx={{ bgcolor: '#f1f5f9', fontWeight: 800, color: '#0f3460', minWidth: 200, borderRight: '1px solid #e2e8f0' }}>
                                                            Hour {h}
                                                        </TableCell>
                                                    ))}
                                                </TableRow>
                                            </TableHead>
                                            <TableBody>
                                                {DAYS.map(day => (
                                                    <TableRow key={day.id} hover>
                                                        <TableCell sx={{ fontWeight: 700, bgcolor: '#f8fafc', borderRight: '1px solid #e2e8f0' }}>{day.name}</TableCell>
                                                        {HOURS.map(hour => {
                                                            const slot = getSlot(day.id, hour);
                                                            return (
                                                                <TableCell key={hour} align="center" sx={{ borderRight: '1px solid #e2e8f0', p: 1, minHeight: 100 }}>
                                                                    {slot ? (
                                                                        <Box sx={{
                                                                            display: 'flex', flexDirection: 'column', gap: 0.5, p: 1,
                                                                            borderRadius: 2,
                                                                            bgcolor: slot.slotType === 'LAB' ? 'rgba(233,69,96,0.05)' : 'rgba(15,52,96,0.05)',
                                                                            border: `1px solid ${slot.slotType === 'LAB' ? '#e94560' : '#0f3460'}`
                                                                        }}>
                                                                            {/* Course code */}
                                                                            <Typography variant="body2" sx={{ fontWeight: 900, color: '#0f3460', lineHeight: 1.2 }}>
                                                                                {slot.course?.courseCode}
                                                                            </Typography>
                                                                            {/* Course name */}
                                                                            <Typography variant="caption" sx={{ fontWeight: 700, color: '#475569', lineHeight: 1.1, display: 'block' }}>
                                                                                {slot.course?.courseName}
                                                                            </Typography>
                                                                            {/* Lab / Venue name — shown as a proper text line */}
                                                                            {slot.venue && (
                                                                                <Typography variant="caption" sx={{
                                                                                    fontWeight: 800, lineHeight: 1.1, display: 'block',
                                                                                    color: slot.slotType === 'LAB' ? '#9d174d' : '#166534',
                                                                                    bgcolor: slot.slotType === 'LAB' ? '#fce7f3' : '#f0fdf4',
                                                                                    borderRadius: 1, px: 0.75, py: 0.15,
                                                                                    border: `1px solid ${slot.slotType === 'LAB' ? '#f9a8d4' : '#bbf7d0'}`
                                                                                }}>
                                                                                    📍 {slot.venue.venueName}
                                                                                </Typography>
                                                                            )}
                                                                            {/* Type chip */}
                                                                            <Box sx={{ display: 'flex', justifyContent: 'flex-start', mt: 0.25 }}>
                                                                                <Chip label={slot.slotType || 'THEORY'} size="small" sx={{
                                                                                    height: 16, fontSize: '0.55rem', fontWeight: 800,
                                                                                    bgcolor: slot.slotType === 'LAB' ? '#e94560' : '#0f3460', color: '#fff'
                                                                                }} />
                                                                            </Box>
                                                                            {/* Default faculty */}
                                                                            {slot.defaultFaculty && (
                                                                                <Typography variant="caption" sx={{
                                                                                    color: '#1a365d', fontWeight: 800,
                                                                                    bgcolor: '#e0f2fe', borderRadius: 1, px: 0.75, py: 0.2,
                                                                                    fontSize: '0.65rem', border: '1px solid #bae6fd', display: 'block'
                                                                                }}>
                                                                                    {slot.defaultFaculty.name}
                                                                                </Typography>
                                                                            )}
                                                                            {/* Additional faculty (lab co-faculty) */}
                                                                            {slot.additionalFaculty && (
                                                                                <Typography variant="caption" sx={{
                                                                                    color: '#9d174d', fontWeight: 800,
                                                                                    bgcolor: '#fce7f3', borderRadius: 1, px: 0.75, py: 0.2,
                                                                                    fontSize: '0.65rem', border: '1px solid #f9a8d4', display: 'block'
                                                                                }}>
                                                                                    + {slot.additionalFaculty.name}
                                                                                </Typography>
                                                                            )}
                                                                        </Box>
                                                                    ) : (
                                                                        <Typography variant="caption" sx={{ color: '#cbd5e1', fontWeight: 500 }}>Empty</Typography>
                                                                    )}
                                                                </TableCell>
                                                            );
                                                        })}
                                                    </TableRow>
                                                ))}
                                            </TableBody>
                                        </Table>
                                    </TableContainer>
                                </CardContent>
                            </Card>
                        )}
                    </>
                )}
            </Box>
        </Box>
    );
};

export default DeptTimetablePage;
