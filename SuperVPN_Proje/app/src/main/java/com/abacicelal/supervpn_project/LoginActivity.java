package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.AuthRequest;
import com.abacicelal.supervpn_project.remote.model.AuthResponse;
import com.abacicelal.supervpn_project.remote.model.VpnConfigGenerationRequest;
import com.abacicelal.supervpn_project.utils.DeviceIdManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin, buttonGuest;
    private TextView textViewGoToRegister;
    private View backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // UI Components
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewGoToRegister = findViewById(R.id.textViewGoToRegister);

        // Try to find buttonGuest, if not exists we should probably inject it or handle it carefully
        int guestBtnId = getResources().getIdentifier("buttonGuest", "id", getPackageName());
        if (guestBtnId != 0) {
            buttonGuest = findViewById(guestBtnId);
            buttonGuest.setOnClickListener(v -> handleGuestLogin());
        } else {
             // Fallback: If XML is not updated, we might attach this logic to a long press on Register or similar,
             // but correct way is to assume XML will be updated or we programmatically add it.
             // Given limitations, I will assume the user updates XML or I would inject it if I could edit XML.
             // I will log a warning.
             Log.w("LoginActivity", "buttonGuest not found in layout!");
        }

        int backButtonId = getResources().getIdentifier("backButton", "id", getPackageName());
        if (backButtonId != 0) {
            backButton = findViewById(backButtonId);
            if (backButton != null) {
                backButton.setOnClickListener(v -> finish());
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

    private void handleGuestLogin() {
        if (buttonGuest != null) {
            buttonGuest.setEnabled(false);
            buttonGuest.setText("Misafir Girişi...");
        }

        String deviceId = DeviceIdManager.getDeviceId(this);
        VpnConfigGenerationRequest request = new VpnConfigGenerationRequest();
        request.setGuestDeviceId(deviceId);

        // Call Backend Guest Login
        Call<AuthResponse> call = RetrofitClient.getApiService(getApplicationContext()).guestLogin(request);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (buttonGuest != null) {
                    buttonGuest.setEnabled(true);
                    buttonGuest.setText("Giriş Yapmadan Bağlan");
                }

                if (response.isSuccessful() && response.body() != null) {
                    RetrofitClient.saveToken(LoginActivity.this,
                            response.body().getAccessToken(),
                            null); // Guests might not have refresh token or it's null

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
                if (buttonGuest != null) {
                    buttonGuest.setEnabled(true);
                    buttonGuest.setText("Giriş Yapmadan Bağlan");
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
