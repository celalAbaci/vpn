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

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.abacicelal.supervpn_project.MainActivity;
import com.abacicelal.supervpn_project.R;

import java.io.File;

import libv2ray.CoreCallbackHandler;
import libv2ray.CoreController;
import libv2ray.Libv2ray;

/**
 * Xray-core (LibXray) tabanlı VPN servisi.
 *
 * LibXray (Xray-core) araştırma dokümanı doğrultusunda uygulanmıştır:
 * - GeoData dosyaları assets'ten filesDir'e çıkarılır (C/Go layer doğrudan erişebilsin diye)
 * - TUN FD, coreController.startLoop() ile çekirdeğe iletilir
 * - Uygulama paketi VPN tünelinden dışarıda bırakılır (routing loop önlemi)
 * - Bellek limiti Libv2ray.setMemoryLimit() ile kontrol altında tutulur
 */
public class XrayVpnService extends VpnService implements CoreCallbackHandler {

    private static final String TAG = "XrayVpnService";

    public static final String ACTION_START = "com.abacicelal.supervpn_project.XRAY_START";
    public static final String ACTION_STOP  = "com.abacicelal.supervpn_project.XRAY_STOP";
    public static final String EXTRA_CONFIG_JSON   = "xray_config_json";
    public static final String EXTRA_SERVER_NAME   = "server_name";
    public static final String EXTRA_PROTOCOL_NAME = "protocol_name";

    // StealthVpnService ile aynı broadcast action — MainActivity'deki mevcut receiver her ikisini de yakalar
    public static final String BROADCAST_VPN_STATE = "com.abacicelal.supervpn_project.VPN_STATE";
    public static final String EXTRA_STATE         = "state";
    public static final String EXTRA_ERROR_MESSAGE = "error_message";

    public static final String STATE_CONNECTING  = "CONNECTING";
    public static final String STATE_CONNECTED   = "CONNECTED";
    public static final String STATE_DISCONNECTED = "DISCONNECTED";
    public static final String STATE_ERROR        = "ERROR";

    private static final String CHANNEL_ID    = "xray_vpn_channel";
    private static final int    NOTIFICATION_ID = 3;

    // 64 MB bellek limiti (araştırma dokümanı: OOM riskini azaltır)
    private static final long XRAY_MEMORY_LIMIT_BYTES = 64L * 1024 * 1024;

    private CoreController coreController;
    private ParcelFileDescriptor tunFd;
    private boolean isRunning = false;

    private String serverName = "VPN Sunucusu";
    private String protocolName = "Xray";
    private long connectionStartTime = 0;
    private final Handler notifHandler = new Handler(Looper.getMainLooper());
    private Runnable notifRunnable;

    // -----------------------------------------------------------------------
    // Yaşam döngüsü
    // -----------------------------------------------------------------------

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Libv2ray.initCoreEnv(getFilesDir().getAbsolutePath(), "");
            Log.i(TAG, "Xray çekirdeği başlatıldı. Sürüm: " + Libv2ray.checkVersionX());
        } catch (Exception e) {
            Log.e(TAG, "Xray ortamı başlatılamadı", e);
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
                    ? intent.getStringExtra(EXTRA_PROTOCOL_NAME) : "Xray";
            if (configJson != null && !configJson.isEmpty()) {
                startXray(configJson);
            } else {
                Log.e(TAG, "Config JSON bulunamadı");
                stopSelf();
            }
        } else if (ACTION_STOP.equals(action)) {
            stopXray();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopXray();
        super.onDestroy();
    }

    @Override
    public void onRevoke() {
        stopXray();
        super.onRevoke();
    }

    // -----------------------------------------------------------------------
    // Xray başlatma / durdurma
    // -----------------------------------------------------------------------

    private void startXray(String configJson) {
        if (isRunning) {
            Log.w(TAG, "Zaten çalışıyor, önce durduruluyor");
            stopXray();
        }

        broadcastState(STATE_CONNECTING, null);

        try {
            createNotificationChannel();
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(NOTIFICATION_ID, buildNotification("Xray VPN Bağlanıyor..."), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(NOTIFICATION_ID, buildNotification("Xray VPN Bağlanıyor..."));
            }
        } catch (Exception e) {
            Log.e(TAG, "startForeground hatası: " + e.getMessage(), e);
            broadcastState(STATE_ERROR, "Ön plan servisi başlatılamadı: " + e.getMessage());
            stopSelf();
            return;
        }

        try {
            // TUN arabirimi konfigürasyonu (araştırma: addRoute 0.0.0.0/0 tüm trafiği yönlendirir)
            Builder builder = new Builder();
            builder.setSession("DataGuard Xray VPN");
            builder.addAddress("10.2.0.1", 30);         // Stealth'ten farklı subnet (çakışmayı önle)
            builder.addDnsServer("1.1.1.1");
            builder.addDnsServer("8.8.8.8");
            builder.addRoute("0.0.0.0", 0);             // IPv4 tüm trafik
            builder.addRoute("::", 0);                   // IPv6 tüm trafik
            builder.setMtu(1500);

            // Araştırma dokümanı: sonsuz yönlendirme döngüsünü önlemek için
            // uygulamanın kendi paketini tünelden dışarıda bırak
            try {
                builder.addDisallowedApplication(getPackageName());
            } catch (Exception e) {
                Log.w(TAG, "Paket dışarıda bırakılamadı", e);
            }

            tunFd = builder.establish();
            if (tunFd == null) {
                Log.e(TAG, "TUN arabirimi oluşturulamadı");
                broadcastState(STATE_ERROR, "VPN arabirimi oluşturulamadı");
                stopSelf();
                return;
            }

            Log.i(TAG, "TUN arabirimi hazır (fd=" + tunFd.getFd() + "), Xray çekirdeği başlatılıyor...");

            coreController = Libv2ray.newCoreController(this);
            // startLoop: configJson doğrudan çekirdeğe, tunFd ağ trafiğini TUN'a yönlendirir
            coreController.startLoop(configJson, tunFd.getFd());

            isRunning = true;
            connectionStartTime = System.currentTimeMillis();
            startNotificationTimer();
            broadcastState(STATE_CONNECTED, null);
            Log.i(TAG, "Xray VPN başarıyla başlatıldı");

        } catch (Exception e) {
            Log.e(TAG, "Xray VPN başlatılamadı", e);
            broadcastState(STATE_ERROR, "Xray VPN hatası: " + e.getMessage());
            stopXray();
        }
    }

    private void stopXray() {
        Log.i(TAG, "Xray VPN durduruluyor...");

        if (coreController != null) {
            try {
                coreController.stopLoop();
            } catch (Exception e) {
                Log.e(TAG, "Xray çekirdeği durdurulurken hata", e);
            }
            coreController = null;
        }

        if (tunFd != null) {
            try {
                tunFd.close();
            } catch (Exception e) {
                Log.e(TAG, "TUN fd kapatılırken hata", e);
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
        Log.i(TAG, "Xray VPN durduruldu");
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

    // -----------------------------------------------------------------------
    // CoreCallbackHandler arayüzü
    // -----------------------------------------------------------------------

    @Override
    public long startup() {
        Log.i(TAG, "Xray çekirdeği başlangıç callback'i");
        return 0;
    }

    @Override
    public long shutdown() {
        Log.i(TAG, "Xray çekirdeği kapatma callback'i");
        return 0;
    }

    @Override
    public long onEmitStatus(long level, String message) {
        Log.d(TAG, "Xray durum [" + level + "]: " + message);
        if (message != null && message.toLowerCase().contains("error")) {
            Log.e(TAG, "Xray çekirdeği hata: " + message);
        }
        return 0;
    }

    // -----------------------------------------------------------------------
    // Yardımcı metodlar
    // -----------------------------------------------------------------------

    private void broadcastState(String state, String errorMessage) {
        Intent intent = new Intent(BROADCAST_VPN_STATE);
        intent.putExtra(EXTRA_STATE, state);
        if (errorMessage != null) {
            intent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        }
        try {
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.d(TAG, "Durum yayını: " + state + (errorMessage != null ? " - " + errorMessage : ""));
        } catch (Exception e) {
            Log.e(TAG, "Durum yayını başarısız", e);
        }
    }

    // -----------------------------------------------------------------------
    // Bildirim zamanlayıcısı
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    // Bildirim yönetimi
    // -----------------------------------------------------------------------

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
        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openPi = PendingIntent.getActivity(this, 0, openIntent,
                PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, XrayVpnService.class);
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
                .setColor(0xFF1565C0)
                .setColorized(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(openPi)
                .addAction(R.drawable.ic_vpn_connected, getString(R.string.notif_disconnect_action), stopPi)
                .build();
    }
}
