package de.blinkt.openvpn.core;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
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
import android.os.IBinder;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Vector;

import de.blinkt.openvpn.VpnProfile;

public class OpenVPNService extends VpnService implements VpnStatus.StateListener, VpnStatus.ByteCountListener {
    public static final String START_SERVICE = "de.blinkt.openvpn.START_SERVICE";
    public static final String DISCONNECT_VPN = "de.blinkt.openvpn.DISCONNECT_VPN";
    private static final String NOTIFICATION_CHANNEL_ID = "vpn_channel";
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
        VpnProfile profile = ProfileManager.get(this, intent.getStringExtra(VpnProfile.EXTRA_PROFILEUUID), 0, 0);

        if (profile == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        mProfile = profile;
        mProfile.checkForRestart(this);

        // Bildirimi oluştur
        Notification notification = createNotification(ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET);

        // Android Sürümüne Göre Foreground Başlatma
        try {
            if (Build.VERSION.SDK_INT >= 34) {
                // Android 14+
                startForeground(BIG_PICTURE_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else if (Build.VERSION.SDK_INT >= 29) {
                // Android 10 - 13 arası
                startForeground(BIG_PICTURE_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE);
            } else {
                // Android 9 ve altı
                startForeground(BIG_PICTURE_NOTIFICATION_ID, notification);
            }
        } catch (Exception e) {
            // Herhangi bir hata durumunda (Örn: Cihaz üreticisi kısıtlamaları)
            try {
                startForeground(BIG_PICTURE_NOTIFICATION_ID, notification);
            } catch (Exception ex) {
                VpnStatus.logError("Failed to start foreground service: " + ex.getLocalizedMessage());
            }
        }

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
        mManagement = new OpenVpnManagementThread(mProfile, this);
        if (mManagement.openManagementInterface(this)) {
            Thread mgtThread = new Thread(mManagement, "OpenVPNManagementThread");
            mgtThread.start();

            String nativeDir = getCacheDir().getAbsolutePath();
            String tmpDir = getCacheDir().getAbsolutePath();
            String[] argv = VPNLaunchHelper.buildOpenvpnArgv(this);

            OpenVPNThread processThread = new OpenVPNThread(this, argv, nativeDir, tmpDir);
            mProcessThread = new Thread(processThread, "OpenVPNProcessThread");
            mProcessThread.start();

            try {
                Thread.sleep(500);
                if (processThread.getOpenVPNStdin() != null) {
                    mProfile.writeConfigFileOutput(this, processThread.getOpenVPNStdin());
                } else {
                    VpnStatus.logError("OpenVPN Stdin is null!");
                }
            } catch (Exception e) {
                VpnStatus.logError("Error writing config to OpenVPN: " + e.getLocalizedMessage());
            }
        }
    }

    public void stopVPN(boolean replace) {
        if (mManagement != null) {
            mManagement.stopVPN(replace);
        }
        if (mProcessThread != null) {
            mProcessThread.interrupt();
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level, Intent intent) {
        if (level == ConnectionStatus.LEVEL_CONNECTED) {
            mStarting = false;
        }

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            try {
                // Android 13 (Tiramisu) ve üzeri için İzin Kontrolü
                if (Build.VERSION.SDK_INT >= 33) { // Build.VERSION_CODES.TIRAMISU yerine 33 yazdık, garanti olsun
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        // İzin yoksa bildirim gönderme (sessizce çık)
                        return;
                    }
                }

                mNotificationManager.notify(BIG_PICTURE_NOTIFICATION_ID, createNotification(level));
            } catch (Exception e) {
                VpnStatus.logError("Error updating notification: " + e.getLocalizedMessage());
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

    private Notification createNotification(ConnectionStatus level) {
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? NOTIFICATION_CHANNEL_ID : "";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);

        builder.setContentTitle("SuperVPN");
        String stateStr = "";

        // Switch-case'i if-else'e çevirdik, bazen enum switch'lerinde sorun çıkabiliyor
        if (level == ConnectionStatus.LEVEL_CONNECTED) stateStr = "Connected";
        else if (level == ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET) stateStr = "Connecting...";
        else if (level == ConnectionStatus.LEVEL_NOTCONNECTED) stateStr = "Disconnected";
        else stateStr = "VPN Status: " + level.toString();

        builder.setContentText(stateStr);
        builder.setSmallIcon(android.R.drawable.ic_lock_lock);
        builder.setOngoing(level != ConnectionStatus.LEVEL_NOTCONNECTED);

        Intent intent = new Intent(this, com.abacicelal.supervpn_project.MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pi);

        return builder.build();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "VPN Status", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);
    }
}