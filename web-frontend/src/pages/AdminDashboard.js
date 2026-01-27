import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const AdminDashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  // Mock Data if API fails or for demo
  const mockData = {
    activeUsers: 1250,
    totalDownloads: 50000,
    activeConnections: 850,
    serverLoad: [
      { name: '00:00', load: 20 },
      { name: '04:00', load: 15 },
      { name: '08:00', load: 45 },
      { name: '12:00', load: 80 },
      { name: '16:00', load: 70 },
      { name: '20:00', load: 90 },
      { name: '23:59', load: 50 },
    ]
  };

  useEffect(() => {
    // Fetch stats from backend
    // In a real app, you'd attach the Bearer token here
    const fetchStats = async () => {
      try {
        const response = await axios.get('/api/v1/admin/statistics/dashboard');
        if (response.data && response.data.data) {
             setStats(response.data.data);
        } else {
             setStats(mockData);
        }
      } catch (error) {
        console.error("Failed to fetch stats, using mock data", error);
        setStats(mockData);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  if (loading) return <div className="p-10 text-center">Loading Dashboard...</div>;

  return (
    <div className="p-6">
      <h2 className="text-3xl font-bold mb-6 text-gray-800">Admin Dashboard</h2>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white p-6 rounded-lg shadow border-l-4 border-blue-500">
          <h3 className="text-gray-500 text-sm font-uppercase">Active Users</h3>
          <p className="text-3xl font-bold text-gray-800">{stats.activeUsers || 0}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow border-l-4 border-green-500">
          <h3 className="text-gray-500 text-sm font-uppercase">Total Downloads</h3>
          <p className="text-3xl font-bold text-gray-800">{stats.totalDownloads || 0}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow border-l-4 border-purple-500">
          <h3 className="text-gray-500 text-sm font-uppercase">Active Connections</h3>
          <p className="text-3xl font-bold text-gray-800">{stats.activeConnections || 0}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow border-l-4 border-red-500">
          <h3 className="text-gray-500 text-sm font-uppercase">Server Load (Avg)</h3>
          <p className="text-3xl font-bold text-gray-800">45%</p>
        </div>
      </div>

      {/* Charts */}
      <div className="bg-white p-6 rounded-lg shadow mb-8">
        <h3 className="text-xl font-bold mb-4">Server Load Over Time</h3>
        <div className="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart
              data={stats.serverLoad || mockData.serverLoad}
              margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
            >
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

      {/* Server Management Table Stub */}
      <div className="bg-white p-6 rounded-lg shadow">
        <div className="flex justify-between items-center mb-4">
            <h3 className="text-xl font-bold">Server Management</h3>
            <button className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Add Server</button>
        </div>
        <table className="min-w-full leading-normal">
            <thead>
                <tr>
                    <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                        Server Name
                    </th>
                    <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                        IP Address
                    </th>
                    <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                        Status
                    </th>
                    <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                        Actions
                    </th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                        <p className="text-gray-900 whitespace-no-wrap">DE-Frankfurt-1</p>
                    </td>
                    <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                        <p className="text-gray-900 whitespace-no-wrap">192.168.1.10</p>
                    </td>
                    <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                        <span className="relative inline-block px-3 py-1 font-semibold text-green-900 leading-tight">
                            <span aria-hidden className="absolute inset-0 bg-green-200 opacity-50 rounded-full"></span>
                            <span className="relative">Active</span>
                        </span>
                    </td>
                    <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                        <button className="text-blue-600 hover:text-blue-900">Edit</button>
                    </td>
                </tr>
            </tbody>
        </table>
      </div>
    </div>
  );
};

export default AdminDashboard;
