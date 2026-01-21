import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    activeUsers: 0,
    totalDownloads: 0,
    activeConnections: 0,
    servers: []
  });

  // Mock Data for graphs
  const data = [
    { name: '10:00', users: 400, load: 24 },
    { name: '11:00', users: 300, load: 13 },
    { name: '12:00', users: 200, load: 98 },
    { name: '13:00', users: 278, load: 39 },
    { name: '14:00', users: 189, load: 48 },
    { name: '15:00', users: 239, load: 38 },
    { name: '16:00', users: 349, load: 43 },
  ];

  useEffect(() => {
    // In a real scenario, fetch data from backend API
    // axios.get('/api/admin/stats').then(...)
    setStats({
      activeUsers: 1250,
      totalDownloads: 45000,
      activeConnections: 890,
      servers: [
        { id: 1, name: 'US-East-1', load: '45%' },
        { id: 2, name: 'EU-Frankfurt', load: '78%' },
        { id: 3, name: 'Asia-Tokyo', load: '22%' },
      ]
    });
  }, []);

  return (
    <div style={{ display: 'flex', height: '100vh', fontFamily: 'Arial, sans-serif' }}>
      {/* Sidebar */}
      <div style={{ width: '250px', backgroundColor: '#2c3e50', color: 'white', padding: '20px' }}>
        <h2>Admin Panel</h2>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          <li style={{ padding: '10px 0', borderBottom: '1px solid #34495e' }}>Dashboard</li>
          <li style={{ padding: '10px 0', borderBottom: '1px solid #34495e' }}>Users</li>
          <li style={{ padding: '10px 0', borderBottom: '1px solid #34495e' }}>Servers</li>
          <li style={{ padding: '10px 0', borderBottom: '1px solid #34495e' }}>Logs</li>
          <li style={{ padding: '10px 0', borderBottom: '1px solid #34495e' }}>Settings</li>
        </ul>
      </div>

      {/* Main Content */}
      <div style={{ flex: 1, padding: '20px', backgroundColor: '#ecf0f1', overflowY: 'auto' }}>
        <h1>Dashboard</h1>

        {/* Stats Cards */}
        <div style={{ display: 'flex', gap: '20px', marginBottom: '30px' }}>
          <StatCard title="Active Users" value={stats.activeUsers} color="#3498db" />
          <StatCard title="Total Downloads" value={stats.totalDownloads} color="#e67e22" />
          <StatCard title="Active Connections" value={stats.activeConnections} color="#27ae60" />
        </div>

        {/* Graph */}
        <div style={{ backgroundColor: 'white', padding: '20px', borderRadius: '8px', marginBottom: '30px' }}>
          <h3>Server Load & User Traffic</h3>
          <div style={{ height: '300px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart
                data={data}
                margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
              >
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="users" stroke="#8884d8" activeDot={{ r: 8 }} />
                <Line type="monotone" dataKey="load" stroke="#82ca9d" />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Server List */}
        <div style={{ backgroundColor: 'white', padding: '20px', borderRadius: '8px' }}>
          <h3>Server Status</h3>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ textAlign: 'left', borderBottom: '1px solid #eee' }}>
                <th style={{ padding: '10px' }}>Server Name</th>
                <th style={{ padding: '10px' }}>Load</th>
                <th style={{ padding: '10px' }}>Status</th>
              </tr>
            </thead>
            <tbody>
              {stats.servers.map(server => (
                <tr key={server.id} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: '10px' }}>{server.name}</td>
                  <td style={{ padding: '10px' }}>{server.load}</td>
                  <td style={{ padding: '10px' }}>
                    <span style={{
                      padding: '5px 10px',
                      borderRadius: '15px',
                      backgroundColor: '#2ecc71',
                      color: 'white',
                      fontSize: '12px'
                    }}>Online</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

const StatCard = ({ title, value, color }) => (
  <div style={{
    flex: 1,
    backgroundColor: 'white',
    padding: '20px',
    borderRadius: '8px',
    borderLeft: `5px solid ${color}`,
    boxShadow: '0 2px 5px rgba(0,0,0,0.05)'
  }}>
    <h3 style={{ margin: '0 0 10px 0', color: '#7f8c8d', fontSize: '14px', textTransform: 'uppercase' }}>{title}</h3>
    <p style={{ margin: 0, fontSize: '24px', fontWeight: 'bold', color: '#2c3e50' }}>{value}</p>
  </div>
);

export default AdminDashboard;
