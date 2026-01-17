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
        String line;

        // Inline dosya okuma durumu
        boolean inInlineFile = false;
        StringBuilder inlineFileBuffer = new StringBuilder();
        String inlineFileTag = "";

        while ((line = mReader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";"))
                continue;

            // <ca>, <cert> vb. inline blokları yakala
            if (line.startsWith("<") && !line.startsWith("</")) {
                inInlineFile = true;
                int endIndex = line.indexOf('>');
                if (endIndex != -1) {
                    inlineFileTag = line.substring(1, endIndex);
                    inlineFileBuffer = new StringBuilder();
                    // Handle case where content follows tag immediately
                    if (line.length() > endIndex + 1) {
                         inlineFileBuffer.append(line.substring(endIndex + 1)).append("\n");
                    }
                }
                continue;
            }

            if (line.startsWith("</")) {
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
    }

    private void parseLine(String line) {
        // Handle options
        // Safely ignore unknown options requested by the user, like shaper
        if (line.startsWith("ignore-unknown-option")) {
            return;
        }

        // Explicitly ignore shaper if it appears alone (handled by OpenVPN usually but good to be safe)
        if (line.startsWith("shaper")) {
            return;
        }

        String[] parts = line.split("\\s+");
        if (parts.length == 0) return;

        String option = parts[0];

        if (option.equals("remote") && parts.length >= 3) {
            mResult.mConnections[0].mServerName = parts[1];
            mResult.mConnections[0].mServerPort = parts[2];
            if (parts.length > 3) {
                mResult.mConnections[0].mUseUdp = parts[3].equalsIgnoreCase("udp");
            }
        }
        else if (option.equals("proto")) {
            if (parts.length > 1) {
                mResult.mConnections[0].mUseUdp = parts[1].toLowerCase().contains("udp");
            }
        }
        else if (option.equals("cipher")) {
             if (parts.length > 1) mResult.mCipher = parts[1];
        }
        else if (option.equals("auth")) {
             if (parts.length > 1) mResult.mAuth = parts[1];
        }
        else if (option.equals("remote-cert-tls")) {
             if (parts.length > 1) mResult.mExpectTLSCert = parts[1].equals("server");
        }
        else if (option.equals("client")) {
            // Client mode
        }
    }

    public VpnProfile convertProfile() {
        return mResult;
    }
}
