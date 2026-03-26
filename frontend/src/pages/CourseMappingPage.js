import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import {
    Box, Typography, Button, Table, TableHead, TableRow, TableCell, TableBody,
    Paper, FormControl, InputLabel, Select, MenuItem, Dialog, DialogTitle,
    DialogContent, DialogActions, Chip, Grid, TextField, IconButton
} from '@mui/material';
import { Add, Delete } from '@mui/icons-material';
import ConfirmDialog from '../components/ConfirmDialog';

const CourseMappingPage = () => {
    const [mappings, setMappings] = useState([]);
    const [faculty, setFaculty] = useState([]);
    const [courses, setCourses] = useState([]);
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState({ facultyId: '', courseId: '', type: 'PRIMARY' });
    const [filters, setFilters] = useState({ name: '', courseCode: '', type: '' });
    const [confirmOpen, setConfirmOpen] = useState(false);
    const [deleteId, setDeleteId] = useState(null);
    const deptId = localStorage.getItem('deptId');

    useEffect(() => {
        if (deptId) fetchData();
    }, [deptId]);

    const fetchData = async () => {
        try {
            const [m, f, c] = await Promise.all([
                api.get(`/hod/mappings/${deptId}`),
                api.get(`/faculty/dept/${deptId}`),
                api.get(`/course-admin/courses`)
            ]);
            setMappings(m.data); setFaculty(f.data); setCourses(c.data);
        } catch { }
    };

    const handleAdd = async () => {
        try { await api.post('/hod/mappings', form); setOpen(false); fetchData(); } catch { }
    };
    const handleDeleteClick = (id) => { setDeleteId(id); setConfirmOpen(true); };
    const handleConfirmDelete = async () => {
        try { await api.delete(`/hod/mappings/${deleteId}`); fetchData(); } catch { }
        setConfirmOpen(false); setDeleteId(null);
    };

    const filtered = mappings.filter(m =>
        (!filters.name || m.faculty?.name?.toLowerCase().includes(filters.name.toLowerCase())) &&
        (!filters.courseCode || m.course?.courseCode?.toLowerCase().includes(filters.courseCode.toLowerCase())) &&
        (!filters.type || m.type === filters.type)
    );

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 4 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 } }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                    <Typography variant="h4" sx={{ fontWeight: 800, color: '#0f3460' }}>Course-Faculty Mapping</Typography>
                    <Button startIcon={<Add />} variant="contained" onClick={() => setOpen(true)}
                        sx={{ background: 'linear-gradient(135deg, #0f3460, #1a365d)', borderRadius: 2, fontWeight: 700 }}>Add Mapping</Button>
                </Box>
                <Grid container spacing={2} sx={{ mb: 2 }}>
                    <Grid item xs={4}><TextField fullWidth size="small" label="Faculty" value={filters.name} onChange={e => setFilters({ ...filters, name: e.target.value })} /></Grid>
                    <Grid item xs={4}><TextField fullWidth size="small" label="Course Code" value={filters.courseCode} onChange={e => setFilters({ ...filters, courseCode: e.target.value })} /></Grid>
                    <Grid item xs={4}>
                        <FormControl fullWidth size="small"><InputLabel>Type</InputLabel>
                            <Select value={filters.type} label="Type" onChange={e => setFilters({ ...filters, type: e.target.value })}>
                                <MenuItem value="">All</MenuItem><MenuItem value="PRIMARY">Primary</MenuItem><MenuItem value="ADDITIONAL">Additional</MenuItem>
                            </Select>
                        </FormControl>
                    </Grid>
                </Grid>
                <Paper className="glass" sx={{ borderRadius: 3, overflow: 'auto' }}>
                    <Table sx={{ minWidth: 700 }}>
                        <TableHead sx={{ bgcolor: '#0f3460' }}>
                            <TableRow>{['Faculty', 'Roll No', 'Course Code', 'Course Name', 'Type', 'Action'].map(h =>
                                <TableCell key={h} sx={{ color: '#ffffff', fontWeight: 700 }}>{h}</TableCell>)}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {filtered.map(m => (
                                <TableRow key={m.id} sx={{ '&:hover': { bgcolor: 'rgba(0,0,0,0.04)' } }}>
                                    <TableCell sx={{ color: '#1e293b', fontWeight: 500 }}>{m.faculty?.name}</TableCell>
                                    <TableCell sx={{ color: '#1e293b' }}>{m.faculty?.rollNumber}</TableCell>
                                    <TableCell sx={{ color: '#1e293b', fontWeight: 500 }}>{m.course?.courseCode}</TableCell>
                                    <TableCell sx={{ color: '#475569' }}>{m.course?.courseName}</TableCell>
                                    <TableCell><Chip label={m.type} size="small" color={m.type === 'PRIMARY' ? 'primary' : 'secondary'} sx={{ fontWeight: 600 }} /></TableCell>
                                    <TableCell><IconButton size="small" onClick={() => handleDeleteClick(m.id)} color="error"><Delete fontSize="small" /></IconButton></TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Paper>
                <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
                    <DialogTitle>Add Course-Faculty Mapping</DialogTitle>
                    <DialogContent>
                        <FormControl fullWidth sx={{ mt: 1, mb: 2 }}><InputLabel>Faculty</InputLabel>
                            <Select value={form.facultyId} label="Faculty" onChange={e => setForm({ ...form, facultyId: e.target.value })}>
                                {faculty.map(f => <MenuItem key={f.id} value={f.id}>{f.name} ({f.rollNumber})</MenuItem>)}
                            </Select>
                        </FormControl>
                        <FormControl fullWidth sx={{ mb: 2 }}><InputLabel>Course</InputLabel>
                            <Select value={form.courseId} label="Course" onChange={e => setForm({ ...form, courseId: e.target.value })}>
                                {courses.map(c => <MenuItem key={c.id} value={c.id}>{c.courseCode} - {c.courseName}</MenuItem>)}
                            </Select>
                        </FormControl>
                        <FormControl fullWidth><InputLabel>Type</InputLabel>
                            <Select value={form.type} label="Type" onChange={e => setForm({ ...form, type: e.target.value })}>
                                <MenuItem value="PRIMARY">Primary</MenuItem><MenuItem value="ADDITIONAL">Additional</MenuItem>
                            </Select>
                        </FormControl>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setOpen(false)}>Cancel</Button>
                        <Button variant="contained" onClick={handleAdd}>Add</Button>
                    </DialogActions>
                </Dialog>
                <ConfirmDialog open={confirmOpen} title="Remove Course Mapping" content="Are you sure you want to remove this mapping?" onConfirm={handleConfirmDelete} onCancel={() => setConfirmOpen(false)} confirmText="Remove" />
            </Box>
        </Box>
    );
};

export default CourseMappingPage;
