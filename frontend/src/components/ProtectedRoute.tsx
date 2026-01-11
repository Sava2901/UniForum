import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import AuthService from '../services/auth.service';

const ProtectedRoute: React.FC = () => {
    const currentUser = AuthService.getCurrentUser();
    
    if (!currentUser) {
        return <Navigate to="/login" replace />;
    }
    
    return <Outlet />;
};

export default ProtectedRoute;