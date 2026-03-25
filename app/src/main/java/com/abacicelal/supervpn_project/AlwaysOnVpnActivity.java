package com.abacicelal.supervpn_project;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageButton; // Geri butonu için eklendi
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AlwaysOnVpnActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_always_on_vpn);

        // --- YENİ EKLENDİ: Geri Butonu ---
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // Mevcut Activity'i sonlandırır ve bir önceki ekrana döner
        });
        // --- Geri Butonu Eklentisi Bitti ---

        Switch switchAlwaysOn = findViewById(R.id.switchAlwaysOnVpn);
        switchAlwaysOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Kullanıcıyı sistemin VPN ayarlarına yönlendir
                openVpnSettings();
            } else {
                // Özellik devre dışı bırakıldı
                Toast.makeText(this, "Her Zaman Açık VPN devre dışı bırakıldı.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openVpnSettings() {
        Toast.makeText(this, "Lütfen sistem ayarlarından VPN'i 'Her Zaman Açık' olarak ayarlayın.", Toast.LENGTH_LONG).show();
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(Settings.ACTION_VPN_SETTINGS);
        } else {
            intent = new Intent("android.net.vpn.SETTINGS");
        }

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "VPN ayarları açılamadı.", Toast.LENGTH_SHORT).show();
        }
    }
}
