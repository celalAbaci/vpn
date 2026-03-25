package com.abacicelal.supervpn_project;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.provider.Settings;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.abacicelal.supervpn_project.BuildConfig;
import com.abacicelal.supervpn_project.util.LocaleManager;

import com.abacicelal.supervpn_project.remote.ApiService;
import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.ApiResponse;
import com.abacicelal.supervpn_project.remote.model.AuthResponse;
import com.abacicelal.supervpn_project.remote.model.ErrorResponse;
import com.abacicelal.supervpn_project.remote.model.ConfigGenerationRequest;
import com.abacicelal.supervpn_project.remote.model.ConnectionLog;
import com.abacicelal.supervpn_project.remote.model.CreateLogRequest;
import com.abacicelal.supervpn_project.remote.model.Device;
import com.abacicelal.supervpn_project.remote.model.DeviceRequest;
import com.abacicelal.supervpn_project.remote.model.GuestAuthRequest;
import com.abacicelal.supervpn_project.remote.model.Subscription;
import com.abacicelal.supervpn_project.remote.model.VpnConfigResponse;
import com.abacicelal.supervpn_project.remote.model.VpnProtocol;
import com.abacicelal.supervpn_project.utils.DeviceIdManager;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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

import com.abacicelal.supervpn_project.core.StealthVpnService;
import com.abacicelal.supervpn_project.core.XrayVpnService;

// VpnStatus.StateListener ekledik. Artık durumları buradan dinleyeceğiz.
public class MainActivity extends BaseActivity implements VpnStatus.StateListener {

    private static final String TAG = "MainActivity";
    public static boolean isConnected = false;
    private boolean isConnecting = false;

    // UI Bileşenleri
    private ImageButton connectButton;
    private ImageButton menuButton;
    private ImageButton premiumButton;
    private ImageButton browserButton;
    private ImageButton notificationButton;
    private View notificationBadge;
    private ImageButton helpButton;
    private TextView languageButton;
    private Button protocolAuto, protocolIKEv2, protocolSuper, protocolOpenVPN;
    private ProgressBar loadingSpinner;
    private LinearLayout sideMenu;
    private SwitchCompat notificationSwitch;
    private TextView notificationStatusText;
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
    private static final int NOTIF_PERMISSION_REQUEST_CODE = 200;
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_SELECTED_SERVER_NAME = "selected_server_name";
    private static final String KEY_SELECTED_SERVER_ID = "selected_server_id";
    private static final String KEY_PENDING_CONNECT_TIME = "pending_connect_time";
    private static final String KEY_PENDING_SERVER_ID = "pending_server_id";
    private static final String KEY_PENDING_DEVICE_ID = "pending_device_id";
    private static final String KEY_PENDING_VPN_IP = "pending_vpn_ip";
    private static final String KEY_PENDING_LOG_ID = "pending_log_id";
    private static final String KEY_PENDING_BYTES_START = "pending_bytes_start";

    // Retrofit ApiService
    private ApiService apiService;

    // Seçilen protokol
    private VpnProtocol selectedProtocol = VpnProtocol.AUTO;

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
        setProtocolSelection(VpnProtocol.AUTO);

        // Önceki oturumdan kalan gönderilmemiş log varsa gönder
        sendPendingLog();
        // Not: checkSubscriptionStatusForUI() onResume()'da çağrılıyor, burada çağrılmasına gerek yok
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Gömülü motorun durum dinleyicisini ekle
        VpnStatus.addStateListener(this);

        // Stealth VPN durum dinleyicisini ekle
        IntentFilter filter = new IntentFilter(StealthVpnService.BROADCAST_VPN_STATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(stealthStateReceiver, filter);

        // Switch durumunu geri yükle (listener tetiklenmeden)
        notificationSwitch.setOnCheckedChangeListener(null);
        boolean notifEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
        notificationSwitch.setChecked(notifEnabled);
        updateNotificationSwitchText(notifEnabled);
        notificationSwitch.setOnCheckedChangeListener((btn, checked) -> onNotificationSwitchChanged());

        String savedServerName = sharedPreferences.getString(KEY_SELECTED_SERVER_NAME, getString(R.string.server_select));
        long savedServerId = sharedPreferences.getLong(KEY_SELECTED_SERVER_ID, 0);

        if (savedServerId != 0) currentServerInfo.setText(String.format(getString(R.string.current_server), savedServerName));
        else currentServerInfo.setText(String.format(getString(R.string.current_server), getString(R.string.server_select)));

        // Servis arka planda öldüyse ama UI "bağlı" gösteriyorsa düzelt
        boolean vpnIsRunning = getSharedPreferences("vpn_prefs", MODE_PRIVATE)
                .getBoolean("vpn_is_running", false);
        if (!vpnIsRunning && (isConnected || isConnecting)) {
            if (isConnected) sendDisconnectUpdate(); // OpenVPN/arka plan kesilme fallback
            isConnected = false;
            isConnecting = false;
            sharedPreferences.edit().remove(KEY_START_TIME).apply();
            stopTimer();
        }

        // Eğer uygulama kapalıyken VPN çalışmaya devam ettiyse süreyi düzelt
        if (isConnected) {
            startTime = sharedPreferences.getLong(KEY_START_TIME, 0);
            if (startTime > 0) startTimer();
        }

        updateUIOnConnectionState();
        checkSubscriptionStatusForUI();
        updateLanguageButtonFlag();
        checkAnnouncementBadge();
    }

    private void checkAnnouncementBadge() {
        if (RetrofitClient.getToken(this) == null) return;
        ApiService api = RetrofitClient.getApiService(this);
        api.getAnnouncements(java.util.Locale.getDefault().getLanguage()).enqueue(new retrofit2.Callback<ApiResponse<java.util.List<com.abacicelal.supervpn_project.remote.model.Announcement>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<java.util.List<com.abacicelal.supervpn_project.remote.model.Announcement>>> call,
                                   retrofit2.Response<ApiResponse<java.util.List<com.abacicelal.supervpn_project.remote.model.Announcement>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    java.util.List<com.abacicelal.supervpn_project.remote.model.Announcement> list = response.body().getData();
                    if (!list.isEmpty()) {
                        long lastSeen = getSharedPreferences("vpn_prefs", MODE_PRIVATE)
                                .getLong("last_seen_announcement_time", 0);
                        // Son görülme zamanından en az 1 duyuru varsa rozet göster
                        notificationBadge.setVisibility(lastSeen == 0 ? View.VISIBLE :
                                (list.size() > 0 ? View.VISIBLE : View.GONE));
                    } else {
                        notificationBadge.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onFailure(retrofit2.Call<ApiResponse<java.util.List<com.abacicelal.supervpn_project.remote.model.Announcement>>> call, Throwable t) {}
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pil tasarrufu için dinleyiciyi kaldır
        VpnStatus.removeStateListener(this);

        // Stealth VPN dinleyicisini kaldır
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(stealthStateReceiver);
        } catch (Exception e) {
            Log.w(TAG, "Failed to unregister stealth receiver", e);
        }
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

                    sendConnectLog();
                }
            } else if (level == ConnectionStatus.LEVEL_NOTCONNECTED || level == ConnectionStatus.LEVEL_AUTH_FAILED || level == ConnectionStatus.LEVEL_NONETWORK) {
                // BAĞLANTI KOPTU / HATA
                if (isConnected || isConnecting) {
                    if (isConnected) sendDisconnectUpdate();
                    isConnected = false;
                    isConnecting = false;
                    stopTimer();
                    sharedPreferences.edit().remove(KEY_START_TIME).apply();
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
        notificationButton = findViewById(R.id.notificationButton);
        notificationBadge = findViewById(R.id.notificationBadge);
        helpButton = findViewById(R.id.helpButton);
        languageButton = findViewById(R.id.languageButton);
        sideMenu = findViewById(R.id.sideMenu);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        notificationStatusText = findViewById(R.id.notificationStatusText);
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
        setupNotificationSwitch();

        serverSelectionLayout.setOnClickListener(v -> startActivity(new Intent(this, ServerSelectionActivity.class)));
        notificationButton.setOnClickListener(v -> startActivity(new Intent(this, AnnouncementsActivity.class)));
        premiumButton.setOnClickListener(v -> startActivity(new Intent(this, PremiumActivity.class)));
        upgradePremiumButton.setOnClickListener(v -> startActivity(new Intent(this, PremiumActivity.class)));
        helpButton.setOnClickListener(v -> startActivity(new Intent(this, HelpSupportActivity.class)));
        languageButton.setOnClickListener(v -> showLanguagePickerDialog());

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
            // isConnecting'i HEMEN set et — çift tıklamada ikinci isteği engeller
            isConnecting = true;
            updateUIOnConnectionState();
            startVPNConnection();
        } else if (isConnected || isConnecting) {
            disconnectVPN();
        }
    }

    private void startVPNConnection() {
        long serverId = sharedPreferences.getLong(KEY_SELECTED_SERVER_ID, 0);
        if (serverId == 0) {
            Toast.makeText(this, getString(R.string.server_select_first), Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, ServerSelectionActivity.class));
            isConnecting = false;
            updateUIOnConnectionState();
            return;
        }
        // Protokolü şimdi snapshot'la — async zinciri sırasında değişirse etkilenmesin
        if (selectedProtocol == VpnProtocol.AUTO) {
            connectingProtocol = resolveAutoProtocol();
            Log.d(TAG, "Otomatik protokol çözümlendi: " + connectingProtocol);
        } else {
            connectingProtocol = selectedProtocol;
        }
        // Yeni girişim ID'si — önceki stale yanıtları geçersiz kılar
        final int attemptId = ++connectionAttemptId;
        Log.d(TAG, "VPN Bağlantısı Başlatılıyor... (attemptId=" + attemptId + ", protokol=" + connectingProtocol + ")");
        fetchDeviceAndCheckSubscription(attemptId);
    }

    private void fetchDeviceAndCheckSubscription(int attemptId) {
        if (RetrofitClient.getToken(this) == null) {
            doGuestAuthentication(attemptId);
            return;
        }

        apiService.getMyDevices().enqueue(new Callback<ApiResponse<List<Device>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Device>>> call, Response<ApiResponse<List<Device>>> response) {
                if (attemptId != connectionAttemptId) return; // stale yanıt, yoksay
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Device> devices = response.body().getData();
                    if (devices == null || devices.isEmpty()) {
                        registerDevice(attemptId);
                    } else {
                        Long deviceId = devices.get(0).getId();
                        DeviceIdManager.saveRegisteredDeviceId(MainActivity.this, deviceId);
                        checkSubscription(deviceId, attemptId);
                    }
                } else {
                    handleConnectionFailure(getString(R.string.error_device_info) + " Kod: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Device>>> call, Throwable t) {
                if (attemptId != connectionAttemptId) return;
                handleConnectionFailure(String.format(getString(R.string.network_error_device), t.getMessage()));
            }
        });
    }

    private void doGuestAuthentication(int attemptId) {
        Log.d(TAG, "Token yok, otomatik misafir girisi baslatiliyor...");
        String deviceUUID = DeviceIdManager.getDeviceId(this);
        String deviceName = "Android " + android.os.Build.MODEL;
        GuestAuthRequest guestRequest = new GuestAuthRequest(deviceUUID, deviceName);

        apiService.loginGuest(guestRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (attemptId != connectionAttemptId) return;
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    RetrofitClient.saveToken(MainActivity.this, auth.getAccessToken(), auth.getRefreshToken());
                    Log.d(TAG, "Misafir girisi basarili, akis yeniden baslatiliyor.");
                    fetchDeviceAndCheckSubscription(attemptId);
                } else {
                    handleConnectionFailure("Misafir girisi basarisiz. Kod: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (attemptId != connectionAttemptId) return;
                handleConnectionFailure("Ag hatasi: " + t.getMessage());
            }
        });
    }

    private void registerDevice(int attemptId) {
        String deviceName = "Android " + android.os.Build.MODEL;
        String deviceIdentifier = DeviceIdManager.getDeviceId(this);
        String osVersion = "Android " + android.os.Build.VERSION.RELEASE;
        String appVersion = BuildConfig.VERSION_NAME;
        apiService.registerDevice(new DeviceRequest(deviceName, deviceIdentifier, osVersion, appVersion)).enqueue(new Callback<ApiResponse<Device>>() {
            @Override
            public void onResponse(Call<ApiResponse<Device>> call, Response<ApiResponse<Device>> response) {
                if (attemptId != connectionAttemptId) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Long deviceId = response.body().getData().getId();
                    DeviceIdManager.saveRegisteredDeviceId(MainActivity.this, deviceId);
                    checkSubscription(deviceId, attemptId);
                } else {
                    handleConnectionFailure(getString(R.string.device_registration_failed));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Device>> call, Throwable t) {
                if (attemptId != connectionAttemptId) return;
                handleConnectionFailure(String.format(getString(R.string.network_error_register), t.getMessage()));
            }
        });
    }

    private void checkSubscriptionStatusForUI() {
        if (RetrofitClient.getToken(this) == null) {
            if (menuAccount != null) menuAccount.setText(getString(R.string.account_title));
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
                        if (menuAccount != null) menuAccount.setText(getString(R.string.account_title));
                    } else {
                        if (premiumButton != null) premiumButton.setVisibility(View.VISIBLE);
                        if (upgradePremiumButton != null) upgradePremiumButton.setVisibility(View.VISIBLE);
                        if (menuAccount != null) menuAccount.setText(getString(R.string.account_title));
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Subscription>>> call, Throwable t) { }
        });
    }

    private void checkSubscription(Long deviceId, int attemptId) {
        boolean isFreeServer = sharedPreferences.getBoolean("selected_server_is_free", false);
        if (isFreeServer) {
            Log.d(TAG, "Free sunucu secili, abonelik kontrolu atlanıyor.");
            fetchVpnConfig(deviceId, attemptId);
            return;
        }

        apiService.getMySubscriptions().enqueue(new Callback<ApiResponse<List<Subscription>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Subscription>>> call, Response<ApiResponse<List<Subscription>>> response) {
                if (attemptId != connectionAttemptId) return;
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
                        fetchVpnConfig(deviceId, attemptId);
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.error_active_subscription), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, PremiumActivity.class));
                        handleConnectionFailure(null);
                    }
                } else {
                    handleConnectionFailure(getString(R.string.error_subscription_check));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Subscription>>> call, Throwable t) {
                if (attemptId != connectionAttemptId) return;
                handleConnectionFailure(String.format(getString(R.string.network_error_sub), t.getMessage()));
            }
        });
    }

    private void fetchVpnConfig(Long deviceId, int attemptId) {
        long serverId = sharedPreferences.getLong(KEY_SELECTED_SERVER_ID, 0);
        if (serverId == 0) {
            Toast.makeText(this, getString(R.string.server_select_first), Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, ServerSelectionActivity.class));
            handleConnectionFailure(null);
            return;
        }

        // connectingProtocol: bağlantı başladığında snapshot'lanan protokol
        ConfigGenerationRequest request = new ConfigGenerationRequest(serverId, deviceId, connectingProtocol);

        apiService.generateConfig(request).enqueue(new Callback<ApiResponse<VpnConfigResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<VpnConfigResponse>> call, Response<ApiResponse<VpnConfigResponse>> response) {
                if (attemptId != connectionAttemptId) {
                    Log.w(TAG, "Stale config yanıtı yoksayıldı (attemptId=" + attemptId + ")");
                    return;
                }
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    VpnConfigResponse configResponse = response.body().getData();
                    String configContent = configResponse.getConfigurationFileContent();
                    String protocol = configResponse.getProtocol();

                    Log.i(TAG, "Config alındı. Protokol: " + protocol);

                    if ("OPENVPN".equalsIgnoreCase(protocol)) {
                        startOpenVpn(configContent);
                    } else if ("V2RAY".equalsIgnoreCase(protocol) || "SUPER".equalsIgnoreCase(protocol)) {
                        startStealth(configContent);
                    } else if ("XRAY".equalsIgnoreCase(protocol)) {
                        startXray(configContent);
                    } else {
                        handleConnectionFailure("Desteklenmeyen protokol: " + protocol);
                    }
                } else {
                    String errMsg = null;
                    if (response.errorBody() != null) {
                        try {
                            String bodyStr = response.errorBody().string();
                            ErrorResponse err = new Gson().fromJson(bodyStr, ErrorResponse.class);
                            if (err != null) {
                                errMsg = (err.getMessage() != null && !err.getMessage().isEmpty())
                                        ? err.getMessage() : err.getError();
                                if (errMsg != null) Log.e(TAG, "Config API hatası: " + errMsg);
                            }
                        } catch (IOException e) {
                            Log.w(TAG, "ErrorBody parse edilemedi", e);
                        }
                    }
                    handleConnectionFailure(errMsg != null ? errMsg :
                            String.format(getString(R.string.config_error), response.code()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<VpnConfigResponse>> call, Throwable t) {
                if (attemptId != connectionAttemptId) return;
                String msg = formatNetworkError(t);
                Log.e(TAG, "Config API onFailure: " + msg, t);
                handleConnectionFailure(String.format(getString(R.string.network_error_config), msg));
            }
        });
    }

    private VpnProfile currentPendingProfile;
    private static final int VPN_REQUEST_CODE = 99;
    private static final int VPN_REQUEST_CODE_STEALTH = 100;
    private static final int VPN_REQUEST_CODE_XRAY = 101;

    private String pendingStealthConfig;
    private String pendingXrayConfig;
    private VpnProtocol currentRunningProtocol = null;

    // Her bağlantı girişimine benzersiz ID verilir.
    // Eski (iptal edilmiş) API yanıtları bu ID eşleşmezse görmezden gelinir.
    private int connectionAttemptId = 0;
    // Bağlantı başladığında seçili protokolün anlık kopyası (sonradan değişirse etkilenmez)
    private VpnProtocol connectingProtocol = null;

    private BroadcastReceiver stealthStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra(StealthVpnService.EXTRA_STATE);
            String errorMessage = intent.getStringExtra(StealthVpnService.EXTRA_ERROR_MESSAGE);

            Log.d(TAG, "Received Stealth state: " + state);

            if (StealthVpnService.STATE_CONNECTING.equals(state)) {
                isConnecting = true;
                isConnected = false;
                updateUIOnConnectionState();
            } else if (StealthVpnService.STATE_CONNECTED.equals(state)) {
                if (!isConnected) {
                    isConnected = true;
                    isConnecting = false;
                    startTime = System.currentTimeMillis();
                    sharedPreferences.edit().putLong(KEY_START_TIME, startTime).apply();
                    startTimer();
                    updateUIOnConnectionState();
                    sendConnectLog();
                }
            } else if (StealthVpnService.STATE_DISCONNECTED.equals(state)) {
                if (isConnected || isConnecting) {
                    if (isConnected) sendDisconnectUpdate();
                    isConnected = false;
                    isConnecting = false;
                    stopTimer();
                    sharedPreferences.edit().remove(KEY_START_TIME).apply();
                    updateUIOnConnectionState();
                }
            } else if (StealthVpnService.STATE_ERROR.equals(state)) {
                isConnecting = false;
                isConnected = false;
                updateUIOnConnectionState();
                if (errorMessage != null) {
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

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
            currentPendingProfile = vp;

            // VPN izni iste (Android'in kendi VpnService sınıfından)
            Intent vpnIntent = android.net.VpnService.prepare(this);
            if (vpnIntent != null) {
                startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
            } else {
                onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
            }

        } catch (IOException | ConfigParser.ConfigParseError e) {
            Log.e(TAG, "Config Parse Error", e);
            handleConnectionFailure("Config hatası: " + e.getLocalizedMessage());
        }
    }

    private void startStealth(String configJson) {
        pendingStealthConfig = configJson;
        Intent vpnIntent = android.net.VpnService.prepare(this);
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE_STEALTH);
        } else {
            onActivityResult(VPN_REQUEST_CODE_STEALTH, RESULT_OK, null);
        }
    }

    private void startXray(String configJson) {
        pendingXrayConfig = configJson;
        Intent vpnIntent = android.net.VpnService.prepare(this);
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE_XRAY);
        } else {
            onActivityResult(VPN_REQUEST_CODE_XRAY, RESULT_OK, null);
        }
    }

    private void launchStealthService(String configJson) {
        currentRunningProtocol = connectingProtocol; // selectedProtocol değil — AUTO çözümlenmiş olabilir
        String srvName = sharedPreferences.getString(KEY_SELECTED_SERVER_NAME, "VPN Sunucusu");
        String protoName = connectingProtocol != null ? connectingProtocol.name() : "V2Ray";
        Intent intent = new Intent(this, StealthVpnService.class);
        intent.setAction(StealthVpnService.ACTION_START);
        intent.putExtra(StealthVpnService.EXTRA_CONFIG_JSON, configJson);
        intent.putExtra(StealthVpnService.EXTRA_SERVER_NAME, srvName);
        intent.putExtra(StealthVpnService.EXTRA_PROTOCOL_NAME, protoName);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            Log.i(TAG, "Stealth service started, waiting for connection state from broadcast...");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start Stealth service", e);
            handleConnectionFailure("Stealth servis başlatılamadı: " + e.getMessage());
        }
        // Durum yönetimi broadcast receiver'dan gelecek, burada set etme
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (currentPendingProfile != null) {
                    currentRunningProtocol = VpnProtocol.OPENVPN;
                    String srvName = sharedPreferences.getString(KEY_SELECTED_SERVER_NAME, "VPN Sunucusu");
                    String protoName = connectingProtocol != null ? connectingProtocol.name() : "OpenVPN";
                    Intent intent = new Intent(this, OpenVPNService.class);
                    intent.setAction(OpenVPNService.START_SERVICE);
                    intent.putExtra("de.blinkt.openvpn.profileUUID", currentPendingProfile.getUUIDString());
                    intent.putExtra(OpenVPNService.EXTRA_SERVER_NAME, srvName);
                    intent.putExtra(OpenVPNService.EXTRA_PROTOCOL_NAME, protoName);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent);
                    } else {
                        startService(intent);
                    }
                    currentPendingProfile = null;
                }
            } else {
                handleConnectionFailure("VPN İzni verilmedi veya iptal edildi.");
            }
        } else if (requestCode == VPN_REQUEST_CODE_STEALTH) {
            if (resultCode == RESULT_OK) {
                if (pendingStealthConfig != null) {
                    launchStealthService(pendingStealthConfig);
                    pendingStealthConfig = null;
                }
            } else {
                pendingStealthConfig = null;
                handleConnectionFailure("VPN İzni verilmedi veya iptal edildi.");
            }
        } else if (requestCode == VPN_REQUEST_CODE_XRAY) {
            if (resultCode == RESULT_OK) {
                if (pendingXrayConfig != null) {
                    launchXrayService(pendingXrayConfig);
                    pendingXrayConfig = null;
                }
            } else {
                pendingXrayConfig = null;
                handleConnectionFailure("VPN İzni verilmedi veya iptal edildi.");
            }
        }
    }

    private void launchXrayService(String configJson) {
        currentRunningProtocol = VpnProtocol.XRAY;
        String srvName = sharedPreferences.getString(KEY_SELECTED_SERVER_NAME, "VPN Sunucusu");
        String protoName = connectingProtocol != null ? connectingProtocol.name() : "Xray";
        Intent intent = new Intent(this, XrayVpnService.class);
        intent.setAction(XrayVpnService.ACTION_START);
        intent.putExtra(XrayVpnService.EXTRA_CONFIG_JSON, configJson);
        intent.putExtra(XrayVpnService.EXTRA_SERVER_NAME, srvName);
        intent.putExtra(XrayVpnService.EXTRA_PROTOCOL_NAME, protoName);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            Log.i(TAG, "Xray servisi başlatıldı, broadcast bekleniyor...");
        } catch (Exception e) {
            Log.e(TAG, "Xray servisi başlatılamadı", e);
            handleConnectionFailure("Xray servisi başlatılamadı: " + e.getMessage());
        }
    }


    private void disconnectVPN() {
        // Uçuştaki tüm bağlantı isteklerini geçersiz kıl
        connectionAttemptId++;
        sendDisconnectUpdate();

        if (currentRunningProtocol == VpnProtocol.XRAY) {
            Intent intent = new Intent(this, XrayVpnService.class);
            intent.setAction(XrayVpnService.ACTION_STOP);
            startService(intent);
        } else if (currentRunningProtocol == VpnProtocol.V2RAY || currentRunningProtocol == VpnProtocol.SUPER) {
            Intent intent = new Intent(this, StealthVpnService.class);
            intent.setAction(StealthVpnService.ACTION_STOP);
            startService(intent);
        } else {
            Intent intent = new Intent(this, OpenVPNService.class);
            intent.setAction(OpenVPNService.DISCONNECT_VPN);
            startService(intent);
        }

        currentRunningProtocol = null;
        isConnecting = false;
        isConnected = false;
        stopTimer();
        sharedPreferences.edit().remove(KEY_START_TIME).apply();
        updateUIOnConnectionState();
        Toast.makeText(this, getString(R.string.vpn_disconnected), Toast.LENGTH_SHORT).show();
    }

    /** SSL, timeout, ağ hatası vb. için anlamlı mesaj üretir */
    private String formatNetworkError(Throwable t) {
        if (t == null) return "Bilinmeyen hata";
        String msg = t.getMessage();
        Throwable cause = t.getCause();
        while (cause != null) {
            String cn = cause.getClass().getSimpleName();
            if (cn.contains("SSL") || cn.contains("Certificate")) return "SSL/Sertifika hatası";
            if (cn.contains("Timeout") || cn.contains("timed out")) return "Bağlantı zaman aşımı";
            if (cn.contains("UnknownHost") || cn.contains("Unable to resolve")) return "Sunucu bulunamadı";
            if (cn.contains("Connection refused") || cn.contains("ConnectException")) return "Bağlantı reddedildi";
            cause = cause.getCause();
        }
        if (msg != null && msg.toLowerCase().contains("ssl")) return "SSL hatası";
        if (msg != null && msg.toLowerCase().contains("timeout")) return "Zaman aşımı";
        return msg != null ? msg : t.getClass().getSimpleName();
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
        protocolIKEv2.setText("Xray");
        protocolSuper.setText("V2Ray");

        View.OnClickListener listener = v -> {
            resetProtocolButtons();
            v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.purple_500)));
            int id = v.getId();
            if (id == R.id.protocolAuto) setProtocolSelection(VpnProtocol.AUTO);
            else if (id == R.id.protocolIKEv2) setProtocolSelection(VpnProtocol.XRAY);
            else if (id == R.id.protocolSuper) setProtocolSelection(VpnProtocol.V2RAY);
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

    /**
     * AUTO modunda seçilen sunucunun ping (ms) ve yük (%) değerlerine göre
     * ağırlıklı olasılıkla en uygun protokolü döndürür.
     *
     * Yük / ping düşükse  → üç protokol eşit dağıtılır (her biri ~%33)
     * Yük / ping orta     → XRAY ve V2RAY ön plana çıkar (%40 / %40 / %20)
     * Yük / ping yüksekse → XRAY ağırlıklı olarak seçilir (%60 / %30 / %10)
     */
    private VpnProtocol resolveAutoProtocol() {
        int    ping = sharedPreferences.getInt("selected_server_ping_ms", -1);
        float  load = sharedPreferences.getFloat("selected_server_load_pct", 50f);

        // Bilinmiyorsa eşit dağıt
        if (ping < 0) return pickWeightedProtocol(34, 33, 33);

        boolean highLoad = load > 60f || ping > 150;
        boolean midLoad  = load > 30f || ping > 80;

        if (highLoad) {
            // Yoğun / gecikmeli → XRAY en hafif
            return pickWeightedProtocol(60, 30, 10);
        } else if (midLoad) {
            // Orta → XRAY + V2RAY dengeli, OpenVPN azaltılmış
            return pickWeightedProtocol(40, 40, 20);
        } else {
            // İdeal → tam eşit dağılım
            return pickWeightedProtocol(34, 33, 33);
        }
    }

    /** xrayPct + v2rayPct + openvpnPct = 100 olmalı */
    private VpnProtocol pickWeightedProtocol(int xrayPct, int v2rayPct, int openvpnPct) {
        int r = new Random().nextInt(100);
        if (r < xrayPct)               return VpnProtocol.XRAY;
        if (r < xrayPct + v2rayPct)    return VpnProtocol.V2RAY;
        return VpnProtocol.OPENVPN;
    }

    private void setProtocolSelection(VpnProtocol protocol) {
        this.selectedProtocol = protocol;
        String name;
        switch (protocol) {
            case AUTO:    name = getString(R.string.protocol_auto); break;
            case OPENVPN: name = "OpenVPN";  break;
            case V2RAY:   name = "V2Ray";    break;
            case SUPER:   name = "Super";    break;
            case XRAY:    name = "Xray";     break;
            default:      name = protocol.name();
        }
        currentProtocolInfo.setText(getString(R.string.protocol_label, name));
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

    // --- CONNECTION LOGGING ---

    private String nowISO8601() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).format(new Date());
    }

    /**
     * VPN bağlantısı kurulduğunda çağrılır.
     * Sadece connectTime ile log oluşturur (disconnectTime yok), dönen log ID'sini saklar.
     */
    private void sendConnectLog() {
        Long deviceId = DeviceIdManager.getRegisteredDeviceId(this);
        long serverId = sharedPreferences.getLong(KEY_SELECTED_SERVER_ID, 0);
        if (deviceId == null || serverId == 0) return;

        String vpnIp;
        if (currentRunningProtocol == VpnProtocol.OPENVPN) {
            vpnIp = "10.0.0.2";
        } else if (currentRunningProtocol == VpnProtocol.XRAY) {
            vpnIp = "10.2.0.1";
        } else {
            vpnIp = "10.1.0.1"; // V2RAY, SUPER, AUTO → StealthVpnService
        }

        String connectTime = nowISO8601();
        long totalBytesAtStart = android.net.TrafficStats.getTotalRxBytes() + android.net.TrafficStats.getTotalTxBytes();
        sharedPreferences.edit()
                .putString(KEY_PENDING_CONNECT_TIME, connectTime)
                .putLong(KEY_PENDING_SERVER_ID, serverId)
                .putLong(KEY_PENDING_DEVICE_ID, deviceId)
                .putString(KEY_PENDING_VPN_IP, vpnIp)
                .putLong(KEY_PENDING_LOG_ID, 0)
                .putLong(KEY_PENDING_BYTES_START, totalBytesAtStart)
                .apply();
        getSharedPreferences("vpn_prefs", MODE_PRIVATE)
                .edit().putBoolean("vpn_is_running", true).apply();

        CreateLogRequest request = new CreateLogRequest(deviceId, serverId, connectTime);
        // disconnectTime gönderilmiyor → log açık kalır → sunucu bağlı kullanıcı sayısını doğru hesaplar
        request.setAssignedVpnIp(vpnIp);

        apiService.createLog(request).enqueue(new Callback<ApiResponse<ConnectionLog>>() {
            @Override
            public void onResponse(Call<ApiResponse<ConnectionLog>> call, Response<ApiResponse<ConnectionLog>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    long logId = response.body().getData().getId();
                    sharedPreferences.edit().putLong(KEY_PENDING_LOG_ID, logId).apply();
                    Log.d(TAG, "Connect log created, id=" + logId);
                } else {
                    Log.w(TAG, "Connect log response failed: code=" + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ConnectionLog>> call, Throwable t) {
                Log.w(TAG, "Connect log request failed: " + t.getMessage());
            }
        });
    }

    /**
     * VPN bağlantısı kesildiğinde çağrılır.
     * Mevcut log varsa PUT ile disconnectTime günceller; yoksa yeni log oluşturur.
     */
    private void sendDisconnectUpdate() {
        String connectTime = sharedPreferences.getString(KEY_PENDING_CONNECT_TIME, null);
        if (connectTime == null) return;

        long logId = sharedPreferences.getLong(KEY_PENDING_LOG_ID, 0);
        String disconnectTime = nowISO8601();

        // MB kullanımını hesapla
        long bytesStart = sharedPreferences.getLong(KEY_PENDING_BYTES_START, 0);
        String dataUsedMbStr = null;
        if (bytesStart > 0) {
            long bytesEnd = android.net.TrafficStats.getTotalRxBytes() + android.net.TrafficStats.getTotalTxBytes();
            long deltaBytes = bytesEnd - bytesStart;
            if (deltaBytes > 0) {
                double deltaMb = deltaBytes / (1024.0 * 1024.0);
                dataUsedMbStr = String.valueOf(Math.round(deltaMb * 100.0) / 100.0);
            }
        }
        final String finalDataUsedMb = dataUsedMbStr;

        if (logId > 0) {
            // Log ID varsa sadece disconnectTime güncelle
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("disconnectTime", disconnectTime);
            if (finalDataUsedMb != null) body.put("dataUsedMb", Double.parseDouble(finalDataUsedMb));
            apiService.updateLogDisconnect(logId, body).enqueue(new Callback<ApiResponse<ConnectionLog>>() {
                @Override
                public void onResponse(Call<ApiResponse<ConnectionLog>> call, Response<ApiResponse<ConnectionLog>> response) {
                    Log.d(TAG, "Disconnect update: " + (response.isSuccessful() ? "OK" : "code=" + response.code()));
                    clearPendingLog();
                }

                @Override
                public void onFailure(Call<ApiResponse<ConnectionLog>> call, Throwable t) {
                    Log.w(TAG, "Disconnect update failed: " + t.getMessage());
                }
            });
        } else {
            // Log ID yoksa (connect log gönderilemedi) → eksiksiz log gönder
            long deviceId = sharedPreferences.getLong(KEY_PENDING_DEVICE_ID, 0);
            long serverId = sharedPreferences.getLong(KEY_PENDING_SERVER_ID, 0);
            if (deviceId == 0 || serverId == 0) { clearPendingLog(); return; }

            CreateLogRequest request = new CreateLogRequest(deviceId, serverId, connectTime);
            request.setDisconnectTime(disconnectTime);
            if (finalDataUsedMb != null) request.setDataUsedMb(new java.math.BigDecimal(finalDataUsedMb));
            String vpnIp = sharedPreferences.getString(KEY_PENDING_VPN_IP, null);
            if (vpnIp != null) request.setAssignedVpnIp(vpnIp);

            apiService.createLog(request).enqueue(new Callback<ApiResponse<ConnectionLog>>() {
                @Override
                public void onResponse(Call<ApiResponse<ConnectionLog>> call, Response<ApiResponse<ConnectionLog>> response) {
                    Log.d(TAG, "Fallback log sent: " + (response.isSuccessful() ? "OK" : "code=" + response.code()));
                    clearPendingLog();
                }

                @Override
                public void onFailure(Call<ApiResponse<ConnectionLog>> call, Throwable t) {
                    Log.w(TAG, "Fallback log failed: " + t.getMessage());
                }
            });
        }
    }

    /**
     * Uygulama yeniden açıldığında önceki oturumdan kalan açık log varsa kapatır.
     */
    private void sendPendingLog() {
        String pendingConnect = sharedPreferences.getString(KEY_PENDING_CONNECT_TIME, null);
        if (pendingConnect == null) return;
        if (RetrofitClient.getToken(this) == null) return;

        long logId = sharedPreferences.getLong(KEY_PENDING_LOG_ID, 0);
        String disconnectTime = nowISO8601();

        // Uygulama yeniden açılışta bytes delta hesaplanamaz, bu yüzden dataUsedMb gönderilmez
        if (logId > 0) {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("disconnectTime", disconnectTime);
            apiService.updateLogDisconnect(logId, body).enqueue(new Callback<ApiResponse<ConnectionLog>>() {
                @Override
                public void onResponse(Call<ApiResponse<ConnectionLog>> call, Response<ApiResponse<ConnectionLog>> response) {
                    Log.d(TAG, "Pending disconnect update: " + (response.isSuccessful() ? "OK" : "code=" + response.code()));
                    clearPendingLog();
                }

                @Override
                public void onFailure(Call<ApiResponse<ConnectionLog>> call, Throwable t) {
                    Log.w(TAG, "Pending disconnect update failed: " + t.getMessage());
                }
            });
        } else {
            long deviceId = sharedPreferences.getLong(KEY_PENDING_DEVICE_ID, 0);
            long serverId = sharedPreferences.getLong(KEY_PENDING_SERVER_ID, 0);
            if (deviceId == 0 || serverId == 0) { clearPendingLog(); return; }

            CreateLogRequest request = new CreateLogRequest(deviceId, serverId, pendingConnect);
            request.setDisconnectTime(disconnectTime);
            String vpnIp = sharedPreferences.getString(KEY_PENDING_VPN_IP, null);
            if (vpnIp != null) request.setAssignedVpnIp(vpnIp);

            apiService.createLog(request).enqueue(new Callback<ApiResponse<ConnectionLog>>() {
                @Override
                public void onResponse(Call<ApiResponse<ConnectionLog>> call, Response<ApiResponse<ConnectionLog>> response) {
                    Log.d(TAG, "Pending log sent: " + (response.isSuccessful() ? "OK" : "code=" + response.code()));
                    clearPendingLog();
                }

                @Override
                public void onFailure(Call<ApiResponse<ConnectionLog>> call, Throwable t) {
                    Log.w(TAG, "Pending log send failed: " + t.getMessage());
                }
            });
        }
    }

    private void clearPendingLog() {
        sharedPreferences.edit()
                .remove(KEY_PENDING_CONNECT_TIME)
                .remove(KEY_PENDING_SERVER_ID)
                .remove(KEY_PENDING_DEVICE_ID)
                .remove(KEY_PENDING_VPN_IP)
                .remove(KEY_PENDING_LOG_ID)
                .remove(KEY_PENDING_BYTES_START)
                .apply();
    }

    // -----------------------------------------------------------------------
    // Bildirim Switch yönetimi — sistem ayarlarına yönlendirir
    // -----------------------------------------------------------------------

    private void setupNotificationSwitch() {
        // Listener onResume() içinde set ediliyor.
        // Burada set edilirse recreate() sırasında view state restore edilirken
        // yanlışlıkla tetiklenir ve bildirim ayarları sayfası açılır.
    }

    /** Switch'e her basışta sistem bildirim ayarları açılır; dönerken onResume switch'i günceller. */
    private void onNotificationSwitchChanged() {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }

    private void updateNotificationSwitchText(boolean enabled) {
        if (notificationStatusText != null) {
            notificationStatusText.setText(enabled ? getString(R.string.status_on) : getString(R.string.status_off));
            notificationStatusText.setTextColor(
                    enabled ? Color.parseColor("#BB86FC") : Color.parseColor("#AAAAAA"));
        }
    }

    // -----------------------------------------------------------------------
    // Dil seçimi
    // -----------------------------------------------------------------------

    private void showLanguagePickerDialog() {
        String[] langNames = {
                getString(R.string.lang_english),
                getString(R.string.lang_turkish),
                getString(R.string.lang_german),
                getString(R.string.lang_french),
                getString(R.string.lang_russian)
        };
        String[] langCodes = {"en", "tr", "de", "fr", "ru"};
        String[] langFlags  = {"🇬🇧", "🇹🇷", "🇩🇪", "🇫🇷", "🇷🇺"};

        String current = LocaleManager.getLanguage(this);
        int currentIdx = 0;
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(current)) { currentIdx = i; break; }
        }
        final int[] selected = {currentIdx};

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_language))
                .setSingleChoiceItems(langNames, currentIdx, (dialog, which) -> selected[0] = which)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String chosenCode = langCodes[selected[0]];
                    if (!chosenCode.equals(LocaleManager.getLanguage(this))) {
                        LocaleManager.setLanguage(this, chosenCode);
                        recreate();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateLanguageButtonFlag() {
        if (languageButton == null) return;
        String lang = LocaleManager.getLanguage(this);
        String flag;
        switch (lang) {
            case "tr": flag = "🇹🇷"; break;
            case "de": flag = "🇩🇪"; break;
            case "fr": flag = "🇫🇷"; break;
            case "ru": flag = "🇷🇺"; break;
            default:   flag = "🇬🇧"; break;
        }
        languageButton.setText(flag);
    }

    // -----------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}