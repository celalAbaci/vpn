package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton; // Geri butonu için eklendi
import androidx.appcompat.app.AppCompatActivity;
// Token kontrolü için RetrofitClient import edildi
import com.abacicelal.supervpn_project.remote.RetrofitClient;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // *** YENİ EKLENDİ: Token (Oturum) Kontrolü ***
        // Activity oluşturulur oluşturulmaz token'ı kontrol et
        if (RetrofitClient.getToken(this) != null) {
            // Eğer kullanıcı zaten giriş yapmışsa (token varsa),
            // "Giriş/Kayıt" ekranı yerine doğrudan Profil Sayfasını (UserProfileActivity) aç.
            Intent intent = new Intent(AccountActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish(); // AccountActivity'yi kapat ki kullanıcı geri dönemesin
            return; // onCreate'in geri kalanının çalışmasını engelle
        }

        // --- Eğer token yoksa (kullanıcı giriş yapmamışsa) ---
        // XML'i yükle ve butonları göster
        setContentView(R.layout.activity_account);

        // --- YENİ EKLENDİ: Geri Butonu ---
        // XML'e eklediğiniz @id/backButton ID'li butonu bulur
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // Mevcut Activity'i sonlandırır ve bir önceki ekrana döner
        });
        // --- Geri Butonu Eklentisi Bitti ---

        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonRegister = findViewById(R.id.buttonRegister);

        buttonLogin.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, LoginActivity.class));
        });

        buttonRegister.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, RegisterActivity.class));
        });
    }
}
