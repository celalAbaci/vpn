package de.blinkt.openvpn.core;

import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

import de.blinkt.openvpn.VpnProfile;

public class OpenVpnManagementThread implements Runnable, OpenVPNManagement {

    private static final String TAG = "OpenVpnManagement";
    private LocalSocket mSocket;
    private VpnProfile mProfile;
    private OpenVPNService mOpenVPNService;
    private LinkedList<FileDescriptor> mFDList = new LinkedList<>();
    private boolean mWaitingForWorkerRelease = false;
    private long mLastStatusUpdate = 0;
    private static final long STATUS_UPDATE_INTERVAL = 2000;
    private volatile boolean mShuttingDown;

    private static class FileDescriptor {
        int fd;
        boolean closed;
    }

    public OpenVpnManagementThread(VpnProfile profile, OpenVPNService openVpnService) {
        mProfile = profile;
        mOpenVPNService = openVpnService;
    }

    public boolean openManagementInterface(Context c) {
        int tries = 8;
        String socketName = (c.getCacheDir().getAbsolutePath() + "/" + "mgmtsocket");
        mSocket = new LocalSocket();

        while (tries > 0 && !mSocket.isConnected()) {
            try {
                mSocket.bind(new LocalSocketAddress(socketName, LocalSocketAddress.Namespace.FILESYSTEM));
            } catch (IOException e) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {}
                tries--;
            }
        }

        try {
            LocalSocketAddress address = new LocalSocketAddress(socketName, LocalSocketAddress.Namespace.FILESYSTEM);
            mSocket.connect(address);
            return true;
        } catch (IOException e) {
            VpnStatus.logError("Error reading from management socket: " + e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[2048];
        InputStream instream;
        try {
            instream = mSocket.getInputStream();
        } catch (IOException e) {
            VpnStatus.logError("Could not get InputStream from socket");
            return;
        }

        while (!mShuttingDown) {
            try {
                int numread = instream.read(buffer);
                if (numread == -1) {
                    if (!mShuttingDown)
                        VpnStatus.logError("Management socket closed unexpectedly");
                    return;
                }

                // Basit string işleme (Byte'dan String'e)
                String input = new String(buffer, 0, numread, StandardCharsets.UTF_8);
                processInput(input);

            } catch (IOException e) {
                if (!mShuttingDown)
                    VpnStatus.logError("Error reading from socket: " + e.getLocalizedMessage());
                return;
            }
        }
    }

    private void processInput(String input) {
        // Gelen mesajları satır satır böl
        String[] lines = input.split("\n");
        for (String line : lines) {
            String cleanLine = line.trim();
            if (cleanLine.isEmpty()) continue;

            if (cleanLine.startsWith(">LOG:")) {
                // Log mesajı
                String[] parts = cleanLine.split(":", 4);
                if (parts.length >= 4) {
                    VpnStatus.logInfo(parts[3]);
                }
            } else if (cleanLine.startsWith(">STATE:")) {
                // Durum değişikliği (CONNECTING, AUTH, CONNECTED vb.)
                String[] parts = cleanLine.split(":", 2);
                if (parts.length > 1) {
                    processStateString(parts[1]);
                }
            } else if (cleanLine.startsWith(">PASSWORD:")) {
                // Şifre isteği (Burada otomatik gönderme yapılabilir)
                processPasswordRequest(cleanLine);
            } else if (cleanLine.startsWith(">HOLD:")) {
                // Bağlantı bekletiliyor, devam et
                managmentCommand("hold release\n");
            }
        }
    }

    private void processStateString(String state) {
        String[] parts = state.split(",");
        // parts[1] = Durum Adı (WAIT, AUTH, CONNECTED)
        // parts[2] = Açıklama
        if (parts.length >= 3) {
            String stateName = parts[1];
            String desc = parts[2];
            ConnectionStatus level = ConnectionStatus.UNKNOWN_LEVEL;

            if (stateName.equals("Wait")) level = ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET;
            else if (stateName.equals("AUTH")) level = ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET;
            else if (stateName.equals("GET_CONFIG")) level = ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED;
            else if (stateName.equals("ASSIGN_IP")) level = ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED;
            else if (stateName.equals("CONNECTED")) {
                level = ConnectionStatus.LEVEL_CONNECTED;
                // IP adreslerini almak için komut gönder
                managmentCommand("state\n");
            }
            else if (stateName.equals("RECONNECTING")) level = ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET;
            else if (stateName.equals("EXITING")) level = ConnectionStatus.LEVEL_NOTCONNECTED;

            VpnStatus.updateStateString(stateName, desc, 0, level);
        }
    }

    private void processPasswordRequest(String req) {
        // >PASSWORD:Need 'Auth' username/password
        if (req.contains("Auth")) {
            String user = mProfile.mUsername;
            String pass = mProfile.mPassword;
            if (user != null && pass != null) {
                String cmdUser = String.format("username \"Auth\" \"%s\"\n", user.replace("\\", "\\\\").replace("\"", "\\\""));
                String cmdPass = String.format("password \"Auth\" \"%s\"\n", pass.replace("\\", "\\\\").replace("\"", "\\\""));
                managmentCommand(cmdUser);
                managmentCommand(cmdPass);
            } else {
                VpnStatus.logError("OpenVPN requested password but no credentials found in profile.");
            }
        }
    }

    private boolean managmentCommand(String cmd) {
        try {
            if (mSocket != null && mSocket.getOutputStream() != null) {
                mSocket.getOutputStream().write(cmd.getBytes());
                mSocket.getOutputStream().flush();
                return true;
            }
        } catch (IOException e) {
            VpnStatus.logError("Error writing command to socket: " + e.getLocalizedMessage());
        }
        return false;
    }

    @Override
    public boolean stopVPN(boolean replaceConnection) {
        mShuttingDown = true;
        managmentCommand("signal SIGINT\n");
        try {
            if (mSocket != null) mSocket.close();
        } catch (IOException e) {
            // Ignore
        }
        return true;
    }

    // Kullanılmayan ama Interface gereği olması gereken metotlar
    @Override
    public void sendCRResponse(String response) { managmentCommand("cr-response " + response + "\n"); }

    @Override
    public void reconnect() { managmentCommand("signal SIGHUP\n"); }

    @Override
    public void pause(pauseReason reason) { }

    @Override
    public void resume() { }

    @Override
    public boolean stopVPNOnPause() { return false; }

    @Override
    public void networkChange(boolean sameNetwork) {
        // Ağ değiştiğinde yapılacaklar (Şimdilik boş)
    }
}