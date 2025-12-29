package com.abacicelal.supervpn_project;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.net.VpnService;
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
import com.abacicelal.supervpn_project.remote.model.GuestLoginRequest;
import com.abacicelal.supervpn_project.remote.model.GuestLoginResponse;
import com.abacicelal.supervpn_project.remote.model.Subscription;
import com.abacicelal.supervpn_project.remote.model.VpnConfigResponse;
import com.abacicelal.supervpn_project.remote.model.VpnProtocol;
import com.abacicelal.supervpn_project.utils.DeviceIdManager;

import java.io.StringReader;
import java.util.List;

// OpenVPN Core Imports
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;
import de.blinkt.openvpn.core.ConnectionStatus;

// WireGuard Imports
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.Config;
import com.abacicelal.supervpn_project.utils.WireGuardTunnel;

// Android Native IKEv2 (API 30+)
import android.net.Ikev2VpnProfile;
import android.net.VpnManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements VpnStatus.StateListener {

    private static final String TAG = "MainActivity";
    private GoBackend wireGuardBackend;
    private Tunnel wireGuardTunnel;
    public static boolean isConnected = false;
    private boolean isConnecting = false;

    // UI Bileşenleri
    private ImageButton connectButton;
    private ImageButton menuButton;
    private ImageButton premiumButton;
    private ImageButton browserButton;
    private ImageButton languageButton;
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

    // Permission Code
    private static final int VPN_PERMISSION_REQUEST_CODE = 70;

    // To handle config when waiting for permission
    private String mPendingConfig = null;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitClient.getApiService(getApplicationContext());

        initializeViews();
        setupListeners();

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize WireGuard Backend
        try {
            wireGuardBackend = new GoBackend(getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, "WireGuard Backend Init Failed", e);
        }

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

        // Register Status Listener
        VpnStatus.addStateListener(this);

        String savedServerName = sharedPreferences.getString(KEY_SELECTED_SERVER_NAME, getString(R.string.server_select));
        long savedServerId = sharedPreferences.getLong(KEY_SELECTED_SERVER_ID, 0);

        if (savedServerId != 0) currentServerInfo.setText(String.format(getString(R.string.current_server), savedServerName));
        else currentServerInfo.setText(String.format(getString(R.string.current_server), getString(R.string.server_select)));

        if (isConnected) {
            startTime = sharedPreferences.getLong(KEY_START_TIME, 0);
            if (startTime > 0) startTimer();
            else disconnectVPN();
        }
        updateUIOnConnectionState();
        checkSubscriptionStatusForUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister Status Listener
        VpnStatus.removeStateListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    /**
     * VpnStatus.StateListener Implementation
     */
    @Override
    public void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level, Intent intent) {
        runOnUiThread(() -> {
            Log.d(TAG, "VPN Status: " + state + " - " + logmessage);

            switch (level) {
                case LEVEL_CONNECTED:
                    if (!isConnected) {
                        handleConnectionSuccess();
                    }
                    break;

                case LEVEL_NOTCONNECTED:
                case LEVEL_AUTH_FAILED:
                case LEVEL_NONETWORK:
                    if (isConnected || isConnecting) {
                        handleConnectionDisconnected();
                    }
                    break;

                case LEVEL_CONNECTING_NO_SERVER_REPLY_YET:
                case LEVEL_CONNECTING_SERVER_REPLIED:
                case LEVEL_WAITING_FOR_USER_INPUT:
                    isConnecting = true;
                    updateUIOnConnectionState();
                    break;
            }
        });
    }

    @Override
    public void setConnectedVPN(String uuid) {
        // Not implemented needed for basic status
    }

    private void initializeViews() {
        connectButton = findViewById(R.id.connectButton);
        menuButton = findViewById(R.id.menuButton);
        premiumButton = findViewById(R.id.premiumButton);
        browserButton = findViewById(R.id.browserButton);
        languageButton = findViewById(R.id.languageButton);
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
            startActivity(new Intent(this, BrowserActivity.class));
        });

        languageButton.setOnClickListener(v -> showLanguageSelectionDialog());
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

        isConnecting = true;
        updateUIOnConnectionState();

        // Guest login or Registered login logic
        if (RetrofitClient.getToken(this) == null) {
            handleGuestConnection();
        } else {
            fetchDeviceAndCheckSubscription();
        }
    }

    private void handleGuestConnection() {
        String uniqueId = DeviceIdManager.getDeviceId(this);
        GuestLoginRequest request = new GuestLoginRequest(uniqueId);

        apiService.guestLogin(request).enqueue(new Callback<GuestLoginResponse>() {
            @Override
            public void onResponse(Call<GuestLoginResponse> call, Response<GuestLoginResponse> response) {
                 if (response.isSuccessful() && response.body() != null) {
                     // Store the Guest JWT token
                     GuestLoginResponse authData = response.body();
                     RetrofitClient.saveToken(MainActivity.this, authData.getToken(), null);

                     fetchVpnConfigForGuest(uniqueId);
                 } else {
                     handleConnectionFailure(getString(R.string.connection_failed) + " (Guest Auth Error: " + response.code() + ")");
                 }
            }

            @Override
            public void onFailure(Call<GuestLoginResponse> call, Throwable t) {
                handleConnectionFailure("Guest Login Network Error: " + t.getMessage());
            }
        });
    }

    private void fetchDeviceAndCheckSubscription() {
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
        String deviceName = "Android Cihaz " + android.os.Build.MODEL;
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
            // Guest User
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
                } else {
                    Log.e(TAG, "Subscription check failed UI: Code=" + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Subscription>>> call, Throwable t) {
                Log.e(TAG, "Subscription check network error UI", t);
            }
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
                        Toast.makeText(MainActivity.this, getString(R.string.error_active_subscription), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, PremiumActivity.class));
                        handleConnectionFailure(null);
                    }
                } else {
                    handleConnectionFailure(getString(R.string.error_subscription_check) + " Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Subscription>>> call, Throwable t) {
                handleConnectionFailure(String.format(getString(R.string.network_error_sub), t.getMessage()));
            }
        });
    }

    private void fetchVpnConfigForGuest(String guestDeviceId) {
        long serverId = sharedPreferences.getLong(KEY_SELECTED_SERVER_ID, 0);
        if (serverId == 0) {
            Toast.makeText(this, getString(R.string.server_select_first), Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, ServerSelectionActivity.class));
            handleConnectionFailure(null);
            return;
        }

        ConfigGenerationRequest request = new ConfigGenerationRequest(serverId, guestDeviceId, selectedProtocol);
        executeConfigFetch(request);
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
        executeConfigFetch(request);
    }

    private void executeConfigFetch(ConfigGenerationRequest request) {
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
                    } else if ("IKEV2".equalsIgnoreCase(protocol)) {
                        startIkev2(configContent);
                    } else if ("V2RAY".equalsIgnoreCase(protocol)) {
                        startV2Ray(configContent);
                    } else if ("SUPER".equalsIgnoreCase(protocol)) {
                        startSuper(configContent);
                    } else {
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

    /**
     * EMBEDDED OPENVPN IMPLEMENTATION
     */
    private void startOpenVpn(String configContent) {
        try {
            if (configContent == null || configContent.isEmpty()) {
                handleConnectionFailure(getString(R.string.config_error));
                return;
            }

            ConfigParser cp = new ConfigParser();
            cp.parseConfig(new StringReader(configContent));
            VpnProfile vp = cp.convertProfile();

            if (vp == null) {
                handleConnectionFailure("VPN Profili oluşturulamadı. Config dosyası hatalı.");
                return;
            }

            String serverName = sharedPreferences.getString(KEY_SELECTED_SERVER_NAME, "Server");
            vp.mName = "SuperVPN " + serverName;

            ProfileManager.getInstance(this).setTemporaryProfile(this, vp);

            mPendingConfig = configContent;

            Intent intent = VpnService.prepare(this);
            if (intent != null) {
                startActivityForResult(intent, VPN_PERMISSION_REQUEST_CODE);
            } else {
                startEmbeddedVpn(vp);
            }

        } catch (Exception e) {
            Log.e(TAG, "OpenVPN Config Error", e);
            handleConnectionFailure("Ayar Hatası: " + e.getMessage());
        }
    }

    private void startEmbeddedVpn(VpnProfile vp) {
        try {
            if (vp.mCustomConfigOptions == null) {
                vp.mCustomConfigOptions = "";
            }
            if (!vp.mCustomConfigOptions.contains("block-outside-dns")) {
                 vp.mCustomConfigOptions += "ignore-unknown-option block-outside-dns\n";
            }
            if (!vp.mCustomConfigOptions.contains("shaper")) {
                 vp.mCustomConfigOptions += "ignore-unknown-option shaper\n";
            }

            VPNLaunchHelper.startOpenVpn(vp, this, "VPN_CONNECT", false);
            mPendingConfig = null;
        } catch (Exception e) {
            Log.e(TAG, "Error starting embedded VPN: ", e);
            handleConnectionFailure("VPN Start Error: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_PERMISSION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (mPendingConfig != null) {
                    if (selectedProtocol == VpnProtocol.OPENVPN) {
                        startOpenVpn(mPendingConfig);
                    } else if (selectedProtocol == VpnProtocol.SUPER) {
                         startSuper(mPendingConfig);
                    } else if (selectedProtocol == VpnProtocol.IKEV2) {
                         startIkev2(mPendingConfig);
                    }
                } else {
                     Toast.makeText(this, "Permission granted. Connecting...", Toast.LENGTH_SHORT).show();
                }
            } else {
                handleConnectionFailure("VPN Permission denied.");
                isConnecting = false;
                updateUIOnConnectionState();
            }
        }
    }

    /**
     * IKEv2 implementation (Native Android - API 30+)
     */
    private void startIkev2(String configContent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            handleConnectionFailure(getString(R.string.ikev2_android_version));
            return;
        }

        try {
            String serverAddr = "";
            String username = "";
            String password = "";

            String[] lines = configContent.split("\n");
            for (String line : lines) {
                if (line.startsWith("Server: ")) serverAddr = line.replace("Server: ", "").trim();
                else if (line.startsWith("User: ")) username = line.replace("User: ", "").trim();
                else if (line.startsWith("Pass: ")) password = line.replace("Pass: ", "").trim();
            }
            String identity = username;

            if (serverAddr.isEmpty()) {
                handleConnectionFailure(getString(R.string.ikev2_config_error));
                return;
            }

            VpnManager vpnManager = (VpnManager) getSystemService(Context.VPN_MANAGEMENT_SERVICE);

            Ikev2VpnProfile.Builder profileBuilder = new Ikev2VpnProfile.Builder(serverAddr, identity);
            profileBuilder.setAuthUsernamePassword(username, password, null);

            Ikev2VpnProfile profile = profileBuilder.build();

            mPendingConfig = configContent;
            Intent intent = vpnManager.provisionVpnProfile(profile);
            if (intent != null) {
                startActivityForResult(intent, VPN_PERMISSION_REQUEST_CODE);
            } else {
                vpnManager.startProvisionedVpnProfileSession();
                handleConnectionSuccess();
                mPendingConfig = null;
            }

        } catch (Exception e) {
            Log.e(TAG, "IKEv2 Error", e);
            handleConnectionFailure(String.format(getString(R.string.ikev2_error), e.getMessage()));
        }
    }

    /**
     * V2Ray implementation.
     */
    private void startV2Ray(String configContent) {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("V2Ray Config", configContent);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, getString(R.string.v2ray_config_copied), Toast.LENGTH_LONG).show();

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.v2ray.ang");
            if (launchIntent != null) {
                startActivity(launchIntent);
                handleConnectionSuccess();
            } else {
                Toast.makeText(this, getString(R.string.v2ray_not_installed), Toast.LENGTH_LONG).show();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.v2ray.ang")));
                } catch (Exception ex) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.v2ray.ang")));
                }
                handleConnectionFailure(null);
            }
        } catch (Exception e) {
            handleConnectionFailure(String.format(getString(R.string.v2ray_error), e.getMessage()));
        }
    }

    /**
     * Super Protocol (WireGuard) implementation.
     */
    private void startSuper(String configContent) {
        try {
            if (wireGuardBackend == null) {
                 handleConnectionFailure("WireGuard Backend not initialized.");
                 return;
            }

            Config config = Config.parse(new StringReader(configContent));

            String tunnelName = "super_wg_tunnel";
            if (wireGuardTunnel == null) {
                wireGuardTunnel = new WireGuardTunnel(tunnelName);
            }

            Intent intent = GoBackend.VpnService.prepare(this);
            if (intent != null) {
                mPendingConfig = configContent;
                startActivityForResult(intent, VPN_PERMISSION_REQUEST_CODE);
                return;
            }

            wireGuardBackend.setState(wireGuardTunnel, Tunnel.State.UP, config);
            handleConnectionSuccess();

        } catch (Exception e) {
            Log.e(TAG, "WireGuard Error", e);
            handleConnectionFailure(String.format(getString(R.string.super_error), e.getMessage()));
        }
    }

    private void disconnectVPN() {
        try {
            if (selectedProtocol == VpnProtocol.OPENVPN) {
                Intent intent = new Intent(this, de.blinkt.openvpn.core.OpenVPNService.class);
                stopService(intent);
            } else if (selectedProtocol == VpnProtocol.SUPER) {
                if (wireGuardBackend != null && wireGuardTunnel != null) {
                    wireGuardBackend.setState(wireGuardTunnel, Tunnel.State.DOWN, null);
                }
            } else if (selectedProtocol == VpnProtocol.IKEV2) {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                     VpnManager vpnManager = (VpnManager) getSystemService(Context.VPN_MANAGEMENT_SERVICE);
                     vpnManager.stopProvisionedVpnProfileSession();
                 }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping VPN service", e);
        }

        handleConnectionDisconnected();
        Toast.makeText(this, getString(R.string.vpn_disconnected), Toast.LENGTH_SHORT).show();
    }

    private void handleConnectionFailure(String message) {
        if (message != null) Log.e(TAG, message);
        handleConnectionDisconnected();
        if (message != null) Toast.makeText(this, getString(R.string.connection_failed), Toast.LENGTH_SHORT).show();
    }

    private void handleConnectionSuccess() {
        isConnected = true;
        isConnecting = false;
        startTime = System.currentTimeMillis();
        sharedPreferences.edit().putLong(KEY_START_TIME, startTime).apply();
        startTimer();
        updateUIOnConnectionState();
    }

    private void handleConnectionDisconnected() {
        isConnecting = false;
        isConnected = false;
        stopTimer();
        sharedPreferences.edit().remove(KEY_START_TIME).apply();
        updateUIOnConnectionState();
    }

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

    private void showLanguageSelectionDialog() {
        String[] languages = {"English", "Türkçe", "Deutsch", "Français", "Русский"};
        String[] codes = {"en", "tr", "de", "fr", "ru"};

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language))
            .setItems(languages, (dialog, which) -> {
                LocaleHelper.setLocale(this, codes[which]);
                recreate();
            })
            .show();
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
            String currentText = currentServerInfo.getText().toString();
            statusSafeText.setText(currentText);
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
}
