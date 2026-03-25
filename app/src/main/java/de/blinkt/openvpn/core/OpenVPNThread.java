package de.blinkt.openvpn.core;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenVPN'i ProcessBuilder + Unix Domain Socket (Filesystem) Management Interface ile çalıştıran thread.
 *
 * Doğru mimari (ics-openvpn ile uyumlu):
 *  - OpenVPN = MANAGEMENT SERVER (socket dosyasını kendisi oluşturur ve dinler)
 *  - Java    = MANAGEMENT CLIENT (OpenVPN başladıktan sonra socket'e bağlanır)
 *
 * Akış:
 *  1. libovpnexec.so → nativeLibraryDir'den ProcessBuilder ile çalıştırılır
 *  2. OpenVPN → filesystem Unix socket oluşturur ve bekler (--management path unix)
 *  3. Java → socket dosyası oluşana kadar bekler (max 10s), sonra LocalSocket ile bağlanır
 *  4. IFCONFIG: sunucudan gelen IP/mask/mtu ile TUN fd oluşturulur
 *  5. OPENTUN: LocalSocket.setFileDescriptorsForSend() ile fd SCM_RIGHTS üzerinden geçirilir
 */
public class OpenVPNThread implements Runnable {
    private static final String TAG = "OpenVPN";

    private final OpenVPNService mService;
    private final String mNativeLibDir;
    private volatile boolean mStopped = false;
    private boolean mReplaceConnection = false;
    private String mConfigContent = null;

    private Process mProcess = null;
    private ParcelFileDescriptor mTunPfd = null;

    // Push reply'dan gelen TUN parametreleri
    private String mPushedIp = "10.8.0.2";
    private int    mPushedPrefix = 24;
    private int    mPushedMtu = 1500;
    private String mPushedDns1 = "8.8.8.8";
    private String mPushedDns2 = "8.8.4.4";

    public OpenVPNThread(OpenVPNService service, String[] argv, String nativeDir, String tmpDir) {
        mService = service;
        mNativeLibDir = nativeDir;
    }

    public void setReplaceConnection() { mReplaceConnection = true; }
    public void setConfigContent(String c) { mConfigContent = c; }

    @Deprecated
    public OutputStream getOpenVPNStdin() { return null; }

    @Override
    public void run() {
        try {
            VpnStatus.logInfo("OpenVPNThread: Starting...");
            runViaProcess();
        } catch (Exception e) {
            VpnStatus.logException("OpenVPNThread Fatal", e);
            VpnStatus.updateStateString("NOPROCESS", "Fatal: " + e.getMessage(),
                    0, ConnectionStatus.LEVEL_NOTCONNECTED);
        } finally {
            if (!mReplaceConnection) mService.openvpnStopped();
        }
    }

    // ─────────────────────────────────────────────────────────
    private void runViaProcess() throws Exception {

        String execPath = mNativeLibDir + "/libovpnexec.so";
        if (!new File(execPath).exists()) {
            VpnStatus.logError("libovpnexec.so bulunamadı: " + execPath);
            return;
        }
        if (mConfigContent == null || mConfigContent.isEmpty()) {
            VpnStatus.logError("Config içeriği yok!");
            return;
        }

        // Filesystem Unix socket — OpenVPN bu dosyayı oluşturur, Java bağlanır
        // Her seferinde benzersiz isim (collision yok)
        File socketFile = new File(mService.getCacheDir(),
                "mgmt_" + android.os.Process.myPid() + "_" + System.currentTimeMillis() + ".sock");
        socketFile.delete(); // Eski dosyayı temizle

        File configFile = File.createTempFile("vpn_cfg", ".ovpn", mService.getCacheDir());
        try {
            try (FileWriter fw = new FileWriter(configFile)) {
                fw.write(mConfigContent);
            }
            configFile.setReadable(true, false);
            VpnStatus.logInfo("Config: " + configFile.getAbsolutePath());
            VpnStatus.logInfo("Management socket: " + socketFile.getAbsolutePath());

            // OpenVPN komutu:
            //   --management <path> unix  →  OpenVPN SERVER olarak dinler (filesystem socket)
            //   --management-hold        →  Biz "hold release" diyene kadar bekler
            //   --management-client YOK  →  OpenVPN artık BAĞLANMAZ, DİNLER
            List<String> cmd = new ArrayList<>();
            cmd.add(execPath);
            cmd.add("--config");               cmd.add(configFile.getAbsolutePath());
            cmd.add("--dev");                  cmd.add("tun");
            cmd.add("--management");           cmd.add(socketFile.getAbsolutePath()); cmd.add("unix");
            cmd.add("--management-query-passwords");
            cmd.add("--management-up-down");   // OPENTUN/IFCONFIG bildirimleri için
            cmd.add("--management-hold");      // Java "hold release" diyene kadar bekler
            cmd.add("--verb");                 cmd.add("4");

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(mService.getCacheDir());
            pb.environment().put("LD_LIBRARY_PATH", mNativeLibDir);
            pb.redirectErrorStream(true);

            VpnStatus.updateStateString("WAIT", "OpenVPN başlatılıyor...",
                    0, ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET);

            mProcess = pb.start();
            VpnStatus.logInfo("OpenVPN başlatıldı, socket dosyası bekleniyor...");
            startOutputLogger(mProcess);

            // OpenVPN'in socket dosyasını oluşturmasını bekle (max 10 saniye)
            LocalSocket mgmt = null;
            long deadline = System.currentTimeMillis() + 10000;
            while (System.currentTimeMillis() < deadline && !mStopped) {
                if (socketFile.exists()) {
                    try {
                        LocalSocket s = new LocalSocket();
                        s.connect(new LocalSocketAddress(
                                socketFile.getAbsolutePath(),
                                LocalSocketAddress.Namespace.FILESYSTEM));
                        mgmt = s;
                        VpnStatus.logInfo("Management bağlandı: " + socketFile.getName());
                        break;
                    } catch (IOException e) {
                        // Socket dosyası var ama henüz hazır değil, tekrar dene
                        VpnStatus.logInfo("Socket bekleniyor... (" + e.getMessage() + ")");
                    }
                }
                Thread.sleep(200);
            }

            if (mgmt == null) {
                VpnStatus.logError("Management socket'e bağlanılamadı (10s timeout)!");
                mProcess.destroy();
                return;
            }

            // Management protokolünü yönet
            try {
                handleManagement(mgmt);
            } finally {
                try { mgmt.close(); } catch (Exception ignored) {}
            }

            int exit = mProcess.waitFor();
            VpnStatus.logInfo("OpenVPN çıkış kodu: " + exit);

        } finally {
            configFile.delete();
            socketFile.delete();
            closeTun();
            VpnStatus.updateStateString("EXITING", "Bağlantı kesildi",
                    0, ConnectionStatus.LEVEL_NOTCONNECTED);
        }
    }

    // ─────────────────────────────────────────────────────────
    private void handleManagement(LocalSocket socket) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

        // OpenVPN bağlandığında durumu ve hold'u ayarla
        writer.println("state on");
        writer.println("hold release");

        String line;
        while (!mStopped) {
            try { line = reader.readLine(); }
            catch (IOException e) { break; }
            if (line == null) break;

            Log.d(TAG, "MGMT< " + line);

            if (line.startsWith(">STATE:")) {
                parseState(line.substring(7));

            } else if (line.startsWith(">LOG:")) {
                VpnStatus.logInfo("[ovpn] " + line.substring(5));

            } else if (line.contains("NEED-OK") && line.contains("IFCONFIG")) {
                // Format: >NEED-OK:Need 'IFCONFIG' confirmation MSG:10.8.0.2 255.255.255.0 1500 subnet
                parseIfconfigMsg(line);
                closeTun();
                mTunPfd = buildTunInterface(mPushedIp, mPushedPrefix, mPushedMtu);
                if (mTunPfd != null) {
                    VpnStatus.logInfo("TUN kuruldu: " + mPushedIp + "/" + mPushedPrefix
                            + " fd=" + mTunPfd.getFd());
                    writer.println("needok IFCONFIG ok");
                } else {
                    VpnStatus.logError("TUN kurulamadı!");
                    writer.println("needok IFCONFIG error");
                }

            } else if (line.contains("NEED-OK") && line.contains("OPENTUN")) {
                // TUN fd'yi SCM_RIGHTS ile gönder
                if (mTunPfd != null) {
                    try {
                        socket.setFileDescriptorsForSend(
                                new FileDescriptor[]{mTunPfd.getFileDescriptor()});
                        writer.println("needok OPENTUN ok");
                        // SCM_RIGHTS: setFileDescriptorsForSend sonraki write ile fd'yi ekler
                        socket.getOutputStream().write('\n');
                        socket.getOutputStream().flush();
                        VpnStatus.logInfo("OPENTUN: TUN fd=" + mTunPfd.getFd() + " SCM_RIGHTS gönderildi");
                    } catch (Exception e) {
                        VpnStatus.logError("OPENTUN fd gönderme hatası: " + e.getMessage());
                        writer.println("needok OPENTUN error");
                    }
                } else {
                    VpnStatus.logError("OPENTUN: TUN fd null!");
                    writer.println("needok OPENTUN error");
                }
            } else if (line.startsWith(">NEED-OK:")) {
                String[] parts = line.split("'");
                if (parts.length >= 3) {
                    String req = parts[1];
                    String status = "ok";
                    if ("PERSIST_TUN_ACTION".equals(req)) {
                        status = "NOACTION";
                    }
                    writer.println("needok " + req + " " + status);
                    VpnStatus.logInfo(req + " otomatik onaylandı: " + status);
                }
            } else if (line.startsWith(">PASSWORD:")) {
                writer.println("password 'Auth' ''");

            } else if (line.startsWith(">HOLD:")) {
                writer.println("hold release");
            }
        }

        VpnStatus.logInfo("Management döngüsü sona erdi");
    }

    // ─────────────────────────────────────────────────────────
    private void parseIfconfigMsg(String line) {
        int msgIdx = line.indexOf("MSG:");
        if (msgIdx < 0) return;
        String[] parts = line.substring(msgIdx + 4).trim().split("\\s+");
        if (parts.length >= 1) mPushedIp = parts[0];
        if (parts.length >= 2) {
            try { mPushedPrefix = maskToPrefix(parts[1]); }
            catch (Exception ignored) {}
        }
        if (parts.length >= 3) {
            try { mPushedMtu = Integer.parseInt(parts[2]); }
            catch (NumberFormatException ignored) {}
        }
        VpnStatus.logInfo("IFCONFIG: ip=" + mPushedIp + " /" + mPushedPrefix + " mtu=" + mPushedMtu);
    }

    private int maskToPrefix(String mask) {
        String[] octets = mask.split("\\.");
        int bits = 0;
        for (String o : octets) bits += Integer.bitCount(Integer.parseInt(o));
        return bits;
    }

    // ─────────────────────────────────────────────────────────
    private void parseState(String s) {
        String[] p = s.split(",", -1);
        if (p.length < 2) return;
        String state = p[1].trim();
        VpnStatus.logInfo("VPN State: " + state);
        switch (state) {
            case "CONNECTING":
                VpnStatus.updateStateString("CONNECTING", "Sunucuya bağlanıyor...",
                        0, ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET); break;
            case "WAIT":
                VpnStatus.updateStateString("WAIT", "Yanıt bekleniyor...",
                        0, ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET); break;
            case "AUTH":
                VpnStatus.updateStateString("AUTH", "Kimlik doğrulanıyor...",
                        0, ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET); break;
            case "GET_CONFIG":
                VpnStatus.updateStateString("GET_CONFIG", "Config alınıyor...",
                        0, ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET); break;
            case "ASSIGN_IP":
                VpnStatus.updateStateString("ASSIGN_IP", "IP atanıyor...",
                        0, ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET); break;
            case "CONNECTED":
                VpnStatus.updateStateString("CONNECTED", "Bağlandı! ✅",
                        0, ConnectionStatus.LEVEL_CONNECTED); break;
            case "RECONNECTING":
                VpnStatus.updateStateString("RECONNECTING", "Yeniden bağlanıyor...",
                        0, ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET); break;
            case "EXITING": case "DISCONNECTED":
                VpnStatus.updateStateString("NOPROCESS", "Bağlantı kesildi",
                        0, ConnectionStatus.LEVEL_NOTCONNECTED); break;
        }
    }

    // ─────────────────────────────────────────────────────────
    private ParcelFileDescriptor buildTunInterface(String ip, int prefix, int mtu) {
        try {
            VpnService.Builder b = mService.new Builder();
            b.setSession("SuperVPN");
            b.addAddress(ip, prefix);
            b.addDnsServer(mPushedDns1);
            b.addDnsServer(mPushedDns2);
            b.addRoute("0.0.0.0", 0);
            b.setMtu(mtu);
            b.setBlocking(true);
            try { b.addDisallowedApplication(mService.getPackageName()); }
            catch (Exception ignored) {}
            return b.establish();
        } catch (Exception e) {
            VpnStatus.logError("TUN build hatası: " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────
    private void startOutputLogger(final Process proc) {
        Thread t = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    VpnStatus.logInfo("[exec] " + line);
                }
            } catch (IOException ignored) {}
        }, "ovpn-logger");
        t.setDaemon(true);
        t.start();
    }

    private void closeTun() {
        if (mTunPfd != null) {
            try { mTunPfd.close(); } catch (Exception ignored) {}
            mTunPfd = null;
        }
    }

    public void stopProcess() {
        mStopped = true;
        if (mProcess != null) mProcess.destroy();
        closeTun();
        VpnStatus.logInfo("stopProcess çağrıldı");
    }
}