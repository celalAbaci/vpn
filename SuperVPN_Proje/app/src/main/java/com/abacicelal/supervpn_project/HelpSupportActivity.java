package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton; // ImageButton için import eklendi
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog; // Onay paneli (AlertDialog) için import eklendi

public class HelpSupportActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        // Geri butonu tanımlandı ve tıklandığında mevcut sayfayı kapatması sağlandı
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // Mevcut Activity'i sonlandırır ve bir önceki ekrana döner
        });

        // İletişim butonu tanımlandı
        Button contactButton = findViewById(R.id.contactButton);
        contactButton.setOnClickListener(v -> {
            // Yönlendirme onayı için AlertDialog oluştur
            new AlertDialog.Builder(HelpSupportActivity.this)
                    .setTitle("Yönlendirme") // Dialog için bir başlık
                    .setMessage("Bir siteye yönlendiriliyorsunuz. Gitmek ister misiniz?")

                    // "Git" butonu (Olumlu)
                    .setPositiveButton("Git", (dialog, which) -> {
                        // Kullanıcı "Git" derse, tarayıcıyı aç
                        String url = "https://www.dataguardvpn.com/contact";
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                        browserIntent.setData(Uri.parse(url));
                        startActivity(browserIntent);
                    })

                    // "Kal" butonu (Olumsuz)
                    .setNegativeButton("Kal", (dialog, which) -> {
                        // Kullanıcı "Kal" derse, dialogu kapat
                        dialog.dismiss();
                    })

                    // Dialogu oluştur ve göster
                    .show();
        });
    }
}

