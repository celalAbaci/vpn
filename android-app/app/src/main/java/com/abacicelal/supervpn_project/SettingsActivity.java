package com.abacicelal.supervpn_project;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.abacicelal.supervpn_project.remote.model.VpnProtocol;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "VPN_PREFS";
    private static final String KEY_PROTOCOL = "selected_protocol";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        TextView versionInfo = findViewById(R.id.versionInfo);
        versionInfo.setText("Sürüm: 1.0.0\nGizlilik Politikası: Verileriniz gizlidir.\nKullanım Şartları: VPN kullanımı yasalara tabidir.");

        setupProtocolSelection();
    }

    private void setupProtocolSelection() {
        RadioGroup group = findViewById(R.id.protocolRadioGroup);

        // Load saved state
        String saved = prefs.getString(KEY_PROTOCOL, "OPENVPN");
        if (saved.equals("OPENVPN")) group.check(R.id.radioOpenVPN);
        else if (saved.equals("IKEV2")) group.check(R.id.radioIKEv2);
        else if (saved.equals("SUPER")) group.check(R.id.radioSuper);
        else group.check(R.id.radioAuto);

        group.setOnCheckedChangeListener((g, checkedId) -> {
            String val = "OPENVPN";
            if (checkedId == R.id.radioOpenVPN) val = "OPENVPN";
            else if (checkedId == R.id.radioIKEv2) val = "IKEV2";
            else if (checkedId == R.id.radioSuper) val = "SUPER";
            else val = "AUTO";

            prefs.edit().putString(KEY_PROTOCOL, val).apply();
        });
    }
}
