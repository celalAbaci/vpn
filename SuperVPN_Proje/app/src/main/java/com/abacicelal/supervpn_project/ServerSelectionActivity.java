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
import com.abacicelal.supervpn_project.remote.model.ApiResponse;
import com.abacicelal.supervpn_project.remote.model.Server;
import com.abacicelal.supervpn_project.remote.model.Subscription;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServerSelectionActivity extends AppCompatActivity {
    private static final String TAG = "ServerSelectionActivity";
    private RecyclerView serverRecyclerView;
    private ServerAdapter serverAdapter;
    private ImageButton backButton;
    private ProgressBar loadingBar;

    private List<Server> serverList = new ArrayList<>();
    private ApiService apiService;
    private boolean isPremium = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_selection);

        // --- AUTH CHECK ---
        if (RetrofitClient.getToken(this) == null) {
             Toast.makeText(this, "Lütfen önce giriş yapın", Toast.LENGTH_LONG).show();
             Intent intent = new Intent(this, LoginActivity.class);
             startActivity(intent);
             finish();
             return;
        }

        serverRecyclerView = findViewById(R.id.serverRecyclerView);
        backButton = findViewById(R.id.backButton);
        loadingBar = findViewById(R.id.loadingBar);

        apiService = RetrofitClient.getApiService(getApplicationContext());

        setupRecyclerView();

        backButton.setOnClickListener(v -> finish());

        checkSubscriptionAndFetchServers();
    }

    private void setupRecyclerView() {
        serverRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        serverAdapter = new ServerAdapter(serverList, this, isPremium);
        serverRecyclerView.setAdapter(serverAdapter);
    }

    private void checkSubscriptionAndFetchServers() {
        loadingBar.setVisibility(View.VISIBLE);

        apiService.getMySubscriptions().enqueue(new Callback<ApiResponse<List<Subscription>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Subscription>>> call, Response<ApiResponse<List<Subscription>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                     List<Subscription> subs = response.body().getData();
                     if (subs != null) {
                         for (Subscription s : subs) {
                             if (s.isActive()) {
                                 isPremium = true;
                                 break;
                             }
                         }
                     }
                }
                serverAdapter.setPremiumStatus(isPremium);
                fetchActiveServers();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Subscription>>> call, Throwable t) {
                fetchActiveServers();
            }
        });
    }

    private void fetchActiveServers() {
        Log.d(TAG, "Aktif sunucular çekiliyor...");

        apiService.getActiveServers().enqueue(new Callback<ApiResponse<List<Server>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Server>>> call, Response<ApiResponse<List<Server>>> response) {
                loadingBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    serverList.clear();
                    List<Server> allServers = response.body().getData();

                    if (!isPremium) {
                        // Show only "Free" servers
                        for (Server s : allServers) {
                            if (s.isFree()) {
                                serverList.add(s);
                            }
                        }

                        // Fallback: If no servers marked free, maybe show a limited amount or inform user
                        if (serverList.isEmpty()) {
                            // Backup logic: top 3
                             int limit = Math.min(allServers.size(), 3);
                             serverList.addAll(allServers.subList(0, limit));
                        }

                        // Use a toast to inform user
                        Toast.makeText(ServerSelectionActivity.this, "Free User: Showing limited servers.", Toast.LENGTH_SHORT).show();
                    } else {
                        serverList.addAll(allServers);
                    }

                    serverAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ServerSelectionActivity.this, "Sunucular yüklenemedi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Server>>> call, Throwable t) {
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(ServerSelectionActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ServerViewHolder> {
        private List<Server> serverList;
        private Context context;
        private boolean isUserPremium;

        public ServerAdapter(List<Server> serverList, Context context, boolean isUserPremium) {
            this.serverList = serverList;
            this.context = context;
            this.isUserPremium = isUserPremium;
        }

        public void setPremiumStatus(boolean status) {
            this.isUserPremium = status;
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
