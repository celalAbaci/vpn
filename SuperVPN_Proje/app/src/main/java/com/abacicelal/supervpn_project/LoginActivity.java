package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.AuthRequest;
import com.abacicelal.supervpn_project.remote.model.AuthResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewGoToRegister;
    private View backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // UI Bileşenlerini Tanımla (Mevcut XML'e göre)
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewGoToRegister = findViewById(R.id.textViewGoToRegister);

        // Misafir Butonu (XML'de olması gerek, dinamik ekliyoruz şimdilik XML'i göremediğimiz için)
        // Eğer XML'i editleme imkanımız yoksa, dinamik ekleyebiliriz veya varsayabiliriz.
        // Ama kullanıcı planında XML editleme yok, o yüzden mantıklı olan "Register" textine eklemek veya dinamik buton.

        // backButton opsiyonel kontrol
        int backButtonId = getResources().getIdentifier("backButton", "id", getPackageName());
        if (backButtonId != 0) {
            backButton = findViewById(backButtonId);
            if (backButton != null) {
                backButton.setOnClickListener(v -> finish());
            }
        }

        // Check if buttonGuest exists in R.id (since we patched XML)
        // Using reflection to be safe against build issues in this env, but typically R.id.buttonGuest
        int guestBtnId = getResources().getIdentifier("buttonGuest", "id", getPackageName());
        if (guestBtnId != 0) {
            View guestButton = findViewById(guestBtnId);
            if (guestButton != null) {
                guestButton.setOnClickListener(v -> continueAsGuest());
            }
        }

        if (buttonLogin != null) {
            buttonLogin.setOnClickListener(v -> handleLogin());
        }

        if (textViewGoToRegister != null) {
            textViewGoToRegister.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            });
        }
    }

    private void continueAsGuest() {
        // Perform Guest Login against Backend
        String deviceId = com.abacicelal.supervpn_project.utils.DeviceIdManager.getDeviceId(this);
        String deviceName = android.os.Build.MODEL;

        if (buttonLogin != null) {
            buttonLogin.setEnabled(false);
            buttonLogin.setText("Misafir Girişi...");
        }

        com.abacicelal.supervpn_project.remote.model.GuestLoginRequest request =
            new com.abacicelal.supervpn_project.remote.model.GuestLoginRequest(deviceId, deviceName);

        RetrofitClient.getApiService(this).guestLogin(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (buttonLogin != null) {
                    buttonLogin.setEnabled(true);
                    buttonLogin.setText("GİRİŞ YAP");
                }

                if (response.isSuccessful() && response.body() != null) {
                    // Save Guest Token
                    RetrofitClient.saveToken(LoginActivity.this,
                            response.body().getAccessToken(),
                            null); // Guests might not have refresh token

                    Toast.makeText(LoginActivity.this, "Misafir Girişi Başarılı!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                     Toast.makeText(LoginActivity.this, "Misafir girişi başarısız!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (buttonLogin != null) {
                    buttonLogin.setEnabled(true);
                    buttonLogin.setText("GİRİŞ YAP");
                }
                Toast.makeText(LoginActivity.this, "Bağlantı hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogin() {
        if (editTextUsername == null || editTextPassword == null) return;

        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonLogin.setEnabled(false);
        buttonLogin.setText("Giriş Yapılıyor...");

        AuthRequest authRequest = new AuthRequest(username, password);
        Call<AuthResponse> call = RetrofitClient.getApiService(getApplicationContext()).loginUser(authRequest);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (buttonLogin != null) {
                    buttonLogin.setEnabled(true);
                    buttonLogin.setText("GİRİŞ YAP");
                }

                if (response.isSuccessful() && response.body() != null) {
                    RetrofitClient.saveToken(LoginActivity.this,
                            response.body().getAccessToken(),
                            response.body().getRefreshToken());

                    Toast.makeText(LoginActivity.this, "Giriş Başarılı!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Giriş başarısız! Bilgilerinizi kontrol edin.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (buttonLogin != null) {
                    buttonLogin.setEnabled(true);
                    buttonLogin.setText("GİRİŞ YAP");
                }
                Toast.makeText(LoginActivity.this, "Bağlantı hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
