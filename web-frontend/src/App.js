import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Home from './pages/Home';
import About from './pages/About';
import Contact from './pages/Contact';
import KVKK from './pages/KVKK';
import AdminDashboard from './pages/AdminDashboard';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50 flex flex-col">
        <nav className="bg-white shadow p-4">
          <div className="container mx-auto flex justify-between items-center">
            <Link to="/" className="text-xl font-bold text-blue-600">DataGuard VPN</Link>
            <div className="space-x-4">
              <Link to="/" className="text-gray-600 hover:text-blue-500">Ana Sayfa</Link>
              <Link to="/about" className="text-gray-600 hover:text-blue-500">Hakkımızda</Link>
              <Link to="/contact" className="text-gray-600 hover:text-blue-500">İletişim</Link>
              <Link to="/admin" className="text-red-500 font-semibold">Admin Panel</Link>
            </div>
          </div>
        </nav>

        <div className="flex-grow container mx-auto p-4">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/about" element={<About />} />
            <Route path="/contact" element={<Contact />} />
            <Route path="/kvkk" element={<KVKK />} />
            <Route path="/admin" element={<AdminDashboard />} />
          </Routes>
        </div>

        <footer className="bg-gray-800 text-white p-4 text-center mt-8">
          <p>© 2024 DataGuard VPN. Tüm hakları saklıdır.</p>
          <div className="mt-2 space-x-2">
            <Link to="/kvkk" className="text-gray-400 hover:text-white text-sm">KVKK & Gizlilik</Link>
          </div>
        </footer>
      </div>
    </Router>
  );
}

export default App;
