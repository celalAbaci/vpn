import React from 'react';
import { Download, Shield, Globe, Zap } from 'lucide-react';

const LandingPage = () => {
  return (
    <div className="min-h-screen bg-gray-900 text-white font-sans">
      {/* Navbar */}
      <nav className="flex justify-between items-center p-6 max-w-7xl mx-auto">
        <div className="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-green-400 to-blue-500">
          DataGuard VPN
        </div>
        <div className="space-x-6 hidden md:flex">
          <a href="#features" className="hover:text-green-400">Features</a>
          <a href="#pricing" className="hover:text-green-400">Pricing</a>
          <a href="#faq" className="hover:text-green-400">FAQ</a>
        </div>
        <button className="bg-green-500 hover:bg-green-600 px-6 py-2 rounded-full font-semibold transition">
          Download App
        </button>
      </nav>

      {/* Hero Section */}
      <header className="flex flex-col items-center text-center mt-20 px-4">
        <h1 className="text-5xl md:text-7xl font-extrabold mb-6 leading-tight">
          Secure Your Digital <br />
          <span className="text-green-500">Footprint</span> Today.
        </h1>
        <p className="text-gray-400 text-xl max-w-2xl mb-10">
          Fast, anonymous, and secure internet access with military-grade encryption.
          Bypass censorship and protect your data.
        </p>
        <div className="flex space-x-4">
          <button className="flex items-center bg-white text-gray-900 px-8 py-3 rounded-lg font-bold hover:bg-gray-200 transition">
            <Download className="mr-2" /> Get for Android
          </button>
          <button className="flex items-center border border-gray-600 px-8 py-3 rounded-lg font-bold hover:border-green-500 hover:text-green-500 transition">
            Learn More
          </button>
        </div>
      </header>

      {/* Features Grid */}
      <section id="features" className="py-24 max-w-7xl mx-auto px-4">
        <h2 className="text-3xl font-bold text-center mb-16">Why Choose DataGuard?</h2>
        <div className="grid md:grid-cols-3 gap-8">
          <FeatureCard
            icon={<Shield className="w-12 h-12 text-green-500" />}
            title="Military-Grade Encryption"
            desc="AES-256 encryption ensures your data remains private and secure from hackers."
          />
          <FeatureCard
            icon={<Globe className="w-12 h-12 text-blue-500" />}
            title="Global Server Network"
            desc="Access content from anywhere with servers in 50+ countries."
          />
          <FeatureCard
            icon={<Zap className="w-12 h-12 text-yellow-500" />}
            title="Lightning Fast Speeds"
            desc="Optimized for streaming and gaming with up to 10Gbps servers."
          />
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-800 py-10 text-center text-gray-400 mt-20">
        <p>&copy; 2024 DataGuard VPN. All rights reserved.</p>
      </footer>
    </div>
  );
};

const FeatureCard = ({ icon, title, desc }) => (
  <div className="bg-gray-800 p-8 rounded-2xl hover:bg-gray-750 transition border border-gray-700">
    <div className="mb-4">{icon}</div>
    <h3 className="text-xl font-bold mb-3">{title}</h3>
    <p className="text-gray-400">{desc}</p>
  </div>
);

export default LandingPage;
