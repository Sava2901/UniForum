import React, { useEffect, useState } from 'react';
import { type ColumnDef } from '@tanstack/react-table';
import { DataTable } from '@/components/ui/data-table';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import AdminService, { type User, type Course } from '../services/admin.service';

/**
 * Admin dashboard component.
 * Provides functionality for managing users, courses, and forums.
 */
const AdminDashboard: React.FC = () => {
    const [unverifiedUsers, setUnverifiedUsers] = useState<User[]>([]);
    const [allUsers, setAllUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);
    const [editingUser, setEditingUser] = useState<User | null>(null);
    const [isEditOpen, setIsEditOpen] = useState(false);
    const [professors, setProfessors] = useState<User[]>([]);
    const [forums, setForums] = useState<any[]>([]);
    const [selectedProfessor, setSelectedProfessor] = useState('');
    const [selectedForum, setSelectedForum] = useState('');
    const [removeProfessorForumId, setRemoveProfessorForumId] = useState('');
    const [allCourses, setAllCourses] = useState<Course[]>([]);
    const [enrollStudentId, setEnrollStudentId] = useState('');
    const [enrollCourseId, setEnrollCourseId] = useState('');
    const [removeStudentId, setRemoveStudentId] = useState('');
    const [removeCourseId, setRemoveCourseId] = useState('');    
    const [assignForumStudentId, setAssignForumStudentId] = useState('');
    const [assignForumId, setAssignForumId] = useState('');
    const [searchQuery, setSearchQuery] = useState('');
    
    const [alertOpen, setAlertOpen] = useState(false);
    const [alertTitle, setAlertTitle] = useState('');
    const [alertMessage, setAlertMessage] = useState('');

    const showAlert = (title: string, message: string) => {
        setAlertTitle(title);
        setAlertMessage(message);
        setAlertOpen(true);
    };

    const fetchUsers = async () => {
        try {
            const users = await AdminService.getUnverifiedUsers();
            const usersAll = await AdminService.getAllUsers();
            setUnverifiedUsers(users);
            setAllUsers(usersAll);
        } catch (error) {
            console.error('Failed to fetch users', error);
        } finally {
            setLoading(false);
        }
    };
    
    const fetchData = async () => {
        try {
            const profs = await AdminService.getProfessors();
            const f = await AdminService.getAllForums();
            
            const c = await AdminService.getAllCourses();
            setProfessors(profs);
            setForums(f);
            
            setAllCourses(c);
        } catch (error) {
            console.error('Failed to fetch data');
        }
    };

    useEffect(() => {
        fetchUsers();
        fetchData();
    }, []);

    const handleVerify = async (userId: number) => {
        try {
            await AdminService.verifyUser(userId);
            fetchUsers(); 
        } catch (error) {
            showAlert('Error', 'Failed to verify user');
        }
    };
    
    const handleEditUser = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!editingUser) return;
        try {
            await AdminService.updateUser(editingUser.id, editingUser);
            setIsEditOpen(false);
            setEditingUser(null);
            fetchUsers(); 
            showAlert('Success', 'User updated successfully');
        } catch (error) {
            showAlert('Error', 'Failed to update user');
        }
    };
    
    const handleDeleteUser = async () => {
        if (!editingUser) return;
        if (!confirm('Are you sure you want to delete this user? This action cannot be undone.')) return;
        
        try {
            await AdminService.deleteUser(editingUser.id);
            setIsEditOpen(false);
            setEditingUser(null);
            fetchUsers();
            showAlert('Success', 'User deleted successfully');
        } catch (error) {
            showAlert('Error', 'Failed to delete user');
        }
    };
    
    const handleEnrollStudent = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!enrollStudentId || !enrollCourseId) return;
        try {
            await AdminService.enrollStudent(Number(enrollStudentId), Number(enrollCourseId));
            showAlert('Success', 'Student enrolled successfully');
        } catch (error) {
            showAlert('Error', 'Failed to enroll student');
        }
    };

    const handleRemoveStudentFromCourse = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!removeStudentId || !removeCourseId) return;
        try {
            await AdminService.removeStudentFromCourse(Number(removeStudentId), Number(removeCourseId));
            showAlert('Success', 'Student removed from course successfully');
        } catch (error) {
            showAlert('Error', 'Failed to remove student from course');
        }
    };

    const handleAssignStudentToForum = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!assignForumStudentId || !assignForumId) return;
        try {
            await AdminService.assignStudentToForum(Number(assignForumStudentId), Number(assignForumId));
            showAlert('Success', 'Student assigned to forum successfully');
        } catch (error) {
            showAlert('Error', 'Failed to assign student to forum');
        }
    };
    
    const handleAssignProfessor = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!selectedForum || !selectedProfessor) return;
        try {
            await AdminService.assignProfessor(Number(selectedForum), Number(selectedProfessor));
            fetchData();
            showAlert('Success', 'Professor assigned successfully');
        } catch (error) {
            showAlert('Error', 'Failed to assign professor');
        }
    };

    const handleRemoveProfessor = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!removeProfessorForumId) return;
        try {
            await AdminService.removeProfessorFromForum(Number(removeProfessorForumId));
            fetchData();
            showAlert('Success', 'Professor removed from forum successfully');
        } catch (error) {
            showAlert('Error', 'Failed to remove professor');
        }
    };

    const columns: ColumnDef<User>[] = [
        {
            accessorKey: "firstName",
            header: "First Name",
        },
        {
            accessorKey: "lastName",
            header: "Last Name",
        },
        {
            accessorKey: "email",
            header: "Email",
        },
        {
            accessorKey: "role",
            header: "Role",
        },
        {
            id: "actions",
            cell: ({ row }) => {
                const user = row.original;
                return (
                    <Button 
                        size="sm" 
                        onClick={() => handleVerify(user.id)}
                        className="bg-green-600 hover:bg-green-700"
                    >
                        Verify
                    </Button>
                );
            },
        },
    ];
    
    const studentColumns: ColumnDef<User>[] = [
        { accessorKey: "id", header: "ID" },
        { accessorKey: "firstName", header: "First Name" },
        { accessorKey: "lastName", header: "Last Name" },
        { accessorKey: "email", header: "Email" },
        { accessorKey: "groupName", header: "Group" },
        { accessorKey: "studyYear", header: "Year" },
        {
            id: "actions",
            cell: ({ row }) => {
                const user = row.original;
                return (
                    <Button 
                        size="sm" 
                        variant="outline"
                        onClick={() => {
                            setEditingUser(user);
                            setIsEditOpen(true);
                        }}
                    >
                        Edit
                    </Button>
                );
            },
        },
    ];

    return (
        <div className="container mx-auto p-6 space-y-8">
            <h1 className="text-3xl font-bold tracking-tight">Admin Dashboard</h1>
            
            <Tabs defaultValue="users" className="space-y-4">
                <TabsList>
                    <TabsTrigger value="users">Unverified Users</TabsTrigger>
                    <TabsTrigger value="students">Manage Students</TabsTrigger>
                    <TabsTrigger value="courses">Manage Courses</TabsTrigger>
                    <TabsTrigger value="professors">Manage Professors</TabsTrigger>
                </TabsList>
                
                <TabsContent value="users" className="space-y-4">
                    <Card>
                        <CardHeader>
                            <CardTitle>Pending Verifications</CardTitle>
                            <CardDescription>
                                Users not found in the University Database require manual verification.
                            </CardDescription>
                        </CardHeader>
                        <CardContent>
                            {loading ? <p>Loading...</p> : <DataTable columns={columns} data={unverifiedUsers} />}
                        </CardContent>
                    </Card>
                </TabsContent>
                
                <TabsContent value="students" className="space-y-4">
                    <Card>
                        <CardHeader>
                            <CardTitle>All Students</CardTitle>
                            <CardDescription>View and edit student details.</CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                             <div className="flex items-center space-x-2">
                                <Label htmlFor="search-students">Search:</Label>
                                <Input 
                                    id="search-students" 
                                    placeholder="Search by name or email..." 
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    className="max-w-sm"
                                />
                             </div>
                             <DataTable 
                                columns={studentColumns} 
                                data={allUsers.filter(u => 
                                    u.role === 'STUDENT' && 
                                    (u.firstName.toLowerCase().includes(searchQuery.toLowerCase()) || 
                                     u.lastName.toLowerCase().includes(searchQuery.toLowerCase()) || 
                                     u.email.toLowerCase().includes(searchQuery.toLowerCase()))
                                )} 
                             />
                        </CardContent>
                    </Card>
                    
                    {}
                    <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
                        <DialogContent className="sm:max-w-[425px]">
                            <DialogHeader>
                                <DialogTitle>Edit User</DialogTitle>
                                <DialogDescription>
                                    Make changes to the user profile here. Click save when you're done.
                                </DialogDescription>
                            </DialogHeader>
                            {editingUser && (
                                <form onSubmit={handleEditUser} className="grid gap-4 py-4">
                                    <div className="grid grid-cols-4 items-center gap-4">
                                        <Label htmlFor="firstName" className="text-right">First Name</Label>
                                        <Input id="firstName" value={editingUser.firstName} onChange={e => setEditingUser({...editingUser, firstName: e.target.value})} className="col-span-3" />
                                    </div>
                                    <div className="grid grid-cols-4 items-center gap-4">
                                        <Label htmlFor="lastName" className="text-right">Last Name</Label>
                                        <Input id="lastName" value={editingUser.lastName} onChange={e => setEditingUser({...editingUser, lastName: e.target.value})} className="col-span-3" />
                                    </div>
                                    <div className="grid grid-cols-4 items-center gap-4">
                                        <Label htmlFor="email" className="text-right">Email</Label>
                                        <Input id="email" value={editingUser.email} onChange={e => setEditingUser({...editingUser, email: e.target.value})} className="col-span-3" />
                                    </div>
                                    <div className="grid grid-cols-4 items-center gap-4">
                                        <Label htmlFor="group" className="text-right">Group</Label>
                                        <Input id="group" value={editingUser.groupName || ''} onChange={e => setEditingUser({...editingUser, groupName: e.target.value})} className="col-span-3" />
                                    </div>
                                    <div className="grid grid-cols-4 items-center gap-4">
                                        <Label htmlFor="year" className="text-right">Year</Label>
                                        <Input id="year" type="number" value={editingUser.studyYear || ''} onChange={e => setEditingUser({...editingUser, studyYear: Number(e.target.value)})} className="col-span-3" />
                                    </div>
                                    <div className="grid grid-cols-4 items-center gap-4">
                                        <Label htmlFor="semester" className="text-right">Semester</Label>
                                        <Input id="semester" type="number" value={editingUser.semester || ''} onChange={e => setEditingUser({...editingUser, semester: Number(e.target.value)})} className="col-span-3" />
                                    </div>
                                    <DialogFooter className="flex justify-between sm:justify-between">
                                        <Button type="button" variant="destructive" onClick={handleDeleteUser}>Delete User</Button>
                                        <Button type="submit">Save changes</Button>
                                    </DialogFooter>
                                </form>
                            )}
                        </DialogContent>
                    </Dialog>
                </TabsContent>
                
                <TabsContent value="courses">
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        <Card>
                            <CardHeader>
                                <CardTitle>Enroll Student in Course</CardTitle>
                                <CardDescription>Manually add a student to a course to grant forum access.</CardDescription>
                            </CardHeader>
                            <CardContent>
                                <form onSubmit={handleEnrollStudent} className="space-y-4">
                                    <div className="space-y-2">
                                        <Label>Select Student</Label>
                                        <Select onValueChange={setEnrollStudentId}>
                                            <SelectTrigger>
                                                <SelectValue placeholder="Select a student" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                {allUsers.filter(u => u.role === 'STUDENT').map(u => (
                                                    <SelectItem key={u.id} value={String(u.id)}>{u.firstName} {u.lastName} ({u.email})</SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    </div>
                                    <div className="space-y-2">
                                        <Label>Select Course</Label>
                                        <Select onValueChange={setEnrollCourseId}>
                                            <SelectTrigger>
                                                <SelectValue placeholder="Select a course" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                {allCourses.map(c => (
                                                    <SelectItem key={c.id} value={String(c.id)}>{c.name}</SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    </div>
                                    <Button type="submit">Enroll Student</Button>
                                </form>
                            </CardContent>
                        </Card>


                        <Card>
                            <CardHeader>
                                <CardTitle>Assign Student to Subforum</CardTitle>
                                <CardDescription>Grant specific access to a forum group.</CardDescription>
                            </CardHeader>
                            <CardContent>
                                <form onSubmit={handleAssignStudentToForum} className="space-y-4">
                                    <div className="space-y-2">
                                        <Label>Select Student</Label>
                                        <Select onValueChange={setAssignForumStudentId}>
                                            <SelectTrigger>
                                                <SelectValue placeholder="Select a student" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                {allUsers.filter(u => u.role === 'STUDENT').map(u => (
                                                    <SelectItem key={u.id} value={String(u.id)}>{u.firstName} {u.lastName} ({u.email})</SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    </div>
                                    <div className="space-y-2">
                                        <Label>Select Forum</Label>
                                        <Select onValueChange={setAssignForumId}>
                                            <SelectTrigger>
                                                <SelectValue placeholder="Select a forum" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                {forums.map(f => (
                                                    <SelectItem key={f.id} value={String(f.id)}>
                                                        {f.course.name} {f.groupName ? `(${f.groupName})` : '(Main)'}
                                                    </SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    </div>
                                    <Button type="submit">Assign to Forum</Button>
                                </form>
                            </CardContent>
                        </Card>
                        
                        <Card>
                            <CardHeader>
                                <CardTitle>Remove Student from Course</CardTitle>
                                <CardDescription>Revoke a student's access to a course.</CardDescription>
                            </CardHeader>
                            <CardContent>
                                <form onSubmit={handleRemoveStudentFromCourse} className="space-y-4">
                                    <div className="space-y-2">
                                        <Label>Select Student</Label>
                                        <Select onValueChange={setRemoveStudentId}>
                                            <SelectTrigger>
                                                <SelectValue placeholder="Select a student" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                {allUsers.filter(u => u.role === 'STUDENT').map(u => (
                                                    <SelectItem key={u.id} value={String(u.id)}>{u.firstName} {u.lastName} ({u.email})</SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    </div>
                                    <div className="space-y-2">
                                        <Label>Select Course</Label>
                                        <Select onValueChange={setRemoveCourseId}>
                                            <SelectTrigger>
                                                <SelectValue placeholder="Select a course" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                {allCourses.map(c => (
                                                    <SelectItem key={c.id} value={String(c.id)}>{c.name}</SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    </div>
                                    <Button type="submit" variant="destructive">Remove Student</Button>
                                </form>
                            </CardContent>
                        </Card>
                    </div>
                </TabsContent>
                
                <TabsContent value="professors">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <Card>
                            <CardHeader>
                                <CardTitle>Assign Professor to Forum</CardTitle>
                                <CardDescription>Grant professor privileges for specific forums.</CardDescription>
                            </CardHeader>
                            <CardContent>
                                <form onSubmit={handleAssignProfessor} className="space-y-4">
                                    <div className="space-y-2">
                                        <Label>Select Professor</Label>
                                        <Select onValueChange={setSelectedProfessor}>
                                            <SelectTrigger>
                                                <SelectValue placeholder="Select a professor" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                {professors.map(p => (
                                                    <SelectItem key={p.id} value={String(p.id)}>{p.firstName} {p.lastName}</SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    </div>
                                    <div className="space-y-2">
                                        <Label>Select Forum</Label>
                                        <Select onValueChange={setSelectedForum}>
                                            <SelectTrigger>
                                                <SelectValue placeholder="Select a forum" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                {forums.map(f => (
                                                    <SelectItem key={f.id} value={String(f.id)}>
                                                        {f.course.name} {f.groupName ? `(${f.groupName})` : '(Main)'}
                                                    </SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    </div>
                                    <Button type="submit">Assign Professor</Button>
                                </form>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader>
                                <CardTitle>Remove Professor from Forum</CardTitle>
                                <CardDescription>Revoke professor privileges for specific forums.</CardDescription>
                            </CardHeader>
                            <CardContent>
                                <form onSubmit={handleRemoveProfessor} className="space-y-4">
                                    <div className="space-y-2">
                                        <Label>Select Forum</Label>
                                        <Select onValueChange={setRemoveProfessorForumId}>
                                            <SelectTrigger>
                                                <SelectValue placeholder="Select a forum" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                {forums
                                                    .filter(f => f.professor) 
                                                    .map(f => (
                                                    <SelectItem key={f.id} value={String(f.id)}>
                                                        {f.course.name} {f.groupName ? `(${f.groupName})` : '(Main)'} 
                                                        - {f.professor ? `${f.professor.firstName} ${f.professor.lastName}` : ''}
                                                    </SelectItem>
                                                ))}
                                            </SelectContent>
                                        </Select>
                                    </div>
                                    <Button type="submit" variant="destructive">Remove Professor</Button>
                                </form>
                            </CardContent>
                        </Card>
                    </div>
                </TabsContent>

                {}
                <Dialog open={alertOpen} onOpenChange={setAlertOpen}>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>{alertTitle}</DialogTitle>
                            <DialogDescription>{alertMessage}</DialogDescription>
                        </DialogHeader>
                        <DialogFooter>
                            <Button onClick={() => setAlertOpen(false)}>Close</Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            </Tabs>
        </div>
    );
};

export default AdminDashboard;
