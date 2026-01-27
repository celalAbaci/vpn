import React from 'react';
import { Shield, Smartphone, Globe } from 'lucide-react';

const Home = () => {
  return (
    <div className="flex flex-col items-center">
      {/* Hero Section */}
      <section className="text-center py-20 bg-gradient-to-b from-blue-500 to-blue-700 text-white w-full rounded-xl shadow-xl">
        <h1 className="text-5xl font-bold mb-4">Secure Your Digital Life</h1>
        <p className="text-xl mb-8">Fast, Private, and Secure VPN Service for everyone.</p>
        <button className="bg-white text-blue-700 font-bold py-3 px-8 rounded-full shadow-lg hover:bg-gray-100 transition">
          Get Started for Free
        </button>
      </section>

      {/* Features */}
      <section className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-16 w-full">
        <div className="bg-white p-6 rounded-lg shadow-md text-center">
          <Shield className="w-12 h-12 mx-auto text-blue-600 mb-4" />
          <h3 className="text-xl font-bold mb-2">Top Security</h3>
          <p className="text-gray-600">Military-grade encryption to keep your data safe.</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow-md text-center">
          <Smartphone className="w-12 h-12 mx-auto text-blue-600 mb-4" />
          <h3 className="text-xl font-bold mb-2">Multi-Device</h3>
          <p className="text-gray-600">Use on Android, iOS, Windows, and Mac.</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow-md text-center">
          <Globe className="w-12 h-12 mx-auto text-blue-600 mb-4" />
          <h3 className="text-xl font-bold mb-2">Global Servers</h3>
          <p className="text-gray-600">Access content from anywhere in the world.</p>
        </div>
      </section>
    </div>
  );
};

export default Home;
