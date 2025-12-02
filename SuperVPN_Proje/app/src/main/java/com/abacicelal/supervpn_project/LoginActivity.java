package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton; // Geri butonu için eklendi
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Gerekli Retrofit ve Model importları
import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.AuthRequest;
import com.abacicelal.supervpn_project.remote.model.AuthResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity"; // Loglama için TAG

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // --- YENİ EKLENDİ: Geri Butonu ---
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // Mevcut Activity'i sonlandırır ve bir önceki ekrana döner
        });
        // --- Geri Butonu Eklentisi Bitti ---

        // XML'deki ID'ler güncellendi (editTextEmail -> editTextUsername)
        EditText usernameInput = findViewById(R.id.editTextUsername);
        EditText passwordInput = findViewById(R.id.editTextPassword);
        Button loginButton = findViewById(R.id.buttonLogin);
        TextView goToRegister = findViewById(R.id.textViewGoToRegister);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Basit doğrulama
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Kullanıcı adı ve şifre boş olamaz", Toast.LENGTH_SHORT).show();
                return;
            }

            // Butonu tıklanmaz yap (isteğe bağlı, çift tıklamayı önler)
            loginButton.setEnabled(false);
            Toast.makeText(LoginActivity.this, "Giriş yapılıyor...", Toast.LENGTH_SHORT).show();

            // 1. AuthRequest modelini oluştur (Backend'in beklediği 'username' ile)
            AuthRequest authRequest = new AuthRequest(username, password);

            // 2. RetrofitClient üzerinden ApiService'i al ve API'yi çağır
            Call<AuthResponse> call = RetrofitClient.getApiService(getApplicationContext()).loginUser(authRequest);

            // 3. Çağrıyı asenkron olarak çalıştır
            call.enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    // Butonu tekrar tıklanabilir yap
                    loginButton.setEnabled(true);

                    // 4. Yanıtı kontrol et
                    if (response.isSuccessful() && response.body() != null) {
                        // 5. Başarılı ise tokenları al
                        String accessToken = response.body().getAccessToken();
                        String refreshToken = response.body().getRefreshToken();

                        // 6. Token'ları SharedPreferences'a kaydet
                        RetrofitClient.saveToken(LoginActivity.this, accessToken, refreshToken);
                        Toast.makeText(LoginActivity.this, "Giriş başarılı!", Toast.LENGTH_SHORT).show();

                        // 7. Kullanıcıyı Profil Sayfasına (UserProfileActivity) yönlendir
                        Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                        // Intent'e FLAG_ACTIVITY_CLEAR_TOP ekleyerek geri tuşuyla Login'e dönmesini engelle
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish(); // LoginActivity'yi kapat
                    } else {
                        // 401 (Unauthorized) veya diğer sunucu hataları
                        Log.e(TAG, "Giriş hatası, Kod: " + response.code() + ", Mesaj: " + response.message());
                        Toast.makeText(LoginActivity.this, "Kullanıcı adı veya şifre hatalı", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    // Butonu tekrar tıklanabilir yap
                    loginButton.setEnabled(true);
                    // Ağ hatası veya JSON parse hatası
                    Log.e(TAG, "Ağ hatası: ", t);
                    Toast.makeText(LoginActivity.this, "Bağlantı hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            // finish(); // Kayıt ekranına giderken bunu kapatmayalım, kullanıcı geri dönebilir
        });
    }
}
