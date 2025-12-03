package com.abacicelal.supervpn_project;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import java.io.IOException;

public class MyVpnService extends VpnService {

    private ParcelFileDescriptor vpnInterface = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        if (intent != null) {
            String config = intent.getStringExtra("VPN_CONFIG");
            String protocol = intent.getStringExtra("VPN_PROTOCOL");

            // Loglama veya işlem başlatma
            // Gerçek uygulamada burada OpenVPN native kütüphanesi başlatılır.

            try {
                if (vpnInterface == null) {
                    Builder builder = new Builder();
                    builder.setSession("MyVPNService")
                            .addAddress("10.0.0.2", 24)
                            .addRoute("0.0.0.0", 0)
                            .addDnsServer("8.8.8.8");
                    vpnInterface = builder.establish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (vpnInterface != null) {
                vpnInterface.close();
                vpnInterface = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}