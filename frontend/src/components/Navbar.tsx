import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/auth.service';
import NotificationService, { type NotificationDto } from '../services/notification.service';
import { Button } from './ui/button';
import { 
    DropdownMenu, 
    DropdownMenuContent, 
    DropdownMenuItem, 
    DropdownMenuLabel, 
    DropdownMenuSeparator, 
    DropdownMenuTrigger 
} from './ui/dropdown-menu';
import { Avatar, AvatarFallback } from './ui/avatar';
import { Mail } from 'lucide-react';
import { Client } from '@stomp/stompjs';

import SockJS from 'sockjs-client';

/**
 * Navigation bar component.
 * Displays user info, notifications, and navigation links.
 * Handles WebSocket connection for real-time notifications.
 */
const Navbar: React.FC = () => {
    const navigate = useNavigate();
    const user = AuthService.getCurrentUser();
    const [notifications, setNotifications] = useState<NotificationDto[]>([]);

    useEffect(() => {
        if (!user) return;

        
        NotificationService.getUserNotifications().then(res => {
            setNotifications(res.data.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()));
        }).catch(err => console.error("Failed to fetch notifications", err));

        
        const socket = new SockJS('http://localhost:8080/ws');
        const stompClient = new Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                stompClient.subscribe(`/topic/notifications/${user.id}`, (message) => {
                    const newNotification = JSON.parse(message.body);
                    setNotifications(prev => [newNotification, ...prev]);
                });
            },
            
        });

        stompClient.activate();

        return () => {
            stompClient.deactivate();
        };
    }, [user?.id]);

    const handleLogout = () => {
        AuthService.logout();
        navigate('/login');
    };

    const handleNotificationClick = async (notification: NotificationDto) => {
        if (!notification.read) {
            try {
                await NotificationService.markAsRead(notification.id);
                setNotifications(prev => prev.map(n => n.id === notification.id ? { ...n, read: true } : n));
            } catch (error) {
                console.error("Failed to mark notification as read", error);
            }
        }
        if (notification.relatedEntityId) {
            navigate(`/forums/posts/${notification.relatedEntityId}`);
        }
    };

    if (!user) return null;

    const initials = `${user.firstName?.charAt(0) || ''}${user.lastName?.charAt(0) || ''}`.toUpperCase() || 'U';
    const fullName = `${user.firstName || 'User'} ${user.lastName || ''}`.trim();
    const unreadCount = notifications.filter(n => !n.read).length;

    return (
        <nav className="border-b bg-background">
            <div className="container mx-auto flex h-16 items-center justify-between px-4">
                <div className="font-bold text-xl cursor-pointer" onClick={() => navigate('/forums')}>
                    UniForum
                </div>
                
                <div className="flex items-center gap-4">
                    {}
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" className="relative h-8 w-8 rounded-full p-2">
                                <Mail className="h-6 w-6 text-gray-700 dark:text-gray-300" strokeWidth={2}/>
                                {unreadCount > 0 && (
                                    <span className="absolute -top-1 -right-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] text-white">
                                        {unreadCount}
                                    </span>
                                )}
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent className="w-80 max-h-96 overflow-y-auto" align="end" forceMount>
                            <DropdownMenuLabel>Notifications</DropdownMenuLabel>
                            <DropdownMenuSeparator />
                            {notifications.length === 0 ? (
                                <div className="p-4 text-center text-sm text-muted-foreground">
                                    No notifications
                                </div>
                            ) : (
                                notifications.map(notification => (
                                    <DropdownMenuItem 
                                        key={notification.id} 
                                        onClick={() => handleNotificationClick(notification)}
                                        className="cursor-pointer"
                                    >
                                        <div className={`flex flex-col gap-1 w-full ${!notification.read ? 'font-semibold bg-slate-50 dark:bg-slate-900 rounded p-1' : ''}`}>
                                            <span className="text-sm">{notification.message}</span>
                                            <span className="text-xs text-muted-foreground">
                                                {new Date(notification.timestamp).toLocaleString()}
                                            </span>
                                        </div>
                                    </DropdownMenuItem>
                                ))
                            )}
                        </DropdownMenuContent>
                    </DropdownMenu>

                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                                <Avatar className="h-8 w-8">
                                    <AvatarFallback>{initials}</AvatarFallback>
                                </Avatar>
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent className="w-56" align="end" forceMount>
                            <DropdownMenuLabel className="font-normal">
                                <div className="flex flex-col space-y-1">
                                    <p className="text-sm font-medium leading-none">{fullName}</p>
                                    <p className="text-xs leading-none text-muted-foreground">
                                        {user.email}
                                    </p>
                                </div>
                            </DropdownMenuLabel>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem onClick={() => navigate('/profile')}>
                                Profile
                            </DropdownMenuItem>
                            {user.role === 'ADMIN' && (
                                <DropdownMenuItem onClick={() => navigate('/admin')}>
                                    Admin Dashboard
                                </DropdownMenuItem>
                            )}
                            <DropdownMenuSeparator />
                            <DropdownMenuItem onClick={handleLogout}>
                                Log out
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;