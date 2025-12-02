package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
// Bu import artık gereksiz olsa da kalabilir, zarar vermez.
import android.content.res.ColorStateList;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.abacicelal.supervpn_project.remote.ApiService;
import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.ConfigGenerationRequest;
import com.abacicelal.supervpn_project.remote.model.Device;
import com.abacicelal.supervpn_project.remote.model.DeviceRequest;
import com.abacicelal.supervpn_project.remote.model.Subscription;
import com.abacicelal.supervpn_project.remote.model.VpnConfigResponse;
import com.abacicelal.supervpn_project.remote.model.VpnProtocol;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Loglama için
    public static boolean isConnected = false;
    private boolean isConnecting = false;

    // UI Bileşenleri
    private ImageButton connectButton;
    private ImageButton menuButton;
    private ImageButton premiumButton;
    // *** DÜZELTME: Üstteki 3 buton için yorumlar kaldırıldı ***
    private ImageButton browserButton;
    private ImageButton locationButton;
    private ImageButton helpButton;
    // *** BİTTİ ***
    private Button protocolAuto, protocolIKEv2, protocolSuper, protocolOpenVPN;
    private ProgressBar loadingSpinner;
    private LinearLayout sideMenu;
    private Switch notificationSwitch;
    private View dimBackground;
    private TextView menuAccount, menuSupport, menuAlwaysOn, menuAbout;
    private TextView statusConnectedText;
    private TextView statusSafeText;
    private LinearLayout connectionTimeLayout;
    private TextView connectionTimeText;
    private TextView connectionStatusLabel;
    private LinearLayout serverSelectionLayout;
    private Button upgradePremiumButton;
    private TextView currentServerInfo;
    private TextView currentProtocolInfo;

    // Zamanlayıcı
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private long startTime;

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "VPN_PREFS";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_SELECTED_SERVER_NAME = "selected_server_name";
    private static final String KEY_SELECTED_SERVER_ID = "selected_server_id";

    // Retrofit ApiService
    private ApiService apiService;

    // Seçilen protokolü saklamak için
    private VpnProtocol selectedProtocol = VpnProtocol.WIREGUARD; // Varsayılan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ApiService'i başlat
        apiService = RetrofitClient.getApiService(getApplicationContext());

        // Tüm UI bileşenlerini bağla
        connectButton = findViewById(R.id.connectButton);
        menuButton = findViewById(R.id.menuButton);
        premiumButton = findViewById(R.id.premiumButton);

        // *** DÜZELTME: Üstteki 3 buton için findViewById çağrıları eklendi ***
        browserButton = findViewById(R.id.browserButton);
        locationButton = findViewById(R.id.locationButton);
        helpButton = findViewById(R.id.helpButton);
        // *** BİTTİ ***

        sideMenu = findViewById(R.id.sideMenu);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        dimBackground = findViewById(R.id.dimBackground);
        protocolAuto = findViewById(R.id.protocolAuto);
        protocolIKEv2 = findViewById(R.id.protocolIKEv2);
        protocolSuper = findViewById(R.id.protocolSuper);
        protocolOpenVPN = findViewById(R.id.protocolOpenVPN);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        menuAccount = findViewById(R.id.menuAccount);
        menuSupport = findViewById(R.id.menuSupport);
        menuAlwaysOn = findViewById(R.id.menuAlwaysOn);
        menuAbout = findViewById(R.id.menuAbout);
        statusConnectedText = findViewById(R.id.statusConnectedText);
        statusSafeText = findViewById(R.id.statusSafeText);
        connectionTimeLayout = findViewById(R.id.connectionTimeLayout);
        connectionTimeText = findViewById(R.id.connectionTimeText);
        connectionStatusLabel = findViewById(R.id.connectionStatusLabel);
        serverSelectionLayout = findViewById(R.id.serverSelectionLayout);
        upgradePremiumButton = findViewById(R.id.upgradePremiumButton);
        currentServerInfo = findViewById(R.id.currentServerInfo);
        currentProtocolInfo = findViewById(R.id.currentProtocolInfo);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        setupProtocolButtons();
        setupConnectButton();
        setupMenuActions();
        setupServerSelection();
        setupPremiumButton();

        // Uygulama başlatıldığında veya geri dönüldüğünde UI'ı doğru duruma getir
        updateUIOnConnectionState();
    }

    private void setupMenuActions() {
        menuButton.setOnClickListener(v -> {
            if (sideMenu.getVisibility() == View.GONE) {
                sideMenu.setVisibility(View.VISIBLE);
                dimBackground.setVisibility(View.VISIBLE);
                sideMenu.animate().translationX(0).setDuration(250).start();
            } else {
                closeSideMenu();
            }
        });

        dimBackground.setOnClickListener(v -> closeSideMenu());

        // *** DÜZELTME: Üstteki 3 buton için OnClickListener eklendi ***
        browserButton.setOnClickListener(v -> {
            // Tarayıcıyı aç
            try {
                // Not: https:// eklemeyi unutmayın
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dataguardvpn.com"));
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Tarayıcı açılamadı.", Toast.LENGTH_SHORT).show();
            }
        });

        helpButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HelpSupportActivity.class)));

        locationButton.setOnClickListener(v -> {
            // Zaten setupServerSelection() içinde tanımlı olanı yap
            startActivity(new Intent(MainActivity.this, ServerSelectionActivity.class));
        });
        // *** BİTTİ ***

        // DÜZELTME: Tam yolu kullan
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> android.widget.Toast.makeText(this, isChecked ? "Bildirimler Açıldı" : "Bildirimler Kapandı", android.widget.Toast.LENGTH_SHORT).show());

        menuAccount.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AccountActivity.class));
            closeSideMenu();
        });
        menuSupport.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HelpSupportActivity.class));
            closeSideMenu();
        });
        menuAlwaysOn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AlwaysOnVpnActivity.class));
            closeSideMenu();
        });
        menuAbout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
            closeSideMenu();
        });
    }

    private void closeSideMenu() {
        if (sideMenu.getVisibility() == View.VISIBLE) {
            sideMenu.animate()
                    .translationX(-sideMenu.getWidth())
                    .setDuration(250)
                    .withEndAction(() -> sideMenu.setVisibility(View.GONE))
                    .start();
            dimBackground.setVisibility(View.GONE);
        }
    }

    private void setupConnectButton() {
        Animation bounceFadeAnim = AnimationUtils.loadAnimation(this, R.anim.bounce_fade_set);
        connectButton.setOnClickListener(v -> {
            connectButton.startAnimation(bounceFadeAnim);
            if (!isConnected && !isConnecting) {
                // Sahte bağlantıyı kaldır, gerçek bağlantıyı başlat
                startVPNConnection();
            } else if (isConnected && !isConnecting) {
                // Sahte bağlantı kesmeyi kaldır, gerçek bağlantı kesmeyi başlat
                disconnectVPN();
            }
        });
    }

    private void startVPNConnection() {
        Log.d(TAG, "startVPNConnection çağrıldı.");

        // 1. Token (Oturum) Kontrolü
        String token = RetrofitClient.getToken(this);
        if (token == null) {
            // DÜZELTME: Tam yolu kullan
            android.widget.Toast.makeText(this, "Lütfen önce giriş yapın.", android.widget.Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, AccountActivity.class));
            return;
        }

        // 2. UI'ı "Bağlanıyor" durumuna getir
        isConnecting = true;
        updateUIOnConnectionState();

        // 3. Cihaz bilgisini al veya kaydet
        fetchDeviceAndCheckSubscription();
    }

    private void fetchDeviceAndCheckSubscription() {
        Log.d(TAG, "Cihaz bilgisi alınıyor...");
        apiService.getMyDevices().enqueue(new Callback<List<Device>>() {
            @Override
            public void onResponse(Call<List<Device>> call, Response<List<Device>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        // Cihaz yok, yeni bir tane kaydet
                        Log.d(TAG, "Cihaz bulunamadı, yeni cihaz kaydediliyor...");
                        registerDevice();
                    } else {
                        // Cihaz var, ilkini kullan
                        Long deviceId = response.body().get(0).getId();
                        Log.d(TAG, "Cihaz bulundu. ID: " + deviceId);
                        checkSubscription(deviceId);
                    }
                } else {
                    // DÜZELTME: TokenAuthenticator 401'i yakalayamazsa (örn. 403, 500 hatası),
                    // hatayı burada göster.
                    handleConnectionFailure("Cihaz bilgisi alınamadı. Hata: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Device>> call, Throwable t) {
                // DÜZELTME: TokenAuthenticator eklendiği için burası artık JSON
                // parse hatası (401'den kaynaklanan) almamalı. Burası SADECE
                // gerçek ağ hatalarında (internet yok vb.) tetiklenmeli.
                handleConnectionFailure("AĞ HATASI (Cihaz): " + t.getMessage());
            }
        });
    }

    private void registerDevice() {
        // Cihaz adı olarak model veya basit bir isim kullanılabilir
        String deviceName = "Android Cihaz " + android.os.Build.MODEL;
        apiService.registerDevice(new DeviceRequest(deviceName)).enqueue(new Callback<Device>() {
            @Override
            public void onResponse(Call<Device> call, Response<Device> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long deviceId = response.body().getId();
                    Log.d(TAG, "Yeni cihaz kaydedildi. ID: " + deviceId);
                    checkSubscription(deviceId);
                } else {
                    handleConnectionFailure("Yeni cihaz kaydedilemedi. Hata: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Device> call, Throwable t) {
                handleConnectionFailure("AĞ HATASI (Cihaz Kayıt): " + t.getMessage());
            }
        });
    }

    private void checkSubscription(Long deviceId) {
        Log.d(TAG, "Abonelik durumu kontrol ediliyor...");
        apiService.getMySubscriptions().enqueue(new Callback<List<Subscription>>() {
            @Override
            public void onResponse(Call<List<Subscription>> call, Response<List<Subscription>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean hasActiveSubscription = false;
                    for (Subscription sub : response.body()) {
                        if (sub.isActive()) {
                            hasActiveSubscription = true;
                            break;
                        }
                    }

                    if (hasActiveSubscription) {
                        Log.d(TAG, "Aktif abonelik bulundu. VPN yapılandırması isteniyor...");
                        fetchVpnConfig(deviceId);
                    } else {
                        Log.w(TAG, "Aktif abonelik bulunamadı.");
                        // DÜZELTME: Tam yolu ve doğru context'i (MainActivity.this) kullan
                        android.widget.Toast.makeText(MainActivity.this, "Aktif bir aboneliğiniz bulunmuyor.", android.widget.Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, PremiumActivity.class));
                        handleConnectionFailure(null); // Bağlantıyı durdur
                    }
                } else {
                    handleConnectionFailure("Abonelik bilgisi alınamadı. Hata: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Subscription>> call, Throwable t) {
                handleConnectionFailure("AĞ HATASI (Abonelik): " + t.getMessage());
            }
        });
    }

    private void fetchVpnConfig(Long deviceId) {
        // ServerSelectionActivity'den kaydedilen sunucu ID'sini al
        long serverId = sharedPreferences.getLong(KEY_SELECTED_SERVER_ID, 0);

        if (serverId == 0) {
            // DÜZELTME: Tam yolu ve doğru context'i (MainActivity.this) kullan
            android.widget.Toast.makeText(MainActivity.this, "Lütfen önce bir sunucu seçin.", android.widget.Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, ServerSelectionActivity.class));
            handleConnectionFailure(null); // Bağlantıyı durdur
            return;
        }

        Log.d(TAG, "VPN yapılandırması isteniyor. ServerID: " + serverId + ", DeviceID: " + deviceId + ", Protocol: " + selectedProtocol.name());

        // Backend'e gönderilecek isteği oluştur
        ConfigGenerationRequest request = new ConfigGenerationRequest(serverId, deviceId, selectedProtocol);
        // İsteğe bağlı: Multi-hop veya DNS ayarları
        // request.setExitServerId(10L);
        // request.setDnsProvider(CustomDnsProvider.ADGUARD);

        apiService.generateConfig(request).enqueue(new Callback<VpnConfigResponse>() {
            @Override
            public void onResponse(Call<VpnConfigResponse> call, Response<VpnConfigResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String configContent = response.body().getConfigurationFileContent();
                    Log.d(TAG, "VPN yapılandırması başarıyla alındı.");
                    // Log.d(TAG, "Config: \n" + configContent); // (Güvenlik için loglamayı kapalı tutun)

                    // DÜZELTME: Tam yolu ve doğru context'i (MainActivity.this) kullan
                    android.widget.Toast.makeText(MainActivity.this, "Konfigürasyon alındı! VPN servisi başlatılıyor...", android.widget.Toast.LENGTH_SHORT).show();

                    // --- BURASI ÇOK ÖNEMLİ ---
                    // Gerçek bir uygulamada, 'configContent' string'i
                    // bir OpenVPN veya WireGuard kütüphanesine (örn: MyVpnService)
                    // gönderilerek bağlantı başlatılır.
                    //
                    // Örnek:
                    // Intent vpnIntent = new Intent(MainActivity.this, MyVpnService.class);
                    // vpnIntent.putExtra("VPN_CONFIG", configContent);
                    // vpnIntent.putExtra("VPN_PROTOCOL", selectedProtocol.name());
                    // startService(vpnIntent);
                    //
                    // Kütüphaneleriniz olmadığı için, burada bağlantıyı "başarılı" varsayıyoruz.
                    // --- BAŞARILI VARSAYMA KISMI ---
                    simulateConnectionSuccess();
                    // ---------------------------------
                } else {
                    handleConnectionFailure("VPN yapılandırması alınamadı. Hata: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<VpnConfigResponse> call, Throwable t) {
                handleConnectionFailure("AĞ HATASI (Yapılandırma): " + t.getMessage());
            }
        });
    }


    // YENİ METOT: Bağlantı sürecinde hata olursa UI'ı sıfırla
    private void handleConnectionFailure(String logMessage) {
        if (logMessage != null) {
            Log.e(TAG, logMessage);
            // DÜZELTME: Tam yolu ve doğru context'i (MainActivity.this) kullan
            android.widget.Toast.makeText(MainActivity.this, "Bağlantı hatası. Detaylar için log'u kontrol edin.", android.widget.Toast.LENGTH_LONG).show();
        }
        isConnecting = false;
        isConnected = false;
        updateUIOnConnectionState();
    }

    // YENİ METOT: API'dan yanıt geldikten sonra sahte bağlantıyı başlat
    private void simulateConnectionSuccess() {
        isConnected = true;
        isConnecting = false;
        // Başlangıç zamanını kaydet
        startTime = System.currentTimeMillis();
        sharedPreferences.edit().putLong(KEY_START_TIME, startTime).apply();
        startTimer();
        updateUIOnConnectionState();
        // DÜZELTME: Tam yolu ve doğru context'i (MainActivity.this) kullan
        android.widget.Toast.makeText(MainActivity.this, "VPN bağlantısı kuruldu", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void disconnectVPN() {
        Log.d(TAG, "disconnectVPN çağrıldı.");
        // Gerçek bir uygulamada burada VPN servisini durdurma komutu olur.
        // Örnek:
        // Intent vpnIntent = new Intent(MainActivity.this, MyVpnService.class);
        // vpnIntent.setAction("ACTION_DISCONNECT");
        // startService(vpnIntent);

        isConnecting = false;
        isConnected = false;
        stopTimer();
        sharedPreferences.edit().remove(KEY_START_TIME).apply();
        updateUIOnConnectionState();
        // DÜZELTME: Tam yolu ve doğru context'i (MainActivity.this) kullan
        android.widget.Toast.makeText(MainActivity.this, "VPN bağlantısı kesildi", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void setupProtocolButtons() {
        Button[] protocolButtons = {protocolAuto, protocolIKEv2, protocolSuper, protocolOpenVPN};
        View.OnClickListener listener = v -> {
            for (Button btn : protocolButtons) {
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_dark)));
            }
            Button button = (Button) v;
            button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.purple_500)));
            String selectedProtocolName = "Otomatik"; // Seçilen protokole göre enum değerini ayarla
            if (button.getId() == R.id.protocolAuto) {
                selectedProtocolName = "Otomatik";
                selectedProtocol = VpnProtocol.WIREGUARD; // "Auto" için varsayılan
            } else if (button.getId() == R.id.protocolIKEv2) {
                selectedProtocolName = "IKEv2";
                selectedProtocol = VpnProtocol.IKEV2;
            } else if (button.getId() == R.id.protocolSuper) {
                selectedProtocolName = "Super";
                selectedProtocol = VpnProtocol.WIREGUARD; // "Super" için varsayılan
            } else if (button.getId() == R.id.protocolOpenVPN) {
                selectedProtocolName = "OpenVPN";
                selectedProtocol = VpnProtocol.OPENVPN;
            }
            currentProtocolInfo.setText("Protokol : " + selectedProtocolName);
            Log.d(TAG, "Protokol seçildi: " + selectedProtocol.name());
        };
        for (Button button : protocolButtons) {
            button.setOnClickListener(listener);
        }
        // Başlangıçta "Auto" (WireGuard) seçili olsun
        protocolAuto.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.purple_500)));
        currentProtocolInfo.setText("Protokol : Otomatik");
        selectedProtocol = VpnProtocol.WIREGUARD;
    }

    private void startTimer() {
        stopTimer(); // Önceki zamanlayıcıyı durdur
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                long minutes = elapsed / 60; // long olarak bırak
                long seconds = elapsed % 60; // long olarak bırak
                connectionTimeText.setText(String.format("%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
            timerRunnable = null;
        }
    }

    private void updateUIOnConnectionState() {
        if (isConnecting) {
            statusConnectedText.setText("Bağlanıyor...");
            statusConnectedText.setTextColor(Color.parseColor("#00BFFF")); // Mavi
            statusSafeText.setText("");
            connectionTimeLayout.setVisibility(View.VISIBLE);
            connectionTimeText.setText("00:00");
            connectionStatusLabel.setText("BAĞLANIYOR");
            connectionStatusLabel.setTextColor(Color.parseColor("#BBBBBB"));
            loadingSpinner.setVisibility(ProgressBar.VISIBLE);
            connectButton.setEnabled(false); // Bağlanırken tekrar tıklanmasın
            connectButton.setBackgroundResource(R.drawable.btn_round);
        } else if (isConnected) {
            statusConnectedText.setText("Bağlandı");
            statusConnectedText.setTextColor(Color.parseColor("#00FF00")); // Yeşil
            statusSafeText.setText(currentServerInfo.getText().toString()); // Sunucu bilgisini göster
            connectionTimeLayout.setVisibility(View.VISIBLE);
            connectionStatusLabel.setText("BAĞLANTIYI KES");
            connectionStatusLabel.setTextColor(Color.parseColor("#BBBBBB"));
            loadingSpinner.setVisibility(ProgressBar.GONE);
            connectButton.setEnabled(true);
            connectButton.setBackgroundResource(R.drawable.btn_round_connected); // Yeşil buton
            // Zamanlayıcıyı sadece burada başlatma, startVPNConnection'da zaten var
        } else { // Disconnected
            statusConnectedText.setText("Bağlantı Yok");
            statusConnectedText.setTextColor(Color.parseColor("#FFFFFF")); // Beyaz
            statusSafeText.setText("Bağlanmak için dokunun");
            connectionTimeLayout.setVisibility(View.VISIBLE);
            connectionTimeText.setText("00:00");
            connectionStatusLabel.setText("BAĞLAN");
            connectionStatusLabel.setTextColor(Color.parseColor("#FFFFFF"));
            loadingSpinner.setVisibility(ProgressBar.GONE);
            connectButton.setEnabled(true);
            connectButton.setBackgroundResource(R.drawable.btn_round);
            stopTimer(); // Bağlantı yokken zamanlayıcıyı durdur
        }
    }

    private void setupServerSelection() {
        View.OnClickListener listener = v -> {
            Intent intent = new Intent(MainActivity.this, ServerSelectionActivity.class);
            startActivity(intent);
        };
        // *** DÜZELTME: 'locationButton' artık 'serverSelectionLayout' ile aynı işi yapıyor ***
        // locationButton.setOnClickListener(listener); // Bu zaten setupMenuActions içinde yapıldı
        serverSelectionLayout.setOnClickListener(listener);
    }

    private void setupPremiumButton() {
        premiumButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PremiumActivity.class));
        });
        upgradePremiumButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PremiumActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ServerSelectionActivity'den dönüldüğünde (veya ilk açılışta)
        // SharedPreferences'dan SADECE KAYITLI VERİYİ OKU.
        // Intent'i burada okumak, aktivite her 'resume' olduğunda
        // (örn. ekranı kilitleyip açınca) sunucunun sıfırlanmasına neden olabilir.
        String savedServerName = sharedPreferences.getString(KEY_SELECTED_SERVER_NAME, "Sunucu Seçin");
        long savedServerId = sharedPreferences.getLong(KEY_SELECTED_SERVER_ID, 0);

        if (savedServerId == 0) {
            currentServerInfo.setText("Mevcut Sunucu : Sunucu Seçin");
        } else {
            currentServerInfo.setText("Mevcut Sunucu : " + savedServerName);
        }

        // Bağlantı durumunu ve zamanlayıcıyı güncelle
        if (isConnected) {
            startTime = sharedPreferences.getLong(KEY_START_TIME, 0);
            if (startTime == 0) {
                // Zamanlayıcı kaydı yok ama 'isConnected' true kalmış, durumu düzelt
                disconnectVPN();
            } else {
                startTimer();
                updateUIOnConnectionState();
            }
        } else {
            updateUIOnConnectionState();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer(); // Aktivite yok edildiğinde zamanlayıcıyı durdur
    }
}
