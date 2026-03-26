import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import {
    Box, Card, CardContent, Typography, Grid, Button, Dialog, DialogTitle,
    DialogContent, DialogActions, TextField, Select, MenuItem, FormControl,
    InputLabel, Table, TableHead, TableRow, TableCell, TableBody, IconButton,
    Alert, Chip, Paper, TableContainer
} from '@mui/material';
import { Delete, Add, People, School, Room, Download } from '@mui/icons-material';
import ConfirmDialog from '../components/ConfirmDialog';

const SuperAdminPage = () => {
    const [stats, setStats] = useState(null);
    const [admins, setAdmins] = useState([]);
    const [open, setOpen] = useState(false);
    const [form, setForm] = useState({ name: '', rollNumber: '', email: '', phone: '', adminType: '', password: '' });
    const [error, setError] = useState('');
    const [confirmOpen, setConfirmOpen] = useState(false);
    const [deleteId, setDeleteId] = useState(null);

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [s, a] = await Promise.all([api.get('/super-admin/dashboard'), api.get('/super-admin/admins')]);
            setStats(s.data);
            setAdmins(a.data);
        } catch (err) { setError('Failed to load data'); }
    };

    const handleCreate = async () => {
        try {
            await api.post('/super-admin/admins', form);
            setOpen(false);
            setForm({ name: '', rollNumber: '', email: '', phone: '', adminType: '', password: '' });
            fetchData();
        } catch (err) { setError(err.response?.data?.message || 'Error'); }
    };

    const handleDeleteClick = (id) => { setDeleteId(id); setConfirmOpen(true); };
    const handleConfirmDelete = async () => {
        try { await api.delete(`/super-admin/admins/${deleteId}`); fetchData(); } catch { setError('Failed to remove admin'); }
        setConfirmOpen(false); setDeleteId(null);
    };

    const adminTypes = ['FACULTY_ADMIN', 'DEPARTMENT_ADMIN', 'COURSE_ADMIN', 'VENUE_ADMIN'];
    const cardColors = ['#e94560', '#0f3460', '#533483', '#16c79a'];

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 4 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 } }}>
                <Typography variant="h4" sx={{ fontWeight: 800, mb: 3, color: '#0f3460' }}>
                    Super Admin Dashboard
                </Typography>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                {stats && (
                    <Grid container spacing={3} sx={{ mb: 4 }}>
                        {stats.departments?.map((dept, i) => (
                            <Grid item xs={12} sm={6} md={4} key={dept.deptId}>
                                <Card sx={{
                                    borderRadius: 3, background: `linear-gradient(135deg, ${cardColors[i % 4]}dd, ${cardColors[i % 4]}88)`,
                                    color: '#ffffff', boxShadow: '0 4px 15px rgba(0,0,0,0.2)', transition: 'transform 0.2s',
                                    '&:hover': { transform: 'translateY(-4px)' }
                                }}>
                                    <CardContent>
                                        <Typography variant="h6" sx={{ fontWeight: 700 }}>{dept.deptName}</Typography>
                                        <Box sx={{ display: 'flex', gap: 2, mt: 2, flexWrap: 'wrap' }}>
                                            <Chip icon={<People />} label={`${dept.facultyCount} Faculty`} sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: '#ffffff' }} />
                                            <Chip icon={<School />} label={`${dept.courseCount} Courses`} sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: '#ffffff' }} />
                                            <Chip icon={<Room />} label={`${dept.labCount} Labs`} sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: '#ffffff' }} />
                                            <Chip icon={<Room />} label={`${dept.classroomCount} Classrooms`} sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: '#ffffff' }} />
                                        </Box>
                                    </CardContent>
                                </Card>
                            </Grid>
                        ))}
                    </Grid>
                )}

                {/* Admin List */}
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                    <Typography variant="h5" sx={{ fontWeight: 800, color: '#0f3460' }}>Admins</Typography>
                    <Button startIcon={<Add />} variant="contained" onClick={() => setOpen(true)}
                        sx={{ background: 'linear-gradient(135deg, #0f3460, #1a365d)', borderRadius: 2, fontWeight: 700 }}>
                        Add Admin
                    </Button>
                </Box>

                <Paper className="glass" sx={{ borderRadius: 3, overflow: 'hidden' }}>
                    <Table sx={{ minWidth: 700 }}>
                        <TableHead sx={{ bgcolor: '#0f3460' }}>
                            <TableRow>
                                {['Name', 'Email', 'Roll No', 'Role', 'Status', 'Action'].map(h => (
                                    <TableCell key={h} sx={{ color: '#ffffff', fontWeight: 700 }}>{h}</TableCell>
                                ))}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {admins.map(a => (
                                <TableRow key={a.id} sx={{ '&:hover': { bgcolor: 'rgba(0,0,0,0.04)' } }}>
                                    <TableCell sx={{ color: '#1e293b', fontWeight: 600 }}>{a.name}</TableCell>
                                    <TableCell sx={{ color: '#475569' }}>{a.email}</TableCell>
                                    <TableCell sx={{ color: '#1e293b' }}>{a.rollNumber}</TableCell>
                                    <TableCell><Chip label={a.role?.replace('_', ' ')} size="small" color="primary" sx={{ fontWeight: 600 }} /></TableCell>
                                    <TableCell><Chip label={a.isActive ? 'Active' : 'Inactive'} color={a.isActive ? 'success' : 'error'} size="small" sx={{ fontWeight: 600 }} /></TableCell>
                                    <TableCell>
                                        <IconButton size="small" color="error" onClick={() => handleDeleteClick(a.id)}><Delete fontSize="small" /></IconButton>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Paper>

                {/* Create Dialog */}
                <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
                    <DialogTitle sx={{ fontWeight: 700 }}>Add New Admin</DialogTitle>
                    <DialogContent>
                        <TextField fullWidth label="Name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} sx={{ mt: 1, mb: 2 }} />
                        <TextField fullWidth label="Faculty ID" value={form.rollNumber} onChange={e => setForm({ ...form, rollNumber: e.target.value })} sx={{ mb: 2 }} />
                        <TextField fullWidth label="Email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} sx={{ mb: 2 }} />
                        <TextField fullWidth label="Phone" value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} sx={{ mb: 2 }} />
                        <FormControl fullWidth sx={{ mb: 2 }}>
                            <InputLabel>Admin Type</InputLabel>
                            <Select value={form.adminType} label="Admin Type" onChange={e => setForm({ ...form, adminType: e.target.value })}>
                                {adminTypes.map(t => <MenuItem key={t} value={t}>{t.replace('_', ' ')}</MenuItem>)}
                            </Select>
                        </FormControl>
                        <TextField fullWidth label="Password" type="password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} />
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setOpen(false)}>Cancel</Button>
                        <Button variant="contained" onClick={handleCreate}>Create</Button>
                    </DialogActions>
                </Dialog>
                <ConfirmDialog open={confirmOpen} title="Remove Admin" content="Are you sure you want to remove this admin?" onConfirm={handleConfirmDelete} onCancel={() => setConfirmOpen(false)} confirmText="Remove" />
            </Box>
        </Box>
    );
};

export default SuperAdminPage;
