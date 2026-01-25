import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Users, Download, Activity, Server } from 'lucide-react';
import axios from 'axios';

const AdminDashboard = () => {
    // Placeholder Data - Real implementation would fetch from /api/v1/admin/statistics
    const [stats, setStats] = useState({
        activeUsers: 1205,
        totalDownloads: 45000,
        activeConnections: 850,
        serverLoad: [
            { name: '00:00', load: 40 },
            { name: '04:00', load: 30 },
            { name: '08:00', load: 60 },
            { name: '12:00', load: 85 },
            { name: '16:00', load: 70 },
            { name: '20:00', load: 90 },
            { name: '24:00', load: 50 },
        ]
    });

    useEffect(() => {
        axios.get('/api/v1/admin/statistics')
            .then(res => {
                if (res.data && res.data.success) {
                    setStats(res.data.data);
                }
            })
            .catch(err => console.error("Failed to fetch stats", err));
    }, []);

    return (
        <div className="p-6">
            <h1 className="text-3xl font-bold mb-6">Admin Dashboard</h1>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                <StatCard icon={<Users />} title="Active Users" value={stats.activeUsers} color="bg-blue-500" />
                <StatCard icon={<Download />} title="Total Downloads" value={stats.totalDownloads} color="bg-green-500" />
                <StatCard icon={<Activity />} title="Active Connections" value={stats.activeConnections} color="bg-purple-500" />
                <StatCard icon={<Server />} title="Servers Online" value="12" color="bg-orange-500" />
            </div>

            <div className="bg-white p-6 rounded-lg shadow">
                <h2 className="text-xl font-bold mb-4">Server Load Over Time</h2>
                <div className="h-64">
                    <ResponsiveContainer width="100%" height="100%">
                        <LineChart data={stats.serverLoad}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="name" />
                            <YAxis />
                            <Tooltip />
                            <Legend />
                            <Line type="monotone" dataKey="load" stroke="#8884d8" activeDot={{ r: 8 }} />
                        </LineChart>
                    </ResponsiveContainer>
                </div>
            </div>
        </div>
    );
};

const StatCard = ({ icon, title, value, color }) => (
    <div className="bg-white p-4 rounded-lg shadow flex items-center">
        <div className={`p-3 rounded-full ${color} text-white mr-4`}>
            {icon}
        </div>
        <div>
            <p className="text-gray-500">{title}</p>
            <p className="text-2xl font-bold">{value}</p>
        </div>
    </div>
);

export default AdminDashboard;
