package de.blinkt.openvpn.core;

import android.content.Context;
import android.text.TextUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;

public class X509Utils {
    public static String getCertificateFriendlyName(Context c, String filename) {
        if (!TextUtils.isEmpty(filename)) {
            return filename;
        }
        return "Unknown Certificate";
    }

    public static X509Certificate getCertificateFromFile(String filename) throws FileNotFoundException, CertificateException {
        File f = new File(filename);
        InputStream inStream = new FileInputStream(f);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(inStream);
    }
}