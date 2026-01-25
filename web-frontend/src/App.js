import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import AdminDashboard from './pages/AdminDashboard';

function Home() {
  return (
    <div className="p-10 text-center">
      <h1 className="text-4xl font-bold mb-4">SuperVPN</h1>
      <p className="text-xl text-gray-600 mb-8">Secure, Fast, and Reliable VPN Service.</p>
      <Link to="/admin" className="px-4 py-2 bg-blue-600 text-white rounded">Admin Login</Link>
    </div>
  );
}

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <nav className="bg-white shadow p-4 flex justify-between">
            <Link to="/" className="text-xl font-bold text-blue-600">SuperVPN</Link>
            <div>
                <Link to="/" className="mr-4">Home</Link>
                <Link to="/about" className="mr-4">About</Link>
                <Link to="/contact">Contact</Link>
            </div>
        </nav>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/admin" element={<AdminDashboard />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
