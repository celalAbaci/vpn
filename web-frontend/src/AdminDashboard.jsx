import React from 'react';

const AdminDashboard = () => {
  return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar */}
      <aside className="w-64 bg-gray-900 text-white">
        <div className="p-6 text-xl font-bold border-b border-gray-800">Admin Panel</div>
        <nav className="mt-6">
          <NavItem label="Dashboard" active />
          <NavItem label="Live Map" />
          <NavItem label="Users" />
          <NavItem label="Servers" />
          <NavItem label="Settings" />
        </nav>
      </aside>

      {/* Main Content */}
      <main className="flex-1 p-8 overflow-y-auto">
        <header className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-gray-800">Dashboard Overview</h1>
          <div className="flex items-center space-x-4">
            <span className="text-gray-600">Admin User</span>
            <div className="w-10 h-10 bg-gray-300 rounded-full"></div>
          </div>
        </header>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <StatCard title="Active Users" value="1,234" change="+12%" color="bg-blue-500" />
          <StatCard title="Total Traffic" value="45 TB" change="+5%" color="bg-green-500" />
          <StatCard title="Servers Online" value="48/50" change="Stable" color="bg-purple-500" />
          <StatCard title="Revenue" value="$12,450" change="+8%" color="bg-yellow-500" />
        </div>

        {/* Server Load & Live Map */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200 h-96 flex flex-col">
                 <h3 className="text-xl font-semibold text-gray-800 mb-4">Server CPU/RAM Load</h3>
                 <div className="flex-1 flex items-end justify-between space-x-2 px-4 pb-4 border-b border-l border-gray-200">
                     {/* Mock Bar Chart */}
                     {[40, 65, 30, 85, 50, 60, 75, 45, 90, 55, 30, 60].map((h, i) => (
                         <div key={i} className="w-full bg-blue-500 rounded-t" style={{ height: `${h}%`, opacity: 0.7 }}></div>
                     ))}
                 </div>
                 <div className="text-center text-xs text-gray-400 mt-2">Real-time Load (Last 12 Hours)</div>
            </div>

            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200 h-96 flex items-center justify-center">
                <div className="text-center">
                    <h3 className="text-xl font-semibold text-gray-500">Live Connection Map</h3>
                    <p className="text-gray-400">Map integration (Leaflet/Mapbox)</p>
                </div>
            </div>
        </div>

        {/* Recent Activity Table */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="p-6 border-b border-gray-100">
            <h3 className="font-bold text-gray-800">Recent Connections</h3>
          </div>
          <table className="w-full text-left">
            <thead className="bg-gray-50">
              <tr>
                <th className="p-4 text-gray-500 text-sm">User / Device</th>
                <th className="p-4 text-gray-500 text-sm">Server</th>
                <th className="p-4 text-gray-500 text-sm">Duration</th>
                <th className="p-4 text-gray-500 text-sm">Status</th>
              </tr>
            </thead>
            <tbody>
              <TableRow user="Guest_8291" server="UK - London" duration="12m" status="Active" />
              <TableRow user="john.doe@email.com" server="US - New York" duration="45m" status="Active" />
              <TableRow user="Guest_1102" server="DE - Frankfurt" duration="2m" status="Disconnected" />
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
};

const NavItem = ({ label, active }) => (
  <div className={`px-6 py-3 cursor-pointer hover:bg-gray-800 ${active ? 'bg-gray-800 border-l-4 border-green-500' : ''}`}>
    {label}
  </div>
);

const StatCard = ({ title, value, change, color }) => (
  <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
    <div className="flex justify-between items-start">
      <div>
        <p className="text-gray-500 text-sm mb-1">{title}</p>
        <h3 className="text-2xl font-bold">{value}</h3>
      </div>
      <div className={`w-3 h-3 rounded-full ${color}`}></div>
    </div>
    <p className="text-green-500 text-sm mt-4 font-medium">{change}</p>
  </div>
);

const TableRow = ({ user, server, duration, status }) => (
  <tr className="border-b border-gray-50 hover:bg-gray-50">
    <td className="p-4 font-medium">{user}</td>
    <td className="p-4 text-gray-600">{server}</td>
    <td className="p-4 text-gray-600">{duration}</td>
    <td className="p-4">
      <span className={`px-2 py-1 rounded-full text-xs font-semibold ${status === 'Active' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'}`}>
        {status}
      </span>
    </td>
  </tr>
);

export default AdminDashboard;
