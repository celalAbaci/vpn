import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Users, Download, Server, Activity, Ban, Trash2, Plus } from 'lucide-react';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    activeUsers: 0,
    totalDownloads: 0,
    activeConnections: 0,
    serverLoad: []
  });
  const [servers, setServers] = useState([]);
  const [logs, setLogs] = useState([]);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      // In a real scenario, fetch from /api/v1/admin/statistics
      // const statsRes = await axios.get('/api/v1/admin/statistics');
      // setStats(statsRes.data);

      // Mock Data
      setStats({
        activeUsers: 1250,
        totalDownloads: 5430,
        activeConnections: 850,
        serverLoad: [
            { name: '10:00', load: 45 },
            { name: '11:00', load: 55 },
            { name: '12:00', load: 80 },
            { name: '13:00', load: 70 },
            { name: '14:00', load: 90 },
        ]
      });

    } catch (error) {
      console.error("Error fetching dashboard data", error);
    }
  };

  return (
    <div className="p-6 bg-gray-100 min-h-screen">
      <h1 className="text-3xl font-bold mb-6 text-gray-800">Admin Dashboard</h1>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <StatCard icon={<Users />} title="Active Users" value={stats.activeUsers} color="bg-blue-500" />
        <StatCard icon={<Download />} title="Total Downloads" value={stats.totalDownloads} color="bg-green-500" />
        <StatCard icon={<Activity />} title="Active Connections" value={stats.activeConnections} color="bg-purple-500" />
        <StatCard icon={<Server />} title="Servers Online" value="12" color="bg-orange-500" />
      </div>

      {/* Charts */}
      <div className="bg-white p-6 rounded-lg shadow-md mb-8">
        <h2 className="text-xl font-semibold mb-4">Server Load Over Time</h2>
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

      {/* Management Sections */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

        {/* Server Management */}
        <div className="bg-white p-6 rounded-lg shadow-md">
            <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-semibold">Server Management</h2>
                <button className="bg-blue-600 text-white px-3 py-1 rounded flex items-center gap-2">
                    <Plus size={16} /> Add Server
                </button>
            </div>
            <ul>
                <li className="flex justify-between items-center py-2 border-b">
                    <span>US-East-1 (Premium)</span>
                    <span className="text-green-500 text-sm">Online</span>
                    <button className="text-red-500"><Trash2 size={16}/></button>
                </li>
                 <li className="flex justify-between items-center py-2 border-b">
                    <span>DE-Frankfurt (Free)</span>
                    <span className="text-green-500 text-sm">Online</span>
                    <button className="text-red-500"><Trash2 size={16}/></button>
                </li>
            </ul>
        </div>

        {/* User Management */}
         <div className="bg-white p-6 rounded-lg shadow-md">
            <h2 className="text-xl font-semibold mb-4">User Actions</h2>
            <div className="flex gap-2 mb-4">
                <input type="text" placeholder="Search user..." className="border p-2 rounded w-full" />
                <button className="bg-gray-200 p-2 rounded">Search</button>
            </div>
            <div>
                <div className="flex justify-between items-center py-2 border-b">
                    <span>user@example.com</span>
                    <button className="text-red-600 border border-red-600 px-2 py-1 rounded text-sm flex items-center gap-1">
                        <Ban size={14}/> Ban
                    </button>
                </div>
            </div>
        </div>

      </div>
    </div>
  );
};

const StatCard = ({ icon, title, value, color }) => (
  <div className={`${color} text-white p-6 rounded-lg shadow-lg flex items-center justify-between`}>
    <div>
        <p className="text-sm opacity-80">{title}</p>
        <p className="text-3xl font-bold">{value}</p>
    </div>
    <div className="opacity-50">
        {React.cloneElement(icon, { size: 40 })}
    </div>
  </div>
);

export default AdminDashboard;
