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
import com.abacicelal.supervpn_project.remote.model.AuthResponse;
import com.abacicelal.supervpn_project.remote.model.RegisterRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity"; // Loglama için TAG

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // --- YENİ EKLENDİ: Geri Butonu ---
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // Mevcut Activity'i sonlandırır ve bir önceki ekrana döner
        });
        // --- Geri Butonu Eklentisi Bitti ---

        // XML'e yeni eklenen 'editTextUsername' alanı da dahil edildi
        EditText usernameInput = findViewById(R.id.editTextUsername);
        EditText emailInput = findViewById(R.id.editTextEmail);
        EditText passwordInput = findViewById(R.id.editTextPassword);
        Button registerButton = findViewById(R.id.buttonRegister);
        TextView goToLogin = findViewById(R.id.textViewGoToLogin);

        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Basit doğrulama
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Tüm alanlar doldurulmalıdır", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Şifre en az 6 karakter olmalıdır", Toast.LENGTH_SHORT).show();
                return;
            }

            // Butonu tıklanmaz yap
            registerButton.setEnabled(false);
            Toast.makeText(RegisterActivity.this, "Kayıt olunuyor...", Toast.LENGTH_SHORT).show();

            // 1. RegisterRequest modelini oluştur
            RegisterRequest registerRequest = new RegisterRequest(username, email, password);

            // 2. API'yi çağır
            Call<AuthResponse> call = RetrofitClient.getApiService(getApplicationContext()).registerUser(registerRequest);

            // 3. Çağrıyı asenkron çalıştır
            call.enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    // Butonu tekrar tıklanabilir yap
                    registerButton.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        // 4. Başarılı kayıt (Genellikle kayıt sonrası token döner ve token'ı kaydedersiniz)
                        // Backend projeniz (superVPNProject.zip) kayıt sonrası token veriyor.
                        String accessToken = response.body().getAccessToken();
                        String refreshToken = response.body().getRefreshToken();

                        // Token'ları kaydet
                        RetrofitClient.saveToken(RegisterActivity.this, accessToken, refreshToken);
                        Toast.makeText(RegisterActivity.this, "Kayıt başarılı! Lütfen giriş yapın.", Toast.LENGTH_LONG).show();

                        // 5. Kayıt başarılı, kullanıcıyı giriş ekranına yönlendir
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        // Kayıt ekranını temizle
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // RegisterActivity'yi kapat
                    } else {
                        // 400 (Bad Request) -örn: kullanıcı adı/email zaten alınmış
                        String hataMesaji = "Kayıt başarısız. Lütfen bilgileri kontrol edin.";
                        if(response.code() == 400) {
                            hataMesaji = "Bu kullanıcı adı veya e-posta zaten kullanılıyor.";
                        }
                        Log.e(TAG, "Kayıt hatası, Kod: " + response.code() + ", Mesaj: " + response.message());
                        Toast.makeText(RegisterActivity.this, hataMesaji, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    // Butonu tekrar tıklanabilir yap
                    registerButton.setEnabled(true);
                    // Ağ hatası
                    Log.e(TAG, "Ağ hatası: ", t);
                    Toast.makeText(RegisterActivity.this, "Bağlantı hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        goToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish(); // RegisterActivity'yi kapat
        });
    }
}
