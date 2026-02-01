import React from 'react';
import { Shield, Lock, Globe } from 'lucide-react';
import { Link } from 'react-router-dom';

const Home = () => {
  return (
    <div className="flex flex-col min-h-screen">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <div className="flex items-center space-x-2">
            <Shield className="h-8 w-8 text-blue-600" />
            <span className="text-xl font-bold text-gray-900">DataGuard VPN</span>
          </div>
          <nav className="hidden md:flex space-x-8">
            <Link to="#" className="text-gray-500 hover:text-gray-900">Features</Link>
            <Link to="#" className="text-gray-500 hover:text-gray-900">Pricing</Link>
            <Link to="#" className="text-gray-500 hover:text-gray-900">Servers</Link>
            <Link to="/admin/dashboard" className="text-blue-600 font-medium">Admin Panel</Link>
          </nav>
        </div>
      </header>

      {/* Hero Section */}
      <main className="flex-grow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 text-center">
          <h1 className="text-4xl tracking-tight font-extrabold text-gray-900 sm:text-5xl md:text-6xl">
            <span className="block">Secure your digital life</span>
            <span className="block text-blue-600">with DataGuard VPN</span>
          </h1>
          <p className="mt-3 max-w-md mx-auto text-base text-gray-500 sm:text-lg md:mt-5 md:text-xl md:max-w-3xl">
            Fast, secure, and anonymous. Access the internet without borders. Now supporting WireGuard and OpenVPN.
          </p>
          <div className="mt-5 max-w-md mx-auto sm:flex sm:justify-center md:mt-8">
            <div className="rounded-md shadow">
              <a href="#" className="w-full flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 md:py-4 md:text-lg md:px-10">
                Get Started
              </a>
            </div>
          </div>
        </div>

        {/* Feature Grid */}
        <div className="bg-gray-100 py-16">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="grid grid-cols-1 gap-8 md:grid-cols-3">
              <div className="flex flex-col items-center text-center p-6 bg-white rounded-lg shadow">
                <Globe className="h-12 w-12 text-blue-500 mb-4" />
                <h3 className="text-lg font-medium text-gray-900">Global Servers</h3>
                <p className="mt-2 text-gray-500">Access content from anywhere with our high-speed global network.</p>
              </div>
              <div className="flex flex-col items-center text-center p-6 bg-white rounded-lg shadow">
                <Lock className="h-12 w-12 text-blue-500 mb-4" />
                <h3 className="text-lg font-medium text-gray-900">Military-Grade Encryption</h3>
                <p className="mt-2 text-gray-500">Your data is secured with AES-256 encryption.</p>
              </div>
              <div className="flex flex-col items-center text-center p-6 bg-white rounded-lg shadow">
                <Shield className="h-12 w-12 text-blue-500 mb-4" />
                <h3 className="text-lg font-medium text-gray-900">No Logs Policy</h3>
                <p className="mt-2 text-gray-500">We never track, collect, or share your private data.</p>
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-gray-800 text-white py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <p>&copy; 2024 DataGuard VPN. All rights reserved.</p>
          <div className="mt-4 space-x-4 text-gray-400 text-sm">
            <a href="#" className="hover:text-white">Privacy Policy</a>
            <a href="#" className="hover:text-white">Terms of Service</a>
            <a href="#" className="hover:text-white">Contact</a>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Home;
