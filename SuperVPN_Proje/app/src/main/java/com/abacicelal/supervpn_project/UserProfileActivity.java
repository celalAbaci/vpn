package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton; // Geri butonu için eklendi
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
// RetrofitClient'a token silme işlemi için ihtiyacımız var
import com.abacicelal.supervpn_project.remote.RetrofitClient;

/**
 * Kullanıcı giriş yaptıktan sonra yönlendirileceği basit profil sayfası.
 * (LoginActivity'nin çökmemesi için oluşturuldu)
 */
public class UserProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // --- YENİ EKLENDİ: Geri Butonu ---
        // Not: Bu sayfa (UserProfileActivity), AccountActivity'den token varsa
        // yönlendirilen asıl "Hesabım" sayfasıdır.
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // Mevcut Activity'i sonlandırır ve bir önceki ekrana döner
        });
        // --- Geri Butonu Eklentisi Bitti ---

        TextView welcomeText = findViewById(R.id.textViewWelcome);
        Button logoutButton = findViewById(R.id.buttonLogout);

        // TODO: Burayı daha sonra /api/v1/user/details gibi bir endpoint'ten
        // gelen kullanıcı adıyla güncelleyebilirsiniz.
        // Şimdilik "Hoş geldiniz" yazıyoruz.
        welcomeText.setText("Hesabım");

        logoutButton.setOnClickListener(v -> {
            // 1. SharedPreferences'daki token'ları sil
            RetrofitClient.saveToken(UserProfileActivity.this, null, null);
            Toast.makeText(UserProfileActivity.this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show();

            // 2. Kullanıcıyı ana ekrana (veya giriş ekranına) yönlendir
            Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
            // Geri tuşuyla profil sayfasına dönülmesini engelle
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Profil sayfasını kapat
        });

        // TODO:
        // Burası artık kullanıcının hesap detaylarını (abonelik, cihazlar vb.)
        // görmek için API istekleri yapacağı yerdir.
        // Örnek:
        // getMySubscriptions();
        // getMyDevices();
    }
}
