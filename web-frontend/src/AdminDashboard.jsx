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

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      // Mocking fetch call structure as I cannot hit the real backend in this environment
      // In production, this would be:
      // const response = await fetch('/api/v1/admin/statistics');
      // const data = await response.json();

      // Simulating API response for "Anlık Sunucu Yoğunluğu (Grafiksel gösterim - CPU/RAM kullanımı), Aktif bağlantı sayısı"
      // and "Sunucu Ekle/Çıkar" etc.

      // For now, I will simulate data that matches the requirement:
      // "Aktif kullanıcı sayısı, Toplam indirme, Anlık Sunucu Yoğunluğu..."

      // Let's assume we fetched this from:
      // GET /api/v1/servers/active (for server count)
      // GET /api/v1/user/stats (mock)

      setStats({
        activeUsers: 142, // Mock
        totalTraffic: '12.5 TB',
        onlineServers: '5/8',
        revenue: '$1,240'
      });

      // Fetch servers
      // const serverRes = await fetch('http://localhost:8080/api/v1/servers/active');
      // const serverData = await serverRes.json();
      // if(serverData.success) setActiveServers(serverData.data);

      // Mock Data for UI demonstration
      setActiveServers([
          { id: 1, serverName: 'US-East-1', currentConnectedUsers: 45, currentLoadPercentage: 65, isFree: true },
          { id: 2, serverName: 'EU-Central-1', currentConnectedUsers: 22, currentLoadPercentage: 30, isFree: false },
          { id: 3, serverName: 'TR-Istanbul-1', currentConnectedUsers: 15, currentLoadPercentage: 12, isFree: true },
          { id: 4, serverName: 'SG-Asia-1', currentConnectedUsers: 60, currentLoadPercentage: 88, isFree: false },
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
        <div className="p-4 border-t border-gray-800 text-sm text-gray-400">
            v1.0.0
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 p-8 overflow-y-auto">
        <header className="flex justify-between items-center mb-8">
          <div>
              <h1 className="text-3xl font-bold text-gray-800">Overview</h1>
              <p className="text-gray-500">Welcome back, Admin</p>
          </div>
          <div className="flex items-center space-x-4">
            <button className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition">
                + Add Server
            </button>
            <div className="w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center text-gray-600 font-bold">A</div>
          </div>
        </header>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <StatCard title="Active Users" value={stats.activeUsers} change="+12% this week" color="bg-blue-500" icon="👥" />
          <StatCard title="Total Traffic" value={stats.totalTraffic} change="+5% vs last month" color="bg-green-500" icon="🌐" />
          <StatCard title="Online Servers" value={stats.onlineServers} change="Stable" color="bg-purple-500" icon="🖥️" />
          <StatCard title="Revenue" value={stats.revenue} change="+8% growth" color="bg-yellow-500" icon="💰" />
        </div>

        {/* Server Load & Graphs */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200 flex flex-col">
                 <h3 className="text-xl font-semibold text-gray-800 mb-4 flex justify-between">
                     <span>Server Load Distribution</span>
                     <span className="text-sm font-normal text-gray-500">Real-time CPU/RAM</span>
                 </h3>
                 <div className="flex-1 flex items-end justify-between space-x-2 px-4 pb-4 border-b border-l border-gray-100 h-64">
                     {/* Dynamic Bar Chart based on mock server loads */}
                     {activeServers.map((server, i) => (
                         <div key={i} className="flex flex-col items-center w-full group relative">
                             <div
                                className={`w-8 rounded-t transition-all duration-500 ${server.currentLoadPercentage > 80 ? 'bg-red-500' : 'bg-blue-500'}`}
                                style={{ height: `${server.currentLoadPercentage}%` }}
                             ></div>
                             <span className="text-xs text-gray-500 mt-2 truncate w-full text-center">{server.serverName.split('-')[0]}</span>

                             {/* Tooltip */}
                             <div className="absolute bottom-full mb-2 hidden group-hover:block bg-black text-white text-xs p-1 rounded z-10">
                                 {server.currentLoadPercentage}%
                             </div>
                         </div>
                     ))}
                 </div>
            </div>

            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
                <h3 className="text-xl font-semibold text-gray-800 mb-4">Server Status</h3>
                <div className="overflow-x-auto">
                    <table className="w-full text-left">
                        <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
                            <tr>
                                <th className="p-3">Server Name</th>
                                <th className="p-3">Users</th>
                                <th className="p-3">Load</th>
                                <th className="p-3">Type</th>
                                <th className="p-3">Status</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100 text-sm">
                            {activeServers.map(server => (
                                <tr key={server.id} className="hover:bg-gray-50">
                                    <td className="p-3 font-medium text-gray-800">{server.serverName}</td>
                                    <td className="p-3 text-gray-600">{server.currentConnectedUsers}</td>
                                    <td className="p-3">
                                        <div className="w-full bg-gray-200 rounded-full h-1.5 dark:bg-gray-200 max-w-[100px]">
                                            <div
                                                className={`h-1.5 rounded-full ${server.currentLoadPercentage > 80 ? 'bg-red-500' : 'bg-green-500'}`}
                                                style={{ width: `${server.currentLoadPercentage}%` }}
                                            ></div>
                                        </div>
                                    </td>
                                    <td className="p-3">
                                        {server.isFree ?
                                            <span className="px-2 py-0.5 rounded text-xs bg-green-100 text-green-700">Free</span> :
                                            <span className="px-2 py-0.5 rounded text-xs bg-yellow-100 text-yellow-700">Premium</span>
                                        }
                                    </td>
                                    <td className="p-3">
                                        <span className="flex items-center gap-1 text-green-600">
                                            <span className="w-2 h-2 rounded-full bg-green-500"></span> Online
                                        </span>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
      </main>
    </div>
  );
};

const NavItem = ({ label, active }) => (
  <div className={`px-6 py-3 cursor-pointer transition-colors ${active ? 'bg-gray-800 border-l-4 border-green-500 text-white' : 'text-gray-400 hover:bg-gray-800 hover:text-white'}`}>
    {label}
  </div>
);

const StatCard = ({ title, value, change, color, icon }) => (
  <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200 hover:shadow-md transition-shadow">
    <div className="flex justify-between items-start">
      <div>
        <p className="text-gray-500 text-sm mb-1">{title}</p>
        <h3 className="text-2xl font-bold text-gray-800">{value}</h3>
      </div>
      <div className={`w-10 h-10 rounded-full ${color} bg-opacity-10 flex items-center justify-center text-lg`}>
          {icon}
      </div>
    </div>
    <p className={`text-sm mt-4 font-medium ${change.includes('+') ? 'text-green-500' : 'text-gray-500'}`}>{change}</p>
  </div>
);

export default AdminDashboard;
