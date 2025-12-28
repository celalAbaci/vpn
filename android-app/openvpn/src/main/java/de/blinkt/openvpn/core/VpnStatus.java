package de.blinkt.openvpn.core;

import android.content.Intent;
import java.util.ArrayList;
import java.util.List;

public class VpnStatus {

    private static List<StateListener> listeners = new ArrayList<>();

    public interface StateListener {
        void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level, Intent intent);
        void setConnectedVPN(String uuid);
    }

    public static void addStateListener(StateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeStateListener(StateListener listener) {
        listeners.remove(listener);
    }

    public static void updateStateString(String state, String msg, int resId, ConnectionStatus level) {
        for (StateListener listener : listeners) {
            listener.updateState(state, msg, resId, level, null);
        }
    }
}
