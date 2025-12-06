package com.abacicelal.supervpn_project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abacicelal.supervpn_project.remote.ApiService;
import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.Server;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServerSelectionActivity extends AppCompatActivity {
    private static final String TAG = "ServerSelectionActivity"; // Loglama için
    private RecyclerView serverRecyclerView;
    private ServerAdapter serverAdapter;
    private ImageButton backButton;
    private ProgressBar loadingBar;

    private List<Server> serverList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_selection);

        // --- ÇÖZÜM: TOKEN KONTROLÜ EKLENDİ ---
        // Kullanıcı giriş yapmamışsa, bu ekranı hiç gösterme.
        if (RetrofitClient.getToken(this) == null) {
            Toast.makeText(this, "Lütfen önce giriş yapın", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, AccountActivity.class);
            startActivity(intent);
            finish(); // Bu Activity'yi kapat
            return; // onCreate metodunun geri kalanını çalıştırma
        }
        // --- ÇÖZÜM BİTTİ ---

        serverRecyclerView = findViewById(R.id.serverRecyclerView);
        backButton = findViewById(R.id.backButton);
        loadingBar = findViewById(R.id.loadingBar);

        apiService = RetrofitClient.getApiService(getApplicationContext());

        setupRecyclerView();

        // Geri butonu işlevi
        backButton.setOnClickListener(v -> finish());

        // Sunucuları backend'den çek
        fetchActiveServers();
    }

    private void setupRecyclerView() {
        serverRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        serverAdapter = new ServerAdapter(serverList, this);
        serverRecyclerView.setAdapter(serverAdapter);
    }

    private void fetchActiveServers() {
        Log.d(TAG, "Aktif sunucular çekiliyor...");
        loadingBar.setVisibility(View.VISIBLE);
        serverRecyclerView.setVisibility(View.GONE);

        apiService.getActiveServers().enqueue(new Callback<List<Server>>() {
            @Override
            public void onResponse(Call<List<Server>> call, Response<List<Server>> response) {
                loadingBar.setVisibility(View.GONE);
                serverRecyclerView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, response.body().size() + " adet sunucu başarıyla çekildi.");
                    serverList.clear();
                    serverList.addAll(response.body());
                    serverAdapter.notifyDataSetChanged();
                } else {
                    // 401 (Yetkisiz) veya 500 (Sunucu Hatası) gibi durumlarda burası çalışır
                    Log.e(TAG, "Sunucular çekilemedi. Hata kodu: " + response.code());
                    Toast.makeText(ServerSelectionActivity.this, "Sunucular yüklenemedi (Hata: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Server>> call, Throwable t) {
                loadingBar.setVisibility(View.GONE);
                // SSL Hatası veya İnternet Yoksa burası çalışır
                Log.e(TAG, "Sunucu çekme hatası (onFailure): ", t);
                Toast.makeText(ServerSelectionActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adapter Sınıfı
    private static class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ServerViewHolder> {
        private List<Server> serverList;
        private Context context;

        public ServerAdapter(List<Server> serverList, Context context) {
            this.serverList = serverList;
            this.context = context;
        }

        @NonNull
        @Override
        public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_server, parent, false);
            return new ServerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
            Server server = serverList.get(position);

            if (server.getCountry() != null) {
                holder.countryCodeTextView.setText(server.getCountry().getCountryCode());
                holder.serverNameTextView.setText(server.getCountry().getCountryName());
            } else {
                holder.countryCodeTextView.setText("??");
                holder.serverNameTextView.setText(server.getServerName());
            }

            float load = server.getCurrentLoadPercentage() != null ? server.getCurrentLoadPercentage() : 0.0f;
            holder.pingTextView.setText(String.format("%.0f%% Yük", load));

            if (load < 40) {
                holder.statusIndicator.setImageResource(R.drawable.ic_signal_strong);
            } else if (load < 75) {
                holder.statusIndicator.setImageResource(R.drawable.ic_signal_medium);
            } else {
                holder.statusIndicator.setImageResource(R.drawable.ic_signal_weak);
            }

            holder.premiumTextView.setVisibility(View.GONE);
            holder.premiumIcon.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(v -> {
                SharedPreferences prefs = context.getSharedPreferences("VPN_PREFS", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putLong("selected_server_id", server.getId());
                editor.putString("selected_server_name", server.getCountry() != null ? server.getCountry().getCountryName() : server.getServerName());
                editor.apply();

                Log.d(TAG, "Sunucu seçildi: ID " + server.getId() + ", Ad " + server.getServerName());

                if (context instanceof AppCompatActivity) {
                    ((AppCompatActivity) context).finish();
                }
            });
        }

        @Override
        public int getItemCount() {
            return serverList.size();
        }

        public static class ServerViewHolder extends RecyclerView.ViewHolder {
            TextView countryCodeTextView;
            TextView serverNameTextView;
            TextView pingTextView;
            TextView premiumTextView;
            ImageView premiumIcon;
            ImageView statusIndicator;

            public ServerViewHolder(View itemView) {
                super(itemView);
                countryCodeTextView = itemView.findViewById(R.id.countryCodeTextView);
                serverNameTextView = itemView.findViewById(R.id.serverNameTextView);
                pingTextView = itemView.findViewById(R.id.pingTextView);
                premiumTextView = itemView.findViewById(R.id.premiumTextView);
                premiumIcon = itemView.findViewById(R.id.premiumIcon);
                statusIndicator = itemView.findViewById(R.id.statusIndicator);
            }
        }
    }
}