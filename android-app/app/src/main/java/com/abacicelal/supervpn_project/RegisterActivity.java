package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.AuthResponse;
import com.abacicelal.supervpn_project.remote.model.RegisterRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextPassword;
    private Button buttonRegister;
    private TextView textViewGoToLogin;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewGoToLogin = findViewById(R.id.textViewGoToLogin);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        buttonRegister.setOnClickListener(v -> handleRegister());

        textViewGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegister() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Geçersiz e-posta adresi.", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonRegister.setEnabled(false);
        buttonRegister.setText("Kayıt Olunuyor...");

        RegisterRequest registerRequest = new RegisterRequest(username, email, password);
        Call<AuthResponse> call = RetrofitClient.getApiService(getApplicationContext()).registerUser(registerRequest);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                buttonRegister.setEnabled(true);
                buttonRegister.setText("KAYIT OL");

                if (response.isSuccessful() && response.body() != null) {
                    // Kayıt başarılı olduğunda otomatik giriş yapmış sayılır (JWT döner)
                    RetrofitClient.saveToken(RegisterActivity.this,
                            response.body().getAccessToken(),
                            response.body().getRefreshToken());

                    Toast.makeText(RegisterActivity.this, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, UserProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Kayıt başarısız. Kullanıcı adı veya e-posta kullanılıyor olabilir.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                buttonRegister.setEnabled(true);
                buttonRegister.setText("KAYIT OL");
                Toast.makeText(RegisterActivity.this, "Bağlantı hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
