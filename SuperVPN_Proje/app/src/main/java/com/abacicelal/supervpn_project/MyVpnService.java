package com.abacicelal.supervpn_project;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import java.io.IOException;

public class MyVpnService extends VpnService {

    private ParcelFileDescriptor vpnInterface = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Builder builder = new Builder();
        builder.setSession("MyVPNService")
                .addAddress("10.0.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8");

        vpnInterface = builder.establish();
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