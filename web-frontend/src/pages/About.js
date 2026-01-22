import React from 'react';

const About = () => {
  return (
    <div className="py-16 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-16">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">About Us</h1>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            We are a team of security experts dedicated to making the internet safer and more open for everyone.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
            <div>
                <img src="https://images.unsplash.com/photo-1550751827-4bd374c3f58b?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80" alt="Team working" className="rounded-xl shadow-lg" />
            </div>
            <div>
                <h2 className="text-2xl font-bold mb-4 text-gray-900">Our Mission</h2>
                <p className="text-gray-600 mb-6 leading-relaxed">
                    In an age of digital surveillance and cyber threats, privacy is not just a luxury, it's a fundamental right.
                    DataGuard VPN was built to provide robust, accessible, and user-friendly privacy tools.
                </p>
                <h2 className="text-2xl font-bold mb-4 text-gray-900">Our Technology</h2>
                <p className="text-gray-600 leading-relaxed">
                    We leverage the latest VPN protocols like WireGuard and OpenVPN to deliver top-tier performance without compromising security.
                    Our no-logs policy ensures your data remains yours alone.
                </p>
            </div>
        </div>
      </div>
    </div>
  );
};

export default About;
