package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog; // Onay paneli (AlertDialog) için import eklendi

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Geri butonu tanımlandı ve tıklandığında aktiviteyi kapatması sağlandı
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // Mevcut aktiviteyi sonlandırır ve bir öncekine döner
        });

        ImageView aboutLogo = findViewById(R.id.aboutLogo);
        TextView aboutTitle = findViewById(R.id.aboutTitle);
        TextView aboutVersion = findViewById(R.id.aboutVersion);
        TextView aboutInfo = findViewById(R.id.aboutInfo);
        Button privacyPolicyButton = findViewById(R.id.privacyPolicyButton);
        Button websiteButton = findViewById(R.id.websiteButton);

        aboutTitle.setText("SuperVPN");
        aboutVersion.setText("Sürüm: 1.0.0");
        aboutInfo.setText("Bu uygulama kullanıcı gizliliğini ön planda tutar.\n\nSunucu bilgileriniz kaydedilmez.\n\nTelif Hakkı © 2025 SuperVPN Ltd.");

        // "Gizlilik Politikası" butonu için onay paneli eklendi
        privacyPolicyButton.setOnClickListener(v -> {
            showUrlConfirmationDialog("https://www.dataguardvpn.com/kvkk");
        });

        // "Web Sitemizi Ziyaret Edin" butonu için onay paneli eklendi
        websiteButton.setOnClickListener(v -> {
            showUrlConfirmationDialog("https://www.dataguardvpn.com/about");
        });
    }

    /**
     * Kullanıcıya bir URL'ye yönlendirilmeden önce onay gösteren bir dialog açar.
     * @param url Açılacak web sitesi adresi.
     */
    private void showUrlConfirmationDialog(String url) {
        new AlertDialog.Builder(AboutActivity.this)
                .setTitle("Yönlendirme")
                .setMessage("Bir siteye yönlendiriliyorsunuz. Gitmek ister misiniz?")

                // "Git" butonu (Olumlu)
                .setPositiveButton("Git", (dialog, which) -> {
                    // Kullanıcı "Git" derse, tarayıcıyı aç
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                })

                // "Kal" butonu (Olumsuz)
                .setNegativeButton("Kal", (dialog, which) -> {
                    // Kullanıcı "Kal" derse, dialogu kapat
                    dialog.dismiss();
                })

                // Dialogu oluştur ve göster
                .show();
    }
}
