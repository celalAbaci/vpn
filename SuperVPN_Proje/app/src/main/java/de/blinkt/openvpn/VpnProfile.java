package de.blinkt.openvpn;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.UUID;
import java.util.Vector;

public class VpnProfile implements Serializable, Cloneable {
    public static final String EXTRA_PROFILEUUID = "de.blinkt.openvpn.profileUUID";
    public static final String EXTRA_PROFILE_VERSION = "de.blinkt.openvpn.profileVersion";
    public static final int MAX_LOG_LEVEL = 4;
    public static final int MIN_LOG_LEVEL = 0;

    // Temel Ayarlar
    public String mName;
    public String mUuid;
    public int mVersion = 0;

    // Kullanıcı Bilgileri
    public String mUsername;
    public String mPassword;
    public boolean mUseLzo = true;

    // Güvenlik & Sertifikalar
    public String mClientCertFilename;
    public String mCaFilename;
    public String mClientKeyFilename;
    public String mTLSAuthFilename;

    // Config içeriğinin tamamı burada saklanacak
    public String mInlineConfig;

    // Gelişmiş Ayarlar
    public boolean mBlockUnusedAddressFamilies = true;
    public boolean mAllowLocalLAN = false;
    public boolean mAllowAppVpnBypass = false;
    public Vector<String> mAllowedAppsVpn = new Vector<>();
    public boolean mAllowedAppsVpnAreDisallowed = true;
    public Connection[] mConnections = new Connection[0];

    // Constructor
    public VpnProfile(String name) {
        mUuid = UUID.randomUUID().toString();
        mName = name;
        mConnections = new Connection[1];
        mConnections[0] = new Connection();
    }

    public String getUUIDString() {
        return mUuid;
    }

    public String getName() {
        if (TextUtils.isEmpty(mName)) return "No Name";
        return mName;
    }

    public void checkForRestart(Context context) {
        // Basitleştirildi
    }

    // --- DÜZELTİLEN METOT BURADA ---
    // Artık FileWriter yerine OutputStream kabul ediyor
    public void writeConfigFileOutput(Context context, OutputStream out) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        writer.write(mInlineConfig);
        writer.write("\n");
        writer.flush();
        // Stream'i kapatmıyoruz, process açık kalsın.
    }

    // Statik Helper
    public static boolean doUseOpenVPN3(Context context) {
        return false; // Her zaman OpenVPN 2.x kullan (daha stabil)
    }

    // İç Sınıf: Bağlantı
    public static class Connection implements Serializable, Cloneable {
        public String mServerName = "openvpn.example.com";
        public String mServerPort = "1194";
        public boolean mUseUdp = true;
        public ProxyType mProxyType = ProxyType.NONE;
        public enum ProxyType { NONE, HTTP, SOCKS5, ORBOT }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}