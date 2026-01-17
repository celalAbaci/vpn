package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.abacicelal.supervpn_project.remote.ApiService;
import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.AuthResponse;
import com.abacicelal.supervpn_project.remote.model.GuestLoginRequest;
import com.abacicelal.supervpn_project.utils.DeviceIdManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button registerButton;
    private Button guestLoginButton; // New Button

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Ensure this layout exists and has the button

        apiService = RetrofitClient.getApiService(this);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        guestLoginButton = findViewById(R.id.guestLoginButton);

        loginButton.setOnClickListener(v -> login());
        registerButton.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        // Guest Login Logic
        guestLoginButton.setOnClickListener(v -> guestLogin());
    }

    private void login() {
        // ... existing login logic ...
    }

    private void guestLogin() {
        String deviceId = DeviceIdManager.getDeviceId(this);
        String deviceName = android.os.Build.MODEL;

        GuestLoginRequest request = new GuestLoginRequest(deviceId, deviceName);

        apiService.guestLogin(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    RetrofitClient.saveToken(LoginActivity.this, auth.getAccessToken());
                    // Navigate to Main
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Guest login failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
