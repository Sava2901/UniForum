import React from 'react';
import AuthService from '../services/auth.service';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

const UserProfile: React.FC = () => {
    const user = AuthService.getCurrentUser();

    if (!user) return <div>Loading...</div>;

    return (
        <div className="container mx-auto p-6 max-w-2xl">
            <Card>
                    <CardHeader>
                        <div className="flex justify-between items-center">
                            <div>
                                <CardTitle className="text-2xl">User Profile</CardTitle>
                                <CardDescription>Manage your account settings and preferences.</CardDescription>
                            </div>
                            <Badge variant="outline" className="text-lg px-3 py-1">{user.role}</Badge>
                        </div>
                    </CardHeader>
                    <CardContent className="space-y-6">
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-1">
                                <h3 className="text-sm font-medium text-muted-foreground">Full Name</h3>
                                <p className="text-lg font-semibold">{user.firstName} {user.lastName}</p>
                            </div>
                            <div className="space-y-1">
                                <h3 className="text-sm font-medium text-muted-foreground">Email</h3>
                                <p className="text-lg font-semibold">{user.email}</p>
                            </div>
                            <div className="space-y-1">
                                <h3 className="text-sm font-medium text-muted-foreground">Nickname</h3>
                                <p className="text-lg font-semibold">{user.nickname || '-'}</p>
                            </div>
                            {user.role === 'STUDENT' && (
                                <>
                                    <div className="space-y-1">
                                        <h3 className="text-sm font-medium text-muted-foreground">Group</h3>
                                        <p className="text-lg font-semibold">{user.groupName || '-'}</p>
                                    </div>
                                    <div className="space-y-1">
                                        <h3 className="text-sm font-medium text-muted-foreground">Year</h3>
                                        <p className="text-lg font-semibold">{user.studyYear || '-'}</p>
                                    </div>
                                    <div className="space-y-1">
                                        <h3 className="text-sm font-medium text-muted-foreground">Semester</h3>
                                        <p className="text-lg font-semibold">{user.semester || '-'}</p>
                                    </div>
                                </>
                            )}
                        </div>
                    </CardContent>
                </Card>
        </div>
    );
};

export default UserProfile;