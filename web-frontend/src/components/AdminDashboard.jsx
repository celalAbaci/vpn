import React, { useEffect, useState } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Users, Download, Activity, Server } from 'lucide-react';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    activeUsers: 0,
    totalDownloads: 0,
    activeConnections: 0,
    serverLoad: []
  });

  useEffect(() => {
    // Mock fetch - replace with axios.get('/api/v1/admin/statistics')
    const fetchData = async () => {
      // Simulation
      setStats({
        activeUsers: 12450,
        totalDownloads: 54320,
        activeConnections: 854,
        serverLoad: [
          { name: '00:00', load: 40 },
          { name: '04:00', load: 30 },
          { name: '08:00', load: 65 },
          { name: '12:00', load: 85 },
          { name: '16:00', load: 75 },
          { name: '20:00', load: 90 },
          { name: '23:59', load: 50 },
        ]
      });
    };
    fetchData();
  }, []);

  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-3xl font-bold mb-8 text-gray-800">Admin Dashboard</h1>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <StatCard icon={<Users />} title="Active Users" value={stats.activeUsers.toLocaleString()} color="bg-blue-500" />
          <StatCard icon={<Download />} title="Total Downloads" value={stats.totalDownloads.toLocaleString()} color="bg-green-500" />
          <StatCard icon={<Activity />} title="Active Connections" value={stats.activeConnections} color="bg-purple-500" />
          <StatCard icon={<Server />} title="Servers Online" value="12" color="bg-orange-500" />
        </div>

        {/* Charts */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold mb-4">Server Load Overview</h2>
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={stats.serverLoad}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="load" stroke="#3b82f6" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};

const StatCard = ({ icon, title, value, color }) => (
  <div className="bg-white p-6 rounded-lg shadow-md flex items-center space-x-4">
    <div className={`p-4 rounded-full ${color} text-white`}>
      {icon}
    </div>
    <div>
      <p className="text-gray-500 text-sm">{title}</p>
      <p className="text-2xl font-bold">{value}</p>
    </div>
  </div>
);

export default AdminDashboard;
