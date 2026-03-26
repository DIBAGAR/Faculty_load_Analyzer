import React, { useState, useEffect } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import { Box, Typography, TextField, Button, Alert, Paper, Grid, Select, MenuItem } from '@mui/material';

const WorkGenerationPage = () => {
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [startHour, setStartHour] = useState('');
    const [endHour, setEndHour] = useState('');
    const [result, setResult] = useState(null);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);
    
    const [skippedSlots, setSkippedSlots] = useState([]);
    const [selectedAssignments, setSelectedAssignments] = useState({});
    const [faculty, setFaculty] = useState([]);
    const deptId = localStorage.getItem('deptId');

    useEffect(() => {
        if (deptId) {
            api.get(`/hod/faculty/dept/${deptId}`)
                .then(res => setFaculty(res.data))
                .catch(console.error);
        }
    }, [deptId]);

    const handleGenerate = async () => {
        setError(''); setSuccess(''); setResult(null); setLoading(true); setSkippedSlots([]);
        try {
            const hStart = startHour ? parseInt(startHour) : 1;
            const hEnd = endHour ? parseInt(endHour) : 7;
            const res = await api.post('/hod/work/generate', { deptId, startDate, endDate, startHour: hStart, endHour: hEnd });
            setResult(res.data);
            if (res.data.skippedDetails) {
                setSkippedSlots(res.data.skippedDetails);
            }
        } catch (err) { setError(err.response?.data?.message || 'Generation failed'); }
        finally { setLoading(false); }
    };

    const handleRemove = async () => {
        setError(''); setSuccess(''); setResult(null); setLoading(true); setSkippedSlots([]);
        try {
            const hStart = startHour ? parseInt(startHour) : 1;
            const hEnd = endHour ? parseInt(endHour) : 7;
            const res = await api.delete('/hod/work/remove', {
                data: { deptId, startDate, endDate, startHour: hStart, endHour: hEnd }
            });
            setSuccess(res.data.message || 'Work removed successfully');
        } catch (err) { setError(err.response?.data?.message || 'Removal failed'); }
        finally { setLoading(false); }
    };

    const handleManualAssign = async (slot, index) => {
        setError('');
        try {
            await api.post('/hod/work/manual-assign', {
                date: slot.date,
                hour: slot.hour,
                courseId: slot.courseId,
                venueId: slot.venueId,
                timetableSlotId: slot.timetableSlotId,
                facultyId: selectedAssignments[index],
                slotType: slot.slotType
            });
            setSuccess(`Successfully assigned ${slot.courseCode} manually!`);
            setTimeout(() => setSuccess(''), 3000);
            
            // Remove from skipped list
            const newSkipped = skippedSlots.filter((_, i) => i !== index);
            setSkippedSlots(newSkipped);
        } catch (err) {
            setError(err.response?.data?.message || 'Manual assignment failed');
        }
    };

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 4 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 }, maxWidth: 800, mx: 'auto' }}>
                <Typography variant="h4" sx={{ fontWeight: 800, mb: 3, color: '#0f3460' }}>Work Assignment Generator</Typography>
                
                {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                {result && (
                    <Alert severity="info" sx={{ mb: 2 }}>
                        Generated! Assigned: {result.assigned}, Skipped: {result.skipped} ({result.startDate} to {result.endDate})
                    </Alert>
                )}
                
                <Paper className="glass" sx={{ p: 4, borderRadius: 3 }}>
                    <Grid container spacing={3}>
                        <Grid item xs={12} md={3}>
                            <TextField fullWidth label="Start Date" type="date" value={startDate}
                                onChange={e => setStartDate(e.target.value)} InputLabelProps={{ shrink: true }} />
                        </Grid>
                        <Grid item xs={12} md={3}>
                            <TextField fullWidth label="End Date" type="date" value={endDate}
                                onChange={e => setEndDate(e.target.value)} InputLabelProps={{ shrink: true }} />
                        </Grid>
                        <Grid item xs={12} md={3}>
                            <TextField fullWidth label="Start Hour (1-7)" type="number" value={startHour}
                                onChange={e => setStartHour(e.target.value)} InputProps={{ inputProps: { min: 1, max: 7 } }} />
                        </Grid>
                        <Grid item xs={12} md={3}>
                            <TextField fullWidth label="End Hour (1-7)" type="number" value={endHour}
                                onChange={e => setEndHour(e.target.value)} InputProps={{ inputProps: { min: 1, max: 7 } }} />
                        </Grid>
                    </Grid>
                    <Typography variant="body2" sx={{ mt: 2, color: '#475569', fontWeight: 500 }}>
                        Maximum range: 15 days. Sundays are skipped. Valid hours are 1 to 7.
                    </Typography>

                    <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
                        <Button fullWidth variant="contained" onClick={handleGenerate} disabled={loading || !startDate || !endDate}
                            sx={{ py: 1.5, background: 'linear-gradient(135deg, #0f3460, #1a365d)', fontWeight: 800, fontSize: 16, borderRadius: 3 }}>
                            {loading ? 'Processing...' : 'Generate Work'}
                        </Button>
                        <Button fullWidth variant="outlined" color="error" onClick={handleRemove} disabled={loading || !startDate || !endDate}
                            sx={{ py: 1.5, fontWeight: 800, fontSize: 16, borderRadius: 3, borderWidth: 2 }}>
                            {loading ? 'Processing...' : 'Remove Work'}
                        </Button>
                    </Box>
                </Paper>

                {skippedSlots.length > 0 && (
                    <Paper sx={{ mt: 4, p: 3, borderRadius: 3, bgcolor: '#fef2f2', border: '2px solid #fecaca' }}>
                        <Typography variant="h6" color="error.main" fontWeight="800" mb={0.5}>
                            ⚠️ Manual Assignment Required ({skippedSlots.length} unassigned slot{skippedSlots.length > 1 ? 's' : ''})
                        </Typography>
                        <Typography variant="body2" mb={3} color="text.secondary">
                            No course-known faculty was available for the slots below. Select an eligible faculty and click Assign.
                            Faculty are ordered by lowest workload first.
                        </Typography>

                        {skippedSlots.map((slot, idx) => {
                            const eligible = slot.availableFaculty || [];
                            return (
                                <Box key={idx} sx={{
                                    display: 'flex', gap: 2, alignItems: 'flex-start',
                                    mb: 2, p: 2, bgcolor: 'white', borderRadius: 2,
                                    boxShadow: '0 1px 3px rgba(0,0,0,0.1)', flexWrap: 'wrap'
                                }}>
                                    {/* Slot info */}
                                    <Box sx={{ flexGrow: 1, minWidth: 220 }}>
                                        <Typography variant="subtitle2" fontWeight="800" color="#0f3460">
                                            {slot.courseCode} — {slot.courseName}
                                            <Typography component="span" sx={{
                                                ml: 1, fontSize: 11, fontWeight: 800,
                                                color: slot.slotType === 'LAB' ? '#e94560' : '#3b82f6'
                                            }}>[{slot.slotType}]</Typography>
                                        </Typography>
                                        <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5, fontSize: '0.78rem' }}>
                                            📅 <strong>{slot.date}</strong> &nbsp;|&nbsp;
                                            ⏰ Hour <strong>{slot.hour}</strong> &nbsp;|&nbsp;
                                            📍 {slot.venueName || 'No venue'}
                                        </Typography>
                                        {(slot.timetableName || slot.sectionName) && (
                                            <Typography variant="caption" sx={{ color: '#7c3aed', fontWeight: 700 }}>
                                                📋 {slot.timetableName}{slot.sectionName ? ` · Section ${slot.sectionName}` : ''}
                                            </Typography>
                                        )}
                                    </Box>

                                    {/* Faculty dropdown — eligible only */}
                                    {eligible.length > 0 ? (
                                        <>
                                            <Select size="small" sx={{ minWidth: 230, bgcolor: '#f8fafc' }} displayEmpty
                                                value={selectedAssignments[idx] || ''}
                                                onChange={(e) => setSelectedAssignments({ ...selectedAssignments, [idx]: e.target.value })}>
                                                <MenuItem value=""><em>Select Faculty (low load first)</em></MenuItem>
                                                {eligible.map(f => (
                                                    <MenuItem key={f.id} value={f.id} sx={{ fontSize: 13, fontWeight: 600 }}>
                                                        {f.name} &nbsp;
                                                        <Typography component="span" sx={{ fontSize: 11, color: '#64748b' }}>
                                                            ({f.load} hrs this month)
                                                        </Typography>
                                                    </MenuItem>
                                                ))}
                                            </Select>
                                            <Button variant="contained"
                                                disabled={!selectedAssignments[idx]}
                                                onClick={() => handleManualAssign(slot, idx)}
                                                sx={{ bgcolor: '#0f3460', '&:hover': { bgcolor: '#1a365d' }, fontWeight: 700, px: 3, alignSelf: 'center' }}>
                                                Assign
                                            </Button>
                                        </>
                                    ) : (
                                        <Box sx={{
                                            px: 2, py: 1, bgcolor: '#fef9c3', borderRadius: 2,
                                            border: '1px solid #fde047', alignSelf: 'center'
                                        }}>
                                            <Typography variant="caption" fontWeight="700" color="#854d0e">
                                                No eligible faculty available for this slot
                                            </Typography>
                                        </Box>
                                    )}
                                </Box>
                            );
                        })}
                    </Paper>
                )}
            </Box>
        </Box>
    );
};

export default WorkGenerationPage;
