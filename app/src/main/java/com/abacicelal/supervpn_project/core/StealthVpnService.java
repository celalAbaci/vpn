package com.abacicelal.supervpn_project.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import androidx.core.app.NotificationCompat;

import com.abacicelal.supervpn_project.MainActivity;
import com.abacicelal.supervpn_project.R;

import libv2ray.CoreCallbackHandler;
import libv2ray.CoreController;
import libv2ray.Libv2ray;

public class StealthVpnService extends VpnService implements CoreCallbackHandler {

    private static final String TAG = "StealthVpnService";

    public static final String ACTION_START = "com.abacicelal.supervpn_project.STEALTH_START";
    public static final String ACTION_STOP = "com.abacicelal.supervpn_project.STEALTH_STOP";
    public static final String EXTRA_CONFIG_JSON = "config_json";
    public static final String EXTRA_SERVER_NAME = "server_name";
    public static final String EXTRA_PROTOCOL_NAME = "protocol_name";
    
    public static final String BROADCAST_VPN_STATE = "com.abacicelal.supervpn_project.VPN_STATE";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_ERROR_MESSAGE = "error_message";
    
    public static final String STATE_CONNECTING = "CONNECTING";
    public static final String STATE_CONNECTED = "CONNECTED";
    public static final String STATE_DISCONNECTED = "DISCONNECTED";
    public static final String STATE_ERROR = "ERROR";

    private static final String CHANNEL_ID    = "stealth_vpn_channel";
    private static final int NOTIFICATION_ID = 2;

    private CoreController coreController;
    private ParcelFileDescriptor tunFd;
    private boolean isRunning = false;

    private String serverName = "VPN Sunucusu";
    private String protocolName = "V2Ray";
    private long connectionStartTime = 0;
    private final Handler notifHandler = new Handler(Looper.getMainLooper());
    private Runnable notifRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            File filesDir = getFilesDir();
            copyAssetToFiles("geoip.dat", filesDir);
            copyAssetToFiles("geosite.dat", filesDir);
            Libv2ray.initCoreEnv(filesDir.getAbsolutePath(), "");
            Log.i(TAG, "Xray env initialized. Version: " + Libv2ray.checkVersionX());
        } catch (Exception e) {
            Log.e(TAG, "Failed to init Xray env", e);
        }
    }

    private void copyAssetToFiles(String assetName, File destDir) {
        try {
            File out = new File(destDir, assetName);
            if (out.exists()) return;
            try (InputStream in = getAssets().open(assetName);
                 FileOutputStream fos = new FileOutputStream(out)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) fos.write(buf, 0, n);
            }
            Log.i(TAG, "Copied " + assetName + " to files dir");
        } catch (Exception e) {
            Log.w(TAG, "Could not copy " + assetName + " (may not exist): " + e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            String configJson = intent.getStringExtra(EXTRA_CONFIG_JSON);
            serverName = intent.getStringExtra(EXTRA_SERVER_NAME) != null
                    ? intent.getStringExtra(EXTRA_SERVER_NAME) : "VPN Sunucusu";
            protocolName = intent.getStringExtra(EXTRA_PROTOCOL_NAME) != null
                    ? intent.getStringExtra(EXTRA_PROTOCOL_NAME) : "V2Ray";
            if (configJson != null && !configJson.isEmpty()) {
                startStealth(configJson);
            } else {
                Log.e(TAG, "No config JSON provided");
                stopSelf();
            }
        } else if (ACTION_STOP.equals(action)) {
            stopStealth();
            return START_NOT_STICKY;
        }

        return START_STICKY;
    }

    private void startStealth(String configJson) {
        if (isRunning) {
            Log.w(TAG, "Already running, stopping first");
            stopStealth();
        }

        broadcastState(STATE_CONNECTING, null);
        
        try {
            createNotificationChannel();
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(NOTIFICATION_ID, buildNotification("Stealth VPN Connecting..."), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(NOTIFICATION_ID, buildNotification("Stealth VPN Connecting..."));
            }
        } catch (Exception e) {
            Log.e(TAG, "CRASH in startForeground: " + e.getMessage(), e);
            broadcastState(STATE_ERROR, "Failed to start foreground service: " + e.getMessage());
            stopSelf();
            return;
        }

        try {
            Builder builder = new Builder();
            builder.setSession("DataGuard Stealth VPN");
            builder.addAddress("10.1.0.1", 30);
            builder.addDnsServer("1.1.1.1");
            builder.addDnsServer("8.8.8.8");
            builder.addRoute("0.0.0.0", 0);
            builder.setMtu(1500);

            try {
                builder.addDisallowedApplication(getPackageName());
            } catch (Exception e) {
                Log.w(TAG, "Could not disallow self", e);
            }

            tunFd = builder.establish();
            if (tunFd == null) {
                Log.e(TAG, "Failed to establish TUN interface");
                broadcastState(STATE_ERROR, "Failed to establish VPN interface");
                stopSelf();
                return;
            }

            Log.i(TAG, "TUN interface established, starting Xray core...");
            coreController = Libv2ray.newCoreController(this);
            coreController.startLoop(configJson, tunFd.getFd());
            isRunning = true;
            connectionStartTime = System.currentTimeMillis();
            startNotificationTimer();
            broadcastState(STATE_CONNECTED, null);
            Log.i(TAG, "Stealth VPN started successfully");

        } catch (Exception e) {
            Log.e(TAG, "Failed to start Stealth VPN", e);
            broadcastState(STATE_ERROR, "Stealth VPN error: " + e.getMessage());
            stopStealth();
        }
    }

    private void stopStealth() {
        Log.i(TAG, "Stopping Stealth VPN...");

        if (coreController != null) {
            try {
                coreController.stopLoop();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping Xray core", e);
            }
            coreController = null;
        }

        if (tunFd != null) {
            try {
                tunFd.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing TUN fd", e);
            }
            tunFd = null;
        }

        isRunning = false;
        stopNotificationTimer();
        getSharedPreferences("vpn_prefs", MODE_PRIVATE)
                .edit().putBoolean("vpn_is_running", false).apply();
        sendDisconnectApiFromService();
        broadcastState(STATE_DISCONNECTED, null);
        stopForeground(true);
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) nm.cancel(NOTIFICATION_ID);
        stopSelf();
        Log.i(TAG, "Stealth VPN stopped");
    }

    private void sendDisconnectApiFromService() {
        android.content.SharedPreferences prefs =
                getSharedPreferences("VPN_PREFS", android.content.Context.MODE_PRIVATE);
        String connectTime = prefs.getString("pending_connect_time", null);
        if (connectTime == null) return;

        long logId = prefs.getLong("pending_log_id", 0);
        long bytesStart = prefs.getLong("pending_bytes_start", 0);

        // pending_connect_time'ı hemen sil: onDestroy() ikinci kez çağırırsa tekrar göndermesin
        prefs.edit().remove("pending_connect_time").apply();

        double deltaMb = 0;
        if (bytesStart > 0) {
            long bytesEnd = android.net.TrafficStats.getTotalRxBytes()
                    + android.net.TrafficStats.getTotalTxBytes();
            long delta = bytesEnd - bytesStart;
            if (delta > 0) deltaMb = delta / (1024.0 * 1024.0);
        }
        final double finalDeltaMb = Math.round(deltaMb * 100.0) / 100.0;
        final String disconnectTime = new java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US)
                .format(new java.util.Date());
        final long finalLogId = logId;

        new Thread(() -> {
            try {
                com.abacicelal.supervpn_project.remote.ApiService api =
                        com.abacicelal.supervpn_project.remote.RetrofitClient
                                .getApiService(getApplicationContext());
                java.util.Map<String, Object> body = new java.util.HashMap<>();
                body.put("disconnectTime", disconnectTime);
                if (finalDeltaMb > 0) body.put("dataUsedMb", finalDeltaMb);

                retrofit2.Response<?> resp;
                if (finalLogId > 0) {
                    resp = api.updateLogDisconnect(finalLogId, body).execute();
                } else {
                    resp = api.updateActiveLogDisconnect(body).execute();
                }
                Log.d(TAG, "Service disconnect API: " + (resp.isSuccessful() ? "OK" : "code=" + resp.code()));
            } catch (Exception e) {
                Log.w(TAG, "Service disconnect API failed: " + e.getMessage());
            } finally {
                prefs.edit()
                        .remove("pending_log_id")
                        .remove("pending_bytes_start")
                        .remove("pending_server_id")
                        .remove("pending_device_id")
                        .remove("pending_vpn_ip")
                        .apply();
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        stopStealth();
        super.onDestroy();
    }

    @Override
    public void onRevoke() {
        stopStealth();
        super.onRevoke();
    }

    // --- CoreCallbackHandler ---

    @Override
    public long startup() {
        Log.i(TAG, "Xray core startup callback");
        return 0;
    }

    @Override
    public long shutdown() {
        Log.i(TAG, "Xray core shutdown callback");
        return 0;
    }

    @Override
    public long onEmitStatus(long level, String message) {
        Log.d(TAG, "Xray status [" + level + "]: " + message);
        if (message != null && message.toLowerCase().contains("error")) {
            Log.e(TAG, "Xray core error detected: " + message);
        }
        return 0;
    }
    
    private void broadcastState(String state, String errorMessage) {
        Intent intent = new Intent(BROADCAST_VPN_STATE);
        intent.putExtra(EXTRA_STATE, state);
        if (errorMessage != null) {
            intent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        }
        try {
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.d(TAG, "Broadcast state: " + state + (errorMessage != null ? " - " + errorMessage : ""));
        } catch (Exception e) {
            Log.e(TAG, "Failed to broadcast state", e);
        }
    }

    // --- Bildirim zamanlayıcısı ---

    private void startNotificationTimer() {
        stopNotificationTimer();
        notifRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;
                refreshNotification();
                notifHandler.postDelayed(this, 1000);
            }
        };
        notifHandler.post(notifRunnable);
    }

    private void stopNotificationTimer() {
        if (notifRunnable != null) {
            notifHandler.removeCallbacks(notifRunnable);
            notifRunnable = null;
        }
    }

    private void refreshNotification() {
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, buildConnectedNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, buildConnectedNotification());
        }
    }

    // --- Bildirim yönetimi ---

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm == null) return;
            NotificationChannel rich = new NotificationChannel(CHANNEL_ID, "DataGuard VPN", NotificationManager.IMPORTANCE_LOW);
            rich.setDescription("VPN bağlantı durumu");
            rich.setShowBadge(false);
            nm.createNotificationChannel(rich);
        }
    }

    private Notification buildNotification(String text) {
        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, notifIntent,
                PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("DataGuard VPN")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_vpn_connected)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
    }

    private Notification buildConnectedNotification() {
        // Uygulama açma niyeti
        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openPi = PendingIntent.getActivity(this, 0, openIntent,
                PendingIntent.FLAG_IMMUTABLE);

        // Bağlantı kesme butonu için niyeti
        Intent stopIntent = new Intent(this, StealthVpnService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getService(this, 0, stopIntent,
                PendingIntent.FLAG_IMMUTABLE);

        long elapsed = (System.currentTimeMillis() - connectionStartTime) / 1000;
        String duration = String.format("%02d:%02d:%02d",
                elapsed / 3600, (elapsed % 3600) / 60, elapsed % 60);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_vpn_connected)
                .setContentTitle(getString(R.string.notif_vpn_connected_title, serverName))
                .setContentText(getString(R.string.notif_vpn_content, protocolName, duration))
                .setColor(0xFF1565C0) // Mavi ton
                .setColorized(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(openPi)
                .addAction(R.drawable.ic_vpn_connected, getString(R.string.notif_disconnect_action), stopPi)
                .build();
    }
}
