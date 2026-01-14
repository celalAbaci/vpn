package de.blinkt.openvpn.core;

import android.content.Context;
import android.util.Log;
import java.util.LinkedList;

public class VpnStatus {
    public static final String TAG = "OpenVPN";

    // Durum Dinleyicileri
    private static final LinkedList<StateListener> stateListeners = new LinkedList<>();
    private static final LinkedList<ByteCountListener> byteCountListeners = new LinkedList<>();

    // Arayüzler
    public interface StateListener {
        void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level, android.content.Intent intent);
        void setConnectedVPN(String uuid);
    }

    public interface ByteCountListener {
        void updateByteCount(long in, long out, long diffIn, long diffOut);
    }

    // Loglama Metotları (Basitleştirildi)
    public static void logInfo(String message) {
        Log.i(TAG, message);
    }

    public static void logDebug(String message) {
        Log.d(TAG, message);
    }

    public static void logError(String message) {
        Log.e(TAG, message);
    }

    public static void logError(String msg, Object... args) {
        Log.e(TAG, String.format(msg, args));
    }

    public static void logWarning(String message) {
        Log.w(TAG, message);
    }

    public static void logException(Throwable e) {
        Log.e(TAG, "Exception", e);
    }

    public static void logException(String msg, Throwable e) {
        Log.e(TAG, msg, e);
    }

    // UI'daki durumu güncelleyen ana metot
    public static void updateStateString(String state, String msg, int resid, ConnectionStatus level) {
        updateStateString(state, msg, resid, level, null);
    }

    public static void updateStateString(String state, String msg, int resid, ConnectionStatus level, android.content.Intent intent) {
        Log.i(TAG, "VPN Status Update: " + state + " (" + level + ") - " + msg);

        synchronized (stateListeners) {
            for (StateListener listener : stateListeners) {
                listener.updateState(state, msg, resid, level, intent);
            }
        }
    }

    public static void setConnectedVPNProfile(String uuid) {
        synchronized (stateListeners) {
            for (StateListener listener : stateListeners) {
                listener.setConnectedVPN(uuid);
            }
        }
    }

    public static void addStateListener(StateListener listener) {
        synchronized (stateListeners) {
            if (!stateListeners.contains(listener))
                stateListeners.add(listener);
        }
    }

    public static void removeStateListener(StateListener listener) {
        synchronized (stateListeners) {
            stateListeners.remove(listener);
        }
    }

    public static void addByteCountListener(ByteCountListener listener) {
        synchronized (byteCountListeners) {
            if (!byteCountListeners.contains(listener)) {
                byteCountListeners.add(listener);
            }
        }
    }

    public static void removeByteCountListener(ByteCountListener listener) {
        synchronized (byteCountListeners) {
            byteCountListeners.remove(listener);
        }
    }

    public static String getLastCleanLogMessage(Context c) {
        return "Log cleared.";
    }

    public static void flushLog() {
        // Log temizleme
    }
}