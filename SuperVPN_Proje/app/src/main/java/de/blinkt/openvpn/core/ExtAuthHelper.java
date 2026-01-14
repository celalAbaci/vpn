package de.blinkt.openvpn.core;

import android.content.Context;
import android.os.Bundle;

public class ExtAuthHelper {

    // Harici kimlik doğrulama yardımcısı (Boşaltıldı)

    public static void onBind(Context context) {
        // Harici sertifika sağlayıcısına bağlanmaya gerek yok
    }

    public static void onUnbind(Context context) {
        // Bağlantıyı kesmeye gerek yok
    }

    public static void externalSigned(String alias) {
        // İmzalama işlemi yok
    }

    public static void setExternalAppPackage(String packageName) {
        // Paket adı ayarlamaya gerek yok
    }
}