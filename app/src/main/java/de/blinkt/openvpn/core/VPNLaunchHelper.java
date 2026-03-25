package de.blinkt.openvpn.core;

import android.content.Context;
import android.os.Build;
import java.io.File;
import java.util.Vector;

public class VPNLaunchHelper {

    public static String[] buildOpenvpnArgv(Context context) {
        Vector<String> args = new Vector<>();

        // Binary Adı (Biz kütüphane kullanıyoruz ama argüman dizisi için gerekli)
        args.add("libovpnexec.so");

        args.add("--config");
        args.add("stdin");

        return args.toArray(new String[args.size()]);
    }

    static String writeMiniVPN(Context context) {
        // Biz .so kütüphanelerini (jniLibs) kullandığımız için
        // minivpn binary dosyasını yazmaya gerek yok.
        // Bu metod sadece uyumluluk için boş string döndürüyor.
        return "";
    }
}