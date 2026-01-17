import React, { useState, useEffect } from 'react';
import { Card, CardContent, Typography, Grid } from '@mui/material';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from 'recharts';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    activeUsers: 0,
    totalDownloads: 0,
    activeConnections: 0,
    serverLoad: []
  });

  useEffect(() => {
    // Fetch stats from backend
    fetch('/api/v1/admin/statistics')
      .then(response => {
          if (response.ok) return response.json();
          throw new Error('Network response was not ok');
      })
      .then(data => {
          // Assuming backend returns matching structure or we map it
          if(data && data.success) {
             setStats({
                 activeUsers: data.data.activeUsers || 0,
                 totalDownloads: data.data.totalDownloads || 0,
                 activeConnections: data.data.activeConnections || 0,
                 serverLoad: data.data.serverLoad || []
             });
          }
      })
      .catch(error => {
          console.error("Error fetching stats:", error);
          // Fallback / Mock Data if backend not ready or unreachable
          setStats({
            activeUsers: 1250,
            totalDownloads: 54300,
            activeConnections: 340,
            serverLoad: [
              { name: 'Server A', cpu: 40, ram: 60 },
              { name: 'Server B', cpu: 70, ram: 80 },
              { name: 'Server C', cpu: 20, ram: 30 },
            ]
          });
      });
  }, []);

  return (
    <div style={{ padding: '20px' }}>
      <Typography variant="h4" gutterBottom>Admin Dashboard</Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Active Users</Typography>
              <Typography variant="h5">{stats.activeUsers}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Total Downloads</Typography>
              <Typography variant="h5">{stats.totalDownloads}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>Active Connections</Typography>
              <Typography variant="h5">{stats.activeConnections}</Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12}>
           <Card>
             <CardContent>
                <Typography variant="h6">Server Load (CPU %)</Typography>
                <LineChart width={600} height={300} data={stats.serverLoad}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="cpu" stroke="#8884d8" activeDot={{ r: 8 }} />
                </LineChart>
             </CardContent>
           </Card>
        </Grid>
      </Grid>
    </div>
  );
};

export default AdminDashboard;
