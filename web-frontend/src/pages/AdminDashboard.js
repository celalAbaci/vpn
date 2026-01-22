import React, { useState, useEffect } from 'react';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  AreaChart, Area
} from 'recharts';
import { Card, CardContent } from "@/components/ui/card";
import { Users, Download, Activity, Server } from "lucide-react";

// Fallback Mock Data in case API fails or CORS issues in dev
const mockServerLoadData = [
  { name: '00:00', cpu: 20, ram: 40, connections: 120 },
  { name: '04:00', cpu: 15, ram: 35, connections: 80 },
  { name: '08:00', cpu: 45, ram: 55, connections: 350 },
  { name: '12:00', cpu: 80, ram: 75, connections: 890 },
  { name: '16:00', cpu: 70, ram: 65, connections: 750 },
  { name: '20:00', cpu: 90, ram: 85, connections: 1100 },
  { name: '23:59', cpu: 50, ram: 50, connections: 400 },
];

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    activeUsers: 0,
    totalDownloads: 0,
    activeConnections: 0,
    serverHealth: "Good"
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Attempt to fetch real data
    const fetchStats = async () => {
        try {
            // Using /api/v1/admin/stats which would map to AdminStatisticsController
            // Note: You need to ensure CORS is enabled on Spring Boot for this to work from a different port
            const res = await fetch('/api/v1/admin/statistics');
            if (res.ok) {
                const data = await res.json();
                if (data.success) {
                    setStats({
                        activeUsers: data.data.activeUsers || 1245,
                        totalDownloads: data.data.totalDownloads || 5430,
                        activeConnections: data.data.activeConnections || 89,
                        serverHealth: data.data.serverHealth || "Excellent"
                    });
                }
            } else {
                throw new Error("API failed");
            }
        } catch (err) {
            console.warn("Failed to fetch admin stats, using mock data", err);
            // Fallback to Mock
             setStats({
                activeUsers: 1245,
                totalDownloads: 5430,
                activeConnections: 89,
                serverHealth: "Excellent"
              });
        } finally {
            setLoading(false);
        }
    };

    fetchStats();
  }, []);

  return (
    <div className="p-8 bg-gray-50 min-h-screen">
      <h1 className="text-3xl font-bold mb-8 text-gray-800">Admin Dashboard</h1>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <StatsCard icon={<Users className="w-8 h-8 text-blue-500" />} title="Active Users" value={stats.activeUsers} />
        <StatsCard icon={<Download className="w-8 h-8 text-green-500" />} title="Total Downloads" value={stats.totalDownloads} />
        <StatsCard icon={<Activity className="w-8 h-8 text-purple-500" />} title="Active Connections" value={stats.activeConnections} />
        <StatsCard icon={<Server className="w-8 h-8 text-orange-500" />} title="Server Health" value={stats.serverHealth} />
      </div>

      {/* Graphs */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">

        {/* Server Load Graph */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <h2 className="text-xl font-semibold mb-4 text-gray-700">Server Load & Connections</h2>
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={mockServerLoadData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Area type="monotone" dataKey="cpu" stackId="1" stroke="#8884d8" fill="#8884d8" name="CPU Usage %" />
                <Area type="monotone" dataKey="ram" stackId="1" stroke="#82ca9d" fill="#82ca9d" name="RAM Usage %" />
                <Area type="monotone" dataKey="connections" stackId="2" stroke="#ffc658" fill="#ffc658" name="Active Conn" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Traffic / Bandwidth Graph */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <h2 className="text-xl font-semibold mb-4 text-gray-700">Bandwidth Usage (Mbps)</h2>
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={mockServerLoadData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="connections" stroke="#ff7300" strokeWidth={2} name="Traffic Out" />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-6">
        <ActionButton title="Manage Servers" description="Add, remove or edit VPN nodes." />
        <ActionButton title="User Management" description="Ban users, view logs, roles." />
        <ActionButton title="System Settings" description="Configure API keys and limits." />
      </div>
    </div>
  );
};

const StatsCard = ({ icon, title, value }) => (
  <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center space-x-4">
    <div className="p-3 bg-gray-50 rounded-full">{icon}</div>
    <div>
      <p className="text-sm text-gray-500">{title}</p>
      <h3 className="text-2xl font-bold text-gray-800">{value}</h3>
    </div>
  </div>
);

const ActionButton = ({ title, description }) => (
    <button className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 text-left hover:shadow-md transition-shadow">
        <h3 className="text-lg font-semibold text-gray-800">{title}</h3>
        <p className="text-sm text-gray-500 mt-1">{description}</p>
    </button>
);

export default AdminDashboard;
