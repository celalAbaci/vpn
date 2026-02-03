import React from 'react';
import { Shield, Globe, Smartphone } from 'lucide-react';

const Home = () => {
  return (
    <div className="min-h-screen bg-gray-900 text-white font-sans">
      <header className="container mx-auto px-6 py-4 flex justify-between items-center">
        <div className="text-2xl font-bold text-blue-500">DataGuard VPN</div>
        <nav>
          <ul className="flex space-x-6">
            <li><a href="#" className="hover:text-blue-400">Home</a></li>
            <li><a href="#" className="hover:text-blue-400">Features</a></li>
            <li><a href="#" className="hover:text-blue-400">Download</a></li>
            <li><a href="/admin" className="hover:text-blue-400">Admin</a></li>
          </ul>
        </nav>
      </header>

      <main className="container mx-auto px-6 py-16 text-center">
        <h1 className="text-5xl font-extrabold mb-4">Secure Your Digital Life</h1>
        <p className="text-xl text-gray-400 mb-8">Fast, Secure, and Anonymous VPN Service.</p>
        <button className="bg-blue-600 hover:bg-blue-700 px-8 py-3 rounded-full text-lg font-semibold">Get Started</button>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-16">
           <div className="p-6 bg-gray-800 rounded-lg">
             <Shield className="w-12 h-12 text-blue-500 mx-auto mb-4" />
             <h3 className="text-xl font-bold mb-2">Military-Grade Encryption</h3>
             <p className="text-gray-400">Your data is safe with us.</p>
           </div>
           <div className="p-6 bg-gray-800 rounded-lg">
             <Globe className="w-12 h-12 text-green-500 mx-auto mb-4" />
             <h3 className="text-xl font-bold mb-2">Global Servers</h3>
             <p className="text-gray-400">Access content from anywhere.</p>
           </div>
           <div className="p-6 bg-gray-800 rounded-lg">
             <Smartphone className="w-12 h-12 text-purple-500 mx-auto mb-4" />
             <h3 className="text-xl font-bold mb-2">Multi-Device Support</h3>
             <p className="text-gray-400">Protect all your devices.</p>
           </div>
        </div>
      </main>

      <footer className="bg-gray-800 py-6 mt-16">
        <div className="container mx-auto text-center text-gray-500">
          &copy; 2024 DataGuard VPN. All rights reserved.
        </div>
      </footer>
    </div>
  );
};

export default Home;
