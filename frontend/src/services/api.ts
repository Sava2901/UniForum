import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use(config => {
    
    
    if (config.url && config.url.includes('/auth/')) {
        return config;
    }

    const userStr = localStorage.getItem('user');
    if (userStr) {
        const user = JSON.parse(userStr);
        if (user && user.token) {
            config.headers.Authorization = 'Bearer ' + user.token;
        }
    }
    return config;
});

export default api;
