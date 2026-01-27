import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Home from './pages/Home';
import AdminDashboard from './pages/AdminDashboard';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-100 font-sans">
        <nav className="bg-blue-900 text-white p-4 shadow-md">
          <div className="container mx-auto flex justify-between items-center">
            <h1 className="text-2xl font-bold">DataGuard VPN</h1>
            <ul className="flex space-x-6">
              <li><Link to="/" className="hover:text-blue-300">Home</Link></li>
              <li><Link to="/about" className="hover:text-blue-300">About</Link></li>
              <li><Link to="/admin" className="hover:text-blue-300">Admin</Link></li>
            </ul>
          </div>
        </nav>

        <main className="container mx-auto p-4">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/admin" element={<AdminDashboard />} />
            <Route path="/about" element={<div className="p-10 text-center"><h1>About Us</h1><p>We provide secure VPN services.</p></div>} />
          </Routes>
        </main>

        <footer className="bg-gray-800 text-white p-4 text-center mt-10">
            <p>&copy; 2024 DataGuard VPN. All rights reserved.</p>
        </footer>
      </div>
    </Router>
  );
}

export default App;
