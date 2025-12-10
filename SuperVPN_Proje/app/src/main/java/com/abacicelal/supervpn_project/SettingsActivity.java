package com.abacicelal.supervpn_project;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView versionInfo = findViewById(R.id.versionInfo);
        versionInfo.setText("Sürüm: 1.0.0\nGizlilik Politikası: Verileriniz gizlidir.\nKullanım Şartları: VPN kullanımı yasalara tabidir.");
    }
}
