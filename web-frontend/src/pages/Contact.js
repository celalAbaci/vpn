import React from 'react';

const Contact = () => (
  <div className="max-w-2xl mx-auto py-12">
    <h1 className="text-3xl font-bold mb-6">İletişim</h1>
    <form className="space-y-4">
      <div>
        <label className="block text-gray-700 mb-2">Adınız</label>
        <input type="text" className="w-full border rounded p-2" placeholder="Adınız Soyadınız" />
      </div>
      <div>
        <label className="block text-gray-700 mb-2">E-posta</label>
        <input type="email" className="w-full border rounded p-2" placeholder="ornek@email.com" />
      </div>
      <div>
        <label className="block text-gray-700 mb-2">Mesajınız</label>
        <textarea className="w-full border rounded p-2 h-32" placeholder="Bize iletmek istediğiniz mesaj..."></textarea>
      </div>
      <button className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700">Gönder</button>
    </form>
  </div>
);

export default Contact;
