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

/**
 * Retrieves all forums visible to the current user.
 */
const getForums = async () => {
    const response = await api.get<Forum[]>('/forums');
    return response.data;
};

/**
 * Retrieves posts for a specific forum.
 * @param forumId The ID of the forum
 */
const getPosts = async (forumId: number) => {
    const response = await api.get<Post[]>(`/forums/${forumId}/posts`);
    return response.data;
};

/**
 * Retrieves a single post by ID.
 * @param postId The ID of the post
 */
const getPost = async (postId: number) => {
    const response = await api.get<Post>(`/forums/posts/${postId}`);
    return response.data;
};

/**
 * Creates a new post in a forum.
 * @param forumId The ID of the forum
 * @param title Post title
 * @param content Post content
 */
const createPost = async (forumId: number, title: string, content: string) => {
    return await api.post(`/forums/${forumId}/posts`, { title, content });
};

/**
 * Adds a comment to a post.
 * @param postId The ID of the post
 * @param content Comment content
 * @param parentId Optional parent comment ID for replies
 */
const addComment = async (postId: number, content: string, parentId?: number) => {
    return await api.post(`/forums/posts/${postId}/comments`, { content, parentId });
};

/**
 * Votes on a post.
 * @param postId The ID of the post
 * @param value Vote value (1 for upvote, -1 for downvote)
 */
const votePost = async (postId: number, value: number) => {
    return await api.post(`/forums/posts/${postId}/vote`, null, { params: { value } });
};

/**
 * Votes on a comment.
 * @param commentId The ID of the comment
 * @param value Vote value (1 for upvote, -1 for downvote)
 */
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
