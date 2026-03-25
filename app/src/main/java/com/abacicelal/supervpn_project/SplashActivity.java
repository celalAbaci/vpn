package com.abacicelal.supervpn_project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.abacicelal.supervpn_project.utils.SecurePrefsManager;
import com.abacicelal.supervpn_project.utils.SecurityUtils;

public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Şifreli depo başlat — eski VPN_PREFS'ten token'ları migrate eder
        SecurePrefsManager.initialize(this);

        if (BuildConfig.ENABLE_SECURITY_CHECKS) {
            // APK kurcalanmış mı? (EXPECTED_SIGNATURE doldurulduğunda aktif olur)
            if (!SecurityUtils.isValidSignature(this)) {
                finish();
                return;
            }
            // Debugger bağlı mı?
            if (SecurityUtils.isDebuggerAttached()) {
                finish();
                return;
            }
            // Root tespiti — kilitleme yerine uyarı göster
            if (SecurityUtils.isRooted(this)) {
                new AlertDialog.Builder(this)
                        .setTitle("Güvenlik Uyarısı")
                        .setMessage("Bu cihaz root erişimine sahip. VPN güvenliği risk altında olabilir.")
                        .setPositiveButton("Devam Et", (d, w) -> launchMain())
                        .setNegativeButton("Çıkış", (d, w) -> finish())
                        .setCancelable(false)
                        .show();
                return;
            }
        }

        launchMain();
    }

    private void launchMain() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 3000);
    }
}