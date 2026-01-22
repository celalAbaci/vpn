import React from 'react';
import { Shield, Globe, Zap, Lock } from 'lucide-react';

const Home = () => {
  return (
    <div>
      {/* Hero Section */}
      <section className="bg-blue-600 text-white py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h1 className="text-4xl md:text-6xl font-bold mb-6">Secure Your Digital Freedom</h1>
          <p className="text-xl md:text-2xl mb-8 text-blue-100">Fast, private, and secure access to the global internet.</p>
          <button className="bg-white text-blue-600 font-bold py-3 px-8 rounded-full text-lg hover:bg-gray-100 transition shadow-lg">
            Get Started Free
          </button>
        </div>
      </section>

      {/* Features */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold text-gray-900">Why Choose DataGuard?</h2>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-10">
            <FeatureCard icon={<Shield className="w-12 h-12 text-blue-500"/>} title="Military-Grade Encryption" desc="We use AES-256 encryption to protect your data from hackers and surveillance." />
            <FeatureCard icon={<Zap className="w-12 h-12 text-yellow-500"/>} title="Lightning Fast Speeds" desc="Optimized servers ensure you never compromise speed for security." />
            <FeatureCard icon={<Globe className="w-12 h-12 text-green-500"/>} title="Global Server Network" desc="Access content from anywhere with servers in 50+ countries." />
          </div>
        </div>
      </section>

       {/* Call to Action */}
       <section className="py-20 bg-gray-900 text-white text-center">
            <h2 className="text-3xl font-bold mb-6">Ready to protect your privacy?</h2>
            <button className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-8 rounded-full text-lg transition">
                Download Now
            </button>
       </section>
    </div>
  );
};

const FeatureCard = ({ icon, title, desc }) => (
  <div className="bg-white p-8 rounded-xl shadow-md text-center hover:shadow-xl transition">
    <div className="flex justify-center mb-6">{icon}</div>
    <h3 className="text-xl font-bold mb-3 text-gray-900">{title}</h3>
    <p className="text-gray-600">{desc}</p>
  </div>
);

export default Home;
