import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import {
    Box, Typography, Button, Table, TableHead, TableRow, TableCell,
    TableBody, Paper, Chip, Alert, TablePagination, Divider,
    Dialog, DialogTitle, DialogContent, DialogActions, TextField,
    Select, MenuItem, CircularProgress
} from '@mui/material';
import { WarningAmber } from '@mui/icons-material';

const HodLeaveApprovalPage = () => {
    const [pendingLeaves, setPendingLeaves] = useState([]);
    const [historyLeaves, setHistoryLeaves] = useState([]);
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Reject dialog
    const [rejectOpen, setRejectOpen] = useState(false);
    const [rejectId, setRejectId] = useState(null);
    const [rejectReason, setRejectReason] = useState('');

    // Unresolved slots popup
    const [unresolvedOpen, setUnresolvedOpen] = useState(false);
    const [unresolvedSlots, setUnresolvedSlots] = useState([]);
    const [approvedContext, setApprovedContext] = useState(null);
    const [slotAssignments, setSlotAssignments] = useState({});
    const [assigning, setAssigning] = useState(false);

    useEffect(() => {
        fetchPendingApprovals();
        fetchHistoryLeaves();
    }, []);

    const fetchPendingApprovals = async () => {
        try {
            const res = await api.get('/hod/leaves/pending-admin');
            setPendingLeaves(res.data);
        } catch { setError('Failed to fetch pending HOD leaves.'); }
    };

    const fetchHistoryLeaves = async () => {
        try {
            const res = await api.get('/hod/leaves/history-admin');
            setHistoryLeaves(res.data);
        } catch { }
    };

    const handleApprove = async (id) => {
        try {
            const res = await api.put(`/hod/leaves/${id}/approve`);
            fetchPendingApprovals();
            fetchHistoryLeaves();
            const unresolved = res.data.unresolvedSlots || [];
            if (unresolved.length > 0) {
                setUnresolvedSlots(unresolved);
                setSlotAssignments({});
                setApprovedContext({
                    facultyName: res.data.facultyName,
                    fromDate: res.data.fromDate,
                    toDate: res.data.toDate
                });
                setUnresolvedOpen(true);
            } else {
                setSuccess('Leave approved. All work slots were automatically reassigned.');
                setTimeout(() => setSuccess(''), 4000);
            }
        } catch {
            setError('Failed to approve leave');
            setTimeout(() => setError(''), 3000);
        }
    };

    const handleSlotAssign = async (slot, idx) => {
        setAssigning(true);
        try {
            await api.post('/hod/work/manual-assign', {
                date: slot.date,
                hour: slot.hour,
                courseId: slot.courseId,
                venueId: slot.venueId,
                timetableSlotId: slot.timetableSlotId,
                facultyId: slotAssignments[idx],
                slotType: slot.slotType
            });
            const remaining = unresolvedSlots.filter((_, i) => i !== idx);
            setUnresolvedSlots(remaining);
            setSlotAssignments(prev => { const next = { ...prev }; delete next[idx]; return next; });
            if (remaining.length === 0) {
                setUnresolvedOpen(false);
                setSuccess('All unresolved slots have been manually assigned.');
                setTimeout(() => setSuccess(''), 4000);
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Assignment failed');
            setTimeout(() => setError(''), 3000);
        } finally { setAssigning(false); }
    };

    const handleRejectClick = (id) => { setRejectId(id); setRejectReason(''); setRejectOpen(true); };

    const handleConfirmReject = async () => {
        if (!rejectReason.trim()) { setError('Rejection reason is required'); return; }
        try {
            await api.put(`/hod/leaves/${rejectId}/reject`, { reason: rejectReason });
            fetchPendingApprovals(); fetchHistoryLeaves();
            setSuccess('HOD Leave rejected');
            setTimeout(() => setSuccess(''), 3000);
        } catch { setError('Failed to reject leave'); setTimeout(() => setError(''), 3000); }
        setRejectOpen(false);
    };

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 4 }}>
            <Navbar />
            <Box sx={{ p: { xs: 2, md: 4 } }}>
                <Typography variant="h4" sx={{ fontWeight: 800, mb: 3, color: '#0f3460' }}>HOD Leave Approvals</Typography>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

                {/* Pending leaves table */}
                <Paper className="glass" sx={{ borderRadius: 3, overflow: 'auto', mb: 4 }}>
                    <Table sx={{ minWidth: 800 }}>
                        <TableHead sx={{ bgcolor: '#0f3460' }}>
                            <TableRow>{['HOD Name', 'Department', 'Type', 'From', 'To', 'Reason', 'Temp HOD', 'Actions'].map(h =>
                                <TableCell key={h} sx={{ color: '#fff', fontWeight: 700 }}>{h}</TableCell>
                            )}</TableRow>
                        </TableHead>
                        <TableBody>
                            {pendingLeaves.length === 0 ? (
                                <TableRow><TableCell colSpan={8} align="center" sx={{ color: '#475569' }}>No pending HOD leaves to approve.</TableCell></TableRow>
                            ) : pendingLeaves.map(l => (
                                <TableRow key={l.id} sx={{ '&:hover': { bgcolor: 'rgba(0,0,0,0.04)' } }}>
                                    <TableCell sx={{ fontWeight: 700, color: '#0f3460' }}>{l.faculty?.name || '—'}</TableCell>
                                    <TableCell><Chip label={l.faculty?.department?.deptName || 'N/A'} size="small" color="primary" sx={{ fontWeight: 600 }} /></TableCell>
                                    <TableCell><Chip label={(l.type || 'LEAVE').replace('_', ' ')} size="small" color="secondary" sx={{ fontWeight: 600 }} /></TableCell>
                                    <TableCell sx={{ color: '#475569' }}>{l.fromDate} {l.fromTime}</TableCell>
                                    <TableCell sx={{ color: '#475569' }}>{l.toDate} {l.toTime}</TableCell>
                                    <TableCell sx={{ color: '#1e293b' }}>{l.reason}</TableCell>
                                    <TableCell>
                                        {l.tempHod
                                            ? <Chip label={`${l.tempHod.name}`} size="small" color="secondary" sx={{ fontWeight: 600 }} />
                                            : <Chip label="None" size="small" color="error" variant="outlined" sx={{ fontWeight: 600 }} />}
                                    </TableCell>
                                    <TableCell>
                                        <Button size="small" color="success" variant="contained" sx={{ mr: 1 }} onClick={() => handleApprove(l.id)}>Approve</Button>
                                        <Button size="small" color="error" variant="contained" onClick={() => handleRejectClick(l.id)}>Reject</Button>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Paper>

                <Divider sx={{ my: 4 }} />
                <Typography variant="h5" sx={{ fontWeight: 700, mb: 3, color: '#0f3460' }}>Approval History (Last 100)</Typography>
                <Paper className="glass" sx={{ borderRadius: 3, overflow: 'hidden' }}>
                    <Table sx={{ minWidth: 700 }}>
                        <TableHead sx={{ bgcolor: '#1a365d' }}>
                            <TableRow>{['HOD Name', 'Department', 'Type', 'From', 'To', 'Status', 'Feedback'].map(h =>
                                <TableCell key={h} sx={{ color: '#fff', fontWeight: 700 }}>{h}</TableCell>
                            )}</TableRow>
                        </TableHead>
                        <TableBody>
                            {historyLeaves.length === 0
                                ? <TableRow><TableCell colSpan={7} align="center" sx={{ color: '#475569' }}>No history found.</TableCell></TableRow>
                                : historyLeaves.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map(l => (
                                    <TableRow key={l.id} sx={{ '&:hover': { bgcolor: 'rgba(0,0,0,0.04)' } }}>
                                        <TableCell sx={{ fontWeight: 600 }}>{l.faculty?.name || 'Unknown'}</TableCell>
                                        <TableCell>{l.faculty?.department?.deptName || 'N/A'}</TableCell>
                                        <TableCell sx={{ fontWeight: 600 }}>{(l.type || 'LEAVE').replace('_', ' ')}</TableCell>
                                        <TableCell>{l.fromDate} {l.fromTime}</TableCell>
                                        <TableCell>{l.toDate} {l.toTime}</TableCell>
                                        <TableCell><Chip label={l.status} size="small" fontWeight={700} color={l.status === 'APPROVED' ? 'success' : 'error'} /></TableCell>
                                        <TableCell sx={{ color: '#e94560' }}>{l.rejectionReason || '-'}</TableCell>
                                    </TableRow>
                                ))
                            }
                        </TableBody>
                    </Table>
                    <TablePagination component="div" count={historyLeaves.length} page={page}
                        onPageChange={(e, p) => setPage(p)} rowsPerPage={rowsPerPage}
                        onRowsPerPageChange={e => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0); }}
                        rowsPerPageOptions={[10]} />
                </Paper>

                {/* ── Reject Dialog ── */}
                <Dialog open={rejectOpen} onClose={() => setRejectOpen(false)} maxWidth="sm" fullWidth PaperProps={{ sx: { borderRadius: 3 } }}>
                    <DialogTitle sx={{ fontWeight: 800, color: '#e94560' }}>Reject Leave Request</DialogTitle>
                    <DialogContent>
                        <Typography variant="body2" sx={{ mb: 2, color: '#475569' }}>Please provide a reason for rejecting this leave request.</Typography>
                        <TextField fullWidth multiline rows={3} label="Rejection Reason" value={rejectReason} onChange={e => setRejectReason(e.target.value)} autoFocus />
                    </DialogContent>
                    <DialogActions sx={{ p: 2 }}>
                        <Button onClick={() => setRejectOpen(false)} sx={{ fontWeight: 700, color: '#475569' }}>Cancel</Button>
                        <Button variant="contained" color="error" onClick={handleConfirmReject} disabled={!rejectReason.trim()} sx={{ fontWeight: 700 }}>Reject Leave</Button>
                    </DialogActions>
                </Dialog>

                {/* ── Unresolved Slots Popup (shown after leave approval when slots can't be auto-reassigned) ── */}
                <Dialog open={unresolvedOpen} maxWidth="md" fullWidth PaperProps={{ sx: { borderRadius: 3 } }}>
                    <DialogTitle sx={{ bgcolor: '#fef2f2', borderBottom: '2px solid #fecaca', pb: 1.5 }}>
                        <Box display="flex" alignItems="center" gap={1.5}>
                            <WarningAmber sx={{ color: '#e94560', fontSize: 32 }} />
                            <Box>
                                <Typography fontWeight="900" color="#0f3460" variant="h6">Manual Assignment Required</Typography>
                                {approvedContext && (
                                    <Typography variant="caption" color="text.secondary">
                                        {approvedContext.facultyName}'s leave ({approvedContext.fromDate} → {approvedContext.toDate})
                                        — {unresolvedSlots.length} slot{unresolvedSlots.length !== 1 ? 's' : ''} could not be auto-reassigned
                                    </Typography>
                                )}
                            </Box>
                        </Box>
                    </DialogTitle>
                    <DialogContent sx={{ pt: 2 }}>
                        <Typography variant="body2" color="text.secondary" mb={2}>
                            No course-known faculty was available for the slots below. Select from eligible faculty (ordered lowest workload first) and click Assign.
                        </Typography>
                        {unresolvedSlots.map((slot, idx) => {
                            const eligible = slot.availableFaculty || [];
                            return (
                                <Box key={idx} sx={{
                                    display: 'flex', gap: 2, alignItems: 'flex-start', flexWrap: 'wrap',
                                    mb: 2, p: 2, bgcolor: '#f8fafc', borderRadius: 2, border: '1px solid #e2e8f0'
                                }}>
                                    <Box sx={{ flexGrow: 1, minWidth: 200 }}>
                                        <Typography variant="subtitle2" fontWeight="800" color="#0f3460">
                                            {slot.courseCode} — {slot.courseName}
                                            <Typography component="span" sx={{ ml: 1, fontSize: 11, fontWeight: 800, color: slot.slotType === 'LAB' ? '#e94560' : '#3b82f6' }}>
                                                [{slot.slotType}]
                                            </Typography>
                                        </Typography>
                                        <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.78rem', mt: 0.25 }}>
                                            📅 <strong>{slot.date}</strong> &nbsp;|&nbsp; ⏰ Hour <strong>{slot.hour}</strong> &nbsp;|&nbsp; 📍 {slot.venueName || 'No venue'}
                                        </Typography>
                                        {(slot.timetableName || slot.sectionName) && (
                                            <Typography variant="caption" sx={{ color: '#7c3aed', fontWeight: 700 }}>
                                                📋 {slot.timetableName}{slot.sectionName ? ` · Section ${slot.sectionName}` : ''}
                                            </Typography>
                                        )}
                                        {slot.absentFacultyName && (
                                            <Typography variant="caption" sx={{ color: '#e94560', fontWeight: 700, display: 'block' }}>
                                                Absent: {slot.absentFacultyName}
                                            </Typography>
                                        )}
                                    </Box>
                                    {eligible.length > 0 ? (
                                        <>
                                            <Select size="small" sx={{ minWidth: 230 }} displayEmpty
                                                value={slotAssignments[idx] || ''}
                                                onChange={e => setSlotAssignments(p => ({ ...p, [idx]: e.target.value }))}>
                                                <MenuItem value=""><em>Select Faculty (low load first)</em></MenuItem>
                                                {eligible.map(f => (
                                                    <MenuItem key={f.id} value={f.id} sx={{ fontSize: 13, fontWeight: 600 }}>
                                                        {f.name}&nbsp;
                                                        <Typography component="span" sx={{ fontSize: 11, color: '#64748b' }}>({f.load} hrs this month)</Typography>
                                                    </MenuItem>
                                                ))}
                                            </Select>
                                            <Button variant="contained" disabled={!slotAssignments[idx] || assigning}
                                                onClick={() => handleSlotAssign(slot, idx)}
                                                sx={{ bgcolor: '#0f3460', fontWeight: 700, alignSelf: 'center' }}>
                                                {assigning ? <CircularProgress size={18} color="inherit" /> : 'Assign'}
                                            </Button>
                                        </>
                                    ) : (
                                        <Box sx={{ px: 2, py: 1, bgcolor: '#fef9c3', borderRadius: 2, border: '1px solid #fde047', alignSelf: 'center' }}>
                                            <Typography variant="caption" fontWeight="700" color="#854d0e">No eligible faculty available</Typography>
                                        </Box>
                                    )}
                                </Box>
                            );
                        })}
                    </DialogContent>
                    <DialogActions sx={{ p: 2, bgcolor: '#f8fafc', borderTop: '1px solid #e2e8f0' }}>
                        <Button onClick={() => setUnresolvedOpen(false)} sx={{ fontWeight: 700, color: '#475569' }}>
                            Close (Handle Later)
                        </Button>
                    </DialogActions>
                </Dialog>
            </Box>
        </Box>
    );
};

export default HodLeaveApprovalPage;
