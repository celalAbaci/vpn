import React from 'react';
import { Shield, Globe, Zap, Smartphone } from 'lucide-react';

const Home = () => {
  return (
    <div className="flex flex-col items-center">
      <header className="text-center py-20 bg-gradient-to-b from-blue-50 to-white w-full">
        <h1 className="text-5xl font-extrabold text-gray-900 mb-6">İnternette Özgürleşin</h1>
        <p className="text-xl text-gray-600 max-w-2xl mx-auto mb-10">
          DataGuard VPN ile güvenli, hızlı ve anonim gezinin. Tek tıkla dünya parmaklarınızın ucunda.
        </p>
        <button className="bg-blue-600 text-white px-8 py-3 rounded-full text-lg font-semibold hover:bg-blue-700 transition">
          Hemen İndir (Android)
        </button>
      </header>

      <section className="py-16 grid grid-cols-1 md:grid-cols-3 gap-8 w-full max-w-6xl">
        <div className="bg-white p-8 rounded-xl shadow-lg text-center border border-gray-100">
          <Shield className="w-12 h-12 text-blue-500 mx-auto mb-4" />
          <h3 className="text-xl font-bold mb-2">Askeri Düzeyde Şifreleme</h3>
          <p className="text-gray-600">Verileriniz 256-bit AES şifreleme ile güvende. Kimse ne yaptığınızı göremez.</p>
        </div>
        <div className="bg-white p-8 rounded-xl shadow-lg text-center border border-gray-100">
          <Zap className="w-12 h-12 text-yellow-500 mx-auto mb-4" />
          <h3 className="text-xl font-bold mb-2">Işık Hızında Bağlantı</h3>
          <p className="text-gray-600">WireGuard protokolü ile yavaşlama olmadan yayın izleyin ve indirin.</p>
        </div>
        <div className="bg-white p-8 rounded-xl shadow-lg text-center border border-gray-100">
          <Smartphone className="w-12 h-12 text-green-500 mx-auto mb-4" />
          <h3 className="text-xl font-bold mb-2">Kayıt Tutmuyoruz</h3>
          <p className="text-gray-600">Aktivite kaydı tutmuyoruz. Gizliliğiniz bizim için en önemli öncelik.</p>
        </div>
      </section>
    </div>
  );
};

export default Home;
