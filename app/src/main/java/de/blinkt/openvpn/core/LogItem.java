package de.blinkt.openvpn.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.Locale;

public class LogItem implements Parcelable {
    private Object[] mArgs = null;
    private String mMessage = null;
    private int mResId;
    private ConnectionStatus mLevel;
    private long mLogtime = System.currentTimeMillis();
    private int mVerbosityLevel = -1;

    public LogItem(ConnectionStatus level, int resId, Object... args) {
        mResId = resId;
        mArgs = args;
        mLevel = level;
    }

    public LogItem(ConnectionStatus level, String msg) {
        mLevel = level;
        mMessage = msg;
    }

    public LogItem(int verbosityLevel, String msg) {
        mLevel = ConnectionStatus.UNKNOWN_LEVEL;
        mMessage = msg;
        mVerbosityLevel = verbosityLevel;
    }

    public String getString(Context c) {
        if (mMessage != null) {
            return mMessage;
        } else {
            // R sınıfı olmadığı için Resource ID'den string çekemiyoruz.
            // Sadece argümanları veya ID'yi döndürüyoruz.
            if (c != null) {
                if (mArgs != null && mArgs.length > 0)
                    return String.format(Locale.getDefault(), "Log ID %d: %s", mResId, Arrays.toString(mArgs));
                else
                    return "Log ID " + mResId;
            }
            return mMessage != null ? mMessage : "Log info";
        }
    }

    // Parcelable Implementation
    protected LogItem(Parcel in) {
        // Basit okuma
        mMessage = in.readString();
        mResId = in.readInt();
        mLogtime = in.readLong();
        mVerbosityLevel = in.readInt();
        int levelInt = in.readInt();
        mLevel = ConnectionStatus.values()[levelInt];
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMessage);
        dest.writeInt(mResId);
        dest.writeLong(mLogtime);
        dest.writeInt(mVerbosityLevel);
        dest.writeInt(mLevel.ordinal());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LogItem> CREATOR = new Creator<LogItem>() {
        @Override
        public LogItem createFromParcel(Parcel in) {
            return new LogItem(in);
        }

        @Override
        public LogItem[] newArray(int size) {
            return new LogItem[size];
        }
    };

    public long getLogtime() {
        return mLogtime;
    }
}