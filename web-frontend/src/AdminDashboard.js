import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Users, Download, Activity, Server } from 'lucide-react';
import axios from 'axios';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    activeUsers: 0,
    totalDownloads: 0,
    activeConnections: 0,
    serverLoad: []
  });

  useEffect(() => {
    // Mock Data fetch or real API call
    // axios.get('/api/v1/admin/statistics').then(...)
    setStats({
      activeUsers: 1245,
      totalDownloads: 8500,
      activeConnections: 342,
      serverLoad: [
        { name: '10:00', load: 45 },
        { name: '10:05', load: 55 },
        { name: '10:10', load: 40 },
        { name: '10:15', load: 70 },
        { name: '10:20', load: 65 },
        { name: '10:25', load: 80 },
        { name: '10:30', load: 50 },
      ]
    });
  }, []);

  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-800 mb-8">Admin Dashboard</h1>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <StatCard icon={<Users size={24} />} title="Active Users" value={stats.activeUsers} color="bg-blue-500" />
          <StatCard icon={<Download size={24} />} title="Total Downloads" value={stats.totalDownloads} color="bg-green-500" />
          <StatCard icon={<Activity size={24} />} title="Active Connections" value={stats.activeConnections} color="bg-purple-500" />
          <StatCard icon={<Server size={24} />} title="Servers Online" value="12" color="bg-orange-500" />
        </div>

        {/* Charts */}
        <div className="bg-white p-6 rounded-xl shadow-md">
          <h2 className="text-xl font-semibold mb-4">Real-time Server Load</h2>
          <div className="h-80 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={stats.serverLoad}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="load" stroke="#3b82f6" activeDot={{ r: 8 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};

const StatCard = ({ icon, title, value, color }) => (
  <div className="bg-white p-6 rounded-xl shadow-md flex items-center space-x-4">
    <div className={`p-4 rounded-lg text-white ${color}`}>
      {icon}
    </div>
    <div>
      <p className="text-gray-500 text-sm">{title}</p>
      <p className="text-2xl font-bold text-gray-800">{value}</p>
    </div>
  </div>
);

export default AdminDashboard;
