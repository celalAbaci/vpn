package de.blinkt.openvpn.core;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Vector;

import de.blinkt.openvpn.VpnProfile;

public class OpenVPNService extends VpnService implements VpnStatus.StateListener, VpnStatus.ByteCountListener {
    public static final String START_SERVICE = "de.blinkt.openvpn.START_SERVICE";
    public static final String DISCONNECT_VPN = "de.blinkt.openvpn.DISCONNECT_VPN";
    public static final String EXTRA_SERVER_NAME   = "server_name";
    public static final String EXTRA_PROTOCOL_NAME = "protocol_name";
    private static final String NOTIFICATION_CHANNEL_ID  = "vpn_channel";
    private static final int BIG_PICTURE_NOTIFICATION_ID = 1337;

    private final Vector<String> mDnslist = new Vector<>();
    private final NetworkSpace mRoutes = new NetworkSpace();

    private Thread mProcessThread = null;
    private VpnProfile mProfile;
    private CIDRIP mLocalIP = null;
    private int mMtu;
    private DeviceStateReceiver mDeviceStateReceiver;
    private boolean mDisplayBytecount = false;
    private boolean mStarting = false;
    private OpenVpnManagementThread mManagement;
    private OpenVPNThread mVpnThread;

    private String serverName = "VPN Sunucusu";
    private String protocolName = "OpenVPN";
    private long connectionStartTime = 0;
    private final Handler notifHandler = new Handler(Looper.getMainLooper());
    private Runnable notifRunnable;

    @Override
    public IBinder onBind(Intent intent) {
        String action = intent.getAction();
        if (VpnService.SERVICE_INTERFACE.equals(action)) {
            return super.onBind(intent);
        }
        return null;
    }

    @Override
    public void onRevoke() {
        VpnStatus.logError("VPN permission revoked by OS.");
        stopVPN(false);
        super.onRevoke();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        VpnStatus.addStateListener(this);
        VpnStatus.addByteCountListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
    }

    @Override
    public void onDestroy() {
        stopVPN(false);
        VpnStatus.removeStateListener(this);
        VpnStatus.removeByteCountListener(this);
        unregisterDeviceStateReceiver();
        super.onDestroy();
    }

    private void unregisterDeviceStateReceiver() {
        if (mDeviceStateReceiver != null) {
            try {
                VpnStatus.removeByteCountListener(mDeviceStateReceiver);
                unregisterReceiver(mDeviceStateReceiver);
            } catch (IllegalArgumentException ignored) {}
            mDeviceStateReceiver = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Android 8+: startForeground() HEMEN çağrılmalı — herhangi bir gecikme crash'e yol açar
        Notification notification = createNotification(ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET);
        try {
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(BIG_PICTURE_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else if (Build.VERSION.SDK_INT >= 29) {
                startForeground(BIG_PICTURE_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE);
            } else {
                startForeground(BIG_PICTURE_NOTIFICATION_ID, notification);
            }
        } catch (Exception e) {
            try { startForeground(BIG_PICTURE_NOTIFICATION_ID, notification); } catch (Exception ignored) {}
        }

        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (START_SERVICE.equals(action)) {
                return onStartService(intent);
            } else if (DISCONNECT_VPN.equals(action)) {
                stopVPN(false);
                return START_NOT_STICKY;
            }
        }
        return START_NOT_STICKY;
    }

    private int onStartService(Intent intent) {
        if (intent.hasExtra(EXTRA_SERVER_NAME)) serverName = intent.getStringExtra(EXTRA_SERVER_NAME);
        if (intent.hasExtra(EXTRA_PROTOCOL_NAME)) protocolName = intent.getStringExtra(EXTRA_PROTOCOL_NAME);
        VpnProfile profile = ProfileManager.get(this, intent.getStringExtra(VpnProfile.EXTRA_PROFILEUUID), 0, 0);
        if (profile == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        mProfile = profile;
        mProfile.checkForRestart(this);
        startOpenVPN();
        return START_STICKY;
    }

    private void startOpenVPN() {
        VpnStatus.logInfo("Starting VPN...");
        mStarting = true;
        stopOldVPN();

        startOpenVPNThread();

        if (mDeviceStateReceiver == null) {
            mDeviceStateReceiver = new DeviceStateReceiver(mManagement);
            VpnStatus.addByteCountListener(mDeviceStateReceiver);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            registerReceiver(mDeviceStateReceiver, filter);
        }
    }

    private void stopOldVPN() {
        if (mManagement != null) {
            mManagement.stopVPN(true);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
    }

    private void startOpenVPNThread() {
        String nativeLibDir = getApplicationInfo().nativeLibraryDir;
        String tmpDir = getCacheDir().getAbsolutePath();
        String[] argv = new String[0];

        OpenVPNThread vpnThread = new OpenVPNThread(this, argv, nativeLibDir, tmpDir);
        mVpnThread = vpnThread;

        if (mProfile != null && mProfile.mInlineConfig != null && !mProfile.mInlineConfig.isEmpty()) {
            VpnStatus.logInfo("OpenVPNService: Passing inline config to JNI thread, length=" + mProfile.mInlineConfig.length());
            vpnThread.setConfigContent(mProfile.mInlineConfig);
        } else {
            VpnStatus.logError("OpenVPNService: No inline config in profile, checking profile source...");
            if (mProfile != null && mProfile.mInlineConfig != null) {
                vpnThread.setConfigContent(mProfile.mInlineConfig);
            }
        }

        mProcessThread = new Thread(vpnThread, "OpenVPNProcessThread");
        mProcessThread.start();
        mManagement = null;

        VpnStatus.logInfo("OpenVPNThread started (JNI mode).");
    }


    public void stopVPN(boolean replace) {
        stopNotificationTimer();
        if (mVpnThread != null) {
            mVpnThread.stopProcess();
        }
        if (mManagement != null) {
            mManagement.stopVPN(replace);
        }
        if (mProcessThread != null) {
            mProcessThread.interrupt();
        }
        stopForeground(true);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(BIG_PICTURE_NOTIFICATION_ID);
        stopSelf();
    }

    @Override
    public void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level, Intent intent) {
        if (level == ConnectionStatus.LEVEL_CONNECTED) {
            mStarting = false;
            connectionStartTime = System.currentTimeMillis();
            startNotificationTimer();
        } else if (level == ConnectionStatus.LEVEL_NOTCONNECTED) {
            stopNotificationTimer();
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(BIG_PICTURE_NOTIFICATION_ID, createNotification(level), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(BIG_PICTURE_NOTIFICATION_ID, createNotification(level));
            }
        }
    }

    @Override
    public void setConnectedVPN(String uuid) {}

    @Override
    public void updateByteCount(long in, long out, long diffIn, long diffOut) {
        if (mDisplayBytecount) { }
    }

    public void addDNS(String dns) {
        mDnslist.add(dns);
    }

    public void addRoute(CIDRIP route, boolean include) {
        mRoutes.addIP(route, include);
    }

    public void addRoutev6(String network, String device) {}

    public void setMtu(int mtu) {
        mMtu = mtu;
    }

    public void setLocalIP(CIDRIP cdrip) {
        mLocalIP = cdrip;
    }

    public void setLocalIPv6(String ipv6) {}

    public void trigger_doBuild() {
        Builder builder = new Builder();
        builder.setMtu(mMtu);

        try {
            if (mLocalIP != null) builder.addAddress(mLocalIP.mIp, mLocalIP.len);
        } catch (IllegalArgumentException e) {
            VpnStatus.logError("Error setting local IP: " + e.getLocalizedMessage());
            return;
        }

        for (String dns : mDnslist) {
            try {
                builder.addDnsServer(dns);
            } catch (IllegalArgumentException e) {
                VpnStatus.logError("Invalid DNS: " + dns);
            }
        }

        for (NetworkSpace.IpAddress route : mRoutes.getPositiveIPList()) {
            try {
                builder.addRoute(route.getIPv4Address(), route.networkMask);
            } catch (IllegalArgumentException e) {
                VpnStatus.logError("Error adding route: " + e.getLocalizedMessage());
            }
        }

        builder.setSession(mProfile.mName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false);
        }

        try {
            builder.establish();
        } catch (Exception e) {
            VpnStatus.logError("Error establishing VPN: " + e.getLocalizedMessage());
        }
    }

    public void openvpnStopped() {
        stopVPN(false);
    }

    private void startNotificationTimer() {
        stopNotificationTimer();
        notifRunnable = new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= 34) {
                    startForeground(BIG_PICTURE_NOTIFICATION_ID, buildConnectedNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
                } else {
                    startForeground(BIG_PICTURE_NOTIFICATION_ID, buildConnectedNotification());
                }
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

    private Notification buildConnectedNotification() {
        Intent openIntent = new Intent(this, com.abacicelal.supervpn_project.MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openPi = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, OpenVPNService.class);
        stopIntent.setAction(DISCONNECT_VPN);
        PendingIntent stopPi = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        long elapsed = (System.currentTimeMillis() - connectionStartTime) / 1000;
        String duration = String.format("%02d:%02d:%02d",
                elapsed / 3600, (elapsed % 3600) / 60, elapsed % 60);

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(com.abacicelal.supervpn_project.R.drawable.ic_vpn_connected)
                .setContentTitle("VPN Bağlı  —  " + serverName)
                .setContentText("Protokol: " + protocolName + "   •   Süre: " + duration)
                .setColor(0xFF1565C0)
                .setColorized(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(openPi)
                .addAction(com.abacicelal.supervpn_project.R.drawable.ic_vpn_connected, "Bağlantıyı Kes", stopPi)
                .build();
    }

    private Notification createNotification(ConnectionStatus level) {
        Intent intent = new Intent(this, com.abacicelal.supervpn_project.MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        String text;
        if (level == ConnectionStatus.LEVEL_CONNECTED) text = "VPN Bağlandı";
        else if (level == ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET) text = "VPN Bağlanıyor...";
        else text = "VPN Bağlantısı Kesildi";

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("DataGuard VPN")
                .setContentText(text)
                .setSmallIcon(com.abacicelal.supervpn_project.R.drawable.ic_vpn_connected)
                .setOngoing(level != ConnectionStatus.LEVEL_NOTCONNECTED)
                .setContentIntent(pi)
                .build();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm == null) return;
        NotificationChannel rich = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "DataGuard VPN", NotificationManager.IMPORTANCE_LOW);
        rich.setDescription("VPN bağlantı durumu");
        rich.setShowBadge(false);
        nm.createNotificationChannel(rich);
    }
}