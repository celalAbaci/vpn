package de.blinkt.openvpn.core;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;

import de.blinkt.openvpn.VpnProfile;

public class OpenVPNThread implements Runnable {
    private static final String TAG = "OpenVPNThread";
    private String[] mArgv;
    private Process mProcess;
    private String mNativeDir;
    private OpenVPNService mService;
    private String mDumpPath;
    private boolean mReplaceConnection = false;

    public OpenVPNThread(OpenVPNService service, String[] argv, String nativeDir, String tmpDir) {
        mService = service;
        mArgv = argv;
        mNativeDir = nativeDir;
        mDumpPath = tmpDir;
    }

    public void setReplaceConnection() {
        mReplaceConnection = true;
    }

    @Override
    public void run() {
        try {
            VpnStatus.logInfo("Starting OpenVPN Thread...");
            startOpenVPNThreadArgs(mArgv);
        } catch (Exception e) {
            VpnStatus.logException("OpenVPN Thread Error", e);
        } finally {
            if (!mReplaceConnection) {
                mService.openvpnStopped();
            }
        }
    }

    private void startOpenVPNThreadArgs(String[] argv) {
        LinkedList<String> args = new LinkedList<>();
        Collections.addAll(args, argv);

        ProcessBuilder pb = new ProcessBuilder(args);

        // Ortam değişkenleri (Native kütüphaneler için)
        pb.environment().put("LD_LIBRARY_PATH", mNativeDir);

        // Process'i başlat
        try {
            mProcess = pb.start();

            // Logları okumak için Stream'leri bağla
            InputStream in = mProcess.getInputStream();
            InputStream err = mProcess.getErrorStream();

            // Log okuyucu thread başlat
            new Thread(new ProcessLogReader(in), "OpenVPN-Stdout").start();
            new Thread(new ProcessLogReader(err), "OpenVPN-Stderr").start();

            // Process bitene kadar bekle
            mProcess.waitFor();

        } catch (IOException | InterruptedException e) {
            VpnStatus.logException(e);
        }

        VpnStatus.logInfo("OpenVPN Process Exited");
    }

    public OutputStream getOpenVPNStdin() {
        if (mProcess != null)
            return mProcess.getOutputStream();
        return null;
    }

    // Logları okuyup VpnStatus'a gönderen yardımcı sınıf
    private static class ProcessLogReader implements Runnable {
        private final InputStream stream;

        public ProcessLogReader(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Log satırlarını işle
                    VpnStatus.logInfo("NATIVE: " + line);
                }
            } catch (IOException e) {
                // Stream kapandı
            }
        }
    }
}