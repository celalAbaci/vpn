import React, { useEffect, useState } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, BarChart, Bar } from 'recharts';
import { Activity, Download, Users, Server } from 'lucide-react';
import axios from 'axios';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    activeUsers: 1234, // Default/Mock
    totalDownloads: 45200,
    activeServers: 12,
    avgLoad: 42
  });
  const [chartData, setChartData] = useState([
      { name: '00:00', users: 400, load: 24 },
      { name: '04:00', users: 300, load: 13 },
      { name: '08:00', users: 200, load: 38 },
      { name: '12:00', users: 278, load: 59 },
      { name: '16:00', users: 189, load: 48 },
      { name: '20:00', users: 239, load: 68 },
      { name: '23:59', users: 349, load: 43 },
  ]);

  useEffect(() => {
    const fetchStats = async () => {
        try {
            // Attempt to fetch from backend
            const response = await axios.get('/api/v1/admin/statistics');
            if (response.data && response.data.success) {
                const data = response.data.data;
                setStats({
                    activeUsers: data.activeUsers || 0,
                    totalDownloads: data.totalDownloads || 0,
                    activeServers: data.activeServers || 0,
                    avgLoad: data.avgLoad || 0
                });
            }
        } catch (error) {
            console.warn("Could not fetch admin statistics, using defaults.", error);
        }
    };

    fetchStats();
  }, []);

  return (
    <div className="p-6 bg-gray-100 min-h-screen">
      <h1 className="text-3xl font-bold mb-8 text-gray-800">Admin Dashboard</h1>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white p-6 rounded-lg shadow flex items-center">
            <div className="p-3 bg-blue-100 rounded-full mr-4">
                <Users className="text-blue-600" />
            </div>
            <div>
                <p className="text-gray-500 text-sm">Active Users</p>
                <p className="text-2xl font-bold">{stats.activeUsers.toLocaleString()}</p>
            </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow flex items-center">
            <div className="p-3 bg-green-100 rounded-full mr-4">
                <Download className="text-green-600" />
            </div>
            <div>
                <p className="text-gray-500 text-sm">Total Downloads</p>
                <p className="text-2xl font-bold">{(stats.totalDownloads / 1000).toFixed(1)}K</p>
            </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow flex items-center">
            <div className="p-3 bg-red-100 rounded-full mr-4">
                <Server className="text-red-600" />
            </div>
            <div>
                <p className="text-gray-500 text-sm">Active Servers</p>
                <p className="text-2xl font-bold">{stats.activeServers}</p>
            </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow flex items-center">
             <div className="p-3 bg-yellow-100 rounded-full mr-4">
                <Activity className="text-yellow-600" />
            </div>
             <div>
                <p className="text-gray-500 text-sm">Avg Load</p>
                <p className="text-2xl font-bold">{stats.avgLoad}%</p>
            </div>
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-xl font-semibold mb-4">Real-time Traffic</h2>
            <div className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={chartData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="name" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Line type="monotone" dataKey="users" stroke="#8884d8" activeDot={{ r: 8 }} />
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-xl font-semibold mb-4">Server Load Distribution</h2>
             <div className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={chartData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="name" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Bar dataKey="load" fill="#82ca9d" />
                    </BarChart>
                </ResponsiveContainer>
            </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
