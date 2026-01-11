import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ForumService, { type Post, type Comment } from '../../services/forum.service';
import AuthService from '../../services/auth.service';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { format } from 'date-fns';
import { useAlert } from '@/context/AlertContext';
import { ChevronUp, ChevronDown, MessageSquare, Trash2, Edit2, X, Check } from 'lucide-react';
import { ConfirmationDialog } from '@/components/ConfirmationDialog';

interface CommentItemProps {
    comment: Comment;
    level: number;
    isAdmin: boolean;
    currentUserId?: number;
    onVote: (type: 'post' | 'comment', id: number, value: number) => void;
    onReply: (parentId: number, content: string) => Promise<void>;
    onDelete: (commentId: number) => Promise<void>;
    onEdit: (commentId: number, content: string) => Promise<void>;
}

const CommentItem: React.FC<CommentItemProps> = ({ comment, level, isAdmin, currentUserId, onVote, onReply, onDelete, onEdit }) => {
    const [areRepliesHidden, setAreRepliesHidden] = useState(false);
    const [isReplying, setIsReplying] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [replyContent, setReplyContent] = useState('');
    const [editContent, setEditContent] = useState(comment.content);
    const hasReplies = comment.replies && comment.replies.length > 0;
    const isAuthor = currentUserId === comment.author.id;

    /**
     * Handles the submission of a reply.
     */
    const handleSubmitReply = async () => {
        if (!replyContent.trim()) return;
        await onReply(comment.id, replyContent);
        setReplyContent('');
        setIsReplying(false);
    };

    /**
     * Handles the submission of an edit.
     */
    const handleSubmitEdit = async () => {
        if (!editContent.trim()) return;
        await onEdit(comment.id, editContent);
        setIsEditing(false);
    };

    return (
        <div className={`mt-4 ${level > 0 ? 'ml-8 border-l-2 pl-4 border-l-border' : ''}`}>
            <Card className={comment.pinned ? "border-yellow-400 dark:border-yellow-600 border-2" : ""}>
                <CardHeader className="pb-2">
                    <div className="flex justify-between text-sm text-muted-foreground">
                        <span className="font-semibold text-foreground flex items-center gap-2">
                            {comment.author.displayName}
                            {comment.author.email && <span className="font-normal text-muted-foreground">({comment.author.email})</span>}
                            {comment.author.role === 'PROFESSOR' && (
                                <Badge className="bg-yellow-400 text-yellow-900 hover:bg-yellow-400 dark:bg-yellow-600 dark:text-yellow-100 dark:hover:bg-yellow-600 pointer-events-none select-none border-yellow-500">
                                    Professor
                                </Badge>
                            )}
                            {comment.author.role === 'ADMIN' && (
                                <Badge variant="destructive" className="pointer-events-none select-none">
                                    Admin
                                </Badge>
                            )}
                        </span>
                        <span>{format(new Date(comment.timestamp), 'PPpp')}</span>
                        {comment.editedAt && <span className="text-xs text-muted-foreground ml-1">(edited {format(new Date(comment.editedAt), 'PPpp')})</span>}
                    </div>
                </CardHeader>
                <CardContent>
                    {isEditing ? (
                        <div className="mb-4 space-y-2">
                            <Textarea 
                                value={editContent}
                                onChange={(e) => setEditContent(e.target.value)}
                                className="min-h-[80px]"
                            />
                            <div className="flex gap-2">
                                <Button size="sm" onClick={handleSubmitEdit}>
                                    <Check className="h-4 w-4 mr-1" /> Save
                                </Button>
                                <Button size="sm" variant="ghost" onClick={() => {
                                    setIsEditing(false);
                                    setEditContent(comment.content);
                                }}>
                                    <X className="h-4 w-4 mr-1" /> Cancel
                                </Button>
                            </div>
                        </div>
                    ) : (
                        <p className="whitespace-pre-wrap mb-4">{comment.content}</p>
                    )}
                    
                    <div className="flex items-center gap-4">
                        <div className="flex items-center gap-1 bg-secondary rounded-md p-1">
                            <Button variant="ghost" size="sm" className="h-6 w-6 p-0" onClick={() => onVote('comment', comment.id, 1)}>
                                <ChevronUp className="h-4 w-4" />
                            </Button>
                            <span className="text-sm font-bold min-w-4 text-center">{comment.score}</span>
                            <Button variant="ghost" size="sm" className="h-6 w-6 p-0" onClick={() => onVote('comment', comment.id, -1)}>
                                <ChevronDown className="h-4 w-4" />
                            </Button>
                        </div>
                        <Button variant="ghost" size="sm" className="h-8 gap-2" onClick={() => setIsReplying(!isReplying)}>
                            <MessageSquare className="h-4 w-4" /> Reply
                        </Button>
                        {isAuthor && !isEditing && (
                            <Button variant="ghost" size="sm" className="h-8 gap-2" onClick={() => setIsEditing(true)}>
                                <Edit2 className="h-4 w-4" /> Edit
                            </Button>
                        )}
                        {(isAdmin || isAuthor) && (
                            <Button variant="ghost" size="sm" className="h-8 gap-2 text-destructive hover:text-destructive" onClick={() => onDelete(comment.id)}>
                                <Trash2 className="h-4 w-4" /> Delete
                            </Button>
                        )}
                        {hasReplies && (
                            <Button 
                                variant="ghost" 
                                size="sm" 
                                className="h-8 gap-2 text-muted-foreground"
                                onClick={() => setAreRepliesHidden(!areRepliesHidden)}
                            >
                                {areRepliesHidden ? (
                                    <>
                                        <ChevronDown className="h-4 w-4" /> Show Replies ({comment.replies!.length})
                                    </>
                                ) : (
                                    <>
                                        <ChevronUp className="h-4 w-4" /> Hide Replies
                                    </>
                                )}
                            </Button>
                        )}
                    </div>
                    
                    {isReplying && (
                        <div className="mt-4 flex gap-2">
                            <Textarea 
                                value={replyContent}
                                onChange={(e) => setReplyContent(e.target.value)}
                                placeholder="Write a reply..."
                                className="min-h-[80px]"
                            />
                            <div className="flex flex-col gap-2">
                                <Button onClick={handleSubmitReply}>Reply</Button>
                                <Button variant="ghost" onClick={() => setIsReplying(false)}>Cancel</Button>
                            </div>
                        </div>
                    )}
                </CardContent>
            </Card>
            {hasReplies && !areRepliesHidden && (
                <div className="mt-2">
                    {comment.replies!.map(reply => (
                        <CommentItem 
                            key={reply.id} 
                            comment={reply} 
                            level={level + 1} 
                            isAdmin={isAdmin}
                            currentUserId={currentUserId}
                            onVote={onVote} 
                            onReply={onReply}
                            onDelete={onDelete}
                            onEdit={onEdit}
                        />
                    ))}
                </div>
            )}
        </div>
    );
};

const PostDetail: React.FC = () => {
    const { postId } = useParams<{ postId: string }>();
    const navigate = useNavigate();
    const { showAlert } = useAlert();
    const [post, setPost] = useState<Post | null>(null);
    const [newComment, setNewComment] = useState('');
    const [loading, setLoading] = useState(true);
    const [isEditingPost, setIsEditingPost] = useState(false);
    const [editPostContent, setEditPostContent] = useState('');
    const [deleteConfirmation, setDeleteConfirmation] = useState<{ type: 'post' | 'comment', id: number } | null>(null);

    const user = AuthService.getCurrentUser();
    const isAdmin = user?.role === 'ADMIN';
    const isAuthor = user?.id === post?.author?.id;

    const fetchPost = async () => {
        if (!postId) return;
        try {
            const data = await ForumService.getPost(Number(postId));
            setPost(data);
            setEditPostContent(data.content);
        } catch (error) {
            console.error('Failed to fetch post', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchPost();
    }, [postId]);

    const handleAddComment = async (parentId?: number, content?: string) => {
        const commentContent = content || newComment;
        if (!commentContent.trim() || !postId) return;
        try {
            await ForumService.addComment(Number(postId), commentContent, parentId);
            if (!parentId) setNewComment('');
            fetchPost(); 
        } catch (error) {
            showAlert('Failed to add comment', 'Error');
        }
    };

    const handleVote = async (type: 'post' | 'comment', id: number, value: number) => {
        try {
            if (type === 'post') {
                await ForumService.votePost(id, value);
            } else {
                await ForumService.voteComment(id, value);
            }
            fetchPost();
        } catch (error) {
            showAlert('Failed to vote', 'Error');
        }
    };

    const handleDeletePost = async () => {
        setDeleteConfirmation({ type: 'post', id: post!.id });
    };

    const handleDeleteComment = async (commentId: number) => {
        setDeleteConfirmation({ type: 'comment', id: commentId });
    };

    const confirmDelete = async () => {
        if (!deleteConfirmation) return;

        try {
            if (deleteConfirmation.type === 'post') {
                await ForumService.deletePost(deleteConfirmation.id);
                showAlert('Post deleted', 'Success');
                navigate(-1);
            } else {
                await ForumService.deleteComment(deleteConfirmation.id);
                fetchPost();
                showAlert('Comment deleted', 'Success');
            }
        } catch (error) {
            showAlert(`Failed to delete ${deleteConfirmation.type}`, 'Error');
        } finally {
            setDeleteConfirmation(null);
        }
    };

    const handleEditPost = async () => {
        if (!editPostContent.trim()) return;
        try {
            await ForumService.updatePost(post!.id, editPostContent);
            setIsEditingPost(false);
            fetchPost();
            showAlert('Post updated', 'Success');
        } catch (error) {
            showAlert('Failed to update post', 'Error');
        }
    };

    const handleEditComment = async (commentId: number, content: string) => {
        try {
            await ForumService.updateComment(commentId, content);
            fetchPost();
            showAlert('Comment updated', 'Success');
        } catch (error) {
            showAlert('Failed to update comment', 'Error');
        }
    };

    if (loading) return <div className="p-6">Loading...</div>;
    if (!post) return <div className="p-6">Post not found</div>;

    return (
        <div className="container mx-auto p-6 max-w-4xl space-y-6">
            <Button variant="ghost" onClick={() => navigate(-1)} className="mb-4">
                &larr; Back to Forum
            </Button>

            <div className="flex gap-4">
                <div className="flex flex-col items-center gap-1 bg-secondary/20 rounded-lg p-2 h-fit">
                    <Button variant="ghost" size="sm" onClick={() => handleVote('post', post.id, 1)}>
                        <ChevronUp className="h-6 w-6" />
                    </Button>
                    <span className="text-lg font-bold">{post.score}</span>
                    <Button variant="ghost" size="sm" onClick={() => handleVote('post', post.id, -1)}>
                        <ChevronDown className="h-6 w-6" />
                    </Button>
                </div>

                <div className="flex-1">
                    <Card className="mb-8 border-l-4 border-l-primary">
                        <CardHeader>
                            <div className="flex justify-between items-start">
                                <div className="space-y-1">
                                    <CardTitle className="text-2xl">{post.title}</CardTitle>
                                    <CardDescription>
                                        Posted by {post.author.displayName} {post.author.email && <span>({post.author.email})</span>} • {format(new Date(post.timestamp), 'PPpp')}
                                        {post.editedAt && <span className="text-muted-foreground ml-1">(edited {format(new Date(post.editedAt), 'PPpp')})</span>}
                                        {post.author.role === 'ADMIN' && (
                                            <Badge variant="destructive" className="ml-2 pointer-events-none select-none">
                                                Admin
                                            </Badge>
                                        )}
                                    </CardDescription>
                                </div>
                                <div className="flex items-center gap-2">
                                    {post.pinned && <Badge>Pinned</Badge>}
                                    {isAuthor && !isEditingPost && (
                                        <Button variant="ghost" size="sm" onClick={() => setIsEditingPost(true)}>
                                            <Edit2 className="h-5 w-5" />
                                        </Button>
                                    )}
                                    {(isAdmin || isAuthor) && (
                                        <Button variant="ghost" size="sm" className="text-destructive hover:text-destructive" onClick={handleDeletePost}>
                                            <Trash2 className="h-5 w-5" />
                                        </Button>
                                    )}
                                </div>
                            </div>
                        </CardHeader>
                        <CardContent>
                            {isEditingPost ? (
                                <div className="space-y-4">
                                    <Textarea 
                                        value={editPostContent}
                                        onChange={(e) => setEditPostContent(e.target.value)}
                                        className="min-h-[200px]"
                                    />
                                    <div className="flex gap-2">
                                        <Button onClick={handleEditPost}>
                                            <Check className="h-4 w-4 mr-2" /> Save Changes
                                        </Button>
                                        <Button variant="ghost" onClick={() => {
                                            setIsEditingPost(false);
                                            setEditPostContent(post.content);
                                        }}>
                                            <X className="h-4 w-4 mr-2" /> Cancel
                                        </Button>
                                    </div>
                                </div>
                            ) : (
                                <div className="prose dark:prose-invert max-w-none whitespace-pre-wrap">
                                    {post.content}
                                </div>
                            )}
                        </CardContent>
                    </Card>

                    <div className="space-y-6">
                        <h3 className="text-xl font-semibold">Comments ({post.comments?.length || 0} threads)</h3>
                        
                        <div className="mb-6 flex gap-2">
                            <Textarea 
                                value={newComment}
                                onChange={(e) => setNewComment(e.target.value)}
                                placeholder="Add a comment..."
                                className="min-h-[100px]"
                            />
                            <Button onClick={() => handleAddComment()} disabled={newComment === ''}>Post</Button>
                        </div>
                        
                        <div className="space-y-4">
                            {post.comments && post.comments.map(comment => (
                                <CommentItem 
                                    key={comment.id} 
                                    comment={comment} 
                                    level={0} 
                                    isAdmin={isAdmin}
                                    currentUserId={user?.id}
                                    onVote={handleVote}
                                    onReply={handleAddComment}
                                    onDelete={handleDeleteComment}
                                    onEdit={handleEditComment}
                                />
                            ))}
                            
                            {(!post.comments || post.comments.length === 0) && (
                                <p className="text-muted-foreground text-center py-8">No comments yet. Be the first to share your thoughts!</p>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            <ConfirmationDialog 
                open={!!deleteConfirmation} 
                onOpenChange={(open) => !open && setDeleteConfirmation(null)}
                title={`Delete ${deleteConfirmation?.type}`}
                description={`Are you sure you want to delete this ${deleteConfirmation?.type}? This action cannot be undone.`}
                onConfirm={confirmDelete}
                onCancel={() => setDeleteConfirmation(null)}
                variant="destructive"
                confirmText="Delete"
            />
        </div>
    );
};

export default PostDetail;