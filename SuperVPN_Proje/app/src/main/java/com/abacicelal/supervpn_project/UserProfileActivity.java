package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.abacicelal.supervpn_project.remote.ApiService;
import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.Subscription;
import com.abacicelal.supervpn_project.remote.model.Device;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {

    private TextView textViewWelcome;
    private Button buttonLogout;
    private View backButton; // View olarak alıyoruz, ImageButton veya Button olabilir
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        apiService = RetrofitClient.getApiService(getApplicationContext());

        // Mevcut ID'leri güvenli şekilde buluyoruz
        textViewWelcome = findViewById(R.id.textViewWelcome);
        buttonLogout = findViewById(R.id.buttonLogout);

        // R.id.backButton XML'de olmayabilir, kontrol ediyoruz
        int backButtonId = getResources().getIdentifier("backButton", "id", getPackageName());
        if (backButtonId != 0) {
            backButton = findViewById(backButtonId);
            if (backButton != null) {
                backButton.setOnClickListener(v -> finish());
            }
        }

        if (buttonLogout != null) {
            buttonLogout.setOnClickListener(v -> {
                RetrofitClient.saveToken(UserProfileActivity.this, null, null);
                Toast.makeText(UserProfileActivity.this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        if (textViewWelcome != null) {
            textViewWelcome.setText("Hesabım\nVeriler Yükleniyor...");
            loadUserData();
        }
    }

    private void loadUserData() {
        // 1. Abonelikleri Çek
        apiService.getMySubscriptions().enqueue(new Callback<List<Subscription>>() {
            @Override
            public void onResponse(Call<List<Subscription>> call, Response<List<Subscription>> response) {
                if (textViewWelcome == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Subscription> subs = response.body();
                    boolean isActive = subs.stream().anyMatch(Subscription::isActive);

                    String statusText = "\n\nAbonelik Durumu: " + (isActive ? "Aktif Premium" : "Ücretsiz / Pasif");
                    textViewWelcome.append(statusText);
                } else {
                     textViewWelcome.append("\n\nAbonelik: Bilgi Alınamadı");
                }
                // Cihazları çekmeye devam et
                loadDevices();
            }

            @Override
            public void onFailure(Call<List<Subscription>> call, Throwable t) {
                if (textViewWelcome != null) textViewWelcome.append("\n\nAbonelik: Bağlantı Hatası");
                loadDevices();
            }
        });
    }

    private void loadDevices() {
        apiService.getMyDevices().enqueue(new Callback<List<Device>>() {
            @Override
            public void onResponse(Call<List<Device>> call, Response<List<Device>> response) {
                if (textViewWelcome == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    int count = response.body().size();
                    textViewWelcome.append("\nBağlı Cihazlar: " + count);
                }
            }

            @Override
            public void onFailure(Call<List<Device>> call, Throwable t) {
                // Sessizce geç
            }
        });
    }
}
