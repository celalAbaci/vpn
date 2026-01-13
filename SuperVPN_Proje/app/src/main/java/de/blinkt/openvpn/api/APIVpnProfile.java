package de.blinkt.openvpn.api;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.UUID;

public class APIVpnProfile implements Parcelable {

    public final String mUUID;
    public final String mName;
    public final boolean mUserEditable;
    public final boolean mIsExternalAppProfile;

    public APIVpnProfile(String uuid, String name, boolean userEditable, boolean isExternalAppProfile) {
        mUUID = uuid;
        mName = name;
        mUserEditable = userEditable;
        mIsExternalAppProfile = isExternalAppProfile;
    }

    public APIVpnProfile(Parcel in) {
        mUUID = in.readString();
        mName = in.readString();
        mUserEditable = in.readInt() != 0;
        mIsExternalAppProfile = in.readInt() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUUID);
        dest.writeString(mName);
        dest.writeInt(mUserEditable ? 1 : 0);
        dest.writeInt(mIsExternalAppProfile ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<APIVpnProfile> CREATOR = new Creator<APIVpnProfile>() {
        @Override
        public APIVpnProfile createFromParcel(Parcel in) {
            return new APIVpnProfile(in);
        }

        @Override
        public APIVpnProfile[] newArray(int size) {
            return new APIVpnProfile[size];
        }
    };
}
