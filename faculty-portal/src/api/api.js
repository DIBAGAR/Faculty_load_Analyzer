import axios from 'axios';

const API = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api',
    withCredentials: true, // supports session cookies if enabled server-side
});

API.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const authApi = {
    login: (credentials) => API.post('/auth/login', credentials),
    me: () => API.get('/auth/me'),
    logout: () => API.post('/auth/logout'),
};

export const notificationApi = {
    latest: () => API.get('/notifications'),
    unreadCount: () => API.get('/notifications/unread-count'),
    markRead: (id) => API.post(`/notifications/${id}/read`),
};

export const departmentApi = {
    list: () => API.get('/departments'),
    create: (payload) => API.post('/departments', payload),
    update: (id, payload) => API.put(`/departments/${id}`, payload),
};

export const facultyApi = {
    create: (payload) => API.post('/faculty', payload),
    listByDepartment: (departmentId) => API.get(`/faculty/department/${departmentId}`),
    assignHod: (departmentId, payload) => API.post(`/faculty/department/${departmentId}/assign-hod`, payload),
    myLeaves: () => API.get('/leaves/me'),
    submitLeave: (payload) => API.post('/leaves', payload),
};

export const courseApi = {
    list: () => API.get('/courses'),
    create: (payload) => API.post('/courses', payload),
};

export const venueApi = {
    listByDepartment: (departmentId) => API.get(`/venues/department/${departmentId}`),
    create: (payload) => API.post('/venues', payload),
};

export const timetableApi = {
    getActive: (departmentId, yearOfStudy, section) =>
        API.get('/timetables/active', { params: { departmentId, yearOfStudy, section } }),
    createDraft: (payload) => API.post('/timetables', payload),
    activate: (timetableId) => API.post(`/timetables/${timetableId}/activate`),
};

export const workloadApi = {
    generateNextWeek: (departmentId, yearOfStudy, section) =>
        API.post('/workload/generate-next-week', null, { params: { departmentId, yearOfStudy, section } }),
};

export default API;