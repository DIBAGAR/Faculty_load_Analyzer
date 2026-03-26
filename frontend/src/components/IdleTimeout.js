import { useEffect, useCallback, useRef } from 'react';
import { useAuth } from '../contexts/AuthContext';

const IdleTimeout = ({ children }) => {
    const { logout, user } = useAuth();
    const timeoutRef = useRef(null);

    // 10 minutes in milliseconds
    const TIMEOUT_DURATION = 10 * 60 * 1000; 

    const resetTimer = useCallback(() => {
        if (timeoutRef.current) clearTimeout(timeoutRef.current);
        
        if (user) {
            timeoutRef.current = setTimeout(() => {
                console.log('User idle for 10 minutes. Logging out.');
                logout();
            }, TIMEOUT_DURATION);
        }
    }, [logout, user, TIMEOUT_DURATION]);

    useEffect(() => {
        const events = ['mousemove', 'keydown', 'scroll', 'click', 'touchstart'];
        
        if (user) {
            resetTimer();
            events.forEach(event => window.addEventListener(event, resetTimer));
        }

        return () => {
            if (timeoutRef.current) clearTimeout(timeoutRef.current);
            events.forEach(event => window.removeEventListener(event, resetTimer));
        };
    }, [user, resetTimer]);

    return children;
};

export default IdleTimeout;
