package com.abacicelal.supervpn_project;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.abacicelal.supervpn_project.remote.ApiService;
import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.ApiResponse;
import com.abacicelal.supervpn_project.remote.model.ConfigGenerationRequest;
import com.abacicelal.supervpn_project.remote.model.Device;
import com.abacicelal.supervpn_project.remote.model.DeviceRequest;
import com.abacicelal.supervpn_project.remote.model.Subscription;
import com.abacicelal.supervpn_project.remote.model.VpnConfigResponse;
import com.abacicelal.supervpn_project.remote.model.VpnProtocol;
import com.abacicelal.supervpn_project.utils.DeviceIdManager;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// --- GÖMÜLÜ OPENVPN KÜTÜPHANELERİ (YENİ) ---
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;
import de.blinkt.openvpn.core.ConnectionStatus;

// VpnStatus.StateListener ekledik. Artık durumları buradan dinleyeceğiz.
public class MainActivity extends AppCompatActivity implements VpnStatus.StateListener {

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

        // Kullanıcı abonelik durumunu kontrol et ve UI güncelle
        checkSubscriptionStatusForUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Gömülü motorun durum dinleyicisini ekle
        VpnStatus.addStateListener(this);

        String savedServerName = sharedPreferences.getString(KEY_SELECTED_SERVER_NAME, getString(R.string.server_select));
        long savedServerId = sharedPreferences.getLong(KEY_SELECTED_SERVER_ID, 0);

        if (savedServerId != 0) currentServerInfo.setText(String.format(getString(R.string.current_server), savedServerName));
        else currentServerInfo.setText(String.format(getString(R.string.current_server), getString(R.string.server_select)));

        // Eğer uygulama kapalıyken VPN çalışmaya devam ettiyse süreyi düzelt
        if (isConnected) {
            startTime = sharedPreferences.getLong(KEY_START_TIME, 0);
            if (startTime > 0) startTimer();
        }

        updateUIOnConnectionState();
        checkSubscriptionStatusForUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pil tasarrufu için dinleyiciyi kaldır
        VpnStatus.removeStateListener(this);
    }

    // --- GÖMÜLÜ MOTOR DURUM DİNLEYİCİSİ ---
    @Override
    public void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level, Intent intent) {
        runOnUiThread(() -> {
            Log.d(TAG, "VPN Durumu: " + state + " (" + level + ")");

            if (level == ConnectionStatus.LEVEL_CONNECTED) {
                // BAĞLANDI
                if (!isConnected) {
                    isConnected = true;
                    isConnecting = false;
                    startTime = System.currentTimeMillis();
                    sharedPreferences.edit().putLong(KEY_START_TIME, startTime).apply();
                    startTimer();
                    updateUIOnConnectionState();
                }
            } else if (level == ConnectionStatus.LEVEL_NOTCONNECTED || level == ConnectionStatus.LEVEL_AUTH_FAILED || level == ConnectionStatus.LEVEL_NONETWORK) {
                // BAĞLANTI KOPTU / HATA
                if (isConnected || isConnecting) {
                    isConnected = false;
                    isConnecting = false;
                    stopTimer();
                    updateUIOnConnectionState();

                    if (level == ConnectionStatus.LEVEL_AUTH_FAILED) {
                        Toast.makeText(this, "Kimlik doğrulama hatası!", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (level == ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED || level == ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET || level == ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT) {
                // BAĞLANIYOR
                isConnecting = true;
                updateUIOnConnectionState();
            }
        });
    }

    @Override
    public void setConnectedVPN(String uuid) {
        // Gerekli değil
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
                Toast.makeText(this, getString(R.string.browser_open_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleConnectButtonClick() {
        Animation bounceFadeAnim = AnimationUtils.loadAnimation(this, R.anim.bounce_fade_set);
        connectButton.startAnimation(bounceFadeAnim);

        if (!isConnected && !isConnecting) {
            startVPNConnection();
        } else if (isConnected || isConnecting) {
            disconnectVPN();
        }
    }

    private void startVPNConnection() {
        Log.d(TAG, "VPN Bağlantısı Başlatılıyor...");
        isConnecting = true;
        updateUIOnConnectionState();
        fetchDeviceAndCheckSubscription();
    }

    private void fetchDeviceAndCheckSubscription() {
        if (RetrofitClient.getToken(this) == null) {
            Long registeredId = DeviceIdManager.getRegisteredDeviceId(this);
            if (registeredId != null) {
                checkSubscription(registeredId);
            } else {
                registerDevice();
            }
            return;
        }

        apiService.getMyDevices().enqueue(new Callback<ApiResponse<List<Device>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Device>>> call, Response<ApiResponse<List<Device>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Device> devices = response.body().getData();
                    if (devices == null || devices.isEmpty()) {
                        registerDevice();
                    } else {
                        checkSubscription(devices.get(0).getId());
                    }
                } else {
                    handleConnectionFailure(getString(R.string.error_device_info) + " Kod: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Device>>> call, Throwable t) {
                handleConnectionFailure(String.format(getString(R.string.network_error_device), t.getMessage()));
            }
        });
    }

    private void registerDevice() {
        String deviceName = "Android " + android.os.Build.MODEL;
        apiService.registerDevice(new DeviceRequest(deviceName)).enqueue(new Callback<ApiResponse<Device>>() {
            @Override
            public void onResponse(Call<ApiResponse<Device>> call, Response<ApiResponse<Device>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    checkSubscription(response.body().getData().getId());
                } else {
                    handleConnectionFailure(getString(R.string.device_registration_failed));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Device>> call, Throwable t) {
                handleConnectionFailure(String.format(getString(R.string.network_error_register), t.getMessage()));
            }
        });
    }

    private void checkSubscriptionStatusForUI() {
        if (RetrofitClient.getToken(this) == null) {
            if (menuAccount != null) menuAccount.setText("Misafir");
            if (premiumButton != null) premiumButton.setVisibility(View.VISIBLE);
            if (upgradePremiumButton != null) upgradePremiumButton.setVisibility(View.VISIBLE);
            return;
        }

        apiService.getMySubscriptions().enqueue(new Callback<ApiResponse<List<Subscription>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Subscription>>> call, Response<ApiResponse<List<Subscription>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Subscription> subs = response.body().getData();
                    boolean hasActive = false;
                    if (subs != null) {
                        for (Subscription s : subs) {
                            if (s.isActive()) {
                                hasActive = true;
                                break;
                            }
                        }
                    }

                    if (hasActive) {
                        if (premiumButton != null) premiumButton.setVisibility(View.GONE);
                        if (upgradePremiumButton != null) upgradePremiumButton.setVisibility(View.GONE);
                        if (menuAccount != null) menuAccount.setText(getString(R.string.account_title) + " (Premium)");
                    } else {
                        if (premiumButton != null) premiumButton.setVisibility(View.VISIBLE);
                        if (upgradePremiumButton != null) upgradePremiumButton.setVisibility(View.VISIBLE);
                        if (menuAccount != null) menuAccount.setText(getString(R.string.account_title) + " (Free)");
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Subscription>>> call, Throwable t) { }
        });
    }

    private void checkSubscription(Long deviceId) {
        apiService.getMySubscriptions().enqueue(new Callback<ApiResponse<List<Subscription>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Subscription>>> call, Response<ApiResponse<List<Subscription>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean hasActive = false;
                    List<Subscription> subs = response.body().getData();
                    if (subs != null) {
                        for (Subscription s : subs) {
                            if (s.isActive()) {
                                hasActive = true;
                                break;
                            }
                        }
                    }
                    if (hasActive) {
                        if (premiumButton != null) premiumButton.setVisibility(View.GONE);
                        if (upgradePremiumButton != null) upgradePremiumButton.setVisibility(View.GONE);
                        fetchVpnConfig(deviceId);
                    } else {
                        // Free/Guest User - Allow connection but keep premium buttons
                        if (premiumButton != null) premiumButton.setVisibility(View.VISIBLE);
                        if (upgradePremiumButton != null) upgradePremiumButton.setVisibility(View.VISIBLE);

                        // Misafir ve Free kullanıcılar bağlanabilir
                        fetchVpnConfig(deviceId);
                    }
                } else {
                    handleConnectionFailure(getString(R.string.error_subscription_check));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Subscription>>> call, Throwable t) {
                handleConnectionFailure(String.format(getString(R.string.network_error_sub), t.getMessage()));
            }
        });
    }

    private void fetchVpnConfig(Long deviceId) {
        long serverId = sharedPreferences.getLong(KEY_SELECTED_SERVER_ID, 0);
        if (serverId == 0) {
            Toast.makeText(this, getString(R.string.server_select_first), Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, ServerSelectionActivity.class));
            handleConnectionFailure(null);
            return;
        }

        ConfigGenerationRequest request = new ConfigGenerationRequest(serverId, deviceId, selectedProtocol);

        apiService.generateConfig(request).enqueue(new Callback<ApiResponse<VpnConfigResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<VpnConfigResponse>> call, Response<ApiResponse<VpnConfigResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    VpnConfigResponse configResponse = response.body().getData();
                    String configContent = configResponse.getConfigurationFileContent();
                    String protocol = configResponse.getProtocol();

                    Log.i(TAG, "Config alındı. Protokol: " + protocol);

                    if ("OPENVPN".equalsIgnoreCase(protocol)) {
                        startOpenVpn(configContent);
                    } else {
                        // V2RAY, IKEv2 şimdilik desteklenmiyor veya aynı şekilde işlem görüyor
                        // Test için OpenVPN'e zorluyoruz
                        startOpenVpn(configContent);
                    }
                } else {
                    handleConnectionFailure(String.format(getString(R.string.config_error), response.code()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<VpnConfigResponse>> call, Throwable t) {
                handleConnectionFailure(String.format(getString(R.string.network_error_config), t.getMessage()));
            }
        });
    }

    // --- YENİ BAŞLATMA METODU (GÖMÜLÜ MOTOR) ---
    private void startOpenVpn(String configContent) {
        ConfigParser cp = new ConfigParser();
        try {
            cp.parseConfig(new StringReader(configContent));
            VpnProfile vp = cp.convertProfile();

            // Config dosyasını profilin içine gömüyoruz
            vp.mInlineConfig = configContent;
            vp.mName = "SuperVPN Connect";

            // Geçici profil olarak ayarla
            ProfileManager.setTemporaryProfile(this, vp);

            // Gömülü servisi başlat
            Intent intent = new Intent(this, OpenVPNService.class);
            intent.setAction(OpenVPNService.START_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }

        } catch (IOException | ConfigParser.ConfigParseError e) {
            Log.e(TAG, "Config Parse Error", e);
            handleConnectionFailure("Config hatası: " + e.getLocalizedMessage());
        }
    }

    private void disconnectVPN() {
        // Gömülü servisi durdur
        Intent intent = new Intent(this, OpenVPNService.class);
        intent.setAction(OpenVPNService.DISCONNECT_VPN);
        startService(intent);

        isConnecting = false;
        isConnected = false;
        stopTimer();
        sharedPreferences.edit().remove(KEY_START_TIME).apply();
        updateUIOnConnectionState();
        Toast.makeText(this, getString(R.string.vpn_disconnected), Toast.LENGTH_SHORT).show();
    }

    private void handleConnectionFailure(String message) {
        if (message != null) Log.e(TAG, message);
        isConnecting = false;
        isConnected = false;
        updateUIOnConnectionState();
        if (message != null) Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
            if (id == R.id.protocolAuto) setProtocolSelection(VpnProtocol.OPENVPN);
            else if (id == R.id.protocolIKEv2) setProtocolSelection(VpnProtocol.IKEV2);
            else if (id == R.id.protocolSuper) setProtocolSelection(VpnProtocol.SUPER);
            else if (id == R.id.protocolOpenVPN) setProtocolSelection(VpnProtocol.OPENVPN);
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
            statusConnectedText.setText(getString(R.string.status_connecting));
            statusConnectedText.setTextColor(Color.parseColor("#00BFFF"));
            loadingSpinner.setVisibility(View.VISIBLE);
            connectButton.setBackgroundResource(R.drawable.btn_round);
            connectionStatusLabel.setText(getString(R.string.status_label_connecting));
        } else if (isConnected) {
            statusConnectedText.setText(getString(R.string.status_connected));
            statusConnectedText.setTextColor(Color.GREEN);
            loadingSpinner.setVisibility(View.GONE);
            connectButton.setBackgroundResource(R.drawable.btn_round_connected);
            connectionStatusLabel.setText(getString(R.string.status_label_connected));
            String serverName = sharedPreferences.getString(KEY_SELECTED_SERVER_NAME, "VPN");
            statusSafeText.setText(serverName);
        } else {
            statusConnectedText.setText(getString(R.string.status_disconnected));
            statusConnectedText.setTextColor(Color.WHITE);
            loadingSpinner.setVisibility(View.GONE);
            connectButton.setBackgroundResource(R.drawable.btn_round);
            connectionStatusLabel.setText(getString(R.string.status_label_disconnected));
            statusSafeText.setText(getString(R.string.tap_to_connect));
            connectionTimeText.setText("00:00");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}