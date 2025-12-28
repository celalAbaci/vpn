package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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

        // DYNAMICALLY ADD GUEST BUTTON
        // Since we cannot modify XML directly safely without seeing the hierarchy, we inject it programmatically.
        addGuestButton();

        // backButton opsiyonel kontrol
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

    private void addGuestButton() {
        Button guestButton = new Button(this);
        guestButton.setText("Giriş Yapmadan Bağlan (Misafir)");
        guestButton.setTransformationMethod(null); // No all-caps
        guestButton.setOnClickListener(v -> continueAsGuest());

        // Attempt to find a suitable container
        ViewGroup root = findViewById(android.R.id.content);
        if (root != null) {
            // Traverse to find the main layout (usually the first child of content)
            if (root.getChildCount() > 0 && root.getChildAt(0) instanceof ViewGroup) {
                 ViewGroup mainLayout = (ViewGroup) root.getChildAt(0);
                 // Add at the bottom or below login button if possible
                 mainLayout.addView(guestButton);
            } else {
                 root.addView(guestButton);
            }
        }
    }

    private void continueAsGuest() {
        // Clear tokens just in case to ensure "Guest" state
        RetrofitClient.saveToken(this, null, null);
        Toast.makeText(this, "Misafir Olarak Devam Ediliyor...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
