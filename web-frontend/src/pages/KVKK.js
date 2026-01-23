import React from 'react';

const KVKK = () => (
  <div className="max-w-4xl mx-auto py-12">
    <h1 className="text-3xl font-bold mb-6">KVKK ve Gizlilik Politikası</h1>
    <div className="prose text-gray-700">
      <p>
        DataGuard VPN olarak kişisel verilerinizin güvenliğine önem veriyoruz.
        6698 sayılı Kişisel Verilerin Korunması Kanunu ("KVKK") kapsamında veri sorumlusu sıfatıyla hareket etmekteyiz.
      </p>
      <h3 className="text-xl font-bold mt-4">Toplanan Veriler</h3>
      <ul className="list-disc pl-5">
        <li>Cihaz ID (Misafir girişi için)</li>
        <li>Kullanıcı Adı ve E-posta (Üyelik için)</li>
        <li>Bağlantı Zaman Damgaları (Sorun giderme için, içerik loglanmaz)</li>
      </ul>
      <h3 className="text-xl font-bold mt-4">Veri Güvenliği</h3>
      <p>
        Verileriniz endüstri standardı şifreleme yöntemleri ile korunmaktadır.
        Hiçbir zaman gezinti geçmişinizi (visited websites, DNS queries) kayıt altına almıyoruz (No-Logs Policy).
      </p>
    </div>
  </div>
);

export default KVKK;
