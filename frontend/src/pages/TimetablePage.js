import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import {
    Box, Typography, Button, TextField, Select, MenuItem, FormControl, InputLabel,
    Table, TableHead, TableRow, TableCell, TableBody, Paper, Alert, Chip,
    Dialog, DialogTitle, DialogContent, DialogActions, Grid, IconButton, Card, CardContent,
    List, ListItem, ListItemText, ListItemSecondaryAction
} from '@mui/material';
import { Add, ContentCopy, Delete, Save, Settings } from '@mui/icons-material';
import ConfirmDialog from '../components/ConfirmDialog';
import { DAYS, HOURS } from '../utils/constants';

const TimetablePage = () => {
    const [sections, setSections] = useState([]);
    const [timetables, setTimetables] = useState([]);
    const [selectedSection, setSelectedSection] = useState('');
    const [selectedTT, setSelectedTT] = useState(null);
    const [slots, setSlots] = useState([]);
    const [courses, setCourses] = useState([]);
    const [venues, setVenues] = useState([]);
    const [faculty, setFaculty] = useState([]);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    
    // Dialogs
    const [confirmOpen, setConfirmOpen] = useState(false);
    const [sectionOpen, setSectionOpen] = useState(false);
    const [sectionForm, setSectionForm] = useState({ year: '', semester: '', sectionName: '' });
    const [copyOpen, setCopyOpen] = useState(false);
    const [copyForm, setCopyForm] = useState({ year: '', semester: '', sectionName: '' });
    
    // Mappings & Conflicts
    const [departmentMappings, setDepartmentMappings] = useState([]);
    const [occupiedVenues, setOccupiedVenues] = useState({});
    const [occupiedFaculty, setOccupiedFaculty] = useState({});
    
    // Lab Mapping Dialog
    const [labMapOpen, setLabMapOpen] = useState(false);
    const [labMappings, setLabMappings] = useState([]);
    const [labForm, setLabForm] = useState({ venueId: '', courseId: '' });

    const deptId = localStorage.getItem('deptId');

    useEffect(() => {
        if (deptId) { fetchSections(); fetchVenues(); fetchFaculty(); fetchMappings(); fetchLabMappings(); }
    }, [deptId]);

    const fetchSections = async () => { try { const r = await api.get(`/hod/sections/${deptId}`); setSections(r.data); } catch { } };
    const fetchVenues = async () => { try { const r = await api.get(`/venue-admin/venues/dept/${deptId}`); setVenues(r.data); } catch { } };
    const fetchFaculty = async () => { try { const r = await api.get(`/faculty/dept/${deptId}`); setFaculty(r.data); } catch { } };
    const fetchMappings = async () => { try { const r = await api.get(`/hod/mappings/${deptId}`); setDepartmentMappings(r.data); } catch { } };
    const fetchLabMappings = async () => { try { const r = await api.get(`/hod/lab-mappings/${deptId}`); setLabMappings(r.data || []); } catch { } };

    const fetchTimetables = async (secId) => {
        try { const r = await api.get(`/hod/timetables/section/${secId}`); setTimetables(r.data); } catch { }
    };

    const handleSelectSection = (secId) => {
        setSelectedSection(secId);
        const sec = sections.find(s => s.id === secId);
        if (sec) fetchCourses(sec.semester);
        fetchTimetables(secId);
        setSelectedTT(null); setSlots([]);
    };

    const fetchCourses = async (sem) => {
        try { const r = await api.get(`/course-admin/courses/dept/${deptId}/semester/${sem}`); setCourses(r.data); } catch { }
    };

    const handleSelectTT = async (tt) => {
        setSelectedTT(tt);
        try {
            const [rSlots, rVenues, rFaculty] = await Promise.all([
                api.get(`/hod/timetables/${tt.id}/slots`),
                api.get(`/hod/timetables/occupied-venues/${deptId}?currentTimetableId=${tt.id}`),
                api.get(`/hod/timetables/occupied-faculty/${deptId}?currentTimetableId=${tt.id}`)
            ]);
            setSlots(rSlots.data);
            setOccupiedVenues(rVenues.data || {});
            setOccupiedFaculty(rFaculty.data || {});
        } catch { } // Error silenced to avoid blocking UI during 500s 
    };

    // --- Actions ---
    const handleCreateSection = async () => {
        try {
            await api.post('/hod/sections', { ...sectionForm, deptId });
            setSectionOpen(false); fetchSections(); setSuccess('Section created!');
            setTimeout(() => setSuccess(''), 3000);
        } catch (err) { setError(err.response?.data?.message || 'Error creating section'); }
    };

    const getSlotValue = (day, hour, field) => {
        const slot = slots.find(s => s.dayOfWeek === day && s.hour === hour);
        if (!slot) return '';
        switch (field) {
            case 'courseId': return (slot.courseId !== undefined ? slot.courseId : slot.course?.id) || '';
            case 'venueId': return (slot.venueId !== undefined ? slot.venueId : slot.venue?.id) || '';
            case 'defaultFacultyId': return (slot.defaultFacultyId !== undefined ? slot.defaultFacultyId : slot.defaultFaculty?.id) || '';
            case 'additionalFacultyId': return (slot.additionalFacultyId !== undefined ? slot.additionalFacultyId : slot.additionalFaculty?.id) || '';
            case 'slotType': return slot.slotType || 'THEORY';
            default: return '';
        }
    };

    const handleSaveSlots = async () => {
        try {
            const payload = [];
            for (let d = 1; d <= 6; d++) {
                for (let h = 1; h <= 7; h++) {
                    const slot = slots.find(s => s.dayOfWeek === d && s.hour === h);
                    if (!slot) continue;

                    const courseId = slot.courseId !== undefined ? slot.courseId : slot.course?.id;
                    const venueId = slot.venueId !== undefined ? slot.venueId : slot.venue?.id;
                    const facultyId = slot.defaultFacultyId !== undefined ? slot.defaultFacultyId : slot.defaultFaculty?.id;
                    const addlFacultyId = slot.additionalFacultyId !== undefined ? slot.additionalFacultyId : slot.additionalFaculty?.id;

                    if (courseId || venueId || facultyId) {
                        payload.push({
                            dayOfWeek: d, hour: h,
                            courseId: courseId || null,
                            venueId: venueId || null,
                            defaultFacultyId: facultyId || null,
                            additionalFacultyId: addlFacultyId || null,
                            slotType: slot.slotType || 'THEORY'
                        });
                    }
                }
            }
            await api.post(`/hod/timetables/${selectedTT.id}/slots`, payload);
            setSuccess('Timetable saved successfully'); setTimeout(() => setSuccess(''), 3000);
        } catch (err) { setError(err.response?.data?.message || 'Failed to save timetable'); }
    };

    const updateSlot = (day, hour, field, value) => {
        setSlots(prev => {
            const existing = prev.find(s => s.dayOfWeek === day && s.hour === hour);
            if (existing) {
                return prev.map(s => s.dayOfWeek === day && s.hour === hour ? { ...s, [field]: value } : s);
            }
            return [...prev, { dayOfWeek: day, hour, [field]: value }];
        });
    };

    const handleCopy = async () => {
        try {
            await api.post(`/hod/timetables/${selectedTT.id}/copy-to-section`, { deptId, ...copyForm });
            setCopyOpen(false); fetchSections(); setSuccess('Timetable copied!');
            setTimeout(() => setSuccess(''), 3000);
        } catch (err) { setError(err.response?.data?.message || 'Error copying'); }
    };

    const handleStatus = async (status) => {
        try {
            await api.put(`/hod/timetables/${selectedTT.id}/status`, { status });
            fetchTimetables(selectedSection);
            setSelectedTT({ ...selectedTT, status });
        } catch { }
    };

    const handleDeleteTTClick = () => setConfirmOpen(true);
    const handleConfirmDeleteTT = async () => {
        try { await api.delete(`/hod/timetables/${selectedTT.id}`); setSelectedTT(null); fetchTimetables(selectedSection); } catch { }
        setConfirmOpen(false);
    };

    // --- Lab Mapping ---
    const handleAddLabMapping = async () => {
        try {
            await api.post('/hod/lab-mappings', labForm);
            fetchLabMappings();
            setLabForm({ venueId: '', courseId: '' });
        } catch (err) { alert(err.response?.data || 'Failed to map lab'); }
    };
    const handleDeleteLabMapping = async (id) => {
        try { await api.delete(`/hod/lab-mappings/${id}`); fetchLabMappings(); } catch { }
    };

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 4 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 } }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                    <Typography variant="h4" sx={{ fontWeight: 800, color: '#0f3460' }}>Timetable Builder</Typography>
                    <Button variant="outlined" startIcon={<Settings />} onClick={() => setLabMapOpen(true)} color="secondary">
                        Manage Supported Lab Courses
                    </Button>
                </Box>
                
                {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
                {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

                <Grid container spacing={2} sx={{ mb: 3 }}>
                    <Grid item xs={12} md={4}>
                        <FormControl fullWidth size="small">
                            <InputLabel>Section</InputLabel>
                            <Select value={selectedSection} label="Section" onChange={e => handleSelectSection(e.target.value)}>
                                {sections.map(s => <MenuItem key={s.id} value={s.id}>Year {s.year} Sem {s.semester} Sec {s.sectionName}</MenuItem>)}
                            </Select>
                        </FormControl>
                    </Grid>
                    <Grid item xs={6} md={3}>
                        <Button fullWidth variant="outlined" onClick={() => setSectionOpen(true)} startIcon={<Add />}>Create New Section</Button>
                    </Grid>
                </Grid>

                {/* Timetable Cards */}
                <Grid container spacing={2} sx={{ mb: 3 }}>
                    {timetables.map(tt => (
                        <Grid item xs={12} sm={6} md={4} key={tt.id}>
                            <Card className="glass" sx={{ borderRadius: 3, cursor: 'pointer', border: selectedTT?.id === tt.id ? '2px solid #0f3460' : '1px solid rgba(15,52,96,0.2)', overflow: 'hidden' }} onClick={() => handleSelectTT(tt)}>
                                {/* Colored header strip for contrast */}
                                <Box sx={{ bgcolor: selectedTT?.id === tt.id ? '#0f3460' : '#1e3a5f', px: 2, py: 1.5 }}>
                                    <Typography variant="subtitle1" fontWeight="900" sx={{ color: '#fff', lineHeight: 1.2 }}>
                                        {tt.timetableLabel || 'Timetable'}
                                    </Typography>
                                    {tt.section && (
                                        <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.85)', fontWeight: 600 }}>
                                            Year {tt.section.year} · Sem {tt.section.semester} · Section {tt.section.sectionName}
                                        </Typography>
                                    )}
                                </Box>
                                <CardContent sx={{ py: 1, '&:last-child': { pb: 1 } }}>
                                    <Chip label={tt.status} size="small" color={tt.status === 'ACTIVE' ? 'success' : 'default'} sx={{ fontWeight: 700 }} />
                                </CardContent>
                            </Card>
                        </Grid>
                    ))}
                </Grid>

                {/* Timetable Grid */}
                {selectedTT && (
                    <>
                        <Box sx={{ display: 'flex', gap: 1, mb: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                            <Button variant="contained" startIcon={<Save />} color="primary" onClick={handleSaveSlots}>Save Timetable</Button>
                            <Button variant="contained" onClick={() => handleStatus(selectedTT.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE')}
                                color={selectedTT.status === 'ACTIVE' ? 'error' : 'secondary'}>
                                {selectedTT.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                            </Button>
                            <Button variant="outlined" startIcon={<ContentCopy />} onClick={() => setCopyOpen(true)}>Copy To New Section</Button>
                            <Button variant="outlined" color="error" startIcon={<Delete />} onClick={handleDeleteTTClick}>Delete Section & Timetable</Button>
                        </Box>

                        <Paper className="glass" sx={{ borderRadius: 3, overflow: 'auto' }}>
                            <Table size="small" sx={{ minWidth: 900 }}>
                                <TableHead sx={{ bgcolor: '#0f3460' }}>
                                    <TableRow>
                                        <TableCell sx={{ color: '#ffffff', fontWeight: 700, borderRight: '2px solid rgba(255,255,255,0.4)' }}>Day</TableCell>
                                        {HOURS.map(h => <TableCell key={h} sx={{ color: '#ffffff', fontWeight: 700, borderRight: '2px solid rgba(255,255,255,0.4)' }}>Hour {h}</TableCell>)}
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {DAYS.map((day, dIdx) => (
                                        <TableRow key={day} sx={{ '&:hover': { bgcolor: 'rgba(0,0,0,0.02)' } }}>
                                            <TableCell sx={{ fontWeight: 800, color: '#0f3460', borderRight: '2px solid #94a3b8', borderBottom: '2px solid #94a3b8', bgcolor: '#f8fafc' }}>{day}</TableCell>
                                            {HOURS.map(h => {
                                                const key = `${dIdx + 1}_${h}`;
                                                const slotType = getSlotValue(dIdx + 1, h, 'slotType');
                                                const slotCourseId = getSlotValue(dIdx + 1, h, 'courseId');
                                                
                                                // Dynamic Venues: If LAB slot & a course is selected, filter by lab mapping
                                                let availableVenues = venues.filter(v => slotType === 'LAB' ? v.venueType === 'LAB' : v.venueType === 'CLASSROOM');
                                                if (slotType === 'LAB' && slotCourseId) {
                                                    const validVenueIds = labMappings.filter(m => m.course?.id === slotCourseId).map(m => m.venue?.id);
                                                    if (validVenueIds.length > 0) {
                                                        availableVenues = availableVenues.filter(v => validVenueIds.includes(v.id));
                                                    }
                                                }

                                                return (
                                                    <TableCell key={h} sx={{
                                                        p: 0.5, 
                                                        minWidth: 120,
                                                        borderRight: '2px solid #94a3b8',
                                                        borderBottom: '2px solid #94a3b8',
                                                        bgcolor: slotType === 'LAB' ? 'rgba(22,199,154,0.15)' : '#f4f7fa'
                                                    }}>
                                                        <Select fullWidth size="small" displayEmpty value={getSlotValue(dIdx + 1, h, 'courseId')}
                                                            onChange={e => updateSlot(dIdx + 1, h, 'courseId', e.target.value)} sx={{ mb: 0.5, fontSize: 11 }}>
                                                            <MenuItem value=""><em>Course</em></MenuItem>
                                                            {courses.map(c => <MenuItem key={c.id} value={c.id} sx={{ fontSize: 12 }}>{c.courseCode} - {c.courseName}</MenuItem>)}
                                                        </Select>

                                                        <Select fullWidth size="small" displayEmpty value={getSlotValue(dIdx + 1, h, 'slotType')}
                                                            onChange={e => updateSlot(dIdx + 1, h, 'slotType', e.target.value)} sx={{ mb: 0.5, fontSize: 11 }}>
                                                            <MenuItem value="THEORY">Theory</MenuItem><MenuItem value="LAB">Lab</MenuItem>
                                                        </Select>
                                                        
                                                        <Select fullWidth size="small" displayEmpty value={getSlotValue(dIdx + 1, h, 'venueId')}
                                                            onChange={e => updateSlot(dIdx + 1, h, 'venueId', e.target.value)} sx={{ mb: 0.5, fontSize: 11 }}>
                                                            <MenuItem value=""><em>Venue</em></MenuItem>
                                                            {availableVenues.map(v => {
                                                                const isOccupied = occupiedVenues[key]?.includes(v.id);
                                                                return <MenuItem key={v.id} value={v.id} disabled={isOccupied} sx={{ fontSize: 12 }}>{v.venueName} {isOccupied ? '(Occ)' : ''}</MenuItem>;
                                                            })}
                                                        </Select>

                                                        <Box sx={{ display: 'flex', gap: 1 }}>
                                                            <Select fullWidth size="small" displayEmpty value={getSlotValue(dIdx + 1, h, 'defaultFacultyId')}
                                                                onChange={e => updateSlot(dIdx + 1, h, 'defaultFacultyId', e.target.value)} sx={{ fontSize: 11 }}>
                                                                <MenuItem value=""><em>Faculty</em></MenuItem>
                                                                {faculty
                                                                    .filter(f => {
                                                                        const scId = getSlotValue(dIdx + 1, h, 'courseId');
                                                                        if (scId && !departmentMappings.some(m => m.faculty.id === f.id && m.course.id === scId)) return false;
                                                                        return true;
                                                                    })
                                                                    .map(f => {
                                                                        const fOcc = occupiedFaculty[key]?.includes(f.id);
                                                                        const isSameAsAdditional = getSlotValue(dIdx + 1, h, 'additionalFacultyId') === f.id;
                                                                        return <MenuItem key={f.id} value={f.id} disabled={fOcc || isSameAsAdditional} sx={{ fontSize: 12 }}>{f.name} {fOcc ? '(Occ)' : ''}</MenuItem>;
                                                                    })
                                                                }
                                                            </Select>

                                                            {slotType === 'LAB' && (
                                                                <Select fullWidth size="small" displayEmpty value={getSlotValue(dIdx + 1, h, 'additionalFacultyId')}
                                                                    onChange={e => updateSlot(dIdx + 1, h, 'additionalFacultyId', e.target.value)} sx={{ fontSize: 11 }}>
                                                                    <MenuItem value=""><em>Addl. Faculty</em></MenuItem>
                                                                    {faculty
                                                                        .filter(f => {
                                                                            const scId = getSlotValue(dIdx + 1, h, 'courseId');
                                                                            if (scId && !departmentMappings.some(m => m.faculty.id === f.id && m.course.id === scId)) return false;
                                                                            return true;
                                                                        })
                                                                        .map(f => {
                                                                            const fOcc = occupiedFaculty[key]?.includes(f.id);
                                                                            const isSameAsDefault = getSlotValue(dIdx + 1, h, 'defaultFacultyId') === f.id;
                                                                            return <MenuItem key={f.id} value={f.id} disabled={fOcc || isSameAsDefault} sx={{ fontSize: 12 }}>{f.name} {fOcc ? '(Occ)' : ''}</MenuItem>;
                                                                        })
                                                                    }
                                                                </Select>
                                                            )}
                                                        </Box>
                                                    </TableCell>
                                                )
                                            })}
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </Paper>
                    </>
                )}

                {/* --- Dialogs --- */}
                <Dialog open={sectionOpen} onClose={() => setSectionOpen(false)}>
                    <DialogTitle>Create Section</DialogTitle>
                    <DialogContent>
                        <TextField fullWidth label="Year (1-4)" type="number" value={sectionForm.year} onChange={e => setSectionForm({ ...sectionForm, year: e.target.value })} sx={{ mt: 1, mb: 2 }} />
                        <TextField fullWidth label="Semester" type="number" value={sectionForm.semester} onChange={e => setSectionForm({ ...sectionForm, semester: e.target.value })} sx={{ mb: 2 }} />
                        <TextField fullWidth label="Section Name" value={sectionForm.sectionName} onChange={e => setSectionForm({ ...sectionForm, sectionName: e.target.value })} />
                    </DialogContent>
                    <DialogActions><Button onClick={() => setSectionOpen(false)}>Cancel</Button><Button variant="contained" onClick={handleCreateSection}>Create</Button></DialogActions>
                </Dialog>

                <Dialog open={copyOpen} onClose={() => setCopyOpen(false)}>
                    <DialogTitle>Copy To New Section</DialogTitle>
                    <DialogContent>
                        <TextField fullWidth label="Year" type="number" value={copyForm.year} onChange={e => setCopyForm({ ...copyForm, year: e.target.value })} sx={{ mt: 1, mb: 2 }} />
                        <TextField fullWidth label="Semester" type="number" value={copyForm.semester} onChange={e => setCopyForm({ ...copyForm, semester: e.target.value })} sx={{ mb: 2 }} />
                        <TextField fullWidth label="Section Name" value={copyForm.sectionName} onChange={e => setCopyForm({ ...copyForm, sectionName: e.target.value })} />
                    </DialogContent>
                    <DialogActions><Button onClick={() => setCopyOpen(false)}>Cancel</Button><Button variant="contained" onClick={handleCopy}>Copy & Create</Button></DialogActions>
                </Dialog>

                <Dialog open={labMapOpen} onClose={() => setLabMapOpen(false)} maxWidth="sm" fullWidth>
                    <DialogTitle>Map Courses to Lab Venues</DialogTitle>
                    <DialogContent>
                        <Box sx={{ display: 'flex', gap: 1, mt: 1, mb: 3 }}>
                            <Select size="small" fullWidth displayEmpty value={labForm.venueId} onChange={e => setLabForm({...labForm, venueId: e.target.value})}>
                                <MenuItem value=""><em>Select Lab Venue</em></MenuItem>
                                {venues.filter(v => v.venueType === 'LAB').map(v => <MenuItem key={v.id} value={v.id}>{v.venueName}</MenuItem>)}
                            </Select>
                            <Select size="small" fullWidth displayEmpty value={labForm.courseId} onChange={e => setLabForm({...labForm, courseId: e.target.value})}>
                                <MenuItem value=""><em>Select Course</em></MenuItem>
                                {courses.map(c => <MenuItem key={c.id} value={c.id}>{c.courseCode} - {c.courseName}</MenuItem>)}
                            </Select>
                            <Button variant="contained" onClick={handleAddLabMapping} disabled={!labForm.venueId || !labForm.courseId}>Add</Button>
                        </Box>
                        <List sx={{ bgcolor: 'rgba(0,0,0,0.02)', borderRadius: 2 }}>
                            {labMappings.map(m => (
                                <ListItem key={m.id} divider>
                                    <ListItemText primary={<Typography sx={{fontWeight:700}}>{m.venue?.venueName}</Typography>} secondary={`${m.course?.courseCode} - ${m.course?.courseName}`} />
                                    <ListItemSecondaryAction><IconButton edge="end" color="error" onClick={() => handleDeleteLabMapping(m.id)}><Delete /></IconButton></ListItemSecondaryAction>
                                </ListItem>
                            ))}
                        </List>
                    </DialogContent>
                    <DialogActions><Button onClick={() => setLabMapOpen(false)}>Close</Button></DialogActions>
                </Dialog>
                <ConfirmDialog open={confirmOpen} title="Delete Timetable" content="Are you sure you want to delete this timetable and its slots? This action cannot be undone." onConfirm={handleConfirmDeleteTT} onCancel={() => setConfirmOpen(false)} confirmText="Delete" />
            </Box>
        </Box>
    );
};

export default TimetablePage;
