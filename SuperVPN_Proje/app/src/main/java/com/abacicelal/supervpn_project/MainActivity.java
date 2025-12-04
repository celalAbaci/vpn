package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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

    private static final String TAG = "MainActivity";
    public static boolean isConnected = false;
    private boolean isConnecting = false;

    // UI Bileşenleri
    private ImageButton connectButton;
    private ImageButton menuButton;
    private ImageButton premiumButton;
    private ImageButton browserButton;
    private ImageButton locationButton;
    private ImageButton helpButton;
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

    // Seçilen protokol
    private VpnProtocol selectedProtocol = VpnProtocol.OPENVPN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitClient.getApiService(getApplicationContext());

        initializeViews();
        setupListeners();

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // UI Başlangıç Durumu
        updateUIOnConnectionState();

        // Varsayılan Protokol Ayarı
        setProtocolSelection(VpnProtocol.OPENVPN);
    }

    private void initializeViews() {
        connectButton = findViewById(R.id.connectButton);
        menuButton = findViewById(R.id.menuButton);
        premiumButton = findViewById(R.id.premiumButton);
        browserButton = findViewById(R.id.browserButton);
        locationButton = findViewById(R.id.locationButton);
        helpButton = findViewById(R.id.helpButton);
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
    }

    private void setupListeners() {
        connectButton.setOnClickListener(v -> handleConnectButtonClick());
        menuButton.setOnClickListener(v -> toggleSideMenu());
        dimBackground.setOnClickListener(v -> closeSideMenu());

        setupProtocolButtons();
        setupMenuNavigation();

        serverSelectionLayout.setOnClickListener(v -> startActivity(new Intent(this, ServerSelectionActivity.class)));
        locationButton.setOnClickListener(v -> startActivity(new Intent(this, ServerSelectionActivity.class)));
        premiumButton.setOnClickListener(v -> startActivity(new Intent(this, PremiumActivity.class)));
        upgradePremiumButton.setOnClickListener(v -> startActivity(new Intent(this, PremiumActivity.class)));
        helpButton.setOnClickListener(v -> startActivity(new Intent(this, HelpSupportActivity.class)));

        browserButton.setOnClickListener(v -> {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dataguardvpn.com"));
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Tarayıcı açılamadı.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleConnectButtonClick() {
        Animation bounceFadeAnim = AnimationUtils.loadAnimation(this, R.anim.bounce_fade_set);
        connectButton.startAnimation(bounceFadeAnim);

        if (!isConnected && !isConnecting) {
            startVPNConnection();
        } else if (isConnected && !isConnecting) {
            disconnectVPN();
        }
    }

    private void startVPNConnection() {
        Log.d(TAG, "VPN Bağlantısı Başlatılıyor...");

        String token = RetrofitClient.getToken(this);
        if (token == null) {
            Toast.makeText(this, "Lütfen önce giriş yapın.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, AccountActivity.class));
            return;
        }

        isConnecting = true;
        updateUIOnConnectionState();
        fetchDeviceAndCheckSubscription();
    }

    private void fetchDeviceAndCheckSubscription() {
        apiService.getMyDevices().enqueue(new Callback<List<Device>>() {
            @Override
            public void onResponse(Call<List<Device>> call, Response<List<Device>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        registerDevice();
                    } else {
                        checkSubscription(response.body().get(0).getId());
                    }
                } else {
                    handleConnectionFailure("Cihaz bilgisi alınamadı. Kod: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Device>> call, Throwable t) {
                handleConnectionFailure("Ağ Hatası (Cihaz): " + t.getMessage());
            }
        });
    }

    private void registerDevice() {
        String deviceName = "Android Cihaz " + android.os.Build.MODEL;
        apiService.registerDevice(new DeviceRequest(deviceName)).enqueue(new Callback<Device>() {
            @Override
            public void onResponse(Call<Device> call, Response<Device> response) {
                if (response.isSuccessful() && response.body() != null) {
                    checkSubscription(response.body().getId());
                } else {
                    handleConnectionFailure("Cihaz kaydedilemedi.");
                }
            }

            @Override
            public void onFailure(Call<Device> call, Throwable t) {
                handleConnectionFailure("Ağ Hatası (Kayıt): " + t.getMessage());
            }
        });
    }

    private void checkSubscription(Long deviceId) {
        apiService.getMySubscriptions().enqueue(new Callback<List<Subscription>>() {
            @Override
            public void onResponse(Call<List<Subscription>> call, Response<List<Subscription>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean hasActive = response.body().stream().anyMatch(Subscription::isActive);
                    if (hasActive) {
                        fetchVpnConfig(deviceId);
                    } else {
                        Toast.makeText(MainActivity.this, "Aktif abonelik bulunamadı.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, PremiumActivity.class));
                        handleConnectionFailure(null);
                    }
                } else {
                    handleConnectionFailure("Abonelik kontrol edilemedi.");
                }
            }

            @Override
            public void onFailure(Call<List<Subscription>> call, Throwable t) {
                handleConnectionFailure("Ağ Hatası (Abonelik): " + t.getMessage());
            }
        });
    }

    private void fetchVpnConfig(Long deviceId) {
        long serverId = sharedPreferences.getLong(KEY_SELECTED_SERVER_ID, 0);
        if (serverId == 0) {
            Toast.makeText(this, "Lütfen önce bir sunucu seçin.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, ServerSelectionActivity.class));
            handleConnectionFailure(null);
            return;
        }

        ConfigGenerationRequest request = new ConfigGenerationRequest(serverId, deviceId, selectedProtocol);

        apiService.generateConfig(request).enqueue(new Callback<VpnConfigResponse>() {
            @Override
            public void onResponse(Call<VpnConfigResponse> call, Response<VpnConfigResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String configContent = response.body().getConfigurationFileContent();
                    String protocol = response.body().getProtocol();

                    Log.i(TAG, "Config alındı. Protokol: " + protocol);

                    // Protokole göre işlem yap
                    if ("OPENVPN".equalsIgnoreCase(protocol)) {
                        startOpenVpn(configContent);
                    } else if ("IKEV2".equalsIgnoreCase(protocol)) {
                        handleUnsupportedProtocol("IKEv2");
                    } else if ("V2RAY".equalsIgnoreCase(protocol)) {
                        handleUnsupportedProtocol("V2Ray");
                    } else if ("SUPER".equalsIgnoreCase(protocol)) {
                         handleUnsupportedProtocol("Super Protocol");
                    } else {
                        // Bilinmeyen protokol (varsayılan OpenVPN dene)
                        startOpenVpn(configContent);
                    }
                } else {
                    handleConnectionFailure("Konfigürasyon hatası: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<VpnConfigResponse> call, Throwable t) {
                handleConnectionFailure("Ağ Hatası (Config): " + t.getMessage());
            }
        });
    }

    private void startOpenVpn(String configContent) {
        try {
            de.blinkt.openvpn.core.VpnStatus.logMessage(de.blinkt.openvpn.core.VpnStatus.LogLevel.INFO, "VPN", "Starting OpenVPN...");

            java.io.StringReader sr = new java.io.StringReader(configContent);
            de.blinkt.openvpn.core.ConfigParser cp = new de.blinkt.openvpn.core.ConfigParser();
            cp.parseConfig(sr);
            de.blinkt.openvpn.VpnProfile vp = cp.convertProfile();

            String serverName = currentServerInfo.getText().toString().replace("Mevcut Sunucu : ", "");
            vp.mName = serverName;

            de.blinkt.openvpn.core.ProfileManager.setTemporaryProfile(this, vp);
            de.blinkt.openvpn.core.VPNLaunchHelper.startOpenVpn(vp, this);

            simulateConnectionSuccess();
        } catch (Exception e) {
            Log.e(TAG, "OpenVPN Başlatma Hatası", e);
            handleConnectionFailure("OpenVPN hatası: " + e.getMessage());
        }
    }

    private void disconnectVPN() {
        try {
             de.blinkt.openvpn.core.OpenVPNService.abortConnection();
        } catch (Exception e) {
            Log.e(TAG, "VPN Durdurma Hatası", e);
        }

        isConnecting = false;
        isConnected = false;
        stopTimer();
        sharedPreferences.edit().remove(KEY_START_TIME).apply();
        updateUIOnConnectionState();
        Toast.makeText(this, "Bağlantı kesildi.", Toast.LENGTH_SHORT).show();
    }

    // YENİ METOT: Desteklenmeyen protokoller için
    private void handleUnsupportedProtocol(String protocolName) {
        String msg = protocolName + " protokolü bu sürümde desteklenmemektedir.";
        Log.w(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        handleConnectionFailure(null); // UI'ı resetle
    }

    private void handleConnectionFailure(String message) {
        if (message != null) Log.e(TAG, message);
        isConnecting = false;
        isConnected = false;
        updateUIOnConnectionState();
        if (message != null) Toast.makeText(this, "Bağlantı başarısız.", Toast.LENGTH_SHORT).show();
    }

    private void simulateConnectionSuccess() {
        isConnected = true;
        isConnecting = false;
        startTime = System.currentTimeMillis();
        sharedPreferences.edit().putLong(KEY_START_TIME, startTime).apply();
        startTimer();
        updateUIOnConnectionState();
    }

    // UI & Animasyon Yardımcıları
    private void toggleSideMenu() {
        if (sideMenu.getVisibility() == View.GONE) {
            sideMenu.setVisibility(View.VISIBLE);
            dimBackground.setVisibility(View.VISIBLE);
            sideMenu.animate().translationX(0).setDuration(250).start();
        } else {
            closeSideMenu();
        }
    }

    private void closeSideMenu() {
        if (sideMenu.getVisibility() == View.VISIBLE) {
            sideMenu.animate().translationX(-sideMenu.getWidth()).setDuration(250)
                    .withEndAction(() -> sideMenu.setVisibility(View.GONE)).start();
            dimBackground.setVisibility(View.GONE);
        }
    }

    private void setupProtocolButtons() {
        View.OnClickListener listener = v -> {
            resetProtocolButtons();
            v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.purple_500)));

            int id = v.getId();
            if (id == R.id.protocolAuto) setProtocolSelection(VpnProtocol.OPENVPN); // Auto defaults to OpenVPN
            else if (id == R.id.protocolIKEv2) setProtocolSelection(VpnProtocol.IKEV2);
            else if (id == R.id.protocolSuper) setProtocolSelection(VpnProtocol.SUPER);
            else if (id == R.id.protocolOpenVPN) setProtocolSelection(VpnProtocol.OPENVPN);
            // V2Ray için buton XML'de yoksa buraya eklenmeli
        };

        protocolAuto.setOnClickListener(listener);
        protocolIKEv2.setOnClickListener(listener);
        protocolSuper.setOnClickListener(listener);
        protocolOpenVPN.setOnClickListener(listener);
    }

    private void resetProtocolButtons() {
        int color = ContextCompat.getColor(this, R.color.gray_dark);
        protocolAuto.setBackgroundTintList(ColorStateList.valueOf(color));
        protocolIKEv2.setBackgroundTintList(ColorStateList.valueOf(color));
        protocolSuper.setBackgroundTintList(ColorStateList.valueOf(color));
        protocolOpenVPN.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void setProtocolSelection(VpnProtocol protocol) {
        this.selectedProtocol = protocol;
        String name = protocol.name();
        if (protocol == VpnProtocol.OPENVPN && protocolAuto.getBackgroundTintList().getDefaultColor() == ContextCompat.getColor(this, R.color.purple_500)) {
            name = "Otomatik";
        }
        currentProtocolInfo.setText("Protokol : " + name);
    }

    private void setupMenuNavigation() {
        menuAccount.setOnClickListener(v -> { startActivity(new Intent(this, AccountActivity.class)); closeSideMenu(); });
        menuSupport.setOnClickListener(v -> { startActivity(new Intent(this, HelpSupportActivity.class)); closeSideMenu(); });
        menuAlwaysOn.setOnClickListener(v -> { startActivity(new Intent(this, AlwaysOnVpnActivity.class)); closeSideMenu(); });
        menuAbout.setOnClickListener(v -> { startActivity(new Intent(this, AboutActivity.class)); closeSideMenu(); });
    }

    private void startTimer() {
        stopTimer();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                connectionTimeText.setText(String.format("%02d:%02d", elapsed / 60, elapsed % 60));
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
            statusConnectedText.setTextColor(Color.parseColor("#00BFFF"));
            loadingSpinner.setVisibility(View.VISIBLE);
            connectButton.setBackgroundResource(R.drawable.btn_round);
            connectionStatusLabel.setText("BAĞLANIYOR");
        } else if (isConnected) {
            statusConnectedText.setText("Bağlandı");
            statusConnectedText.setTextColor(Color.GREEN);
            loadingSpinner.setVisibility(View.GONE);
            connectButton.setBackgroundResource(R.drawable.btn_round_connected);
            connectionStatusLabel.setText("BAĞLANTIYI KES");
            statusSafeText.setText(currentServerInfo.getText().toString().replace("Mevcut Sunucu : ", ""));
        } else {
            statusConnectedText.setText("Bağlantı Yok");
            statusConnectedText.setTextColor(Color.WHITE);
            loadingSpinner.setVisibility(View.GONE);
            connectButton.setBackgroundResource(R.drawable.btn_round);
            connectionStatusLabel.setText("BAĞLAN");
            statusSafeText.setText("Bağlanmak için dokunun");
            connectionTimeText.setText("00:00");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String savedServerName = sharedPreferences.getString(KEY_SELECTED_SERVER_NAME, "Sunucu Seçin");
        long savedServerId = sharedPreferences.getLong(KEY_SELECTED_SERVER_ID, 0);

        if (savedServerId != 0) currentServerInfo.setText("Mevcut Sunucu : " + savedServerName);
        else currentServerInfo.setText("Mevcut Sunucu : Sunucu Seçin");

        if (isConnected) {
            startTime = sharedPreferences.getLong(KEY_START_TIME, 0);
            if (startTime > 0) startTimer();
            else disconnectVPN();
        }
        updateUIOnConnectionState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}
