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

/**
 * Retrieves all unverified users.
 */
const getUnverifiedUsers = async () => {
    
    const response = await api.get<User[]>('/admin/users/pending');
    return response.data;
};

/**
 * Verifies a user account.
 * @param userId The ID of the user
 */
const verifyUser = async (userId: number) => {
    await api.post(`/admin/users/${userId}/verify`);
};

/**
 * Creates a new course.
 * @param name Course name
 * @param description Course description
 */
const createCourse = async (name: string, description: string) => {
    return await api.post('/admin/courses', { name, description });
};


/**
 * Retrieves all student groups.
 */
const getAllGroups = async () => {
    
    const response = await api.get<string[]>('/admin/groups');
    
    return response.data;
};

/**
 * Retrieves all professors.
 */
const getProfessors = async () => {
    
    
    
    const response = await api.get<User[]>('/admin/users/professors');
    return response.data;
};

/**
 * Retrieves all forums (admin view).
 */
const getAllForums = async () => {
    
    const response = await api.get<any[]>('/forums'); 
    return response.data;
};

/**
 * Retrieves all users.
 */
const getAllUsers = async () => {
    const response = await api.get<User[]>('/admin/users');
    return response.data;
};

/**
 * Updates a user's details.
 * @param userId The ID of the user
 * @param data The data to update
 */
const updateUser = async (userId: number, data: Partial<User>) => {
    return await api.put(`/admin/users/${userId}`, data);
};

/**
 * Retrieves all courses from forums data.
 */
const getAllCourses = async () => {
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    const response = await api.get<any[]>('/forums');
    
    const courses = new Map();
    response.data.forEach((f: any) => {
        if (f.course && !courses.has(f.course.id)) {
            courses.set(f.course.id, f.course);
        }
    });
    return Array.from(courses.values());
};

/**
 * Enrolls a student in a course.
 * @param userId The ID of the student
 * @param courseId The ID of the course
 */
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
