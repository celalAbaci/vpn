import React, { useEffect, useState } from 'react';

const AdminDashboard = () => {
    const [stats, setStats] = useState({
        activeUsers: 0,
        serverLoad: 0,
        activeConnections: 0,
        bandwidthUsage: 0
    });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // In a real application, you would use axios or fetch to get data from your Spring Boot Backend
        // Example:
        // axios.get('/api/v1/admin/stats').then(response => setStats(response.data)).catch(err => console.error(err));

        const fetchStats = async () => {
            try {
                // Simulating API call delay
                await new Promise(resolve => setTimeout(resolve, 500));

                // Mock data representing what the backend would return
                // The backend endpoint /api/v1/admin/stats should return a JSON like this.
                const mockData = {
                    activeUsers: 142,
                    serverLoad: 65, // percentage
                    activeConnections: 98,
                    bandwidthUsage: 15.2 // TB
                };
                setStats(mockData);
            } catch (error) {
                console.error("Error fetching stats", error);
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
        const interval = setInterval(fetchStats, 30000); // Refresh every 30s
        return () => clearInterval(interval);
    }, []);

    if (loading) return <div className="loading">Loading Dashboard...</div>;

    return (
        <div className="admin-dashboard" style={{ padding: '20px', fontFamily: 'sans-serif' }}>
            <header className="dashboard-header" style={{ marginBottom: '20px', borderBottom: '1px solid #ddd', paddingBottom: '10px' }}>
                <h1 style={{ margin: 0 }}>Admin Dashboard</h1>
                <span className="live-indicator" style={{ color: 'green', fontSize: '0.9em' }}>● Live System Monitor</span>
            </header>

            <div className="stats-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginBottom: '30px' }}>
                <div className="stat-card" style={{ background: '#f4f4f4', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
                    <h3 style={{ marginTop: 0, color: '#666' }}>Active Users</h3>
                    <p className="stat-value" style={{ fontSize: '24px', fontWeight: 'bold', margin: '10px 0 0 0', color: '#333' }}>{stats.activeUsers}</p>
                </div>
                <div className="stat-card" style={{ background: '#f4f4f4', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
                    <h3 style={{ marginTop: 0, color: '#666' }}>Server Load</h3>
                    <div className="progress-bar" style={{ width: '100%', height: '10px', background: '#e0e0e0', borderRadius: '5px', margin: '10px 0' }}>
                        <div className="progress-fill" style={{ height: '100%', background: stats.serverLoad > 80 ? '#dc3545' : '#4caf50', borderRadius: '5px', width: `${stats.serverLoad}%` }}></div>
                    </div>
                    <p className="stat-value" style={{ fontSize: '24px', fontWeight: 'bold', margin: '0', color: '#333' }}>{stats.serverLoad}%</p>
                </div>
                <div className="stat-card" style={{ background: '#f4f4f4', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
                    <h3 style={{ marginTop: 0, color: '#666' }}>Active Connections</h3>
                    <p className="stat-value" style={{ fontSize: '24px', fontWeight: 'bold', margin: '10px 0 0 0', color: '#333' }}>{stats.activeConnections}</p>
                </div>
                <div className="stat-card" style={{ background: '#f4f4f4', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
                    <h3 style={{ marginTop: 0, color: '#666' }}>Total Bandwidth</h3>
                    <p className="stat-value" style={{ fontSize: '24px', fontWeight: 'bold', margin: '10px 0 0 0', color: '#333' }}>{stats.bandwidthUsage} TB</p>
                </div>
            </div>

            <div className="dashboard-sections" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '20px' }}>
                <div className="chart-section" style={{ background: 'white', padding: '20px', borderRadius: '8px', border: '1px solid #eee' }}>
                    <h2 style={{ marginTop: 0 }}>Traffic Overview</h2>
                    <div className="chart-placeholder" style={{ height: '200px', background: '#f9f9f9', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '1px dashed #ccc', color: '#888' }}>
                        [Graph: Traffic vs Time would render here using Chart.js]
                    </div>
                </div>

                <div className="management-section" style={{ background: 'white', padding: '20px', borderRadius: '8px', border: '1px solid #eee' }}>
                    <h2 style={{ marginTop: 0 }}>Quick Actions</h2>
                    <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                        <button className="btn btn-primary" style={{ padding: '10px 20px', border: 'none', borderRadius: '4px', cursor: 'pointer', color: 'white', background: '#007bff' }}>Add Server</button>
                        <button className="btn btn-danger" style={{ padding: '10px 20px', border: 'none', borderRadius: '4px', cursor: 'pointer', color: 'white', background: '#dc3545' }}>Block User</button>
                        <button className="btn btn-secondary" style={{ padding: '10px 20px', border: 'none', borderRadius: '4px', cursor: 'pointer', color: 'white', background: '#6c757d' }}>View Logs</button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;
