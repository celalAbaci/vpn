import React, { useState, useEffect } from 'react';

const AdminDashboard = () => {
    const [stats, setStats] = useState({
        activeUsers: 0,
        totalDownloads: 0,
        activeServers: 0,
        activeConnections: 0
    });

    useEffect(() => {
        // Fetch stats from backend
        // fetch('/api/v1/admin/stats').then(...)
        // Mock data for now
        setStats({
            activeUsers: 120,
            totalDownloads: 5400,
            activeServers: 15,
            activeConnections: 45
        });
    }, []);

    return (
        <div className="p-6 bg-gray-100 min-h-screen">
            <h1 className="text-3xl font-bold mb-6">Admin Dashboard</h1>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                <div className="bg-white p-6 rounded-lg shadow-md">
                    <h3 className="text-gray-500 text-sm">Active Users</h3>
                    <p className="text-3xl font-bold text-blue-600">{stats.activeUsers}</p>
                </div>
                <div className="bg-white p-6 rounded-lg shadow-md">
                    <h3 className="text-gray-500 text-sm">Total Downloads</h3>
                    <p className="text-3xl font-bold text-green-600">{stats.totalDownloads}</p>
                </div>
                <div className="bg-white p-6 rounded-lg shadow-md">
                    <h3 className="text-gray-500 text-sm">Active Servers</h3>
                    <p className="text-3xl font-bold text-purple-600">{stats.activeServers}</p>
                </div>
                <div className="bg-white p-6 rounded-lg shadow-md">
                    <h3 className="text-gray-500 text-sm">Current Connections</h3>
                    <p className="text-3xl font-bold text-red-600">{stats.activeConnections}</p>
                </div>
            </div>

            {/* Server Load Chart Placeholder */}
            <div className="bg-white p-6 rounded-lg shadow-md mb-8">
                <h2 className="text-xl font-bold mb-4">Server Load Distribution</h2>
                <div className="h-64 bg-gray-50 flex items-center justify-center border-2 border-dashed border-gray-300">
                    <span className="text-gray-400">Chart Visualization Area (CPU/RAM)</span>
                </div>
            </div>

            {/* Actions */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <button className="bg-blue-600 text-white py-3 px-4 rounded hover:bg-blue-700">
                    Add New Server
                </button>
                <button className="bg-yellow-500 text-white py-3 px-4 rounded hover:bg-yellow-600">
                    User Management
                </button>
                <button className="bg-gray-800 text-white py-3 px-4 rounded hover:bg-gray-900">
                    View Logs
                </button>
            </div>
        </div>
    );
};

export default AdminDashboard;
