package de.blinkt.openvpn.core;

import android.content.Context;
import de.blinkt.openvpn.VpnProfile;

public class ProfileManager {
    public static ProfileManager getInstance(Context c) { return new ProfileManager(); }
    public void setTemporaryProfile(Context c, VpnProfile vp) { }
}
