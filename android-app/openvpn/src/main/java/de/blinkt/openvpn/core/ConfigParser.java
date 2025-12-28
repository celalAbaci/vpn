package de.blinkt.openvpn.core;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import de.blinkt.openvpn.VpnProfile;

public class ConfigParser {

    private VpnProfile profile;

    public void parseConfig(Reader reader) throws Exception {
        BufferedReader br = new BufferedReader(reader);
        String line;
        profile = new VpnProfile("Imported Profile");

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) continue;

            String[] parts = line.split("\\s+", 2);
            String option = parts[0];
            String args = parts.length > 1 ? parts[1] : "";

            // Handle shaper and ignore-unknown-option to prevent errors
            if (option.equals("shaper")) {
                // Store shaper limit logic if needed, or just ignore as it's handled server/client side transparently
                profile.mCustomConfigOptions += line + "\n";
            } else if (option.equals("ignore-unknown-option")) {
                // handled
                profile.mCustomConfigOptions += line + "\n";
            } else {
                // Basic parsing (mock)
                profile.mCustomConfigOptions += line + "\n";
            }
        }
    }

    public VpnProfile convertProfile() {
        return profile;
    }
}
