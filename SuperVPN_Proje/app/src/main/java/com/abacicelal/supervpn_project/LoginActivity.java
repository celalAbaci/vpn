package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.abacicelal.supervpn_project.remote.ApiService;
import com.abacicelal.supervpn_project.remote.model.AuthRequest;
import com.abacicelal.supervpn_project.remote.model.AuthResponse;
import com.abacicelal.supervpn_project.remote.model.GuestLoginRequest;
import com.abacicelal.supervpn_project.remote.model.GuestLoginResponse;
import com.abacicelal.supervpn_project.utils.DeviceIdManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnRegister;
    private Button btnGuestLogin; // New Guest Button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnGuestLogin = findViewById(R.id.btnGuestLogin); // Bind button (Ensure ID exists in XML)

        // Standard Login
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            performLogin(username, password);
        });

        // Register Navigation
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Guest Login Logic
        btnGuestLogin.setOnClickListener(v -> performGuestLogin());
    }

    private void performLogin(String username, String password) {
        // ... (Existing implementation)
    }

    private void performGuestLogin() {
        String deviceId = DeviceIdManager.getDeviceId(this);
        String deviceName = android.os.Build.MODEL;

        GuestLoginRequest request = new GuestLoginRequest(deviceId, deviceName);

        // Assuming RetrofitClient is configured somewhere, or building simplistic here for demo
        // In real app use: RetrofitClient.getInstance().getApiService()...
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // Emulator localhost
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<GuestLoginResponse> call = apiService.guestLogin(request);
        call.enqueue(new Callback<GuestLoginResponse>() {
            @Override
            public void onResponse(Call<GuestLoginResponse> call, Response<GuestLoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getAccessToken();
                    // Save token to SharedPrefs
                    getSharedPreferences("VPN_PREFS", MODE_PRIVATE)
                            .edit()
                            .putString("access_token", token)
                            .apply();

                    Toast.makeText(LoginActivity.this, "Guest Mode Activated", Toast.LENGTH_SHORT).show();

                    // Navigate to Main
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Guest Login Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GuestLoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
