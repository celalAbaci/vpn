package com.abacicelal.supervpn_project.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class ConfigParser {

    public static class VpnProfileDetails {
        public String remoteIp;
        public int remotePort;
        public String proto;
        public String ca;
        public String clientCert;
        public String clientKey;
        public String tlsAuth;
    }

    public static VpnProfileDetails parse(String configContent) {
        VpnProfileDetails details = new VpnProfileDetails();
        try (BufferedReader reader = new BufferedReader(new StringReader(configContent))) {
            String line;
            boolean inCa = false;
            boolean inCert = false;
            boolean inKey = false;
            boolean inTls = false;
            StringBuilder currentBlock = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("remote ")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) details.remoteIp = parts[1];
                    if (parts.length >= 3) details.remotePort = Integer.parseInt(parts[2]);
                    continue;
                }

                if (line.startsWith("proto ")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) details.proto = parts[1];
                    continue;
                }

                if (line.equals("<ca>")) { inCa = true; currentBlock = new StringBuilder(); continue; }
                if (line.equals("</ca>")) { inCa = false; details.ca = currentBlock.toString(); continue; }

                if (line.equals("<cert>")) { inCert = true; currentBlock = new StringBuilder(); continue; }
                if (line.equals("</cert>")) { inCert = false; details.clientCert = currentBlock.toString(); continue; }

                if (line.equals("<key>")) { inKey = true; currentBlock = new StringBuilder(); continue; }
                if (line.equals("</key>")) { inKey = false; details.clientKey = currentBlock.toString(); continue; }

                if (line.equals("<tls-crypt>") || line.equals("<tls-auth>")) { inTls = true; currentBlock = new StringBuilder(); continue; }
                if (line.equals("</tls-crypt>") || line.equals("</tls-auth>")) { inTls = false; details.tlsAuth = currentBlock.toString(); continue; }

                if (inCa || inCert || inKey || inTls) {
                    currentBlock.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return details;
    }
}
