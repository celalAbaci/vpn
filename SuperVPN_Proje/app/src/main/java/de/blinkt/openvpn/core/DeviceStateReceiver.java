package de.blinkt.openvpn.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import java.util.LinkedList;

public class DeviceStateReceiver extends BroadcastReceiver implements VpnStatus.ByteCountListener {
    private final Handler mPostHandler;
    private boolean mIsUserPaused;

    // Dinleyiciler listesi
    private final LinkedList<VpnStatus.StateListener> stateListeners = new LinkedList<>();

    // Constructor - Yönetim arayüzünü (Management) alır
    public DeviceStateReceiver(OpenVPNManagement management) {
        super();
        mPostHandler = new Handler();
    }

    @Override
    public void updateByteCount(long in, long out, long diffIn, long diffOut) {
        // Byte sayacını burada işlememize gerek yok (Headless mod)
    }

    public void userPause(boolean pause) {
        mIsUserPaused = pause;
        // Kullanıcı durdurduysa yapılacak işlemler (Şimdilik boş)
    }

    public boolean isUserPaused() {
        return mIsUserPaused;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            networkStateChange(context);
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            // Ekran kapandı (İstersen VPN'i duraklatabilirsin ama genelde devam etmesi istenir)
        } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
            // Ekran açıldı
            networkStateChange(context);
        }
    }

    public void networkStateChange(Context context) {
        NetworkInfo networkInfo = getCurrentNetworkInfo(context);
        String networkState;

        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            String subtype = networkInfo.getSubtypeName();
            if (subtype != null && !subtype.isEmpty()) {
                type += " (" + subtype + ")";
            }
            networkState = type;
        } else {
            networkState = "No Network";
        }

        // R.string kullanmadan durumu logluyoruz
        VpnStatus.logInfo("Network Status Changed: " + networkState);
    }

    private NetworkInfo getCurrentNetworkInfo(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMgr.getActiveNetworkInfo();
    }
}