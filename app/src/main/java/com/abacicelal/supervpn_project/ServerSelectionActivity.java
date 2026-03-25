package com.abacicelal.supervpn_project;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abacicelal.supervpn_project.remote.ApiService;
import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.ApiResponse;
import com.abacicelal.supervpn_project.remote.model.AuthResponse;
import com.abacicelal.supervpn_project.remote.model.GuestAuthRequest;
import com.abacicelal.supervpn_project.remote.model.Server;
import com.abacicelal.supervpn_project.utils.DeviceIdManager;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServerSelectionActivity extends BaseActivity {
    private static final String TAG = "ServerSelectionActivity";

    private RecyclerView serverRecyclerView;
    private ServerAdapter serverAdapter;
    private ImageButton backButton;
    private ProgressBar loadingBar;
    private Button btnFilterFree;
    private Button btnFilterPremium;

    private final List<Server> allServerList  = new ArrayList<>();
    private final List<Server> filteredList   = new ArrayList<>();
    private ApiService apiService;
    private boolean showingFree = true;

    // Ping altyapısı
    private final HashMap<Long, Integer> pingResults = new HashMap<>(); // -1=ölçülüyor, -2=erişilemiyor, >=0=ms
    private ExecutorService pingExecutor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_selection);

        serverRecyclerView = findViewById(R.id.serverRecyclerView);
        backButton         = findViewById(R.id.backButton);
        loadingBar         = findViewById(R.id.loadingBar);
        btnFilterFree      = findViewById(R.id.btnFilterFree);
        btnFilterPremium   = findViewById(R.id.btnFilterPremium);

        apiService = RetrofitClient.getApiService(getApplicationContext());

        setupRecyclerView();
        setupFilterButtons();
        backButton.setOnClickListener(v -> finish());

        if (RetrofitClient.getToken(this) == null) {
            doGuestAuthAndFetchServers();
        } else {
            fetchActiveServers();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pingExecutor != null) pingExecutor.shutdownNow();
    }

    // -----------------------------------------------------------------------
    // Kurulum
    // -----------------------------------------------------------------------

    private void setupRecyclerView() {
        serverRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        serverAdapter = new ServerAdapter(filteredList, pingResults, this);
        serverRecyclerView.setAdapter(serverAdapter);
    }

    private void setupFilterButtons() {
        btnFilterFree.setOnClickListener(v -> {
            showingFree = true;
            updateFilterButtonStyles();
            applyFilter();
        });
        btnFilterPremium.setOnClickListener(v -> {
            showingFree = false;
            updateFilterButtonStyles();
            applyFilter();
        });
    }

    private void updateFilterButtonStyles() {
        int active   = ContextCompat.getColor(this, R.color.purple_500);
        int inactive = ContextCompat.getColor(this, R.color.gray_dark);
        btnFilterFree.setBackgroundTintList(ColorStateList.valueOf(showingFree ? active : inactive));
        btnFilterPremium.setBackgroundTintList(ColorStateList.valueOf(showingFree ? inactive : active));
    }

    // -----------------------------------------------------------------------
    // Filtreleme
    // -----------------------------------------------------------------------

    private void applyFilter() {
        filteredList.clear();
        for (Server s : allServerList) {
            if (showingFree) {
                if (s.isFree()) filteredList.add(s);          // Free sekmesi: sadece ücretsiz
            } else {
                filteredList.add(s);                           // Premium sekmesi: hepsi
            }
        }
        serverAdapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            Toast.makeText(this,
                    showingFree ? getString(R.string.no_free_servers) : getString(R.string.no_premium_servers),
                    Toast.LENGTH_SHORT).show();
        } else {
            startPingAll();
        }
    }

    // -----------------------------------------------------------------------
    // API çağrıları
    // -----------------------------------------------------------------------

    private void doGuestAuthAndFetchServers() {
        loadingBar.setVisibility(View.VISIBLE);
        serverRecyclerView.setVisibility(View.GONE);

        String deviceUUID = DeviceIdManager.getDeviceId(this);
        String deviceName = "Android " + android.os.Build.MODEL;
        apiService.loginGuest(new GuestAuthRequest(deviceUUID, deviceName)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RetrofitClient.saveToken(ServerSelectionActivity.this,
                            response.body().getAccessToken(), response.body().getRefreshToken());
                    fetchActiveServers();
                } else {
                    loadingBar.setVisibility(View.GONE);
                    Toast.makeText(ServerSelectionActivity.this,
                            "Oturum açılamadı. Kod: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(ServerSelectionActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchActiveServers() {
        loadingBar.setVisibility(View.VISIBLE);
        serverRecyclerView.setVisibility(View.GONE);

        apiService.getActiveServers().enqueue(new Callback<ApiResponse<List<Server>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Server>>> call, Response<ApiResponse<List<Server>>> response) {
                loadingBar.setVisibility(View.GONE);
                serverRecyclerView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess() && response.body().getData() != null) {
                    allServerList.clear();
                    allServerList.addAll(response.body().getData());
                    applyFilter();
                } else {
                    Toast.makeText(ServerSelectionActivity.this,
                            "Sunucular yüklenemedi", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Server>>> call, Throwable t) {
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(ServerSelectionActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -----------------------------------------------------------------------
    // Ping ölçümü — ekran açılınca tek seferlik, her sunucu için paralel
    // -----------------------------------------------------------------------

    private void startPingAll() {
        if (pingExecutor != null) pingExecutor.shutdownNow();
        pingResults.clear();

        // Tüm sunucuları "ölçülüyor" durumuna getir
        for (Server s : filteredList) pingResults.put(s.getId(), -1);
        serverAdapter.notifyDataSetChanged();

        int threadCount = Math.min(filteredList.size(), 8);
        if (threadCount == 0) return;
        pingExecutor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < filteredList.size(); i++) {
            final Server server = filteredList.get(i);
            final int pos = i;
            if (server.getServerIpAddress() == null || server.getServerIpAddress().isEmpty()) {
                pingResults.put(server.getId(), -2);
                continue;
            }
            pingExecutor.execute(() -> {
                int ms = measurePingMs(server.getServerIpAddress());
                mainHandler.post(() -> {
                    pingResults.put(server.getId(), ms);
                    serverAdapter.notifyItemChanged(pos);
                });
            });
        }
    }

    /** TCP bağlantısı kurarak ping ölçer. 443, 80, 8080 sırayla denenir. */
    private int measurePingMs(String ip) {
        int[] ports = {443, 80, 8080};
        for (int port : ports) {
            long start = System.currentTimeMillis();
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ip, port), 3000);
                return (int) (System.currentTimeMillis() - start);
            } catch (Exception ignored) {}
        }
        return -2; // erişilemiyor
    }

    // -----------------------------------------------------------------------
    // Adapter
    // -----------------------------------------------------------------------

    private static class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ServerViewHolder> {
        private final List<Server> serverList;
        private final HashMap<Long, Integer> pingResults;
        private final Context context;

        ServerAdapter(List<Server> serverList, HashMap<Long, Integer> pingResults, Context context) {
            this.serverList  = serverList;
            this.pingResults = pingResults;
            this.context     = context;
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

            // Ülke / sunucu adı
            if (server.getCountry() != null) {
                holder.countryCodeTextView.setText(server.getCountry().getCountryCode());
                holder.serverNameTextView.setText(server.getCountry().getCountryName());
            } else {
                holder.countryCodeTextView.setText("??");
                holder.serverNameTextView.setText(server.getServerName());
            }

            // Ping gösterimi
            Integer ping = pingResults.get(server.getId());
            if (ping == null || ping == -1) {
                // Ölçülüyor
                holder.pingTextView.setText(context.getString(R.string.ping_measuring));
                holder.pingTextView.setTextColor(0xFFAAAAAA);
                holder.statusIndicator.setImageResource(R.drawable.ic_signal_medium);
                holder.statusIndicator.setAlpha(0.4f);
            } else if (ping == -2) {
                // Erişilemiyor
                holder.pingTextView.setText(context.getString(R.string.ping_timeout));
                holder.pingTextView.setTextColor(0xFFFF5555);
                holder.statusIndicator.setImageResource(R.drawable.ic_signal_weak);
                holder.statusIndicator.setAlpha(1f);
            } else if (ping < 80) {
                holder.pingTextView.setText(ping + " ms");
                holder.pingTextView.setTextColor(0xFF4CAF50); // yeşil
                holder.statusIndicator.setImageResource(R.drawable.ic_signal_strong);
                holder.statusIndicator.setAlpha(1f);
            } else if (ping < 150) {
                holder.pingTextView.setText(ping + " ms");
                holder.pingTextView.setTextColor(0xFFFFC107); // sarı
                holder.statusIndicator.setImageResource(R.drawable.ic_signal_medium);
                holder.statusIndicator.setAlpha(1f);
            } else {
                holder.pingTextView.setText(ping + " ms");
                holder.pingTextView.setTextColor(0xFFFF5555); // kırmızı
                holder.statusIndicator.setImageResource(R.drawable.ic_signal_weak);
                holder.statusIndicator.setAlpha(1f);
            }

            // Premium rozeti
            if (server.isFree()) {
                holder.premiumTextView.setVisibility(View.GONE);
                holder.premiumIcon.setVisibility(View.GONE);
            } else {
                holder.premiumTextView.setVisibility(View.VISIBLE);
                holder.premiumIcon.setVisibility(View.VISIBLE);
                holder.premiumTextView.setText(context.getString(R.string.premium));
            }

            // Tıklama
            holder.itemView.setOnClickListener(v -> {
                SharedPreferences prefs = context.getSharedPreferences("VPN_PREFS", MODE_PRIVATE);
                String name = server.getCountry() != null
                        ? server.getCountry().getCountryName() : server.getServerName();

                // Ping ve yük bilgisini kaydet (Auto protokol seçimi için)
                Integer pingVal = pingResults.get(server.getId());
                int savedPing = (pingVal != null && pingVal >= 0) ? pingVal : -1;
                float load = server.getCurrentLoadPercentage() != null
                        ? server.getCurrentLoadPercentage() : 50f;

                prefs.edit()
                        .putLong("selected_server_id", server.getId())
                        .putString("selected_server_name", name)
                        .putBoolean("selected_server_is_free", server.isFree())
                        .putInt("selected_server_ping_ms", savedPing)
                        .putFloat("selected_server_load_pct", load)
                        .apply();
                Toast.makeText(context, name + context.getString(R.string.server_selected), Toast.LENGTH_SHORT).show();
                if (context instanceof AppCompatActivity) {
                    ((AppCompatActivity) context).finish();
                }
            });
        }

        @Override
        public int getItemCount() { return serverList.size(); }

        static class ServerViewHolder extends RecyclerView.ViewHolder {
            TextView  countryCodeTextView, serverNameTextView, pingTextView, premiumTextView;
            ImageView premiumIcon, statusIndicator;

            ServerViewHolder(View itemView) {
                super(itemView);
                countryCodeTextView = itemView.findViewById(R.id.countryCodeTextView);
                serverNameTextView  = itemView.findViewById(R.id.serverNameTextView);
                pingTextView        = itemView.findViewById(R.id.pingTextView);
                premiumTextView     = itemView.findViewById(R.id.premiumTextView);
                premiumIcon         = itemView.findViewById(R.id.premiumIcon);
                statusIndicator     = itemView.findViewById(R.id.statusIndicator);
            }
        }
    }
}
