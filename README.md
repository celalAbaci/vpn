# DataGuard VPN — Android Uygulaması

Android VPN istemcisi. OpenVPN, VLESS/Stealth ve Xray protokollerini destekler.
Backend için ayrı repo: [superVPNProject](../superVPNProject)

---

## Gereksinimler

| Araç | Minimum Sürüm |
|------|--------------|
| Android Studio | Hedgehog (2023.1.1)+ |
| JDK | 17 |
| Android SDK | 35 |
| Gradle | Wrapper üzerinden otomatik |

---

## Kurulum

### 1. Projeyi klonla

```bash
git clone https://github.com/celalAbaci/vpn.git
cd vpn
```

### 2. `local.properties` dosyasını oluştur

Proje kök dizininde (`SuperVPN_Proje/local.properties`) aşağıdaki içeriği oluştur. Bu dosya `.gitignore`'a eklidir, commit edilmez.

```properties
# Android SDK yolu (kendi bilgisayarına göre düzenle)
sdk.dir=C\:\\Users\\KULLANICI_ADI\\AppData\\Local\\Android\\Sdk

# Release imzalama (release APK almak için gerekli)
KEYSTORE_PATH=C:/path/to/your-keystore.jks
KEYSTORE_PASSWORD=keystore_sifren
KEY_ALIAS=alias_adin
KEY_PASSWORD=key_sifren
```

> **Not:** Release build almak istemiyorsan imzalama satırları boş bırakılabilir; debug build sorunsuz çalışır.

### 3. Android Studio'da aç

```
File → Open → SuperVPN_Proje klasörünü seç → OK
```

Gradle sync otomatik başlar. Tüm bağımlılıklar indirilir.

---

## Build & Çalıştırma

### Debug APK (geliştirme için)

```bash
./gradlew assembleDebug
# Çıktı: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (dağıtım için)

`local.properties` içindeki keystore ayarları doldurulmuş olmalı.

```bash
./gradlew assembleRelease
# Çıktı: app/build/outputs/apk/release/app-release.apk
```

### Bağlı cihaza / emülatöre yükle

```bash
./gradlew installDebug
```

### Testler

```bash
# Unit testler
./gradlew test

# Instrumented testler (cihaz/emülatör gerekli)
./gradlew connectedAndroidTest
```

---

## APK Teknik Detayları

| Özellik | Değer |
|---------|-------|
| Application ID | `com.abacicelal.supervpn_project` |
| Min SDK | 24 (Android 7.0 Nougat) |
| Target SDK | 35 (Android 15) |
| Version | 1.0 (code: 1) |
| Java | 17 |
| Desteklenen ABI (release) | `arm64-v8a`, `armeabi-v7a` |
| Desteklenen ABI (debug) | + `x86_64` (emülatör) |

---

## Desteklenen VPN Protokolleri

| Protokol | Servis | Açıklama |
|----------|--------|----------|
| OpenVPN | `OpenVPNService` | Gömülü ics-openvpn motoru |
| VLESS/Stealth | `StealthVpnService` | libv2ray üzerinden Xray-core |
| Xray | `XrayVpnService` | LibXray (Xray-core) direkt entegrasyon |

Protokol seçimi `MainActivity`'de yapılır. `AUTO` modunda sunucu ping ve yüküne göre otomatik seçilir.

---

## Proje Yapısı

```
app/src/main/java/com/abacicelal/supervpn_project/
├── core/
│   ├── StealthVpnService.java    # VLESS/Stealth VPN servisi
│   └── XrayVpnService.java       # Xray VPN servisi
├── remote/
│   ├── ApiService.java           # Retrofit endpoint tanımları
│   ├── AuthInterceptor.java      # JWT token enjeksiyonu
│   ├── RetrofitClient.java       # HTTP istemci yapılandırması
│   ├── TokenAuthenticator.java   # Otomatik token yenileme
│   └── model/                    # API request/response modelleri
├── utils/
│   └── DeviceIdManager.java      # Cihaz kimliği yönetimi
├── util/
│   └── LocaleManager.java        # Dil yönetimi
├── BaseActivity.java             # Dil desteği için temel activity
├── MainActivity.java             # VPN kontrol merkezi
├── LoginActivity.java
├── RegisterActivity.java
├── SplashActivity.java
├── ServerSelectionActivity.java
├── PremiumActivity.java
└── ...
de/blinkt/openvpn/               # Gömülü OpenVPN kütüphanesi
```

---

## İzinler (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

---

## Backend API

Uygulama `BuildConfig.API_HOST` üzerinden backend'e bağlanır:

- **Release:** `api.dataguardvpn.com`
- **Debug:** `api.dataguardvpn.com`

Farklı bir backend kullanmak için `app/build.gradle` içindeki `buildConfigField "String", "API_HOST"` satırını değiştir.

**Kimlik Doğrulama:** JWT (24 saatlik access token + 7 günlük refresh token)

---

## Sık Karşılaşılan Sorunlar

**Gradle sync başarısız:**
```
File → Invalidate Caches → Invalidate and Restart
```

**`local.properties` bulunamadı hatası:**
Yukarıdaki [Kurulum → Adım 2](#2-localproperties-dosyasını-oluştur) adımını uygula.

**Release build imzalama hatası:**
`local.properties` içindeki keystore yolu ve şifrelerini kontrol et.

**VPN bağlantısı kurulamıyor:**
Backend sunucusunun çalışır durumda olduğunu ve API_HOST değerinin doğru ayarlandığını kontrol et.

---

## Backend Projesi Kurulumu

Backend kurulumu için `superVPNProject/README.md` dosyasına bak.

---

## Lisans

Özel kullanım. Tüm hakları saklıdır.
