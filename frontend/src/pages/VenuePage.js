import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import {
    Box, Typography, Button, TextField, Grid, Card, CardContent, Dialog, DialogTitle,
    DialogContent, DialogActions, Table, TableHead, TableRow, TableCell, TableBody,
    Paper, FormControl, InputLabel, Select, MenuItem, Chip, Alert, IconButton, TableContainer
} from '@mui/material';
import { Add, Search, Edit, Delete, Download } from '@mui/icons-material';
import ConfirmDialog from '../components/ConfirmDialog';

const VenuePage = () => {
    const [venues, setVenues] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [open, setOpen] = useState(false);
    const [editId, setEditId] = useState(null);
    const [form, setForm] = useState({ block: '', venueName: '', venueType: '', departmentId: '', capacity: '' });
    const [filters, setFilters] = useState({ block: '', department: '', venueType: '', capacity: '' });
    const [error, setError] = useState('');
    const [confirmOpen, setConfirmOpen] = useState(false);
    const [deleteId, setDeleteId] = useState(null);

    useEffect(() => { fetchData(); }, []);

    const fetchData = async () => {
        try {
            const [v, d] = await Promise.all([api.get('/venue-admin/venues'), api.get('/department')]);
            setVenues(v.data); setDepartments(d.data);
        } catch { setError('Failed to load'); }
    };

    const handleSave = async () => {
        try {
            if (editId) await api.put(`/venue-admin/venues/${editId}`, form);
            else await api.post('/venue-admin/venues', form);
            setOpen(false); setEditId(null); setForm({ block: '', venueName: '', venueType: '', departmentId: '', capacity: '' }); fetchData();
        } catch (err) { setError(err.response?.data?.message || 'Error'); }
    };

    const handleEdit = (v) => {
        setForm({ block: v.block, venueName: v.venueName, venueType: v.venueType, departmentId: v.department?.id || '', capacity: v.capacity || '' });
        setEditId(v.id); setOpen(true);
    };

    const handleDeleteClick = (id) => { setDeleteId(id); setConfirmOpen(true); };
    const handleConfirmDelete = async () => {
        try { await api.delete(`/venue-admin/venues/${deleteId}`); fetchData(); } catch { setError('Failed to delete venue'); }
        setConfirmOpen(false); setDeleteId(null);
    };
    const handleExport = () => { window.open('http://localhost:8080/api/venue-admin/venues/export', '_blank'); };

    const filtered = venues.filter(v =>
        v.block?.toLowerCase().includes(filters.block.toLowerCase()) &&
        (filters.department === '' || v.department?.deptName === filters.department) &&
        (filters.venueType === '' || v.venueType === filters.venueType)
    );

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 4 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 } }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 1 }}>
                    <Typography variant="h4" sx={{ fontWeight: 800, color: '#0f3460' }}>Venue Management</Typography>
                    <Box>
                        <Button startIcon={<Download />} onClick={handleExport} sx={{ mr: 1, color: '#0f3460', borderColor: '#0f3460' }} variant="outlined">Export Excel</Button>
                        <Button startIcon={<Add />} variant="contained" onClick={() => { setEditId(null); setOpen(true); }}
                            sx={{ background: 'linear-gradient(135deg, #0f3460, #1a365d)', borderRadius: 2, fontWeight: 700 }}>Add Venue</Button>
                    </Box>
                </Box>
                {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
                <Paper className="glass" sx={{ borderRadius: 3, p: 3, mb: 4 }}>
                    <Grid container spacing={2}>
                        <Grid item xs={6} md={3}><TextField fullWidth size="small" label="Block" value={filters.block} onChange={e => setFilters({ ...filters, block: e.target.value })} /></Grid>
                        <Grid item xs={6} md={3}>
                            <FormControl fullWidth size="small">
                                <InputLabel>Department</InputLabel>
                                <Select value={filters.department} label="Department" onChange={e => setFilters({ ...filters, department: e.target.value })}>
                                    <MenuItem value="">All</MenuItem>{departments.map(d => <MenuItem key={d.id} value={d.deptName}>{d.deptName}</MenuItem>)}
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={6} md={3}>
                            <FormControl fullWidth size="small">
                                <InputLabel>Type</InputLabel>
                                <Select value={filters.venueType} label="Type" onChange={e => setFilters({ ...filters, venueType: e.target.value })}>
                                    <MenuItem value="">All</MenuItem><MenuItem value="LAB">Lab</MenuItem><MenuItem value="CLASSROOM">Classroom</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={6} md={3}><TextField fullWidth size="small" label="Min Capacity" type="number" value={filters.capacity} onChange={e => setFilters({ ...filters, capacity: e.target.value })} /></Grid>
                    </Grid>
                </Paper>
                <Paper className="glass" sx={{ borderRadius: 3, overflow: 'auto' }}>
                    <Table sx={{ minWidth: 800 }}>
                        <TableHead sx={{ bgcolor: '#0f3460' }}>
                            <TableRow>{['Block', 'Name', 'Type', 'Department', 'Capacity', 'Actions'].map(h => <TableCell key={h} sx={{ color: '#ffffff', fontWeight: 700 }}>{h}</TableCell>)}</TableRow>
                        </TableHead>
                        <TableBody>
                            {filtered.map(v => (
                                <TableRow key={v.id} sx={{ '&:hover': { bgcolor: 'rgba(0,0,0,0.04)' } }}>
                                    <TableCell sx={{ color: '#1e293b', fontWeight: 500 }}>{v.block}</TableCell>
                                    <TableCell sx={{ color: '#1e293b', fontWeight: 500 }}>{v.venueName}</TableCell>
                                    <TableCell><Chip label={v.venueType} size="small" sx={{ fontWeight: 600, color: '#fff', bgcolor: v.venueType === 'LAB' ? '#e94560' : '#0f3460' }} /></TableCell>
                                    <TableCell sx={{ color: '#475569' }}>{v.department?.deptName || 'N/A'}</TableCell>
                                    <TableCell sx={{ color: '#1e293b', fontWeight: 600 }}>{v.capacity}</TableCell>
                                    <TableCell>
                                        <IconButton size="small" color="primary" sx={{ mr: 1 }} onClick={() => handleEdit(v)}><Edit fontSize="small" /></IconButton>
                                        <IconButton size="small" color="error" onClick={() => handleDeleteClick(v.id)}><Delete fontSize="small" /></IconButton>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Paper>
                <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
                    <DialogTitle sx={{ fontWeight: 700 }}>{editId ? 'Edit' : 'Add'} Venue</DialogTitle>
                    <DialogContent>
                        <TextField fullWidth label="Block" value={form.block} onChange={e => setForm({ ...form, block: e.target.value })} sx={{ mt: 1, mb: 2 }} />
                        <TextField fullWidth label="Venue Name" value={form.venueName} onChange={e => setForm({ ...form, venueName: e.target.value })} sx={{ mb: 2 }} />
                        <FormControl fullWidth sx={{ mb: 2 }}><InputLabel>Type</InputLabel>
                            <Select value={form.venueType} label="Type" onChange={e => setForm({ ...form, venueType: e.target.value })}>
                                <MenuItem value="LAB">Lab</MenuItem><MenuItem value="CLASSROOM">Classroom</MenuItem>
                            </Select>
                        </FormControl>
                        <FormControl fullWidth sx={{ mb: 2 }}><InputLabel>Department</InputLabel>
                            <Select value={form.departmentId} label="Department" onChange={e => setForm({ ...form, departmentId: e.target.value })}>
                                {departments.map(d => <MenuItem key={d.id} value={d.id}>{d.deptName}</MenuItem>)}
                            </Select>
                        </FormControl>
                        <TextField fullWidth label="Capacity" type="number" value={form.capacity} onChange={e => setForm({ ...form, capacity: e.target.value })} />
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setOpen(false)}>Cancel</Button>
                        <Button variant="contained" onClick={handleSave}>{editId ? 'Update' : 'Create'}</Button>
                    </DialogActions>
                </Dialog>
                <ConfirmDialog open={confirmOpen} title="Delete Venue" content="Are you sure you want to delete this venue?" onConfirm={handleConfirmDelete} onCancel={() => setConfirmOpen(false)} confirmText="Delete" />
            </Box>
        </Box>
    );
};

export default VenuePage;
