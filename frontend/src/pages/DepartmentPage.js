import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import {
    Box, Typography, Button, TextField, Table, TableHead, TableRow, TableCell,
    TableBody, Paper, IconButton, Alert, Dialog, DialogTitle, DialogContent, DialogActions
} from '@mui/material';
import { Add, Edit, Delete } from '@mui/icons-material';
import ConfirmDialog from '../components/ConfirmDialog';

const DepartmentPage = () => {
    const [departments, setDepartments] = useState([]);
    const [open, setOpen] = useState(false);
    const [editId, setEditId] = useState(null);
    const [form, setForm] = useState({ deptCode: '', deptName: '' });
    const [error, setError] = useState('');
    const [confirmOpen, setConfirmOpen] = useState(false);
    const [deleteId, setDeleteId] = useState(null);

    useEffect(() => { fetchData(); }, []);

    const fetchData = async () => {
        try { const res = await api.get('/department'); setDepartments(res.data); } catch { setError('Failed to load'); }
    };

    const handleSave = async () => {
        try {
            if (editId) await api.put(`/department/${editId}`, form);
            else await api.post('/department', form);
            setOpen(false); setEditId(null); setForm({ deptCode: '', deptName: '' }); fetchData();
        } catch (err) { setError(err.response?.data?.message || 'Error'); }
    };

    const handleEdit = (d) => { setForm({ deptCode: d.deptCode, deptName: d.deptName }); setEditId(d.id); setOpen(true); };
    const handleDeleteClick = (id) => { setDeleteId(id); setConfirmOpen(true); };
    const handleConfirmDelete = async () => {
        try { await api.delete(`/department/${deleteId}`); fetchData(); } catch { setError('Failed to delete'); }
        setConfirmOpen(false); setDeleteId(null);
    };

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 4 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 } }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                    <Typography variant="h4" sx={{ fontWeight: 800, color: '#0f3460' }}>Department Management</Typography>
                    <Button startIcon={<Add />} variant="contained" onClick={() => { setEditId(null); setForm({ deptCode: '', deptName: '' }); setOpen(true); }}
                        sx={{ background: 'linear-gradient(135deg, #0f3460, #1a365d)', borderRadius: 2, fontWeight: 700 }}>Add Department</Button>
                </Box>
                {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
                <Paper className="glass" sx={{ borderRadius: 3, overflow: 'hidden' }}>
                    <Table sx={{ minWidth: 500 }}>
                        <TableHead sx={{ bgcolor: '#0f3460' }}>
                            <TableRow>{['Code', 'Name', 'Actions'].map(h => <TableCell key={h} sx={{ color: '#ffffff', fontWeight: 700 }}>{h}</TableCell>)}</TableRow>
                        </TableHead>
                        <TableBody>
                            {departments.map(d => (
                                <TableRow key={d.id} sx={{ '&:hover': { bgcolor: 'rgba(0,0,0,0.04)' } }}>
                                    <TableCell sx={{ color: '#1e293b', fontWeight: 500 }}>{d.deptCode}</TableCell>
                                    <TableCell sx={{ color: '#1e293b', fontWeight: 500 }}>{d.deptName}</TableCell>
                                    <TableCell>
                                        <IconButton size="small" onClick={() => handleEdit(d)} color="primary" sx={{ mr: 1 }}><Edit fontSize="small" /></IconButton>
                                        <IconButton size="small" onClick={() => handleDeleteClick(d.id)} color="error"><Delete fontSize="small" /></IconButton>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Paper>
                <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
                    <DialogTitle sx={{ fontWeight: 700 }}>{editId ? 'Edit' : 'Add'} Department</DialogTitle>
                    <DialogContent>
                        <TextField fullWidth label="Department Code" value={form.deptCode} onChange={e => setForm({ ...form, deptCode: e.target.value })} sx={{ mt: 1, mb: 2 }} />
                        <TextField fullWidth label="Department Name" value={form.deptName} onChange={e => setForm({ ...form, deptName: e.target.value })} />
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setOpen(false)}>Cancel</Button>
                        <Button variant="contained" onClick={handleSave}>{editId ? 'Update' : 'Create'}</Button>
                    </DialogActions>
                </Dialog>
                <ConfirmDialog open={confirmOpen} title="Delete Department" content="Are you sure you want to delete this department? Associated data will be lost." onConfirm={handleConfirmDelete} onCancel={() => setConfirmOpen(false)} confirmText="Delete" />
            </Box>
        </Box>
    );
};

export default DepartmentPage;
