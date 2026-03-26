import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import api from '../services/api';
import {
    Box, Card, CardContent, TextField, Button, Typography, Alert, CircularProgress,
    InputAdornment, IconButton
} from '@mui/material';
import { Lock, Visibility, VisibilityOff } from '@mui/icons-material';

const ResetPasswordPage = () => {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    const [newPassword, setNewPassword] = useState('');
    const [confirm, setConfirm] = useState('');
    const [showPw, setShowPw] = useState(false);
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(''); setSuccess('');
        if (newPassword.length < 6) { setError('Password must be at least 6 characters.'); return; }
        if (newPassword !== confirm) { setError('Passwords do not match.'); return; }
        setLoading(true);
        try {
            await api.post('/auth/reset-password', { token, newPassword });
            setSuccess('Password reset successful! Redirecting to login...');
            setTimeout(() => navigate('/login'), 2500);
        } catch (err) {
            setError(err.response?.data?.message || 'Reset link is invalid or expired.');
        } finally {
            setLoading(false);
        }
    };

    if (!token) {
        return (
            <Box className="gradient-bg" sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', p: 2 }}>
                <Alert severity="error">Invalid reset link. Please request a new one.</Alert>
            </Box>
        );
    }

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', p: 2 }}>
            <Card className="glass" sx={{ maxWidth: 440, width: '100%', borderRadius: 4, border: '1px solid rgba(255,255,255,0.2)', boxShadow: '0 15px 35px rgba(0,0,0,0.4)' }}>
                <CardContent sx={{ p: 4 }}>
                    <Box sx={{ textAlign: 'center', mb: 3 }}>
                        <Box sx={{
                            width: 60, height: 60, borderRadius: '50%', mx: 'auto', mb: 2,
                            background: 'linear-gradient(135deg, #0f3460, #1a365d)',
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            boxShadow: '0 4px 15px rgba(15,52,96,0.3)'
                        }}>
                            <Lock sx={{ color: '#ffffff', fontSize: 30 }} />
                        </Box>
                        <Typography variant="h5" sx={{ color: '#0f3460', fontWeight: 800 }}>Set New Password</Typography>
                        <Typography variant="body2" sx={{ color: '#475569', mt: 0.5 }}>
                            Choose a strong password for your account.
                        </Typography>
                    </Box>

                    {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                    {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

                    {!success && (
                        <form onSubmit={handleSubmit}>
                            <TextField
                                fullWidth label="New Password" type={showPw ? 'text' : 'password'}
                                value={newPassword} onChange={e => setNewPassword(e.target.value)} required
                                InputProps={{
                                    endAdornment: (
                                        <InputAdornment position="end">
                                            <IconButton onClick={() => setShowPw(!showPw)}>
                                                {showPw ? <VisibilityOff /> : <Visibility />}
                                            </IconButton>
                                        </InputAdornment>
                                    )
                                }}
                                sx={{ mb: 2.5 }}
                            />
                            <TextField
                                fullWidth label="Confirm New Password" type="password"
                                value={confirm} onChange={e => setConfirm(e.target.value)} required
                                sx={{ mb: 3 }}
                            />
                            <Button fullWidth type="submit" variant="contained" disabled={loading}
                                sx={{
                                    py: 1.5, borderRadius: 3, fontWeight: 800, fontSize: 16,
                                    background: 'linear-gradient(135deg, #0f3460, #1a365d)',
                                    '&:hover': { background: 'linear-gradient(135deg, #1a365d, #0f3460)' }
                                }}>
                                {loading ? <CircularProgress size={24} color="inherit" /> : 'Reset Password'}
                            </Button>
                        </form>
                    )}
                </CardContent>
            </Card>
        </Box>
    );
};

export default ResetPasswordPage;
