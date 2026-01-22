import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import AdminDashboard from './pages/AdminDashboard';
import Home from './pages/Home';
import About from './pages/About';
import Contact from './pages/Contact';

const App = () => {
  return (
    <Router>
      <div className="font-sans text-gray-900 antialiased">
        <nav className="bg-white shadow-sm sticky top-0 z-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between h-16">
                    <div className="flex items-center">
                        <Link to="/" className="text-2xl font-bold text-blue-600">DataGuard VPN</Link>
                        <div className="hidden md:flex ml-10 space-x-8">
                            <Link to="/" className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md font-medium">Home</Link>
                            <Link to="/about" className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md font-medium">About</Link>
                            <Link to="/contact" className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md font-medium">Contact</Link>
                        </div>
                    </div>
                    <div className="flex items-center">
                         <Link to="/admin/dashboard" className="bg-gray-100 text-gray-700 hover:bg-gray-200 px-4 py-2 rounded-full text-sm font-medium">Admin Panel</Link>
                    </div>
                </div>
            </div>
        </nav>

        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/about" element={<About />} />
          <Route path="/contact" element={<Contact />} />
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
        </Routes>

        <footer className="bg-gray-800 text-white py-12">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                    <div>
                        <h3 className="text-lg font-semibold mb-4">DataGuard VPN</h3>
                        <p className="text-gray-400">Secure your digital life with military-grade encryption.</p>
                    </div>
                    <div>
                        <h3 className="text-lg font-semibold mb-4">Legal</h3>
                         <ul className="space-y-2 text-gray-400">
                            <li><a href="#" className="hover:text-white">Privacy Policy</a></li>
                            <li><a href="#" className="hover:text-white">Terms of Service</a></li>
                            <li><a href="#" className="hover:text-white">KVKK</a></li>
                        </ul>
                    </div>
                    <div>
                        <h3 className="text-lg font-semibold mb-4">Connect</h3>
                        <p className="text-gray-400">support@dataguardvpn.com</p>
                    </div>
                </div>
                <div className="mt-8 border-t border-gray-700 pt-8 text-center text-gray-500">
                    &copy; 2023 DataGuard VPN. All rights reserved.
                </div>
            </div>
        </footer>
      </div>
    </Router>
  );
};

export default App;
