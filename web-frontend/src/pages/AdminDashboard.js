import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './AdminDashboard.css';

// Mock Data for graphs (since we don't have a real charting lib installed in this environment)
// In a real scenario, use Chart.js or Recharts
const AdminDashboard = () => {
    const [stats, setStats] = useState({
        activeUsers: 0,
        totalDownloads: 0,
        activeConnections: 0,
        serverLoad: []
    });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchStats();
        const interval = setInterval(fetchStats, 30000); // Refresh every 30s
        return () => clearInterval(interval);
    }, []);

    const fetchStats = async () => {
        try {
            // Placeholder for real API call
            // const response = await axios.get('/api/v1/admin/stats');
            // setStats(response.data);

            // Mocking data for demonstration
            setStats({
                activeUsers: Math.floor(Math.random() * 500) + 100,
                totalDownloads: 15420,
                activeConnections: Math.floor(Math.random() * 200) + 50,
                serverLoad: [
                    { name: 'US-East', load: 45 },
                    { name: 'EU-West', load: 78 },
                    { name: 'Asia-Sing', load: 23 }
                ]
            });
            setLoading(false);
        } catch (error) {
            console.error("Failed to fetch admin stats", error);
            setLoading(false);
        }
    };

    if (loading) return <div className="loading">Loading Dashboard...</div>;

    return (
        <div className="admin-dashboard">
            <header className="dashboard-header">
                <h1>DataGuard VPN - Admin Panel</h1>
                <div className="user-info">Logged in as Moderator/Admin</div>
            </header>

            <div className="stats-grid">
                <div className="stat-card">
                    <h3>Active Users</h3>
                    <p className="stat-number">{stats.activeUsers}</p>
                </div>
                <div className="stat-card">
                    <h3>Total Downloads</h3>
                    <p className="stat-number">{stats.totalDownloads}</p>
                </div>
                <div className="stat-card">
                    <h3>Active Connections</h3>
                    <p className="stat-number">{stats.activeConnections}</p>
                    <div className="live-indicator">LIVE</div>
                </div>
            </div>

            <div className="main-content">
                <div className="panel server-load-panel">
                    <h2>Instant Server Load</h2>
                    <div className="server-list">
                        {stats.serverLoad.map((server, index) => (
                            <div key={index} className="server-item">
                                <span className="server-name">{server.name}</span>
                                <div className="progress-bar-container">
                                    <div
                                        className="progress-bar"
                                        style={{ width: `${server.load}%`, backgroundColor: server.load > 80 ? 'red' : '#4caf50' }}
                                    ></div>
                                </div>
                                <span className="server-load-text">{server.load}%</span>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="panel actions-panel">
                    <h2>Management Actions</h2>
                    <div className="action-buttons">
                        <button className="btn btn-primary">Add New Server</button>
                        <button className="btn btn-warning">Manage Users</button>
                        <button className="btn btn-danger">View System Logs</button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;
