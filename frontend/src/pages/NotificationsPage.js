import React, { useEffect, useState } from 'react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import {
    Box, Typography, List, ListItem, ListItemText, ListItemIcon, Chip, Button,
    Paper, Divider
} from '@mui/material';
import { Notifications as NotifIcon, MarkEmailRead } from '@mui/icons-material';

const NotificationsPage = () => {
    const [notifications, setNotifications] = useState([]);

    useEffect(() => { fetchData(); }, []);

    const fetchData = async () => {
        try { const res = await api.get('/notifications'); setNotifications(res.data); } catch { }
    };

    const handleMarkRead = async (id) => {
        await api.put(`/notifications/${id}/read`);
        fetchData();
    };

    const handleMarkAllRead = async () => {
        await api.put('/notifications/read-all');
        fetchData();
    };

    return (
        <Box className="gradient-bg" sx={{ minHeight: '100vh', pb: 4 }}>
            <Navbar notifCount={notifications.filter(n => !n.readStatus).length} />
            <Box sx={{ p: { xs: 2, md: 4 }, maxWidth: 800, mx: 'auto' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                    <Typography variant="h4" sx={{ fontWeight: 800, color: '#0f3460' }}>Notifications</Typography>
                    <Button startIcon={<MarkEmailRead />} onClick={handleMarkAllRead}>Mark All Read</Button>
                </Box>
                <Paper className="glass" sx={{ borderRadius: 3, overflow: 'hidden' }}>
                    <List>
                        {notifications.map((n, i) => (
                            <React.Fragment key={n.id}>
                                <ListItem sx={{
                                    bgcolor: n.readStatus ? 'inherit' : 'rgba(15,52,96,0.05)',
                                    cursor: 'pointer', '&:hover': { bgcolor: 'rgba(0,0,0,0.04)' }
                                }} onClick={() => !n.readStatus && handleMarkRead(n.id)}>
                                    <ListItemIcon>
                                        <NotifIcon sx={{ color: n.readStatus ? '#94a3b8' : '#0f3460' }} />
                                    </ListItemIcon>
                                    <ListItemText
                                        primary={<Typography sx={{ fontWeight: n.readStatus ? 500 : 700, color: '#0f3460' }}>{n.title}</Typography>}
                                        secondary={<Typography variant="body2" sx={{ color: '#475569' }}>{n.message}<br /><small>{new Date(n.createdAt).toLocaleString()}</small></Typography>}
                                    />
                                    {!n.readStatus && <Chip label="New" size="small" color="error" />}
                                </ListItem>
                                {i < notifications.length - 1 && <Divider />}
                            </React.Fragment>
                        ))}
                        {notifications.length === 0 && (
                            <ListItem><ListItemText primary="No notifications" /></ListItem>
                        )}
                    </List>
                </Paper>
            </Box>
        </Box>
    );
};

export default NotificationsPage;
