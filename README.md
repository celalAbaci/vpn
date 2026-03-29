# SuperVPN — Android VPN Uygulaması

<div align="center">
  <h3>Güvenli, Hızlı ve Kolay Kullanımlı VPN</h3>
  <p>
    <a href="#türkçe-kurulum-rehberi">🇹🇷 Türkçe</a> &nbsp;|&nbsp;
    <a href="#english-installation-guide">🇬🇧 English</a>
  </p>
</div>

---

## En Son Sürümü İndir

📥 **[Releases sayfasına git → APK'yı indir](../../releases/latest)**

---

---

# 🇹🇷 Türkçe Kurulum Rehberi

## Adım 1 — APK'yı İndir

1. Bu sayfanın üstündeki **"Releases"** bölümüne veya [releases/latest](../../releases/latest) bağlantısına tıkla.
2. En üstteki (en yeni) sürümü bul.
3. **Assets** bölümünün altında `SuperVPN-vX.X.X.apk` dosyasına tıkla — indirme başlayacak.

> Tarayıcın "Bu dosya zararlı olabilir" uyarısı gösterebilir. Bu normaldir; uygulamayı Google Play dışından indirdiğin için tarayıcı bu uyarıyı verir. **"Yine de indir"** veya **"Devam et"** seçeneğini seç.

---

## Adım 2 — Bilinmeyen Kaynaklardan Yüklemeyi Etkinleştir

Android varsayılan olarak Google Play dışından uygulama yüklemeyi engeller. Aşağıdaki adımları izle:

### Android 8 ve üzeri (modern telefonlar):
1. **Ayarlar** → **Uygulamalar** (veya Uygulama Yöneticisi) aç.
2. Sağ üstten **⋮ (üç nokta)** → **Özel uygulama erişimi** → **Bilinmeyen uygulamalar yükle** seç.
3. İndirdiğin tarayıcıyı (Chrome, Firefox vb.) listede bul ve **"Bu kaynaktan izin ver"** seçeneğini aç.

### Samsung telefonlar (One UI):
1. **Ayarlar** → **Biyometri ve güvenlik** → **Bilinmeyen uygulamalar yükle**.
2. Kullandığın tarayıcıyı seç ve izin ver.

### Xiaomi / MIUI telefonlar:
1. **Ayarlar** → **Ek ayarlar** → **Gizlilik** → **Bilinmeyen kaynaklardan yükleme** aç.
2. Ya da indirme başladığında ekranda çıkan "Ayarlar" uyarısına tıkla.

---

## Adım 3 — APK'yı Yükle

1. İndirdiğin APK dosyasını **Dosya Yöneticisi** veya **İndirilenler** klasöründen bul.
2. Dosyaya tıkla → **Yükle** düğmesine bas.
3. Yükleme tamamlandığında **Aç** düğmesiyle uygulamayı başlat.

---

## Adım 4 — İlk Giriş

1. Uygulamayı açtığında **Kayıt Ol** ekranı gelecek.
2. E-posta ve şifre ile hesap oluştur.
3. VPN bağlantısı için uygulamanın istediği **VPN izinlerine** izin ver (Android bu izni bir kez ister).
4. Bağlantı sunucusunu seç ve **Bağlan** tuşuna bas.

---

## Sık Sorulan Sorular

**S: Uygulama neden Play Store'da yok?**
C: Uygulama şu an test/beta aşamasındadır. Yakında Play Store'a yüklenecektir.

**S: VPN bağlantısı kurulamıyor, ne yapmalıyım?**
C: Farklı bir sunucu seçmeyi dene. Sorun devam ederse uygulama içindeki destek bölümünü kullan.

**S: Eski sürümü kaldırıp yeni sürümü mü yüklemeliyim?**
C: Hayır. Yeni APK'yı direkt yükle — Android mevcut uygulamayı güncelleyecektir (hesap bilgilerin korunur).

---

## Proje Hakkında

SuperVPN, kendi sunucu altyapısı üzerine kurulu bir VPN uygulamasıdır. Kullanıcı hesap yönetimi, abonelik sistemi ve VPN bağlantısı tek bir platformda sunulmaktadır.

**Desteklenen Protokoller:**
- OpenVPN
- VLESS / Reality (Stealth mod)

**Kullanılan Teknolojiler:**
- **Mobil:** Android (Java)
- **Sunucu:** Java tabanlı REST API, PostgreSQL veritabanı
- **Altyapı:** Docker tabanlı konteyner mimarisi

---
---

# 🇬🇧 English Installation Guide

## Step 1 — Download the APK

1. Go to the **"Releases"** section at the top of this page or click [releases/latest](../../releases/latest).
2. Find the latest release at the top.
3. Under **Assets**, click `SuperVPN-vX.X.X.apk` — the download will begin.

> Your browser may show a warning like "This file may be harmful." This is normal when downloading apps outside Google Play. Click **"Download anyway"** or **"Keep"** to proceed.

---

## Step 2 — Enable Installation from Unknown Sources

Android blocks app installation from outside the Play Store by default. Follow these steps:

### Android 8 and above (modern phones):
1. Open **Settings** → **Apps** (or Application Manager).
2. Tap **⋮ (three dots)** → **Special app access** → **Install unknown apps**.
3. Find your browser (Chrome, Firefox, etc.) and enable **"Allow from this source"**.

### Samsung phones (One UI):
1. **Settings** → **Biometrics and security** → **Install unknown apps**.
2. Select your browser and allow it.

### Xiaomi / MIUI phones:
1. **Settings** → **Additional settings** → **Privacy** → **Unknown sources**.
2. Or tap the "Settings" prompt that appears when the download starts.

---

## Step 3 — Install the APK

1. Find the downloaded APK in your **File Manager** or **Downloads** folder.
2. Tap the file → press **Install**.
3. Once installed, tap **Open** to launch the app.

---

## Step 4 — First Launch

1. You'll see a **Sign Up** screen when you first open the app.
2. Create an account with your email and password.
3. Allow the **VPN permission** the app requests (Android asks for this once).
4. Select a VPN server and tap **Connect**.

---

## FAQ

**Q: Why isn't the app on the Play Store?**
A: The app is currently in beta/testing phase. It will be published on the Play Store soon.

**Q: VPN connection failed, what should I do?**
A: Try a different server. If the issue persists, use the support section inside the app.

**Q: Do I need to uninstall the old version before installing a new one?**
A: No. Install the new APK directly — Android will update the existing app (your account data is preserved).

---

## About the Project

SuperVPN is a VPN application built on a proprietary server infrastructure. User account management, subscription system, and VPN connectivity are all provided in a single platform.

**Supported Protocols:**
- OpenVPN
- VLESS / Reality (Stealth mode)

**Technologies Used:**
- **Mobile:** Android (Java)
- **Server:** Java-based REST API, PostgreSQL database
- **Infrastructure:** Docker-based container architecture

---
---

# 🤖 [CLAUDE — Internal Release Workflow]

> Bu bölüm geliştiriciye yönelik notlar içermektedir. Kullanıcılar bu bölümü yok sayabilir.
> This section contains developer notes. End users can ignore this section.

## APK Yayınlama Süreci (Claude için)

Kullanıcı **"APK'yı githuba at"**, **"yeni sürümü yayınla"**, **"release at"** veya benzeri bir komut verdiğinde aşağıdaki süreç uygulanır:

### Hedef Repo
```
https://github.com/celalAbaci/vpn
```
- Bu repo **SADECE APK dosyaları** içerir.
- Kaynak kodu, gradle dosyaları, manifest veya herhangi bir proje dosyası bu repoya **asla** yüklenmez.
- Versiyon takibi **GitHub Releases** üzerinden yapılır.

---

### Adım 1 — Release APK'yı Derle (gerekirse)

Kullanıcı zaten derlenmiş bir APK sağlamadıysa:

```bash
cd SuperVPN_Proje
gradlew assembleRelease
```

Çıktı konumu:
```
SuperVPN_Proje/app/build/outputs/apk/release/app-release.apk
```

---

### Adım 2 — Versiyon Numarasını Belirle

- Mevcut son release versiyonunu kontrol et:
  ```bash
  gh release list --repo celalAbaci/vpn --limit 5
  ```
- Eğer son sürüm `v1.0.0` ise → yeni sürüm `v1.1.0` veya `v1.0.1` olur (kullanıcıya sor).
- Kullanıcı versiyon belirtmemişse bir önceki versiyondan bir artır.

---

### Adım 3 — APK Dosyasını Yeniden Adlandır

APK dosyasını sürüm numarası içerecek şekilde kopyala/yeniden adlandır:
```bash
cp "SuperVPN_Proje/app/build/outputs/apk/release/app-release.apk" "SuperVPN-v1.X.X.apk"
```

---

### Adım 4 — GitHub Release Oluştur ve APK Yükle

```bash
gh release create v1.X.X "SuperVPN-v1.X.X.apk" \
  --title "SuperVPN v1.X.X" \
  --notes "Sürüm notları buraya..." \
  --repo celalAbaci/vpn
```

Tek komutla hem release oluşturulur hem APK yüklenir.

---

### Adım 5 — Doğrula

```bash
gh release view v1.X.X --repo celalAbaci/vpn
```

Release sayfasını kontrol et, APK'nın Assets bölümünde göründüğünden emin ol.

---

### Önemli Kurallar

| Kural | Açıklama |
|-------|----------|
| ✅ Sadece APK | Repoya yalnızca `.apk` dosyası yüklenir |
| ✅ Release kullan | `git push` değil, `gh release create` kullanılır |
| ✅ Eski sürümleri silme | Eski release'ler silinmez, tüm sürüm geçmişi korunur |
| ✅ Sürüm adlandırma | `vMAJOR.MINOR.PATCH` formatı (örn. `v1.2.0`) |
| ✅ APK adı | `SuperVPN-vX.X.X.apk` formatında adlandır |
| ❌ Kaynak kodu yükleme | Hiçbir `.java`, `.xml`, `.gradle`, `.json` dosyası yüklenmez |
| ❌ Force push | `--force` veya `git push --force` kullanılmaz |
| ❌ Release silme | Mevcut release'ler silinmez |

---

### gh CLI Kurulu Değilse

Eğer `gh` CLI kurulu değilse kullanıcıya şunu söyle:

> "GitHub CLI (gh) kurulu değil. Şu komutu çalıştırarak kurabilirsin:
> `winget install --id GitHub.cli`
> Kurulduktan sonra `gh auth login` ile giriş yap, ardından APK'yı yayınlayabilirim."

---

*Son güncelleme: 2026-03-29*
