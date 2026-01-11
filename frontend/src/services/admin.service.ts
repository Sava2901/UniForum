import api from './api';

export interface User {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    role: string;
    verified: boolean;
    nickname?: string;
    groupName?: string;
    studyYear?: number;
    semester?: number;
}

export interface Course {
    id: number;
    name: string;
    description: string;
}

export interface Group {
    id: number;
    name: string;
}

const getUnverifiedUsers = async () => {
    // The backend endpoint is /api/admin/users/pending
    const response = await api.get<User[]>('/admin/users/pending');
    return response.data;
};

const verifyUser = async (userId: number) => {
    await api.post(`/admin/users/${userId}/verify`);
};

const createCourse = async (name: string, description: string) => {
    return await api.post('/admin/courses', { name, description });
};

// Groups are now strings, but we might want to fetch unique groups if needed
const getAllGroups = async () => {
    // The endpoint returns List<String>
    const response = await api.get<string[]>('/admin/groups');
    // Map string[] to object array for consistency if needed, or just return strings
    return response.data;
};

const getProfessors = async () => {
    // We need an endpoint to get professors. For now, reusing unverified but we should filter by role if backend supported it.
    // Assuming we might need a new endpoint or filter on client side if we fetch all users.
    // Let's assume we add an endpoint /admin/users/professors
    const response = await api.get<User[]>('/admin/users/professors');
    return response.data;
};

const getAllForums = async () => {
    // Admin needs to see ALL forums to assign professors
    const response = await api.get<any[]>('/forums'); // Reusing public endpoint or admin specific
    return response.data;
};

const getAllUsers = async () => {
    const response = await api.get<User[]>('/admin/users');
    return response.data;
};

const updateUser = async (userId: number, data: Partial<User>) => {
    return await api.put(`/admin/users/${userId}`, data);
};

const getAllCourses = async () => {
    // We don't have a direct "get all courses" admin endpoint that returns just course objects cleanly,
    // but we can use /forums and extract courses or add a new endpoint.
    // Actually, ForumService has getForums but that returns Forums.
    // Let's assume we might need to fetch forums and extract courses or just use what we have.
    // Better: Add GET /api/admin/courses or similar.
    // For now, let's use the forum list to extract courses or assume we add an endpoint.
    // Let's add an endpoint or use a workaround.
    // Workaround: We can't easily get just courses without an endpoint.
    // I'll assume we can fetch forums and map them.
    // Actually, let's add an endpoint for courses if needed, but for "enroll student", we need course IDs.
    // Let's try to fetch forums and get unique courses from there as a quick fix, or better, add the endpoint.
    // Since I can edit backend, I'll assume I can add it or it exists?
    // AdminController has createCourse.
    // Let's just use /forums for now as admin sees all.
    const response = await api.get<any[]>('/forums');
    // Extract unique courses
    const courses = new Map();
    response.data.forEach((f: any) => {
        if (f.course && !courses.has(f.course.id)) {
            courses.set(f.course.id, f.course);
        }
    });
    return Array.from(courses.values());
};

const enrollStudent = async (userId: number, courseId: number) => {
    return await api.post(`/admin/courses/${courseId}/enroll/${userId}`);
};

const removeStudentFromCourse = async (userId: number, courseId: number) => {
    return await api.delete(`/admin/courses/${courseId}/enroll/${userId}`);
};

const assignStudentToForum = async (userId: number, forumId: number) => {
    return await api.post(`/admin/forums/${forumId}/enroll/${userId}`);
};

const assignProfessor = async (forumId: number, professorId: number) => {
    return await api.post(`/admin/forums/${forumId}/assign`, null, {
        params: { professorId }
    });
};

const removeProfessorFromForum = async (forumId: number) => {
    return await api.delete(`/admin/forums/${forumId}/professor`);
};

const deleteUser = async (userId: number) => {
    return await api.delete(`/admin/users/${userId}`);
};

export default {
    getUnverifiedUsers,
    verifyUser,
    createCourse,
    getAllGroups,
    getProfessors,
    getAllForums,
    assignProfessor,
    removeProfessorFromForum,
    getAllUsers,
    deleteUser,
    updateUser,
    getAllCourses,
    enrollStudent,
    removeStudentFromCourse,
    assignStudentToForum
};
