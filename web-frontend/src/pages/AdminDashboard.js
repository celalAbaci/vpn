import React, { useEffect, useState } from 'react';
import { Users, Activity, Server, Download } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import axios from 'axios';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    totalUsers: 0,
    activeSubscriptions: 0,
    totalConnectedUsers: 0,
    averageServerLoadPercentage: 0
  });

  const [loading, setLoading] = useState(true);

  // Mock Data for the graph (Real API returns summary stats, not time-series)
  const serverLoadData = [
    { name: '00:00', load: 40 },
    { name: '04:00', load: 30 },
    { name: '08:00', load: 65 },
    { name: '12:00', load: 85 },
    { name: '16:00', load: 75 },
    { name: '20:00', load: 90 },
    { name: '23:59', load: 60 },
  ];

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
        // In a real scenario, you would have an interceptor to add the Bearer token.
        // For scaffold purposes, we assume public access or mocked.
        // const response = await axios.get('/api/v1/admin/statistics/dashboard');
        // setStats(response.data.data);

        // Simulating API response for demo
        setTimeout(() => {
            setStats({
                totalUsers: 1250,
                activeSubscriptions: 850,
                totalConnectedUsers: 342,
                averageServerLoadPercentage: 68.5
            });
            setLoading(false);
        }, 1000);

    } catch (error) {
      console.error("Failed to fetch statistics", error);
      setLoading(false);
    }
  };

  if (loading) return <div className="p-8">Loading dashboard...</div>;

  return (
    <div className="flex min-h-screen bg-gray-100">
      {/* Sidebar (Simplified) */}
      <div className="w-64 bg-gray-800 text-white min-h-screen p-4 hidden md:block">
        <h2 className="text-2xl font-bold mb-8">Admin Panel</h2>
        <nav className="space-y-2">
          <a href="#" className="block py-2 px-4 bg-gray-900 rounded">Dashboard</a>
          <a href="#" className="block py-2 px-4 hover:bg-gray-700 rounded">Servers</a>
          <a href="#" className="block py-2 px-4 hover:bg-gray-700 rounded">Users</a>
          <a href="#" className="block py-2 px-4 hover:bg-gray-700 rounded">Logs</a>
          <a href="/" className="block py-2 px-4 text-gray-400 mt-8 hover:text-white">Back to Home</a>
        </nav>
      </div>

      {/* Main Content */}
      <div className="flex-1 p-8">
        <h1 className="text-3xl font-bold text-gray-800 mb-8">Dashboard Overview</h1>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <StatCard
                title="Total Users"
                value={stats.totalUsers}
                icon={<Users className="text-blue-500" />}
                color="border-l-4 border-blue-500"
            />
             <StatCard
                title="Active Connections"
                value={stats.totalConnectedUsers}
                icon={<Activity className="text-green-500" />}
                 color="border-l-4 border-green-500"
            />
            <StatCard
                title="Premium Subs"
                value={stats.activeSubscriptions}
                icon={<Download className="text-yellow-500" />}
                 color="border-l-4 border-yellow-500"
            />
            <StatCard
                title="Avg Server Load"
                value={`${stats.averageServerLoadPercentage}%`}
                icon={<Server className="text-red-500" />}
                 color="border-l-4 border-red-500"
            />
        </div>

        {/* Chart */}
        <div className="bg-white p-6 rounded-lg shadow mb-8">
            <h3 className="text-lg font-bold text-gray-800 mb-4">Server Load (24h)</h3>
            <div className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={serverLoadData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="name" />
                        <YAxis />
                        <Tooltip />
                        <Line type="monotone" dataKey="load" stroke="#3B82F6" strokeWidth={2} />
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </div>

        {/* Recent Activity Mock */}
        <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-bold text-gray-800 mb-4">System Alerts</h3>
            <div className="space-y-4">
                <div className="p-3 bg-yellow-50 text-yellow-800 rounded border border-yellow-200">
                    Warning: Server US-East-1 load > 90%
                </div>
                 <div className="p-3 bg-green-50 text-green-800 rounded border border-green-200">
                    System Update: Deployment successful at 02:00 AM
                </div>
            </div>
        </div>

      </div>
    </div>
  );
};

const StatCard = ({ title, value, icon, color }) => (
    <div className={`bg-white p-6 rounded-lg shadow flex items-center justify-between ${color}`}>
        <div>
            <p className="text-gray-500 text-sm">{title}</p>
            <h3 className="text-2xl font-bold text-gray-800 mt-1">{value}</h3>
        </div>
        <div className="p-3 bg-gray-50 rounded-full">
            {icon}
        </div>
    </div>
);

export default AdminDashboard;
