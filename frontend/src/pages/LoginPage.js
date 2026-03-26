import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { ROLES } from '../utils/constants';
import {
    Box, Card, CardContent, TextField, Button, Typography, Alert, CircularProgress,
    InputAdornment, IconButton
} from '@mui/material';
import { Visibility, VisibilityOff, Login as LoginIcon } from '@mui/icons-material';

const LoginPage = () => {
    const [userId, setUserId] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const getRedirectPath = (role) => {
        switch (role) {
            case ROLES.SUPER_ADMIN: return '/super-admin';
            case ROLES.FACULTY_ADMIN: return '/faculty-admin';
            case ROLES.DEPARTMENT_ADMIN: return '/departments';
            case ROLES.COURSE_ADMIN: return '/courses';
            case ROLES.VENUE_ADMIN: return '/venues';
            case ROLES.HOD: case ROLES.TEMP_HOD: return '/hod';
            case ROLES.FACULTY: return '/faculty';
            default: return '/login';
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const data = await login(userId, password);
            navigate(getRedirectPath(data.role));
        } catch (err) {
            setError(err.response?.data?.message || 'Invalid credentials');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box className="gradient-bg" sx={{
            minHeight: '100vh',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            p: 2
        }}>
            <Card className="glass" sx={{
                maxWidth: 440, width: '100%', borderRadius: 4,
                border: '1px solid rgba(255,255,255,0.2)', boxShadow: '0 15px 35px rgba(0,0,0,0.5)'
            }}>
                <CardContent sx={{ p: 4 }}>
                    <Box sx={{ textAlign: 'center', mb: 4 }}>
                        <Box sx={{
                            width: 70, height: 70, borderRadius: '50%', mx: 'auto', mb: 2,
                            background: 'linear-gradient(135deg, #0f3460, #1a365d)',
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            boxShadow: '0 4px 15px rgba(15,52,96,0.3)'
                        }}>
                            <LoginIcon sx={{ color: '#ffffff', fontSize: 35 }} />
                        </Box>
                        <Typography variant="h5" sx={{ color: '#0f3460', fontWeight: 800 }}>
                            Faculty Load Analyzer
                        </Typography>
                        <Typography variant="body2" sx={{ color: '#475569', mt: 0.5, fontWeight: 500 }}>
                            ABC Engineering College
                        </Typography>
                    </Box>

                    {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                    <form onSubmit={handleSubmit}>
                        <TextField
                            fullWidth label="Email or Faculty ID" value={userId}
                            onChange={(e) => setUserId(e.target.value)} required
                            sx={{ mb: 2.5 }}
                        />
                        <TextField
                            fullWidth label="Password" type={showPassword ? 'text' : 'password'}
                            value={password} onChange={(e) => setPassword(e.target.value)} required
                            InputProps={{
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton onClick={() => setShowPassword(!showPassword)}>
                                            {showPassword ? <VisibilityOff /> : <Visibility />}
                                        </IconButton>
                                    </InputAdornment>
                                )
                            }}
                            sx={{ mb: 3 }}
                        />
                        <Button fullWidth type="submit" variant="contained" disabled={loading}
                            sx={{
                                py: 1.5, borderRadius: 3, fontWeight: 800, fontSize: 16,
                                background: 'linear-gradient(135deg, #0f3460, #1a365d)',
                                '&:hover': { background: 'linear-gradient(135deg, #1a365d, #0f3460)' }
                            }}>
                            {loading ? <CircularProgress size={24} color="inherit" /> : 'Sign In'}
                        </Button>
                    </form>
                    <Box sx={{ textAlign: 'center', mt: 2 }}>
                        <Typography
                            variant="body2"
                            sx={{ color: '#0f3460', fontWeight: 600, cursor: 'pointer', '&:hover': { textDecoration: 'underline' } }}
                            onClick={() => navigate('/forgot-password')}
                        >
                            Forgot password?
                        </Typography>
                    </Box>
                </CardContent>
            </Card>
        </Box>
    );
};

export default LoginPage;
