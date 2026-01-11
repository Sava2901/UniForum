import React, { useEffect, useState } from 'react';
import ForumService, { type Forum } from '../../services/forum.service';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useNavigate } from 'react-router-dom';

/**
 * Forum list page.
 * Displays all forums available to the user, grouped by course.
 */
const ForumList: React.FC = () => {
    const [forums, setForums] = useState<Forum[]>([]);
    const navigate = useNavigate();

    useEffect(() => {
        ForumService.getForums().then(setForums).catch(console.error);
    }, []);

    // Group forums by course ID
    const groupedForums = React.useMemo(() => {
        const groups: Record<number, { courseName: string; forums: Forum[] }> = {};
        
        forums.forEach(forum => {
            if (forum.course) {
                if (!groups[forum.course.id]) {
                    groups[forum.course.id] = {
                        courseName: forum.course.name,
                        forums: []
                    };
                }
                groups[forum.course.id].forums.push(forum);
            }
        });
        
        
        Object.values(groups).forEach(group => {
            group.forums.sort((a, b) => {
                if (a.type === 'MAIN_COURSE') return -1;
                if (b.type === 'MAIN_COURSE') return 1;
                return 0;
            });
        });
        
        return groups;
    }, [forums]);

    return (
        <div className="container mx-auto p-6 space-y-8">
            <h1 className="text-3xl font-bold">My Forums</h1>
            
            {Object.keys(groupedForums).length === 0 ? (
                <p className="text-muted-foreground">No forums found.</p>
            ) : (
                Object.values(groupedForums).map((group, index) => (
                    <div key={index} className="space-y-4">
                        <h2 className="text-2xl font-semibold border-b pb-2">{group.courseName}</h2>
                        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                            {group.forums.map(forum => (
                                <Card key={forum.id} className="hover:shadow-lg  cursor-pointer border-l-4 border-l-primary dark:border-l-primary" onClick={() => navigate(`/forums/${forum.id}`)}>
                                    <CardHeader>
                                        <CardTitle className="text-lg">
                                            {forum.type === 'MAIN_COURSE' ? 'Main Course Forum' : `Group: ${forum.groupName}`}
                                        </CardTitle>
                                        <CardDescription>
                                            {forum.type === 'MAIN_COURSE' 
                                                ? 'General discussions for all students' 
                                                : `Private discussions for ${forum.groupName}`}
                                        </CardDescription>
                                        {forum.professor && (
                                            <p className="text-sm text-muted-foreground mt-2">
                                                Professor: <span className="font-medium text-foreground">{forum.professor.firstName} {forum.professor.lastName}</span>
                                            </p>
                                        )}
                                    </CardHeader>
                                    <CardContent>
                                        <Button variant="outline" className="w-full">View Discussions</Button>
                                    </CardContent>
                                </Card>
                            ))}
                        </div>
                    </div>
                ))
            )}
        </div>
    );
};

export default ForumList;
