import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Users, Download, Activity, Server, Shield, Globe } from 'lucide-react';
import axios from 'axios';

const StatCard = ({ title, value, icon: Icon, color }) => (
  <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex items-center space-x-4">
    <div className={`p-3 rounded-full ${color} bg-opacity-10`}>
      <Icon className={`w-6 h-6 ${color.replace('bg-', 'text-')}`} />
    </div>
    <div>
      <p className="text-sm text-gray-500 font-medium">{title}</p>
      <h3 className="text-2xl font-bold text-gray-800">{value}</h3>
    </div>
  </div>
);

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    activeUsers: 0,
    totalDownloads: 0,
    activeConnections: 0,
    serverLoad: []
  });

  useEffect(() => {
    // Simulate fetching data
    // In real app: axios.get('/api/v1/admin/statistics')...
    setStats({
      activeUsers: 1245,
      totalDownloads: 5432,
      activeConnections: 320,
      serverLoad: [
        { time: '00:00', load: 20 },
        { time: '04:00', load: 15 },
        { time: '08:00', load: 45 },
        { time: '12:00', load: 80 },
        { time: '16:00', load: 70 },
        { time: '20:00', load: 90 },
        { time: '23:59', load: 50 },
      ]
    });
  }, []);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Sidebar */}
      <aside className="fixed inset-y-0 left-0 w-64 bg-slate-900 text-white transition-transform duration-300 ease-in-out z-20 hidden md:block">
        <div className="p-6">
          <h1 className="text-2xl font-bold flex items-center gap-2">
            <Shield className="text-blue-500" />
            DataGuard
          </h1>
        </div>
        <nav className="mt-6 px-4 space-y-2">
          <a href="#" className="flex items-center gap-3 px-4 py-3 bg-blue-600 rounded-lg text-white">
            <Activity size={20} /> Dashboard
          </a>
          <a href="#" className="flex items-center gap-3 px-4 py-3 text-slate-300 hover:bg-slate-800 rounded-lg transition">
            <Server size={20} /> Servers
          </a>
          <a href="#" className="flex items-center gap-3 px-4 py-3 text-slate-300 hover:bg-slate-800 rounded-lg transition">
            <Users size={20} /> Users
          </a>
          <a href="#" className="flex items-center gap-3 px-4 py-3 text-slate-300 hover:bg-slate-800 rounded-lg transition">
            <Globe size={20} /> Locations
          </a>
        </nav>
      </aside>

      {/* Main Content */}
      <main className="md:ml-64 p-8">
        <div className="mb-8 flex justify-between items-center">
          <div>
            <h2 className="text-3xl font-bold text-gray-800">Dashboard</h2>
            <p className="text-gray-500 mt-1">System Overview & Statistics</p>
          </div>
          <div className="flex gap-3">
             <button className="px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-50">
               Refresh Data
             </button>
             <button className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700">
               Add Server
             </button>
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <StatCard
            title="Active Users"
            value={stats.activeUsers.toLocaleString()}
            icon={Users}
            color="text-blue-600"
          />
          <StatCard
            title="Total Downloads"
            value={stats.totalDownloads.toLocaleString()}
            icon={Download}
            color="text-green-600"
          />
          <StatCard
            title="Active Connections"
            value={stats.activeConnections.toLocaleString()}
            icon={Activity}
            color="text-purple-600"
          />
        </div>

        {/* Charts */}
        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 mb-8">
          <h3 className="text-lg font-bold text-gray-800 mb-6">Server Load Analysis</h3>
          <div className="h-80 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={stats.serverLoad}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f0f0f0" />
                <XAxis dataKey="time" axisLine={false} tickLine={false} tick={{fill: '#9ca3af'}} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#9ca3af'}} />
                <Tooltip
                  contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                />
                <Line
                  type="monotone"
                  dataKey="load"
                  stroke="#2563eb"
                  strokeWidth={3}
                  dot={{ r: 4, fill: '#2563eb', strokeWidth: 2, stroke: '#fff' }}
                  activeDot={{ r: 8 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </main>
    </div>
  );
};

export default AdminDashboard;
