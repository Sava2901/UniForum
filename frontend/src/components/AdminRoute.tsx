import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import AuthService from '../services/auth.service';

const AdminRoute: React.FC = () => {
    const currentUser = AuthService.getCurrentUser();
    
    if (!currentUser) {
        return <Navigate to="/login" replace />;
    }
    
    if (currentUser.role !== 'ADMIN') {
        
        return <Navigate to="/forums" replace />;
    }
    
    return <Outlet />;
};

export default AdminRoute;