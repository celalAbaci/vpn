package de.blinkt.openvpn.core;

import android.content.Intent;
import android.net.VpnService;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;

/**
 * OpenVPN Service implementation.
 * This class extends VpnService to manage the tunnel interface.
 */
public class OpenVPNService extends VpnService {

    private static final String TAG = "OpenVPNService";
    private Thread mVpnThread;
    private ParcelFileDescriptor mInterface;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            stopVpn();
            return START_NOT_STICKY;
        }

        // Retrieve profile info
        // String profileUUID = intent.getStringExtra("de.blinkt.openvpn.profileUUID");

        // Start VPN Thread
        if (mVpnThread != null) {
            mVpnThread.interrupt();
        }

        mVpnThread = new Thread(() -> {
            try {
                runVpn();
            } catch (Exception e) {
                Log.e(TAG, "Error running VPN", e);
                stopVpn();
            }
        });
        mVpnThread.start();

        return START_STICKY;
    }

    private void runVpn() throws Exception {
        Log.i(TAG, "Establishing VPN connection...");

        // Update Status: Connecting
        VpnStatus.updateStateString("CONNECTING", "Starting handshake...", R.string.state_connecting, ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED);

        // Configure the TUN interface
        // In a real OpenVPN implementation, these parameters come from the server push or config file.
        // We mock a standard configuration here.
        Builder builder = new Builder();
        builder.setMtu(1500);
        builder.addAddress("10.8.0.2", 32);
        builder.addRoute("0.0.0.0", 0);
        builder.addDnsServer("8.8.8.8");
        builder.setSession("SuperVPN Session");

        // Apply "block-outside-dns" logic if requested (simulated)
        // builder.addDnsServer("1.1.1.1");

        mInterface = builder.establish();

        if (mInterface == null) {
            Log.e(TAG, "Failed to establish VPN interface");
            VpnStatus.updateStateString("FAILED", "Interface creation failed", R.string.state_auth_failed, ConnectionStatus.LEVEL_AUTH_FAILED);
            return;
        }

        Log.i(TAG, "VPN Interface established: " + mInterface.getFileDescriptor());

        // Update Status: Connected
        VpnStatus.updateStateString("CONNECTED", "Tunnel established", R.string.state_connected, ConnectionStatus.LEVEL_CONNECTED);

        // Keep thread alive to maintain connection
        while (!Thread.interrupted()) {
            Thread.sleep(1000);
            // In a real app, we would read/write packets here using FileInputStream/FileOutputStream on mInterface.getFileDescriptor()
            // and forward them to the OpenVPN native binary or socket.
        }
    }

    private void stopVpn() {
        if (mInterface != null) {
            try {
                mInterface.close();
            } catch (Exception e) {
                // ignore
            }
            mInterface = null;
        }
        if (mVpnThread != null) {
            mVpnThread.interrupt();
            mVpnThread = null;
        }
        VpnStatus.updateStateString("DISCONNECTED", "VPN Stopped", R.string.state_disconnected, ConnectionStatus.LEVEL_NOTCONNECTED);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        stopVpn();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder(); // Simple binder
    }
}
