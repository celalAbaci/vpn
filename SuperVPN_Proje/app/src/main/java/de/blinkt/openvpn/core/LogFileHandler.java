package de.blinkt.openvpn.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogFileHandler extends Handler {
    public static final int LOG_MESSAGE = 106;
    private static final String TAG = "OpenVPNLogHandler";
    private static File mLogFile;
    private static BufferedWriter mWriter;

    public LogFileHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == LOG_MESSAGE && msg.obj instanceof String) {
            String logMessage = (String) msg.obj;
            writeLog(logMessage);
        }
    }

    private void writeLog(String log) {
        // Log dosyası ayarlanmadıysa yazma
        if (mLogFile == null || mWriter == null) {
            return;
        }

        try {
            // Zaman damgası ekle
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            mWriter.write(timeStamp + " " + log + "\n");
            mWriter.flush();
        } catch (IOException e) {
            VpnStatus.logError("Error writing to log file: " + e.getLocalizedMessage());
        }
    }

    public static void setLogFile(File logFile) {
        mLogFile = logFile;
        try {
            if (mWriter != null) {
                mWriter.close();
            }
            // Dosya varsa üzerine ekle (append = true)
            mWriter = new BufferedWriter(new FileWriter(logFile, true));
        } catch (IOException e) {
            VpnStatus.logError("Could not open log file: " + e.getLocalizedMessage());
            mLogFile = null;
        }
    }
}