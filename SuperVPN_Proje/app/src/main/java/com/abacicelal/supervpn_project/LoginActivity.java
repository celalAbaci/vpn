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
import com.abacicelal.supervpn_project.remote.ApiService;
import com.abacicelal.supervpn_project.remote.model.AuthRequest;
import com.abacicelal.supervpn_project.remote.model.AuthResponse;
import com.abacicelal.supervpn_project.remote.model.GuestLoginRequest;
import com.abacicelal.supervpn_project.utils.DeviceIdManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewGoToRegister;
    private TextView textViewContinueGuest;
    private View backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewGoToRegister = findViewById(R.id.textViewGoToRegister);

        // Guest Button Logic
        int guestId = getResources().getIdentifier("textViewGuest", "id", getPackageName());
        if (guestId != 0) {
            textViewContinueGuest = findViewById(guestId);
            textViewContinueGuest.setOnClickListener(v -> loginAsGuest());
        }

        if (buttonLogin != null) buttonLogin.setOnClickListener(v -> handleLogin());
        if (textViewGoToRegister != null) textViewGoToRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        int backButtonId = getResources().getIdentifier("backButton", "id", getPackageName());
        if (backButtonId != 0) {
            backButton = findViewById(backButtonId);
            if (backButton != null) backButton.setOnClickListener(v -> finish());
        }
    }

    private void loginAsGuest() {
        if (buttonLogin != null) {
            buttonLogin.setEnabled(false);
            buttonLogin.setText(R.string.status_connecting);
        }

        String deviceId = DeviceIdManager.getDeviceId(this);
        GuestLoginRequest request = new GuestLoginRequest(deviceId);

        ApiService api = RetrofitClient.getApiService(getApplicationContext());
        Call<AuthResponse> call = api.guestLogin(request);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (buttonLogin != null) {
                    buttonLogin.setEnabled(true);
                    buttonLogin.setText(R.string.login);
                }

                if (response.isSuccessful() && response.body() != null) {
                    // Save Token (Backend issued GUEST token)
                    RetrofitClient.saveToken(LoginActivity.this,
                            response.body().getAccessToken(),
                            response.body().getRefreshToken());

                    Toast.makeText(LoginActivity.this, getString(R.string.guest_continue), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Fallback to offline/tokenless guest mode if backend fails
                    // This ensures the user can still try to connect even if auth endpoint is quirky,
                    // relying on the VpnConfigService auto-device creation.
                    RetrofitClient.saveToken(LoginActivity.this, null, null);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (buttonLogin != null) {
                    buttonLogin.setEnabled(true);
                    buttonLogin.setText(R.string.login);
                }
                // Fallback
                RetrofitClient.saveToken(LoginActivity.this, null, null);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
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
                    RetrofitClient.saveToken(LoginActivity.this, response.body().getAccessToken(), response.body().getRefreshToken());
                    Toast.makeText(LoginActivity.this, "Giriş Başarılı!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Giriş başarısız!", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (buttonLogin != null) {
                    buttonLogin.setEnabled(true);
                    buttonLogin.setText("GİRİŞ YAP");
                }
                Toast.makeText(LoginActivity.this, "Hata: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
