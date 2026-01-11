import api from './api';

export interface User {
    token: string;
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
    nickname?: string;
    groupName?: string;
    studyYear?: number;
    semester?: number;
}

/**
 * Registers a new user.
 * @param data Registration data
 */
const register = (data: any) => {
    return api.post('/auth/register', data);
};

/**
 * Logs in a user.
 * @param email User email
 * @param password User password
 * @returns User data with token
 */
const login = async (email: string, password: string) => {
    const response = await api.post<User>('/auth/login', { email, password });
    if (response.data.token) {
        localStorage.setItem('user', JSON.stringify(response.data));
    }
    return response.data;
};

/**
 * Logs out the current user.
 */
const logout = () => {
    localStorage.removeItem('user');
    window.location.href = "/login"; 
};

/**
 * Gets the current logged-in user from local storage.
 * @returns User object or null
 */
const getCurrentUser = (): User | null => {
    const userStr = localStorage.getItem('user');
    if (userStr) return JSON.parse(userStr);
    return null;
};

export default {
    register,
    login,
    logout,
    getCurrentUser,
};
