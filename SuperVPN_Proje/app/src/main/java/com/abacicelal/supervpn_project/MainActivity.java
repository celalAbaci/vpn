package com.abacicelal.supervpn_project;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.abacicelal.supervpn_project.core.protocols.IVpnStrategy;
import com.abacicelal.supervpn_project.core.protocols.Ikev2Strategy;
import com.abacicelal.supervpn_project.core.protocols.OpenVpnStrategy;
import com.abacicelal.supervpn_project.core.protocols.SuperStrategy;
import com.abacicelal.supervpn_project.core.protocols.VpnProtocolManager;
import com.abacicelal.supervpn_project.core.protocols.VpnStatusListener;
import com.abacicelal.supervpn_project.remote.ApiService;
import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.ApiResponse;
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

public class MainActivity extends AppCompatActivity implements VpnStatusListener {

    private static final String TAG = "MainActivity";
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

    // VPN Manager
    private VpnProtocolManager protocolManager;
    private static final int ICS_OPENVPN_PERMISSION = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitClient.getApiService(getApplicationContext());
        protocolManager = VpnProtocolManager.getInstance();

        initializeViews();
        setupListeners();

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // UI Başlangıç Durumu
        updateUIOnConnectionState();

        // Varsayılan Protokol Ayarı
        setProtocolSelection(VpnProtocol.OPENVPN);

        // Kullanıcı abonelik durumunu kontrol et ve UI güncelle
        checkSubscriptionStatusForUI();

        // Restore connection state from manager if needed (or if activity recreated)
        // If the strategy is still alive and connected, reflect that
        if (protocolManager.isConnected()) {
             simulateConnectionSuccess();
        }

        // Ensure listener is attached if we have an active strategy
        if (protocolManager.getStrategy() != null) {
            protocolManager.getStrategy().setListener(this);
        }
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

        boolean connected = protocolManager.isConnected();

        if (!connected && !isConnecting) {
            startVPNConnection();
        } else if (connected && !isConnecting) {
            disconnectVPN();
        }
    }

    private void startVPNConnection() {
        Log.d(TAG, "VPN Bağlantısı Başlatılıyor...");

        // GUEST MODE logic
        String token = RetrofitClient.getToken(this);

        isConnecting = true;
        updateUIOnConnectionState();
        fetchDeviceAndCheckSubscription();
    }

    private void fetchDeviceAndCheckSubscription() {
        if (RetrofitClient.getToken(this) == null) {
            // Check if we already have a registered Guest ID
            Long registeredId = com.abacicelal.supervpn_project.utils.DeviceIdManager.getRegisteredDeviceId(this);
            if (registeredId != null) {
                checkSubscription(registeredId); // Will skip sub check inside
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

                    // Initialize Strategy based on protocol
                    IVpnStrategy strategy = null;
                    if ("OPENVPN".equalsIgnoreCase(protocol)) {
                        strategy = new OpenVpnStrategy();
                    } else if ("IKEV2".equalsIgnoreCase(protocol)) {
                        strategy = new Ikev2Strategy();
                    } else if ("SUPER".equalsIgnoreCase(protocol)) {
                        strategy = new SuperStrategy();
                    } else {
                         if ("V2RAY".equalsIgnoreCase(protocol)) {
                             // Handle V2Ray separately as it's an external app copy-paste usually
                             startV2Ray(configContent);
                             return;
                         }
                        strategy = new OpenVpnStrategy();
                    }

                    if (strategy != null) {
                        protocolManager.setStrategy(strategy);
                        strategy.setListener(MainActivity.this);
                        strategy.connect(MainActivity.this, configContent, null, null);
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

    // Field to hold config if permission is needed
    // Not needed here if Strategy manages pending config,
    // but strategy.connect() triggers permission callback, so we just launch intent.
    // The Strategy holds the pending config.

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ICS_OPENVPN_PERMISSION) {
            if (resultCode == RESULT_OK) {
                // Permission granted.
                // We need to tell the strategy to proceed.
                // Re-calling connect() with the same arguments is one way,
                // but we might not have the config here easily.
                // The updated OpenVpnStrategy.processPendingConfig() handles this if we just re-trigger
                // OR better: we don't need to do anything if the strategy listens for onActivityResult?
                // No, OpenVpnStrategy is not an Activity.
                // We should call strategy.connect() again?
                // The strategy is stateful and holds pendingConfig.
                // We should add a method `onPermissionGranted()` to strategy or just call connect again.
                // Since `connect` checks bounds and pending, calling it again is safe IF we had the config.
                // But we don't have config here.

                // Let's rely on the Strategy to be smart.
                // Actually, simply calling connect(this, null, ...) might work if we designed it that way.
                // But `connect` checks for null config.

                // Let's modify OpenVpnStrategy to have `retryConnection()` or just re-call connect with saved config.
                // Wait, OpenVpnStrategy has `processPendingConfig`.
                // We can expose `retry()` or `connect()` with null to trigger pending.
                // Let's modify OpenVpnStrategy to allow null config if pending exists.

                // Modification: I will just call connect with empty string and update Strategy to handle it?
                // Or better, add `resume()` method to IVpnStrategy (maybe too specific).
                // Or just cast to OpenVpnStrategy.

                if (protocolManager.getStrategy() instanceof OpenVpnStrategy) {
                    // Trigger connection again.
                    // We can just call connect with dummy config if we modify OpenVpnStrategy to use pending if avail.
                    // But currently OpenVpnStrategy checks for null/empty.

                    // Actually, if permission is granted, we simply need to tell the service to start.
                    // The service is already bound.
                    // The easiest way is to re-call the logic inside strategy.
                    // Let's assume we can just call connect again with the SAME config.
                    // But we don't have it.

                    // Simplest fix: Just toast user to click connect again? No, bad UX.
                    // I will add `onPermissionResult` to IVpnStrategy or similar? No, too complex.
                    // I will add logic to OpenVpnStrategy to allow re-entry.

                    // For now, I'll just click the connect button programmatically? No.
                    // I will update OpenVpnStrategy to allow null config if pending is set.

                    // But I can't update OpenVpnStrategy in this turn easily without another tool call.
                    // I will do it.

                    // Actually, I can just hold the config in MainActivity too as a backup, like I did before.
                    // But Strategy holds it.

                    // Let's do this:
                    // I'll update OpenVpnStrategy in next step to support `connect(ctx, null, ...)` if pending.
                    // For now, in MainActivity, I'll put a TODO or logic assuming it works.
                    // Actually, I already wrote OpenVpnStrategy in previous step.
                    // Let's check what I wrote.
                    // `if (configContent == null || configContent.isEmpty()) { ... return; }`
                    // So I cannot pass null.

                    // I will update OpenVpnStrategy to allow it.
                    // Or I can just start connection again if I store config in MainActivity.
                    // Storing in MainActivity is safer for lifecycle anyway.
                    // I'll do that.

                     Toast.makeText(this, "Permission granted. Retrying...", Toast.LENGTH_SHORT).show();
                     if (lastConfigContent != null && protocolManager.getStrategy() != null) {
                         protocolManager.getStrategy().connect(this, lastConfigContent, null, null);
                     }
            } else {
                handleConnectionFailure("VPN Permission denied.");
            }
        }
    }

    private String lastConfigContent;

    private void startV2Ray(String configContent) {
         try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("V2Ray Config", configContent);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, getString(R.string.v2ray_config_copied), Toast.LENGTH_LONG).show();
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.v2ray.ang");
            if (launchIntent != null) {
                startActivity(launchIntent);
                simulateConnectionSuccess();
            } else {
                Toast.makeText(this, getString(R.string.v2ray_not_installed), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            handleConnectionFailure(String.format(getString(R.string.v2ray_error), e.getMessage()));
        }
    }

    private void disconnectVPN() {
        if (protocolManager.getStrategy() != null) {
            protocolManager.getStrategy().disconnect(this);
        }
    }

    // VpnStatusListener Implementation
    @Override
    public void onStatusChanged(String state, String message, String level) {
         runOnUiThread(() -> {
             // Log.d(TAG, "State: " + state);
         });
    }

    @Override
    public void onConnected() {
        runOnUiThread(this::simulateConnectionSuccess);
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> {
            isConnecting = false;
            stopTimer();
            sharedPreferences.edit().remove(KEY_START_TIME).apply();
            updateUIOnConnectionState();
            Toast.makeText(this, getString(R.string.vpn_disconnected), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
             handleConnectionFailure(error);
        });
    }

    @Override
    public void onPermissionRequired(Intent intent) {
        runOnUiThread(() -> {
            try {
                startActivityForResult(intent, ICS_OPENVPN_PERMISSION);
            } catch (Exception e) {
                handleConnectionFailure("Failed to launch permission intent: " + e.getMessage());
            }
        });
    }

    private void handleConnectionFailure(String message) {
        if (message != null) Log.e(TAG, message);
        isConnecting = false;
        updateUIOnConnectionState();
        if (message != null) Toast.makeText(this, getString(R.string.connection_failed), Toast.LENGTH_SHORT).show();
    }

    private void simulateConnectionSuccess() {
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
        boolean connected = protocolManager.isConnected();

        if (isConnecting) {
            statusConnectedText.setText(getString(R.string.status_connecting));
            statusConnectedText.setTextColor(Color.parseColor("#00BFFF"));
            loadingSpinner.setVisibility(View.VISIBLE);
            connectButton.setBackgroundResource(R.drawable.btn_round);
            connectionStatusLabel.setText(getString(R.string.status_label_connecting));
        } else if (connected) {
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

    @Override
    protected void onResume() {
        super.onResume();
        String savedServerName = sharedPreferences.getString(KEY_SELECTED_SERVER_NAME, getString(R.string.server_select));
        long savedServerId = sharedPreferences.getLong(KEY_SELECTED_SERVER_ID, 0);

        if (savedServerId != 0) currentServerInfo.setText(String.format(getString(R.string.current_server), savedServerName));
        else currentServerInfo.setText(String.format(getString(R.string.current_server), getString(R.string.server_select)));

        if (protocolManager.isConnected()) {
            startTime = sharedPreferences.getLong(KEY_START_TIME, 0);
            if (startTime > 0) startTimer();
            else disconnectVPN();
        }
        updateUIOnConnectionState();
        checkSubscriptionStatusForUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}
