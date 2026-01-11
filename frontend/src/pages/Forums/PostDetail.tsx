import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ForumService, { type Post, type Comment } from '../../services/forum.service';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { format } from 'date-fns';
import { useAlert } from '@/context/AlertContext';
import { ChevronUp, ChevronDown, MessageSquare, Minus, Plus } from 'lucide-react';

interface CommentItemProps {
    comment: Comment;
    level: number;
    onVote: (type: 'post' | 'comment', id: number, value: number) => void;
    onReply: (parentId: number, content: string) => Promise<void>;
}

const CommentItem: React.FC<CommentItemProps> = ({ comment, level, onVote, onReply }) => {
    const [areRepliesHidden, setAreRepliesHidden] = useState(false);
    const [isReplying, setIsReplying] = useState(false);
    const [replyContent, setReplyContent] = useState('');
    const hasReplies = comment.replies && comment.replies.length > 0;

    const handleSubmitReply = async () => {
        if (!replyContent.trim()) return;
        await onReply(comment.id, replyContent);
        setReplyContent('');
        setIsReplying(false);
    };

    return (
        <div className={`mt-4 ${level > 0 ? 'ml-8 border-l-2 pl-4' : ''}`}>
            <Card className={comment.pinned ? "border-yellow-400 border-2" : ""}>
                <CardHeader className="pb-2">
                    <div className="flex justify-between text-sm text-muted-foreground">
                        <span className="font-semibold text-foreground flex items-center gap-2">
                            {comment.author.displayName}
                            {comment.author.email && <span className="font-normal text-muted-foreground">({comment.author.email})</span>}
                            {comment.author.role === 'PROFESSOR' && (
                                <Badge className="bg-yellow-400 text-yellow-900 hover:bg-yellow-400 pointer-events-none select-none border-yellow-500">
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
                    </div>
                </CardHeader>
                <CardContent>
                    <p className="whitespace-pre-wrap mb-4">{comment.content}</p>
                    <div className="flex items-center gap-4">
                        <div className="flex items-center gap-1 bg-secondary rounded-md p-1">
                            <Button variant="ghost" size="sm" className="h-6 w-6 p-0" onClick={() => onVote('comment', comment.id, 1)}>
                                <ChevronUp className="h-4 w-4" />
                            </Button>
                            <span className="text-sm font-bold w-4 text-center">{comment.score}</span>
                            <Button variant="ghost" size="sm" className="h-6 w-6 p-0" onClick={() => onVote('comment', comment.id, -1)}>
                                <ChevronDown className="h-4 w-4" />
                            </Button>
                        </div>
                        <Button variant="ghost" size="sm" className="h-8 gap-2" onClick={() => setIsReplying(!isReplying)}>
                            <MessageSquare className="h-4 w-4" /> Reply
                        </Button>
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
                            onVote={onVote} 
                            onReply={onReply}
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

    const fetchPost = async () => {
        if (!postId) return;
        try {
            const data = await ForumService.getPost(Number(postId));
            setPost(data);
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
            fetchPost(); // Refresh comments
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
                                        {post.author.role === 'ADMIN' && (
                                            <Badge variant="destructive" className="ml-2 pointer-events-none select-none">
                                                Admin
                                            </Badge>
                                        )}
                                    </CardDescription>
                                </div>
                                {post.pinned && <Badge>Pinned</Badge>}
                            </div>
                        </CardHeader>
                        <CardContent>
                            <div className="prose dark:prose-invert max-w-none whitespace-pre-wrap">
                                {post.content}
                            </div>
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
                                    onVote={handleVote} 
                                    onReply={handleAddComment}
                                />
                            ))}
                            
                            {(!post.comments || post.comments.length === 0) && (
                                <p className="text-muted-foreground text-center py-8">No comments yet. Be the first to share your thoughts!</p>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PostDetail;