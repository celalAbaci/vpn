import React, { useState } from 'react';
import { Mail, MapPin, Phone } from 'lucide-react';

const Contact = () => {
  const [formData, setFormData] = useState({ name: '', email: '', message: '' });

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = (e) => {
    e.preventDefault();
    alert("Message sent! (Simulation)");
  };

  return (
    <div className="py-16 bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-16">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">Contact Us</h1>
          <p className="text-xl text-gray-600">Have questions? We're here to help.</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-12">
            {/* Contact Info */}
            <div className="bg-white p-8 rounded-xl shadow-sm">
                <h2 className="text-2xl font-bold mb-6 text-gray-900">Get in Touch</h2>
                <div className="space-y-6">
                    <div className="flex items-start space-x-4">
                        <Mail className="w-6 h-6 text-blue-600 mt-1" />
                        <div>
                            <h3 className="font-semibold text-gray-900">Email</h3>
                            <p className="text-gray-600">support@dataguardvpn.com</p>
                        </div>
                    </div>
                    <div className="flex items-start space-x-4">
                        <Phone className="w-6 h-6 text-blue-600 mt-1" />
                        <div>
                            <h3 className="font-semibold text-gray-900">Phone</h3>
                            <p className="text-gray-600">+1 (555) 123-4567</p>
                        </div>
                    </div>
                    <div className="flex items-start space-x-4">
                        <MapPin className="w-6 h-6 text-blue-600 mt-1" />
                        <div>
                            <h3 className="font-semibold text-gray-900">Office</h3>
                            <p className="text-gray-600">123 Privacy Lane, Cyber City, CA 90210</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Contact Form */}
            <div className="bg-white p-8 rounded-xl shadow-sm">
                <form onSubmit={handleSubmit}>
                    <div className="mb-6">
                        <label className="block text-gray-700 font-medium mb-2">Name</label>
                        <input
                            type="text"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none"
                            required
                        />
                    </div>
                    <div className="mb-6">
                        <label className="block text-gray-700 font-medium mb-2">Email</label>
                        <input
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none"
                            required
                        />
                    </div>
                    <div className="mb-6">
                        <label className="block text-gray-700 font-medium mb-2">Message</label>
                        <textarea
                            name="message"
                            value={formData.message}
                            onChange={handleChange}
                            rows="4"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none"
                            required
                        ></textarea>
                    </div>
                    <button type="submit" className="w-full bg-blue-600 text-white font-bold py-3 rounded-lg hover:bg-blue-700 transition">
                        Send Message
                    </button>
                </form>
            </div>
        </div>
      </div>
    </div>
  );
};

export default Contact;
