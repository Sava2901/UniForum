import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import AdminDashboard from './pages/AdminDashboard';
import ForumList from './pages/Forums/ForumList';
import ForumView from './pages/Forums/ForumView';
import PostDetail from './pages/Forums/PostDetail';
import UserProfile from './pages/UserProfile';
import ProtectedRoute from './components/ProtectedRoute';
import AdminRoute from './components/AdminRoute';
import Layout from './components/Layout';

const App: React.FC = () => {
    return (
        <Router>
            <div className="app">
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    
                    {/* Protected Routes for All Authenticated Users */}
                    <Route element={<ProtectedRoute />}>
                        <Route element={<Layout />}>
                            <Route path="/forums" element={<ForumList />} />
                            <Route path="/forums/:forumId" element={<ForumView />} />
                            <Route path="/forums/posts/:postId" element={<PostDetail />} />
                            <Route path="/profile" element={<UserProfile />} />
                        </Route>
                    </Route>
                    
                    {/* Protected Routes for Admins Only */}
                    <Route element={<AdminRoute />}>
                        <Route element={<Layout />}>
                            <Route path="/admin" element={<AdminDashboard />} />
                        </Route>
                    </Route>
                    
                    <Route path="/" element={<Navigate to="/login" />} />
                </Routes>
            </div>
        </Router>
    );
};

export default App;
