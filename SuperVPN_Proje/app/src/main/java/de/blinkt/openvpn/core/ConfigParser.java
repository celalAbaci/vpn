package de.blinkt.openvpn.core;

import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;

import de.blinkt.openvpn.VpnProfile;

public class ConfigParser {

    private VpnProfile mResult;
    private BufferedReader mReader;

    public static class ConfigParseError extends Exception {
        public ConfigParseError(String msg) {
            super(msg);
        }
    }

    public ConfigParser() {
        mResult = new VpnProfile("Generated Config");
    }

    public void parseConfig(Reader reader) throws IOException, ConfigParseError {
        mReader = new BufferedReader(reader);
        StringBuilder fullConfig = new StringBuilder();
        String line;

        // Inline dosya okuma durumu
        boolean inInlineFile = false;
        StringBuilder inlineFileBuffer = new StringBuilder();
        String inlineFileTag = "";

        while ((line = mReader.readLine()) != null) {
            fullConfig.append(line).append("\n"); // Append every line to full config

            if (line.trim().isEmpty() || line.startsWith("#") || line.startsWith(";"))
                continue;

            // <ca>, <cert> vb. inline blokları yakala
            if (line.trim().startsWith("<") && !line.trim().startsWith("</")) {
                inInlineFile = true;
                inlineFileTag = line.trim().substring(1, line.trim().indexOf('>'));
                inlineFileBuffer = new StringBuilder();
                continue;
            }

            if (line.trim().startsWith("</")) {
                inInlineFile = false;
                String content = inlineFileBuffer.toString();

                // Yakalanan içeriği profile ata
                if (inlineFileTag.equals("ca")) mResult.mCaFilename = "[[INLINE]]" + content;
                else if (inlineFileTag.equals("cert")) mResult.mClientCertFilename = "[[INLINE]]" + content;
                else if (inlineFileTag.equals("key")) mResult.mClientKeyFilename = "[[INLINE]]" + content;
                else if (inlineFileTag.equals("tls-auth") || inlineFileTag.equals("tls-crypt")) mResult.mTLSAuthFilename = "[[INLINE]]" + content;

                continue;
            }

            if (inInlineFile) {
                inlineFileBuffer.append(line).append("\n");
                continue;
            }

            // Normal ayarları işle
            parseLine(line);
        }

        mResult.mInlineConfig = fullConfig.toString();
    }

    private void parseLine(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length == 0) return;

        String option = parts[0];

        if (option.equals("remote") && parts.length >= 3) {
            mResult.mConnections[0].mServerName = parts[1];
            mResult.mConnections[0].mServerPort = parts[2];
            if (parts.length > 3) {
                mResult.mConnections[0].mUseUdp = parts[3].equals("udp");
            }
        }
        else if (option.equals("proto")) {
            if (parts.length > 1) {
                mResult.mConnections[0].mUseUdp = parts[1].toLowerCase().contains("udp");
            }
        }
        else if (option.equals("auth") && parts.length > 1) {
            mResult.mAuth = parts[1];
        }
        else if (option.equals("cipher") && parts.length > 1) {
            mResult.mCipher = parts[1];
        }
        else if (option.equals("remote-cert-tls") && parts.length > 1) {
            mResult.mRemoteCertTls = parts[1];
        }
    }

    public VpnProfile convertProfile() {
        // Profilin tamamlanmış halini döndür
        // MainActivity'deki startOpenVpn metodunda configContent 
        // mInlineConfig içine yazılmalı.
        return mResult;
    }

    // Orijinal koddaki eksik metodu bypass etmek için bu sınıfı güncelledik.
}