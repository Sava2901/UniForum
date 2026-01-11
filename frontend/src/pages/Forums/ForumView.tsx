import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ForumService, { type Post } from '../../services/forum.service';
import { Button } from '@/components/ui/button';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';

/**
 * Forum view page.
 * Displays the list of posts within a specific forum and allows creating new posts.
 */
const ForumView: React.FC = () => {
    const { forumId } = useParams<{ forumId: string }>();
    const [posts, setPosts] = useState<Post[]>([]);
    const navigate = useNavigate();
    
    
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [open, setOpen] = useState(false);
    const [forumName, setForumName] = useState('Forum Discussions');

    useEffect(() => {
        if (forumId) {
            ForumService.getPosts(Number(forumId)).then(setPosts).catch(console.error);
            
            
            ForumService.getForums().then(forums => {
                const current = forums.find(f => f.id === Number(forumId));
                if (current) {
                    const name = current.course ? current.course.name : 'General Forum';
                    const sub = current.groupName ? ` (${current.groupName})` : '';
                    setForumName(`Discussions in ${name}${sub}`);
                }
            }).catch(console.error);
        }
    }, [forumId]);

    const handleCreatePost = async (e: React.FormEvent) => {
        e.preventDefault();
        if (forumId) {
            await ForumService.createPost(Number(forumId), title, content);
            setOpen(false);
            setTitle('');
            setContent('');
            
            ForumService.getPosts(Number(forumId)).then(setPosts);
        }
    };

    return (
        <div className="container mx-auto p-6 space-y-6">
            <Button variant="ghost" onClick={() => navigate('/forums')} className="mb-4">
                &larr; Back to All Forums
            </Button>

            <div className="flex justify-between items-center">
                <h1 className="text-3xl font-bold">{forumName}</h1>
                <Dialog open={open} onOpenChange={setOpen}>
                    <DialogTrigger asChild>
                        <Button>New Post</Button>
                    </DialogTrigger>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>Create a new discussion</DialogTitle>
                        </DialogHeader>
                        <form onSubmit={handleCreatePost} className="space-y-4">
                            <Input placeholder="Title" value={title} onChange={e => setTitle(e.target.value)} required />
                            <Textarea placeholder="Content..." value={content} onChange={e => setContent(e.target.value)} required />
                            <Button type="submit">Post</Button>
                        </form>
                    </DialogContent>
                </Dialog>
            </div>

            <div className="space-y-4">
                {posts.map(post => (
                    <Card key={post.id} className="cursor-pointer hover:bg-slate-50 dark:hover:bg-slate-800" onClick={() => navigate(`/forums/posts/${post.id}`)}>
                        <CardHeader>
                            <div className="flex justify-between items-start">
                                <CardTitle className="text-xl">{post.title}</CardTitle>
                                <div className="flex items-center gap-2">
                                    <span className={`text-sm font-bold px-2 py-1 rounded ${post.score > 0 ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' : post.score < 0 ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200' : 'bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-200'}`}>
                                        {post.score > 0 ? '+' : ''}{post.score}
                                    </span>
                                    {post.pinned && <span className="bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200 text-xs px-2 py-1 rounded">Pinned</span>}
                                </div>
                            </div>
                            <p className="text-sm text-muted-foreground">
                                Posted by {post.author.displayName} {post.author.email && <span className="text-xs">({post.author.email})</span>} • {new Date(post.timestamp).toLocaleDateString()}
                            </p>
                        </CardHeader>
                        <CardContent>
                            <p className="line-clamp-2">{post.content}</p>
                        </CardContent>
                    </Card>
                ))}
            </div>
        </div>
    );
};

export default ForumView;
