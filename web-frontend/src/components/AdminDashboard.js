import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Users, Download, Activity, Server } from 'lucide-react';
import axios from 'axios';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    activeUsers: 1240,
    totalDownloads: 45200,
    activeConnections: 850,
    serverLoad: [
      { name: '00:00', load: 40 },
      { name: '04:00', load: 30 },
      { name: '08:00', load: 65 },
      { name: '12:00', load: 85 },
      { name: '16:00', load: 70 },
      { name: '20:00', load: 90 },
    ]
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const token = localStorage.getItem('token');
        const config = token ? { headers: { Authorization: `Bearer ${token}` } } : {};

        // Adjust URL based on environment
        const response = await axios.get('http://localhost:8080/api/v1/admin/statistics', config);

        if (response.data && response.data.success) {
             // Adapt backend response to frontend state if necessary
             // For now assuming backend returns compatible structure or we just log it
             console.log("Fetched stats:", response.data);
             // setStats(response.data.data); // Uncomment if real data matches structure
        }
      } catch (error) {
        console.error("Error fetching stats, falling back to mock data:", error);
      }
    };
    fetchData();
  }, []);

  return (
    <div className="p-6 bg-gray-100 min-h-screen">
      <h1 className="text-3xl font-bold mb-8 text-gray-800">Admin Dashboard</h1>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <StatCard icon={<Users />} title="Active Users" value={stats.activeUsers} color="bg-blue-500" />
        <StatCard icon={<Download />} title="Total Downloads" value={stats.totalDownloads} color="bg-green-500" />
        <StatCard icon={<Activity />} title="Active Connections" value={stats.activeConnections} color="bg-purple-500" />
        <StatCard icon={<Server />} title="Servers Online" value="12" color="bg-orange-500" />
      </div>

      {/* Chart */}
      <div className="bg-white p-6 rounded-lg shadow-md">
        <h2 className="text-xl font-semibold mb-4">Server Load (24h)</h2>
        <div className="h-80">
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
  <div className="bg-white p-6 rounded-lg shadow-md flex items-center">
    <div className={`p-4 rounded-full ${color} text-white mr-4`}>
      {icon}
    </div>
    <div>
      <p className="text-gray-500 text-sm">{title}</p>
      <p className="text-2xl font-bold text-gray-800">{value}</p>
    </div>
  </div>
);

export default AdminDashboard;
