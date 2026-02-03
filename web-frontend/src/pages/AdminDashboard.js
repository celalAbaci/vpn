import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Users, Download, Activity, Server } from 'lucide-react';
import axios from 'axios';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    activeUsers: 1240,
    totalDownloads: 54300,
    activeServers: 15,
    activeConnections: 890
  });

  const data = [
    { name: '00:00', load: 40, connections: 240 },
    { name: '04:00', load: 30, connections: 139 },
    { name: '08:00', load: 60, connections: 580 },
    { name: '12:00', load: 85, connections: 890 },
    { name: '16:00', load: 70, connections: 750 },
    { name: '20:00', load: 90, connections: 920 },
  ];

  useEffect(() => {
    // Fetch real data from backend
    // axios.get('/api/v1/admin/statistics').then(res => setStats(res.data));
  }, []);

  return (
    <div className="flex h-screen bg-gray-100 font-sans">
      <aside className="w-64 bg-gray-800 text-white p-6">
        <h2 className="text-2xl font-bold mb-8">Admin Panel</h2>
        <ul>
          <li className="mb-4 text-blue-400 font-semibold">Dashboard</li>
          <li className="mb-4 text-gray-400">Servers</li>
          <li className="mb-4 text-gray-400">Users</li>
          <li className="mb-4 text-gray-400">Logs</li>
        </ul>
      </aside>

      <main className="flex-1 p-8 overflow-y-auto">
        <h1 className="text-3xl font-bold mb-8 text-gray-800">Dashboard Overview</h1>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <StatCard icon={<Users />} title="Active Users" value={stats.activeUsers} color="bg-blue-500" />
          <StatCard icon={<Download />} title="Total Downloads" value={stats.totalDownloads} color="bg-green-500" />
          <StatCard icon={<Server />} title="Active Servers" value={stats.activeServers} color="bg-purple-500" />
          <StatCard icon={<Activity />} title="Live Connections" value={stats.activeConnections} color="bg-red-500" />
        </div>

        <div className="bg-white p-6 rounded-lg shadow-md mb-8">
            <h3 className="text-xl font-bold mb-4">Server Load & Connections</h3>
            <div className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={data}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line type="monotone" dataKey="load" stroke="#8884d8" name="CPU Load %" />
                    <Line type="monotone" dataKey="connections" stroke="#82ca9d" name="Connections" />
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </div>
      </main>
    </div>
  );
};

const StatCard = ({ icon, title, value, color }) => (
  <div className="bg-white p-6 rounded-lg shadow-md flex items-center">
    <div className={`p-4 rounded-full text-white mr-4 ${color}`}>
      {icon}
    </div>
    <div>
      <p className="text-gray-500 text-sm">{title}</p>
      <p className="text-2xl font-bold text-gray-800">{value}</p>
    </div>
  </div>
);

export default AdminDashboard;
