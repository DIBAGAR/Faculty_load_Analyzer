import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography } from '@mui/material';

const ConfirmDialog = ({ open, title, content, onConfirm, onCancel, confirmText = 'Confirm', color = 'error' }) => {
    return (
        <Dialog open={open} onClose={onCancel} maxWidth="xs" fullWidth PaperProps={{ sx: { borderRadius: 3 } }}>
            <DialogTitle sx={{ fontWeight: 800, color: color === 'error' ? '#e94560' : '#0f3460' }}>
                {title}
            </DialogTitle>
            <DialogContent>
                <Typography variant="body1" sx={{ color: '#0f172a' }}>{content}</Typography>
            </DialogContent>
            <DialogActions sx={{ p: 2 }}>
                <Button onClick={onCancel} sx={{ fontWeight: 700, color: '#475569' }}>Cancel</Button>
                <Button variant="contained" color={color} onClick={onConfirm} sx={{ fontWeight: 700 }}>
                    {confirmText}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default ConfirmDialog;
