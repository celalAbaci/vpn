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

        // Read everything first to ensure mInlineConfig is complete
        while ((line = mReader.readLine()) != null) {
            fullConfig.append(line).append("\n");
            parseLine(line);
        }

        mResult.mInlineConfig = fullConfig.toString();

        // Ensure some defaults if not parsed
        if (mResult.mConnections[0].mServerName == null) {
             mResult.mConnections[0].mServerName = "Unknown Server";
        }
    }

    private void parseLine(String line) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) return;

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
    }

    public VpnProfile convertProfile() {
        return mResult;
    }
}
