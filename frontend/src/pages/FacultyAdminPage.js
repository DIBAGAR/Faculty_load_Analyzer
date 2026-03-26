import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import Navbar from '../components/Navbar';
import {
    Box, Typography, Button, TextField, Dialog, DialogTitle, DialogContent,
    DialogActions, Table, TableHead, TableRow, TableCell, TableBody, Paper,
    IconButton, Alert, FormControl, InputLabel, Select, MenuItem, Grid, Chip
} from '@mui/material';
import { Add, Edit, Delete, Download } from '@mui/icons-material';
import ConfirmDialog from '../components/ConfirmDialog';

const FacultyAdminPage = () => {
    const navigate = useNavigate();
    const [faculties, setFaculties] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [open, setOpen] = useState(false);
    const [editId, setEditId] = useState(null);
    const [form, setForm] = useState({ name: '', rollNumber: '', email: '', phone: '', departmentId: '', bloodGroup: '', password: '', role: 'FACULTY' });
    const [filters, setFilters] = useState({ name: '', rollNumber: '', email: '', department: '' });
    const [error, setError] = useState('');
    const [confirmOpen, setConfirmOpen] = useState(false);
    const [deleteId, setDeleteId] = useState(null);

    useEffect(() => { fetchData(); }, []);

    const fetchData = async () => {
        try {
            const [f, d] = await Promise.all([api.get('/faculty-admin/faculties'), api.get('/department')]);
            setFaculties(f.data);
            setDepartments(d.data);
        } catch (err) { setError('Failed to load'); }
    };

    const handleSave = async () => {
        try {
            if (editId) await api.put(`/faculty-admin/faculties/${editId}`, form);
            else await api.post('/faculty-admin/faculties', form);
            setOpen(false);
            setEditId(null);
            setForm({ name: '', rollNumber: '', email: '', phone: '', departmentId: '', bloodGroup: '', password: '', role: 'FACULTY' });
            fetchData();
        } catch (err) { setError(err.response?.data?.message || 'Error'); }
    };

    const handleEdit = (f) => {
        setForm({ name: f.name, rollNumber: f.rollNumber, email: f.email, phone: f.phone, departmentId: f.departmentId, bloodGroup: f.bloodGroup, password: '', role: f.role });
        setEditId(f.id);
        setOpen(true);
    };

    const handleDeleteClick = (id) => { setDeleteId(id); setConfirmOpen(true); };
    const handleConfirmDelete = async () => {
        try { await api.delete(`/faculty-admin/faculties/${deleteId}`); fetchData(); } catch { setError('Failed to remove faculty'); }
        setConfirmOpen(false); setDeleteId(null);
    };

    const handleExport = () => { window.open('http://localhost:8080/api/faculty-admin/faculties/export', '_blank'); };

    const filtered = faculties.filter(f =>
        (f.name || '').toLowerCase().includes((filters.name || '').toLowerCase()) &&
        (f.rollNumber || '').toLowerCase().includes((filters.rollNumber || '').toLowerCase()) &&
        (f.email || '').toLowerCase().includes((filters.email || '').toLowerCase()) &&
        (filters.department === '' || (f.departmentName || '') === filters.department)
    );

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 4 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 } }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 1 }}>
                    <Typography variant="h4" sx={{ fontWeight: 800, color: '#0f3460' }}>Faculty Management</Typography>
                    <Box>
                        <Button startIcon={<Download />} onClick={handleExport} sx={{ mr: 1, color: '#0f3460', borderColor: '#0f3460' }} variant="outlined">Export Excel</Button>
                        <Button startIcon={<Add />} variant="contained" onClick={() => { setEditId(null); setOpen(true); }}
                            sx={{ background: 'linear-gradient(135deg, #0f3460, #1a365d)', borderRadius: 2, fontWeight: 700 }}>Add Faculty</Button>
                    </Box>
                </Box>

                {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

                <Grid container spacing={2} sx={{ mb: 3 }}>
                    <Grid item xs={6} md={3}><TextField fullWidth size="small" label="Name" value={filters.name} onChange={e => setFilters({ ...filters, name: e.target.value })} /></Grid>
                    <Grid item xs={6} md={3}><TextField fullWidth size="small" label="Roll Number" value={filters.rollNumber} onChange={e => setFilters({ ...filters, rollNumber: e.target.value })} /></Grid>
                    <Grid item xs={6} md={3}><TextField fullWidth size="small" label="Email" value={filters.email} onChange={e => setFilters({ ...filters, email: e.target.value })} /></Grid>
                    <Grid item xs={6} md={3}>
                        <FormControl fullWidth size="small">
                            <InputLabel>Department</InputLabel>
                            <Select value={filters.department} label="Department" onChange={e => setFilters({ ...filters, department: e.target.value })}>
                                <MenuItem value="">All</MenuItem>
                                {departments.map(d => <MenuItem key={d.id} value={d.deptName}>{d.deptName}</MenuItem>)}
                            </Select>
                        </FormControl>
                    </Grid>
                </Grid>

                <Paper className="glass" sx={{ borderRadius: 3, overflow: 'auto' }}>
                    <Table sx={{ minWidth: 900 }}>
                        <TableHead sx={{ bgcolor: '#0f3460' }}>
                            <TableRow>{['Name', 'Faculty ID', 'Email', 'Phone', 'Department', 'Blood', 'Role', 'Hours', 'Actions'].map(h => (
                                <TableCell key={h} sx={{ color: '#ffffff', fontWeight: 700 }}>{h}</TableCell>
                            ))}</TableRow>
                        </TableHead>
                        <TableBody>
                            {filtered.map(f => (
                                <TableRow key={f.id} sx={{ '&:hover': { bgcolor: 'rgba(0,0,0,0.04)' } }}>
                                    <TableCell sx={{ color: '#1e293b', fontWeight: 600 }}>{f.name}</TableCell>
                                    <TableCell sx={{ color: '#1e293b' }}>{f.rollNumber}</TableCell>
                                    <TableCell sx={{ color: '#475569' }}>{f.email}</TableCell>
                                    <TableCell sx={{ color: '#475569' }}>{f.phone}</TableCell>
                                    <TableCell sx={{ color: '#1e293b', fontWeight: 500 }}>{f.departmentName}</TableCell>
                                    <TableCell sx={{ color: '#e94560', fontWeight: 600 }}>{f.bloodGroup}</TableCell>
                                    <TableCell><Chip label={f.role} size="small" color={f.role === 'HOD' ? 'warning' : 'primary'} sx={{ fontWeight: 600 }} /></TableCell>
                                    <TableCell sx={{ color: '#1e293b', fontWeight: 700 }}>{f.currentMonthHours || 0}</TableCell>
                                    <TableCell>
                                        <IconButton size="small" onClick={() => handleEdit(f)} color="primary" sx={{ mr: 1 }}><Edit fontSize="small" /></IconButton>
                                        <IconButton size="small" onClick={() => handleDeleteClick(f.id)} color="error"><Delete fontSize="small" /></IconButton>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Paper>

                <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
                    <DialogTitle sx={{ fontWeight: 700 }}>{editId ? 'Edit Faculty' : 'Add Faculty'}</DialogTitle>
                    <DialogContent>
                        <TextField fullWidth label="Name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} sx={{ mt: 1, mb: 2 }} />
                        <TextField fullWidth label="Faculty ID" value={form.rollNumber} onChange={e => setForm({ ...form, rollNumber: e.target.value })} sx={{ mb: 2 }} disabled={!!editId} />
                        <TextField fullWidth label="Email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} sx={{ mb: 2 }} disabled={!!editId} />
                        <TextField fullWidth label="Phone" value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} sx={{ mb: 2 }} />
                        <FormControl fullWidth sx={{ mb: 2 }}>
                            <InputLabel>Department</InputLabel>
                            <Select value={form.departmentId} label="Department" onChange={e => setForm({ ...form, departmentId: e.target.value })}>
                                {departments.map(d => <MenuItem key={d.id} value={d.id}>{d.deptName}</MenuItem>)}
                            </Select>
                        </FormControl>
                        <TextField fullWidth label="Blood Group" value={form.bloodGroup} onChange={e => setForm({ ...form, bloodGroup: e.target.value })} sx={{ mb: 2 }} />
                        <FormControl fullWidth sx={{ mb: 2 }}>
                            <InputLabel>Role</InputLabel>
                            <Select value={form.role} label="Role" onChange={e => setForm({ ...form, role: e.target.value })}>
                                <MenuItem value="HOD">HOD</MenuItem>
                                <MenuItem value="FACULTY">FACULTY</MenuItem>
                            </Select>
                        </FormControl>
                        <TextField fullWidth label="Password" type="password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} helperText={editId ? 'Leave blank to keep current' : ''} />
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setOpen(false)}>Cancel</Button>
                        <Button variant="contained" onClick={handleSave}>{editId ? 'Update' : 'Create'}</Button>
                    </DialogActions>
                </Dialog>
                <ConfirmDialog open={confirmOpen} title="Remove Faculty" content="Are you sure you want to remove this faculty?" onConfirm={handleConfirmDelete} onCancel={() => setConfirmOpen(false)} confirmText="Remove" />
            </Box>
        </Box>
    );
};

export default FacultyAdminPage;
