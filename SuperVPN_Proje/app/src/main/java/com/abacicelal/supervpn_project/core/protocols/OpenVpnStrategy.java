package com.abacicelal.supervpn_project.core.protocols;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import de.blinkt.openvpn.api.IOpenVPNAPIService;
import de.blinkt.openvpn.api.IOpenVPNStatusCallback;

/**
 * Strategy implementation for OpenVPN using the embedded ics-openvpn library.
 */
public class OpenVpnStrategy implements IVpnStrategy {

    private static final String TAG = "OpenVpnStrategy";
    private IOpenVPNAPIService mService;
    private VpnStatusListener listener;
    private boolean isBound = false;
    private boolean isConnected = false;

    // Store pending connection details to execute once service is bound
    private String pendingConfig;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IOpenVPNAPIService.Stub.asInterface(service);
            isBound = true;
            Log.d(TAG, "OpenVPN Service Connected");
            try {
                mService.registerStatusCallback(mCallback);

                // If we have a pending connection request, check permission then start
                if (pendingConfig != null) {
                    processPendingConfig();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Error registering status callback", e);
                if (listener != null) listener.onError("Service connection error: " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            isBound = false;
            Log.d(TAG, "OpenVPN Service Disconnected");
        }
    };

    // Context used for binding (should be Application Context)
    private Context bindContext;

    private final IOpenVPNStatusCallback mCallback = new IOpenVPNStatusCallback.Stub() {
        @Override
        public void newStatus(String uuid, String state, String message, String level) throws RemoteException {
            Log.d(TAG, "Status: " + state + " (" + message + ")");
            if (listener != null) {
                listener.onStatusChanged(state, message, level);

                if ("CONNECTED".equals(state)) {
                    isConnected = true;
                    listener.onConnected();
                } else if ("NONETWORK".equals(state) || "DISCONNECTED".equals(state) || "AUTH_FAILED".equals(state)) {
                    isConnected = false;
                    listener.onDisconnected();
                }
            }
        }
    };

    @Override
    public void connect(Context context, String configContent, String username, String password) {
        if (configContent == null || configContent.isEmpty()) {
            if (listener != null) listener.onError("Config content is empty");
            return;
        }

        pendingConfig = configContent;

        // If not bound, bind first using Application Context
        if (!isBound || mService == null) {
            bindService(context.getApplicationContext());
        } else {
            // Already bound, proceed directly
            processPendingConfig();
        }
    }

    private void processPendingConfig() {
        if (mService == null || bindContext == null) return;

        try {
            // Check permissions using the bound service
            // prepare() requires the package name of the app
            Intent intent = mService.prepare(bindContext.getPackageName());
            if (intent != null) {
                // Permission required
                if (listener != null) {
                    listener.onPermissionRequired(intent);
                }
            } else {
                // Permission granted, start VPN
                mService.startVPN(pendingConfig);
                pendingConfig = null; // Clear pending
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error checking permission or starting VPN", e);
            if (listener != null) listener.onError("VPN Error: " + e.getMessage());
        }
    }

    @Override
    public void disconnect(Context context) {
        if (mService != null) {
            try {
                mService.disconnect();
            } catch (RemoteException e) {
                Log.e(TAG, "Error disconnecting", e);
            }
        }
        unbindService();
        isConnected = false;
        if (listener != null) listener.onDisconnected();
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void setListener(VpnStatusListener listener) {
        this.listener = listener;
    }

    @Override
    public String getProtocolName() {
        return "OPENVPN";
    }

    private void bindService(Context appContext) {
        if (isBound) return;

        this.bindContext = appContext;
        Intent intent = new Intent("de.blinkt.openvpn.api.IOpenVPNAPIService");
        intent.setPackage(appContext.getPackageName());

        // Bind to service
        try {
            appContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.e(TAG, "Failed to bind to OpenVPN Service", e);
            if (listener != null) listener.onError("Failed to bind service: " + e.getMessage());
        }
    }

    private void unbindService() {
        if (isBound && bindContext != null) {
            try {
                if (mService != null) mService.unregisterStatusCallback(mCallback);
                bindContext.unbindService(mConnection);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering/unbinding", e);
            }
            isBound = false;
            mService = null;
            bindContext = null;
        }
    }
}
