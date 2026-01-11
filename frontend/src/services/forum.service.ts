import api from './api';

export interface Forum {
    id: number;
    course?: { id: number; name: string };
    groupName?: string;
    type: 'MAIN_COURSE' | 'GROUP_SUBFORUM';
}

export interface AuthorDto {
    id: number;
    displayName: string;
    role: string;
    email?: string | null;
}

export interface Post {
    id: number;
    title: string;
    content: string;
    author: AuthorDto;
    timestamp: string;
    pinned: boolean;
    score: number;
    comments?: Comment[];
}

export interface Comment {
    id: number;
    content: string;
    author: AuthorDto;
    timestamp: string;
    pinned: boolean;
    score: number;
    parentId?: number;
    replies?: Comment[];
}

const getForums = async () => {
    const response = await api.get<Forum[]>('/forums');
    return response.data;
};

const getPosts = async (forumId: number) => {
    const response = await api.get<Post[]>(`/forums/${forumId}/posts`);
    return response.data;
};

const getPost = async (postId: number) => {
    const response = await api.get<Post>(`/forums/posts/${postId}`);
    return response.data;
};

const createPost = async (forumId: number, title: string, content: string) => {
    return await api.post(`/forums/${forumId}/posts`, { title, content });
};

const addComment = async (postId: number, content: string, parentId?: number) => {
    return await api.post(`/forums/posts/${postId}/comments`, { content, parentId });
};

const votePost = async (postId: number, value: number) => {
    return await api.post(`/forums/posts/${postId}/vote`, null, { params: { value } });
};

const voteComment = async (commentId: number, value: number) => {
    return await api.post(`/forums/comments/${commentId}/vote`, null, { params: { value } });
};

export default {
    getForums,
    getPosts,
    getPost,
    createPost,
    addComment,
    votePost,
    voteComment
};
