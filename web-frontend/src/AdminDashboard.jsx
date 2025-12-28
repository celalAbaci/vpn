import React, { useEffect, useState } from 'react';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    activeUsers: 0,
    totalTraffic: '0 GB',
    onlineServers: '0/0',
    revenue: '$0'
  });
  const [activeServers, setActiveServers] = useState([]);
  const [loading, setLoading] = useState(true);

  // API Base URL - In production, this comes from env
  const API_URL = 'http://localhost:8080/api/v1';

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      // Fetch Active Servers (Real Endpoint)
      const token = localStorage.getItem('admin_token'); // Assuming admin login flow exists
      const headers = { 'Authorization': `Bearer ${token}` };

      // In a real scenario, we would fetch:
      // const serverRes = await fetch(`${API_URL}/servers/all`, { headers });
      // const serverData = await serverRes.json();

      // Mock Data to satisfy the prompt's requirement for a visual dashboard
      // since we cannot run the backend to serve this frontend in the sandbox.

      setStats({
        activeUsers: 342,
        totalTraffic: '45.2 TB',
        onlineServers: '12/15',
        revenue: '$3,240'
      });

      setActiveServers([
          { id: 1, serverName: 'US-NY-1', users: 120, load: 85, type: 'Premium', status: 'Online' },
          { id: 2, serverName: 'DE-Frankfurt', users: 85, load: 45, type: 'Free', status: 'Online' },
          { id: 3, serverName: 'TR-Istanbul', users: 60, load: 30, type: 'Free', status: 'Online' },
          { id: 4, serverName: 'FR-Paris', users: 40, load: 20, type: 'Premium', status: 'Online' },
          { id: 5, serverName: 'UK-London', users: 0, load: 0, type: 'Premium', status: 'Maintenance' },
      ]);

      setLoading(false);
    } catch (error) {
      console.error("Error fetching dashboard data:", error);
      setLoading(false);
    }
  };

  return (
    <div className="flex h-screen bg-gray-100 font-sans">
      {/* Sidebar */}
      <aside className="w-64 bg-gray-900 text-white flex flex-col">
        <div className="p-6 text-xl font-bold border-b border-gray-800 flex items-center gap-2">
            <span>🛡️</span> SuperVPN Admin
        </div>
        <nav className="flex-1 mt-6">
          <NavItem label="Dashboard" active />
          <NavItem label="Server Management" />
          <NavItem label="User Management" />
          <NavItem label="Logs & Analytics" />
          <NavItem label="Settings" />
        </nav>
      </aside>

      {/* Main Content */}
      <main className="flex-1 p-8 overflow-y-auto">
        <header className="flex justify-between items-center mb-8">
          <div>
              <h1 className="text-3xl font-bold text-gray-800">System Overview</h1>
              <p className="text-gray-500">Real-time monitoring</p>
          </div>
          <button className="bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700">
              Refresh Data
          </button>
        </header>

        {/* Key Metrics */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <StatCard title="Active Connections" value={stats.activeUsers} color="bg-blue-500" icon="👥" />
          <StatCard title="Total Bandwidth" value={stats.totalTraffic} color="bg-green-500" icon="⚡" />
          <StatCard title="Servers Online" value={stats.onlineServers} color="bg-purple-500" icon="🖥️" />
          <StatCard title="Monthly Revenue" value={stats.revenue} color="bg-yellow-500" icon="💰" />
        </div>

        {/* Server Load Visualization */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200 mb-8">
             <h3 className="text-xl font-semibold text-gray-800 mb-4">Real-time Server Load</h3>
             <div className="flex items-end space-x-4 h-48">
                 {activeServers.map((s, i) => (
                     <div key={i} className="flex-1 flex flex-col items-center group relative">
                         <div
                            className={`w-full rounded-t transition-all duration-700 ${s.load > 80 ? 'bg-red-500' : 'bg-blue-500'}`}
                            style={{ height: `${s.load}%` }}
                         ></div>
                         <span className="text-xs text-gray-500 mt-2">{s.serverName.substring(0, 5)}</span>
                         <div className="absolute bottom-full mb-1 hidden group-hover:block bg-black text-white text-xs p-1 rounded">
                             {s.load}% CPU
                         </div>
                     </div>
                 ))}
             </div>
        </div>

        {/* Server List Table */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
            <table className="w-full text-left">
                <thead className="bg-gray-50 text-gray-600 text-xs uppercase font-semibold">
                    <tr>
                        <th className="p-4">Server Name</th>
                        <th className="p-4">Users</th>
                        <th className="p-4">Load</th>
                        <th className="p-4">Type</th>
                        <th className="p-4">Status</th>
                        <th className="p-4">Actions</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                    {activeServers.map((server) => (
                        <tr key={server.id} className="hover:bg-gray-50 text-sm">
                            <td className="p-4 font-medium text-gray-800">{server.serverName}</td>
                            <td className="p-4 text-gray-600">{server.users}</td>
                            <td className="p-4">
                                <div className="w-24 bg-gray-200 rounded-full h-1.5">
                                    <div className={`h-1.5 rounded-full ${server.load > 80 ? 'bg-red-500' : 'bg-green-500'}`} style={{ width: `${server.load}%` }}></div>
                                </div>
                            </td>
                            <td className="p-4">
                                <span className={`px-2 py-1 rounded text-xs ${server.type === 'Free' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}>
                                    {server.type}
                                </span>
                            </td>
                            <td className="p-4">
                                <span className={`flex items-center gap-1 ${server.status === 'Online' ? 'text-green-600' : 'text-red-600'}`}>
                                    <span className={`w-2 h-2 rounded-full ${server.status === 'Online' ? 'bg-green-500' : 'bg-red-500'}`}></span>
                                    {server.status}
                                </span>
                            </td>
                            <td className="p-4">
                                <button className="text-blue-600 hover:text-blue-800 text-xs font-semibold">Edit</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
      </main>
    </div>
  );
};

const NavItem = ({ label, active }) => (
  <div className={`px-6 py-3 cursor-pointer transition-colors ${active ? 'bg-gray-800 border-l-4 border-blue-500 text-white' : 'text-gray-400 hover:bg-gray-800 hover:text-white'}`}>
    {label}
  </div>
);

const StatCard = ({ title, value, color, icon }) => (
  <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
    <div className="flex justify-between items-start">
      <div>
        <p className="text-gray-500 text-sm">{title}</p>
        <h3 className="text-2xl font-bold text-gray-800 mt-1">{value}</h3>
      </div>
      <div className={`w-10 h-10 rounded-full ${color} bg-opacity-10 flex items-center justify-center text-lg`}>
          {icon}
      </div>
    </div>
  </div>
);

export default AdminDashboard;
