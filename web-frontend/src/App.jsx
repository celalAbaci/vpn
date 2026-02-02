import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AdminDashboard from './components/AdminDashboard';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/" element={
           <div className="flex items-center justify-center h-screen bg-gray-900 text-white">
             <div className="text-center">
               <h1 className="text-4xl font-bold mb-4">DataGuard VPN</h1>
               <p className="mb-6">Secure, Fast, Anonymous.</p>
               <a href="/admin" className="text-blue-400 hover:underline">Go to Admin Dashboard</a>
             </div>
           </div>
        } />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
