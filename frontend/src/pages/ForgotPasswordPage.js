import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import {
    Box, Card, CardContent, TextField, Button, Typography, Alert, CircularProgress
} from '@mui/material';
import { Email, ArrowBack } from '@mui/icons-material';

const ForgotPasswordPage = () => {
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(''); setSuccess(''); setLoading(true);
        try {
            await api.post('/auth/forgot-password', { email });
            setSuccess('If this email is registered, a password reset link has been sent. Please check your inbox (and spam folder).');
        } catch (err) {
            setError(err.response?.data?.message || 'Something went wrong. Please try again.');
        } finally {
            setLoading(false);
        }
    };

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
                            <Email sx={{ color: '#ffffff', fontSize: 30 }} />
                        </Box>
                        <Typography variant="h5" sx={{ color: '#0f3460', fontWeight: 800 }}>Forgot Password</Typography>
                        <Typography variant="body2" sx={{ color: '#475569', mt: 0.5 }}>
                            Enter your registered email address to receive a reset link.
                        </Typography>
                    </Box>

                    {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                    {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

                    {!success && (
                        <form onSubmit={handleSubmit}>
                            <TextField
                                fullWidth label="Registered Email Address" type="email"
                                value={email} onChange={e => setEmail(e.target.value)} required
                                sx={{ mb: 3 }}
                            />
                            <Button fullWidth type="submit" variant="contained" disabled={loading}
                                sx={{
                                    py: 1.5, borderRadius: 3, fontWeight: 800, fontSize: 16,
                                    background: 'linear-gradient(135deg, #0f3460, #1a365d)',
                                    '&:hover': { background: 'linear-gradient(135deg, #1a365d, #0f3460)' }
                                }}>
                                {loading ? <CircularProgress size={24} color="inherit" /> : 'Send Reset Link'}
                            </Button>
                        </form>
                    )}
                    <Box sx={{ textAlign: 'center', mt: 2 }}>
                        <Typography
                            variant="body2"
                            sx={{ color: '#0f3460', fontWeight: 600, cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: 0.5, '&:hover': { textDecoration: 'underline' } }}
                            onClick={() => navigate('/login')}
                        >
                            <ArrowBack fontSize="small" /> Back to Login
                        </Typography>
                    </Box>
                </CardContent>
            </Card>
        </Box>
    );
};

export default ForgotPasswordPage;
