import React from 'react';
import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';

/**
 * Main layout component.
 * Wraps the application content with the navigation bar.
 */
const Layout: React.FC = () => {
    return (
        <div className="min-h-screen bg-slate-50 dark:bg-slate-900">
            <Navbar />
            <Outlet />
        </div>
    );
};

export default Layout;