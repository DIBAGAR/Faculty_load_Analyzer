import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import {
    Box, Typography, Button, TextField, Table, TableHead, TableRow, TableCell,
    TableBody, Paper, IconButton, Alert, Dialog, DialogTitle, DialogContent,
    DialogActions, FormControl, InputLabel, Select, MenuItem, Grid
} from '@mui/material';
import { Add, Edit, Delete, Download } from '@mui/icons-material';
import ConfirmDialog from '../components/ConfirmDialog';

const CoursePage = () => {
    const [courses, setCourses] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [open, setOpen] = useState(false);
    const [editId, setEditId] = useState(null);
    const [form, setForm] = useState({ courseCode: '', courseName: '', credit: '', departmentId: '', semester: '' });
    const [filters, setFilters] = useState({ courseCode: '', courseName: '', department: '', semester: '' });
    const [error, setError] = useState('');
    const [confirmOpen, setConfirmOpen] = useState(false);
    const [deleteId, setDeleteId] = useState(null);

    useEffect(() => { fetchData(); }, []);

    const fetchData = async () => {
        try {
            const [c, d] = await Promise.all([api.get('/course-admin/courses'), api.get('/department')]);
            setCourses(c.data); setDepartments(d.data);
        } catch { setError('Failed to load'); }
    };

    const handleSave = async () => {
        try {
            if (editId) await api.put(`/course-admin/courses/${editId}`, form);
            else await api.post('/course-admin/courses', form);
            setOpen(false); setEditId(null); setForm({ courseCode: '', courseName: '', credit: '', departmentId: '', semester: '' }); fetchData();
        } catch (err) { setError(err.response?.data?.message || 'Error'); }
    };

    const handleEdit = (c) => {
        setForm({ courseCode: c.courseCode, courseName: c.courseName, credit: c.credit || '', departmentId: c.department?.id || '', semester: c.semester || '' });
        setEditId(c.id); setOpen(true);
    };

    const handleDeleteClick = (id) => { setDeleteId(id); setConfirmOpen(true); };
    const handleConfirmDelete = async () => {
        try { await api.delete(`/course-admin/courses/${deleteId}`); fetchData(); } catch { setError('Failed to delete'); }
        setConfirmOpen(false); setDeleteId(null);
    };
    const handleExport = () => { window.open('http://localhost:8080/api/course-admin/courses/export', '_blank'); };

    const filtered = courses.filter(c =>
        c.courseCode?.toLowerCase().includes(filters.courseCode.toLowerCase()) &&
        c.courseName?.toLowerCase().includes(filters.courseName.toLowerCase()) &&
        (filters.department === '' || c.department?.deptName === filters.department) &&
        (filters.semester === '' || c.semester?.toString() === filters.semester)
    );

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 4 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 } }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 1 }}>
                    <Typography variant="h4" sx={{ fontWeight: 800, color: '#0f3460' }}>Course Management</Typography>
                    <Box>
                        <Button startIcon={<Download />} onClick={handleExport} sx={{ mr: 1, color: '#0f3460', borderColor: '#0f3460' }} variant="outlined">Export Excel</Button>
                        <Button startIcon={<Add />} variant="contained" onClick={() => { setEditId(null); setOpen(true); }}
                            sx={{ background: 'linear-gradient(135deg, #0f3460, #1a365d)', borderRadius: 2, fontWeight: 700 }}>Add Course</Button>
                    </Box>
                </Box>
                {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
                <Paper className="glass" sx={{ borderRadius: 3, p: 3, mb: 4 }}>
                    <Grid container spacing={2}>
                        <Grid item xs={6} md={3}><TextField fullWidth size="small" label="Course Code" value={filters.courseCode} onChange={e => setFilters({ ...filters, courseCode: e.target.value })} /></Grid>
                        <Grid item xs={6} md={3}><TextField fullWidth size="small" label="Course Name" value={filters.courseName} onChange={e => setFilters({ ...filters, courseName: e.target.value })} /></Grid>
                        <Grid item xs={6} md={3}>
                            <FormControl fullWidth size="small"><InputLabel>Department</InputLabel>
                                <Select value={filters.department} label="Department" onChange={e => setFilters({ ...filters, department: e.target.value })}>
                                    <MenuItem value="">All</MenuItem>{departments.map(d => <MenuItem key={d.id} value={d.deptName}>{d.deptName}</MenuItem>)}
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={6} md={3}><TextField fullWidth size="small" label="Semester" type="number" value={filters.semester} onChange={e => setFilters({ ...filters, semester: e.target.value })} /></Grid>
                    </Grid>
                </Paper>
                <Paper className="glass" sx={{ borderRadius: 3, overflow: 'auto' }}>
                    <Table sx={{ minWidth: 700 }}>
                        <TableHead sx={{ bgcolor: '#0f3460' }}>
                            <TableRow>{['Code', 'Name', 'Credit', 'Department', 'Semester', 'Actions'].map(h => <TableCell key={h} sx={{ color: '#ffffff', fontWeight: 700 }}>{h}</TableCell>)}</TableRow>
                        </TableHead>
                        <TableBody>
                            {filtered.map(c => (
                                <TableRow key={c.id} sx={{ '&:hover': { bgcolor: '#f0f0f5' } }}>
                                    <TableCell>{c.courseCode}</TableCell><TableCell>{c.courseName}</TableCell>
                                    <TableCell>{c.credit}</TableCell><TableCell>{c.department?.deptName}</TableCell>
                                    <TableCell>{c.semester}</TableCell>
                                    <TableCell>
                                        <IconButton size="small" onClick={() => handleEdit(c)} color="primary" sx={{ mr: 1 }}><Edit fontSize="small" /></IconButton>
                                        <IconButton size="small" onClick={() => handleDeleteClick(c.id)} color="error"><Delete fontSize="small" /></IconButton>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Paper>
                <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
                    <DialogTitle sx={{ fontWeight: 700 }}>{editId ? 'Edit' : 'Add'} Course</DialogTitle>
                    <DialogContent>
                        <TextField fullWidth label="Course Code" value={form.courseCode} onChange={e => setForm({ ...form, courseCode: e.target.value })} sx={{ mt: 1, mb: 2 }} />
                        <TextField fullWidth label="Course Name" value={form.courseName} onChange={e => setForm({ ...form, courseName: e.target.value })} sx={{ mb: 2 }} />
                        <TextField fullWidth label="Credit" type="number" value={form.credit} onChange={e => setForm({ ...form, credit: e.target.value })} sx={{ mb: 2 }} />
                        <FormControl fullWidth sx={{ mb: 2 }}><InputLabel>Department</InputLabel>
                            <Select value={form.departmentId} label="Department" onChange={e => setForm({ ...form, departmentId: e.target.value })}>
                                {departments.map(d => <MenuItem key={d.id} value={d.id}>{d.deptName}</MenuItem>)}
                            </Select>
                        </FormControl>
                        <TextField fullWidth label="Semester" type="number" value={form.semester} onChange={e => setForm({ ...form, semester: e.target.value })} />
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setOpen(false)}>Cancel</Button>
                        <Button variant="contained" onClick={handleSave}>{editId ? 'Update' : 'Create'}</Button>
                    </DialogActions>
                </Dialog>
                <ConfirmDialog open={confirmOpen} title="Delete Course" content="Are you sure you want to delete this course?" onConfirm={handleConfirmDelete} onCancel={() => setConfirmOpen(false)} confirmText="Delete" />
            </Box>
        </Box>
    );
};

export default CoursePage;
