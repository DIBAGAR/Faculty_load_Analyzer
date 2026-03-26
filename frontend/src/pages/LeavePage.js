import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import { useAuth } from '../contexts/AuthContext';
import {
    Box, Typography, Button, TextField, Table, TableHead, TableRow, TableCell,
    TableBody, Paper, Chip, Dialog, DialogTitle, DialogContent, DialogActions, Alert, Grid,
    Select, MenuItem, InputLabel, FormControl, Tabs, Tab, TablePagination, TableContainer
} from '@mui/material';

const LeavePage = () => {
    const { user } = useAuth();
    const [tabIndex, setTabIndex] = useState(0);
    const [myLeaves, setMyLeaves] = useState([]);
    const [pendingLeaves, setPendingLeaves] = useState([]);
    const [historyLeaves, setHistoryLeaves] = useState([]);

    // Apply Leave Form
    const [openApply, setOpenApply] = useState(false);
    const [form, setForm] = useState({ type: 'LEAVE', fromDate: '', fromTime: '', toDate: '', toTime: '', reason: '', tempHodId: '' });

    // Reject Leave Dialog
    const [openReject, setOpenReject] = useState(false);
    const [rejectReason, setRejectReason] = useState('');
    const [selectedLeaveId, setSelectedLeaveId] = useState(null);

    // Cancel Leave Dialog
    const [openCancel, setOpenCancel] = useState(false);

    const [faculties, setFaculties] = useState([]);
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);

    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const deptId = localStorage.getItem('deptId');

    const isHodOrAdmin = ['HOD', 'TEMP_HOD', 'FACULTY_ADMIN', 'SUPER_ADMIN'].includes(user?.role);
    const isAdmin = ['FACULTY_ADMIN', 'SUPER_ADMIN'].includes(user?.role);
    const isHod = user?.role === 'HOD';

    useEffect(() => {
        fetchMyLeaves();
        if (isHodOrAdmin) {
            fetchPendingApprovals();
            fetchHistoryLeaves();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [user, deptId]);

    const fetchMyLeaves = async () => {
        try { const res = await api.get('/faculty/leaves'); setMyLeaves(res.data); } catch { }
    };

    const fetchFaculties = async () => {
        if (deptId && isHod) {
            try {
                const res = await api.get(`/faculty/dept/${deptId}`);
                setFaculties(res.data.filter(f => f.role !== 'HOD'));
            } catch { }
        }
    };

    useEffect(() => {
        if (openApply && isHod) { fetchFaculties(); }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [openApply, isHod, deptId]);

    const fetchPendingApprovals = async () => {
        try {
            if (isAdmin) {
                const res = await api.get('/hod/leaves/pending-admin');
                setPendingLeaves(res.data);
            } else if (deptId) {
                const res = await api.get(`/hod/leaves/pending/${deptId}`);
                setPendingLeaves(res.data);
            }
        } catch { }
    };

    const fetchHistoryLeaves = async () => {
        try {
            if (isAdmin) {
                const res = await api.get('/hod/leaves/history-admin');
                setHistoryLeaves(res.data);
            } else if (deptId) {
                const res = await api.get(`/hod/leaves/history/${deptId}`);
                setHistoryLeaves(res.data);
            }
        } catch { }
    };

    const handleApply = async () => {
        try {
            const payload = { ...form };
            if (!isHod) delete payload.tempHodId;
            else if (payload.tempHodId === '') delete payload.tempHodId;

            await api.post('/faculty/leaves', payload);
            setOpenApply(false);
            setSuccess('Leave applied successfully!');
            setTimeout(() => setSuccess(''), 3000);
            setForm({ type: 'LEAVE', fromDate: '', fromTime: '', toDate: '', toTime: '', reason: '', tempHodId: '' });
            fetchMyLeaves();
            setTabIndex(isHodOrAdmin ? 2 : 0); // Jump to My Leaves
        } catch (err) { setError(err.response?.data?.message || 'Error'); }
    };

    const handleApprove = async (id) => {
        try {
            await api.put(`/hod/leaves/${id}/approve`);
            fetchPendingApprovals();
            fetchHistoryLeaves();
            setSuccess('Leave approved successfully');
            setTimeout(() => setSuccess(''), 3000);
        } catch { }
    };

    const confirmReject = async () => {
        if (!rejectReason) { setError('Rejection reason is required.'); return; }
        try {
            await api.put(`/hod/leaves/${selectedLeaveId}/reject`, { reason: rejectReason });
            fetchPendingApprovals();
            fetchHistoryLeaves();
            setSuccess('Leave rejected successfully');
            setTimeout(() => setSuccess(''), 3000);
            setOpenReject(false);
            setRejectReason('');
            setSelectedLeaveId(null);
        } catch { setError('Failed to reject leave'); }
    };

    const confirmCancel = async () => {
        try {
            await api.delete(`/faculty/leaves/${selectedLeaveId}`);
            setSuccess('Leave successfully cancelled.');
            setTimeout(() => setSuccess(''), 3000);
            fetchMyLeaves();
            setOpenCancel(false);
            setSelectedLeaveId(null);
        } catch (err) { setError(err.response?.data?.message || 'Error cancelling leave'); }
    };

    const renderPendingTable = () => (
        <TableContainer component={Paper} className="glass" sx={{ borderRadius: 3, overflow: 'auto' }}>
            <Table size="small" sx={{ minWidth: 800 }}>
                <TableHead sx={{ bgcolor: '#0f3460' }}>
                    <TableRow>{['Faculty / Role', 'Department', 'Type', 'From', 'To', 'Reason', 'Actions'].map(h =>
                        <TableCell key={h} sx={{ color: '#ffffff', fontWeight: 700, py: 1.5 }}>{h}</TableCell>
                    )}</TableRow>
                </TableHead>
                <TableBody>
                    {pendingLeaves.length === 0 ? (
                        <TableRow><TableCell colSpan={6} align="center" sx={{ color: '#475569', py: 3 }}>No pending leaves to approve.</TableCell></TableRow>
                    ) : (
                        pendingLeaves.map(l => (
                            <TableRow key={l.id} sx={{ '&:hover': { bgcolor: 'rgba(0,0,0,0.04)' } }}>
                                <TableCell sx={{ color: '#1e293b', fontWeight: 600, fontSize: { xs: '0.75rem', md: '0.875rem' } }}>
                                    {l.faculty?.name || 'Unknown'} (Roll: {l.faculty?.rollNumber || 'N/A'})
                                    <br /><Chip size="small" label={l.faculty?.user?.role?.name?.replace('_', ' ') || 'FACULTY'} sx={{ mt: 0.5, fontWeight: 600, height: 20, fontSize: '0.65rem' }} />
                                </TableCell>
                                <TableCell><Chip label={l.faculty?.department?.deptName || 'N/A'} size="small" color="primary" sx={{ fontWeight: 600, height: 20, fontSize: '0.65rem' }} /></TableCell>
                                <TableCell><Chip label={Math.max(1, (l.type || 'LEAVE').replace('_', ' '))} size="small" color="secondary" sx={{ fontWeight: 600, height: 20, fontSize: '0.65rem' }} /></TableCell>
                                <TableCell sx={{ color: '#475569', fontSize: { xs: '0.75rem', md: '0.875rem' } }}>{l.fromDate} <br />{l.fromTime}</TableCell>
                                <TableCell sx={{ color: '#475569', fontSize: { xs: '0.75rem', md: '0.875rem' } }}>{l.toDate} <br />{l.toTime}</TableCell>
                                <TableCell sx={{ color: '#1e293b', maxWidth: 200, fontSize: { xs: '0.75rem', md: '0.875rem' } }}>{l.reason}</TableCell>
                                <TableCell>
                                    <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 1 }}>
                                        <Button size="small" color="success" variant="contained" sx={{ fontWeight: 700, px: 1 }} onClick={() => handleApprove(l.id)}>Approve</Button>
                                        <Button size="small" color="error" variant="contained" sx={{ fontWeight: 700, px: 1 }} onClick={() => { setSelectedLeaveId(l.id); setOpenReject(true); }}>Reject</Button>
                                    </Box>
                                </TableCell>
                            </TableRow>
                        ))
                    )}
                </TableBody>
            </Table>
        </TableContainer>
    );
    const renderHistoryTable = () => (
        <TableContainer component={Paper} className="glass" sx={{ borderRadius: 3, overflow: 'auto' }}>
            <Table size="small" sx={{ minWidth: 800 }}>
                <TableHead sx={{ bgcolor: '#1a365d' }}>
                    <TableRow>{['Faculty Name', 'Department', 'Type', 'From', 'To', 'Status', 'Feedback'].map(h =>
                        <TableCell key={h} sx={{ color: '#ffffff', fontWeight: 700, py: 1.5 }}>{h}</TableCell>
                    )}</TableRow>
                </TableHead>
                <TableBody>
                    {historyLeaves.length === 0 ? (
                        <TableRow><TableCell colSpan={6} align="center" sx={{ color: '#475569', py: 3 }}>No history found.</TableCell></TableRow>
                    ) : (
                        historyLeaves.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map(l => (
                            <TableRow key={l.id} sx={{ '&:hover': { bgcolor: 'rgba(0,0,0,0.04)' } }}>
                                <TableCell sx={{ color: '#1e293b', fontWeight: 600, fontSize: { xs: '0.75rem', md: '0.875rem' } }}>{l.faculty?.name || 'Unknown'}</TableCell>
                                <TableCell sx={{ color: '#475569', fontSize: { xs: '0.75rem', md: '0.875rem' } }}><Chip label={l.faculty?.department?.deptName || 'N/A'} size="small" variant="outlined" sx={{ height: 20, fontSize: '0.65rem' }} /></TableCell>
                                <TableCell sx={{ color: '#475569', fontSize: { xs: '0.75rem', md: '0.875rem' } }}>{(l.type || 'LEAVE').replace('_', ' ')}</TableCell>
                                <TableCell sx={{ color: '#475569', fontSize: { xs: '0.75rem', md: '0.875rem' } }}>{l.fromDate} <br />{l.fromTime}</TableCell>
                                <TableCell sx={{ color: '#475569', fontSize: { xs: '0.75rem', md: '0.875rem' } }}>{l.toDate} <br />{l.toTime}</TableCell>
                                <TableCell>
                                    <Chip label={l.status} size="small" sx={{ fontWeight: 700, height: 20, fontSize: '0.65rem' }}
                                        color={l.status === 'APPROVED' ? 'success' : 'error'} />
                                </TableCell>
                                <TableCell sx={{ color: '#e94560', fontWeight: 500, fontSize: { xs: '0.75rem', md: '0.875rem' } }}>{l.rejectionReason || '-'}</TableCell>
                            </TableRow>
                        ))
                    )}
                </TableBody>
            </Table>
            <TablePagination
                component="div"
                count={historyLeaves.length}
                page={page}
                onPageChange={(e, newPage) => setPage(newPage)}
                rowsPerPage={rowsPerPage}
                onRowsPerPageChange={(e) => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0); }}
                rowsPerPageOptions={[10, 25]}
            />
        </TableContainer>
    );

    const renderMyLeavesTable = () => (
        <TableContainer component={Paper} className="glass" sx={{ borderRadius: 3, overflow: 'auto' }}>
            <Table size="small" sx={{ minWidth: 800 }}>
                <TableHead sx={{ bgcolor: '#0f3460' }}>
                    <TableRow>{['Type', 'From', 'To', 'Reason', 'Status', 'Feedback', 'Actions'].map(h =>
                        <TableCell key={h} sx={{ color: '#ffffff', fontWeight: 700, py: 1.5 }}>{h}</TableCell>
                    )}</TableRow>
                </TableHead>
                <TableBody>
                    {myLeaves.length === 0 ? (
                        <TableRow><TableCell colSpan={6} align="center" sx={{ color: '#475569', py: 3 }}>No leave requests found.</TableCell></TableRow>
                    ) : (
                        myLeaves.slice(0, 10).map(l => (
                            <TableRow key={l.id} sx={{ '&:hover': { bgcolor: 'rgba(0,0,0,0.04)' } }}>
                                <TableCell sx={{ color: '#475569', fontSize: { xs: '0.75rem', md: '0.875rem' }, fontWeight: 700 }}>{(l.type || 'LEAVE').replace('_', ' ')}</TableCell>
                                <TableCell sx={{ color: '#475569', fontSize: { xs: '0.75rem', md: '0.875rem' } }}>{l.fromDate} <br />{l.fromTime}</TableCell>
                                <TableCell sx={{ color: '#475569', fontSize: { xs: '0.75rem', md: '0.875rem' } }}>{l.toDate} <br />{l.toTime}</TableCell>
                                <TableCell sx={{ color: '#1e293b', maxWidth: 250, fontSize: { xs: '0.75rem', md: '0.875rem' } }}>{l.reason}</TableCell>
                                <TableCell>
                                    <Chip
                                        label={l.status}
                                        size="small"
                                        sx={{
                                            fontWeight: 800,
                                            height: 22,
                                            fontSize: '0.7rem',
                                            boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
                                        }}
                                        color={l.status === 'APPROVED' ? 'success' : l.status === 'REJECTED' ? 'error' : 'warning'}
                                    />
                                </TableCell>
                                <TableCell sx={{ color: '#e94560', fontWeight: 500, maxWidth: 200, fontSize: { xs: '0.75rem', md: '0.875rem' } }}>{l.rejectionReason || '-'}</TableCell>
                                <TableCell>
                                    {l.status === 'PENDING' && (
                                        <Button size="small" color="error" variant="outlined" sx={{ fontWeight: 700, fontSize: '0.7rem', px: 1 }} onClick={() => { setSelectedLeaveId(l.id); setOpenCancel(true); }}>Cancel</Button>
                                    )}
                                </TableCell>
                            </TableRow>
                        ))
                    )}
                </TableBody>
            </Table>
        </TableContainer>
    );

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 4 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 } }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                    <Typography variant="h4" sx={{ fontWeight: 800, color: '#0f3460' }}>Leave Management</Typography>
                    <Button variant="contained" onClick={() => setOpenApply(true)}
                        sx={{ background: 'linear-gradient(135deg, #0f3460, #1a365d)', borderRadius: 2, fontWeight: 700, px: 3, py: 1.5 }}>
                        Apply Leave
                    </Button>
                </Box>

                {error && <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError('')}>{error}</Alert>}
                {success && <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccess('')}>{success}</Alert>}

                {isHodOrAdmin ? (
                    <Box sx={{ width: '100%' }}>
                        <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
                            <Tabs
                                value={tabIndex}
                                onChange={(e, val) => setTabIndex(val)}
                                textColor="primary"
                                indicatorColor="primary"
                                variant="scrollable"
                                scrollButtons="auto"
                                allowScrollButtonsMobile
                                sx={{
                                    '& .MuiTab-root': {
                                        fontWeight: 700,
                                        fontSize: { xs: '0.75rem', md: '0.875rem' },
                                        minWidth: { xs: 120, md: 160 }
                                    }
                                }}
                            >
                                <Tab label={`Pending (${pendingLeaves.length})`} />
                                <Tab label="History" />
                                <Tab label="My Leaves" />
                            </Tabs>
                        </Box>
                        {tabIndex === 0 && renderPendingTable()}
                        {tabIndex === 1 && renderHistoryTable()}
                        {tabIndex === 2 && renderMyLeavesTable()}
                    </Box>
                ) : (
                    <>
                        <Typography variant="h5" sx={{ mb: 2, fontWeight: 700, color: '#0f3460' }}>My Leaves</Typography>
                        {renderMyLeavesTable()}
                    </>
                )}

                {/* Apply Leave Dialog */}
                <Dialog open={openApply} onClose={() => setOpenApply(false)} maxWidth="sm" fullWidth PaperProps={{ sx: { borderRadius: 3 } }}>
                    <DialogTitle sx={{ fontWeight: 800, color: '#0f3460', borderBottom: '1px solid #eee' }}>Apply for Leave or On-Duty</DialogTitle>
                    <DialogContent sx={{ p: 3, pt: 3 }}>
                        <Grid container spacing={3} sx={{ mt: 0.5 }}>
                            <Grid item xs={12}>
                                <FormControl fullWidth size="small">
                                    <InputLabel id="type-label">Application Type</InputLabel>
                                    <Select labelId="type-label" label="Application Type" value={form.type} onChange={e => setForm({ ...form, type: e.target.value })}>
                                        <MenuItem value="LEAVE">Leave</MenuItem>
                                        <MenuItem value="ON_DUTY">On Duty</MenuItem>
                                    </Select>
                                </FormControl>
                            </Grid>
                            <Grid item xs={6}><TextField fullWidth size="small" label="From Date" type="date" value={form.fromDate} onChange={e => setForm({ ...form, fromDate: e.target.value })} InputLabelProps={{ shrink: true }} required /></Grid>
                            <Grid item xs={6}><TextField fullWidth size="small" label="From Time" type="time" value={form.fromTime} onChange={e => setForm({ ...form, fromTime: e.target.value })} InputLabelProps={{ shrink: true }} required /></Grid>
                            <Grid item xs={6}><TextField fullWidth size="small" label="To Date" type="date" value={form.toDate} onChange={e => setForm({ ...form, toDate: e.target.value })} InputLabelProps={{ shrink: true }} required /></Grid>
                            <Grid item xs={6}><TextField fullWidth size="small" label="To Time" type="time" value={form.toTime} onChange={e => setForm({ ...form, toTime: e.target.value })} InputLabelProps={{ shrink: true }} required /></Grid>
                            <Grid item xs={12}><TextField fullWidth size="small" multiline rows={3} label="Reason / Details" placeholder="Please describe briefly..." value={form.reason} onChange={e => setForm({ ...form, reason: e.target.value })} required /></Grid>
                            {isHod && (
                                <Grid item xs={12}>
                                    <FormControl fullWidth>
                                        <InputLabel id="temp-hod-label">Temporary Delegate HOD (Optional)</InputLabel>
                                        <Select labelId="temp-hod-label" value={form.tempHodId} label="Temporary Delegate HOD (Optional)" onChange={e => setForm({ ...form, tempHodId: e.target.value })}>
                                            <MenuItem value=""><em>None</em></MenuItem>
                                            {faculties.map(f => <MenuItem key={f.id} value={f.id}>{f.rollNumber} - {f.name}</MenuItem>)}
                                        </Select>
                                    </FormControl>
                                </Grid>
                            )}
                        </Grid>
                    </DialogContent>
                    <DialogActions sx={{ p: 3, pt: 0, borderTop: 'none' }}>
                        <Button onClick={() => setOpenApply(false)} sx={{ fontWeight: 700 }}>Cancel</Button>
                        <Button variant="contained" onClick={handleApply} sx={{ fontWeight: 700, bgcolor: '#0f3460', '&:hover': { bgcolor: '#1a365d' } }}>Submit Application</Button>
                    </DialogActions>
                </Dialog>

                {/* Reject Dialog */}
                <Dialog open={openReject} onClose={() => { setOpenReject(false); setRejectReason(''); }} maxWidth="xs" fullWidth PaperProps={{ sx: { borderRadius: 3 } }}>
                    <DialogTitle sx={{ fontWeight: 800, color: '#e94560' }}>Reject Leave Request</DialogTitle>
                    <DialogContent>
                        <Typography variant="body2" sx={{ mb: 2, color: '#475569' }}>Please provide a reason for rejecting this leave application. This will be visible to the faculty.</Typography>
                        <TextField fullWidth autoFocus multiline rows={3} label="Rejection Reason" value={rejectReason} onChange={e => setRejectReason(e.target.value)} required />
                    </DialogContent>
                    <DialogActions sx={{ p: 2, pt: 0 }}>
                        <Button onClick={() => { setOpenReject(false); setRejectReason(''); }} sx={{ fontWeight: 700, color: '#475569' }}>Cancel</Button>
                        <Button variant="contained" color="error" onClick={confirmReject} sx={{ fontWeight: 700 }}>Confirm Rejection</Button>
                    </DialogActions>
                </Dialog>

                {/* Cancel Dialog */}
                <Dialog open={openCancel} onClose={() => setOpenCancel(false)} maxWidth="xs" fullWidth PaperProps={{ sx: { borderRadius: 3 } }}>
                    <DialogTitle sx={{ fontWeight: 800, color: '#e94560' }}>Cancel Leave Application</DialogTitle>
                    <DialogContent>
                        <Typography variant="body1" sx={{ color: '#0f172a' }}>Are you sure you want to cancel this pending leave application? This action cannot be undone.</Typography>
                    </DialogContent>
                    <DialogActions sx={{ p: 2 }}>
                        <Button onClick={() => setOpenCancel(false)} sx={{ fontWeight: 700, color: '#475569' }}>Go Back</Button>
                        <Button variant="contained" color="error" onClick={confirmCancel} sx={{ fontWeight: 700 }}>Yes, Cancel Leave</Button>
                    </DialogActions>
                </Dialog>

            </Box>
        </Box>
    );
};

export default LeavePage;
