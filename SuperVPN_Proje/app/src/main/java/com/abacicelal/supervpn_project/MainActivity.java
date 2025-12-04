package com.abacicelal.supervpn_project;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Ikev2VpnProfile;
import android.net.VpnManager;
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
import com.abacicelal.supervpn_project.remote.model.ConfigGenerationRequest;
import com.abacicelal.supervpn_project.remote.model.Device;
import com.abacicelal.supervpn_project.remote.model.DeviceRequest;
import com.abacicelal.supervpn_project.remote.model.Subscription;
import com.abacicelal.supervpn_project.remote.model.VpnConfigResponse;
import com.abacicelal.supervpn_project.remote.model.VpnProtocol;

import java.util.List;

// Import OpenVpnApi from the library
import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static boolean isConnected = false;
    private boolean isConnecting = false;

    // UI Components
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

    // Timer
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private long startTime;

    // Prefs
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "VPN_PREFS";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_SELECTED_SERVER_NAME = "selected_server_name";
    private static final String KEY_SELECTED_SERVER_ID = "selected_server_id";

    // Retrofit
    private ApiService apiService;

    // Protocol state
    private VpnProtocol selectedProtocol = VpnProtocol.OPENVPN; // User's preference
    private VpnProtocol activeProtocol = null; // Currently running protocol

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitClient.getApiService(getApplicationContext());

        initializeViews();
        setupListeners();

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        updateUIOnConnectionState();
        setProtocolSelection(VpnProtocol.OPENVPN);

        // Initialize OpenVPN Log Cache safely
        try {
            VpnStatus.initLogCache(this.getCacheDir());
        } catch (Throwable t) {
            Log.w(TAG, "Failed to init VpnStatus cache: " + t.getMessage());
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
                Toast.makeText(this, R.string.browser_error, Toast.LENGTH_SHORT).show();
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
        String token = RetrofitClient.getToken(this);
        if (token == null) {
            Toast.makeText(this, R.string.login_needed, Toast.LENGTH_LONG).show();
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
                    handleConnectionFailure(getString(R.string.device_error));
                }
            }

            @Override
            public void onFailure(Call<List<Device>> call, Throwable t) {
                handleConnectionFailure(getString(R.string.device_error) + ": " + t.getMessage());
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
                        Toast.makeText(MainActivity.this, R.string.subscription_error, Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, R.string.server_select_title, Toast.LENGTH_LONG).show();
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
                    String protocolStr = response.body().getProtocol();

                    VpnProtocol protocol = VpnProtocol.OPENVPN; // default
                    if ("IKEV2".equalsIgnoreCase(protocolStr)) protocol = VpnProtocol.IKEV2;
                    else if ("V2RAY".equalsIgnoreCase(protocolStr)) protocol = VpnProtocol.V2RAY;
                    else if ("SUPER".equalsIgnoreCase(protocolStr)) protocol = VpnProtocol.SUPER;

                    // Track active protocol
                    activeProtocol = protocol;

                    if (protocol == VpnProtocol.OPENVPN) {
                        startOpenVpn(configContent);
                    } else if (protocol == VpnProtocol.IKEV2) {
                        startIkev2(configContent);
                    } else if (protocol == VpnProtocol.V2RAY) {
                        startV2Ray(configContent);
                    } else if (protocol == VpnProtocol.SUPER) {
                        startSuper(configContent);
                    } else {
                        // Fallback
                        startOpenVpn(configContent);
                        activeProtocol = VpnProtocol.OPENVPN;
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

    // --- PROTOCOL IMPLEMENTATIONS ---

    private void startOpenVpn(String configContent) {
        try {
            OpenVpnApi.startVpn(this, configContent, "VPN", null, null);
            simulateConnectionSuccess();
        } catch (Exception e) {
            Log.e(TAG, "OpenVPN Start Error", e);
            handleConnectionFailure("OpenVPN Start Error: " + e.getMessage());
        }
    }

    private void startIkev2(String configContent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            handleConnectionFailure(getString(R.string.ikev2_not_supported));
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

            if (serverAddr.isEmpty()) {
                handleConnectionFailure("Config Error: Missing Server IP");
                return;
            }

            VpnManager vpnManager = (VpnManager) getSystemService(Context.VPN_MANAGEMENT_SERVICE);

            Ikev2VpnProfile.Builder builder = new Ikev2VpnProfile.Builder(serverAddr, serverAddr);
            builder.setAuthUsernamePassword(username, password, null);

            Ikev2VpnProfile profile = builder.build();

            vpnManager.provisionVpnProfile(profile);
            vpnManager.startProvisionedVpnProfileSession();

            simulateConnectionSuccess();

        } catch (Exception e) {
            Log.e(TAG, "IKEv2 Error", e);
            handleConnectionFailure("IKEv2 Error: " + e.getMessage());
        }
    }

    private void startV2Ray(String configContent) {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("V2Ray Config", configContent);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, R.string.v2ray_copied, Toast.LENGTH_LONG).show();

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.v2ray.ang");
            if (launchIntent != null) {
                startActivity(launchIntent);
                simulateConnectionSuccess();
            } else {
                Toast.makeText(this, R.string.v2ray_not_installed, Toast.LENGTH_LONG).show();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.v2ray.ang")));
                } catch (Exception ex) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.v2ray.ang")));
                }
                handleConnectionFailure(null);
            }
        } catch (Exception e) {
            handleConnectionFailure("V2Ray Error: " + e.getMessage());
        }
    }

    private void startSuper(String configContent) {
        try {
             ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Super Config", configContent);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, R.string.super_copied, Toast.LENGTH_SHORT).show();
            simulateConnectionSuccess();
        } catch (Exception e) {
            handleConnectionFailure("Super Protocol Error: " + e.getMessage());
        }
    }

    private void disconnectVPN() {
        try {
            // Use activeProtocol if available, otherwise fallback to selectedProtocol (though dangerous)
            VpnProtocol protocolToStop = (activeProtocol != null) ? activeProtocol : selectedProtocol;

            if (protocolToStop == VpnProtocol.OPENVPN) {
                OpenVPNThread.stop();
            } else if (protocolToStop == VpnProtocol.IKEV2) {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                     VpnManager vpnManager = (VpnManager) getSystemService(Context.VPN_MANAGEMENT_SERVICE);
                     vpnManager.stopProvisionedVpnProfileSession();
                 }
            }
            // V2Ray/Super are external apps, can't stop them programmatically easily without API.
        } catch (Exception e) {
            Log.e(TAG, "Disconnect Error", e);
        }

        isConnecting = false;
        isConnected = false;
        activeProtocol = null; // Reset
        stopTimer();
        sharedPreferences.edit().remove(KEY_START_TIME).apply();
        updateUIOnConnectionState();
        Toast.makeText(this, R.string.disconnect, Toast.LENGTH_SHORT).show();
    }

    private void handleConnectionFailure(String message) {
        if (message != null) Log.e(TAG, message);
        isConnecting = false;
        isConnected = false;
        activeProtocol = null;
        updateUIOnConnectionState();
        if (message != null) Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void simulateConnectionSuccess() {
        isConnected = true;
        isConnecting = false;
        startTime = System.currentTimeMillis();
        sharedPreferences.edit().putLong(KEY_START_TIME, startTime).apply();
        startTimer();
        updateUIOnConnectionState();
    }

    // --- UI HELPERS ---

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
        currentProtocolInfo.setText(getString(R.string.protocol, name));
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
            statusConnectedText.setText(R.string.connecting);
            statusConnectedText.setTextColor(Color.parseColor("#00BFFF"));
            loadingSpinner.setVisibility(View.VISIBLE);
            connectButton.setBackgroundResource(R.drawable.btn_round);
            connectionStatusLabel.setText("BAĞLANIYOR");
        } else if (isConnected) {
            statusConnectedText.setText(R.string.connected);
            statusConnectedText.setTextColor(Color.GREEN);
            loadingSpinner.setVisibility(View.GONE);
            connectButton.setBackgroundResource(R.drawable.btn_round_connected);
            connectionStatusLabel.setText("BAĞLANTIYI KES");
            String sName = currentServerInfo.getText().toString().replace(getString(R.string.current_server, "").replace("%s", ""), "").trim();
            statusSafeText.setText(sName);
        } else {
            statusConnectedText.setText(R.string.no_connection);
            statusConnectedText.setTextColor(Color.WHITE);
            loadingSpinner.setVisibility(View.GONE);
            connectButton.setBackgroundResource(R.drawable.btn_round);
            connectionStatusLabel.setText("BAĞLAN");
            statusSafeText.setText(R.string.tap_to_connect);
            connectionTimeText.setText("00:00");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String savedServerName = sharedPreferences.getString(KEY_SELECTED_SERVER_NAME, "Sunucu Seçin");
        long savedServerId = sharedPreferences.getLong(KEY_SELECTED_SERVER_ID, 0);

        currentServerInfo.setText(getString(R.string.current_server, savedServerId != 0 ? savedServerName : "Sunucu Seçin"));

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
