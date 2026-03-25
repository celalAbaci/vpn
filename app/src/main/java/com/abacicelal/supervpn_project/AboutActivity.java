package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog; // Onay paneli (AlertDialog) için import eklendi

public class AboutActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Geri butonu tanımlandı ve tıklandığında aktiviteyi kapatması sağlandı
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // Mevcut aktiviteyi sonlandırır ve bir öncekine döner
        });

        Button privacyPolicyButton = findViewById(R.id.privacyPolicyButton);
        Button websiteButton = findViewById(R.id.websiteButton);

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
                .setTitle(getString(R.string.dialog_redirect_title))
                .setMessage(getString(R.string.dialog_redirect_message))
                .setPositiveButton(getString(R.string.dialog_redirect_go), (dialog, which) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                })
                .setNegativeButton(getString(R.string.dialog_redirect_stay), (dialog, which) -> {
                    dialog.dismiss();
                })

                // Dialogu oluştur ve göster
                .show();
    }
}
