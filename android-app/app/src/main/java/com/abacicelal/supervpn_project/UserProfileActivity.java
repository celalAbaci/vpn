package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.abacicelal.supervpn_project.remote.ApiService;
import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.ApiResponse;
import com.abacicelal.supervpn_project.remote.model.Subscription;
import com.abacicelal.supervpn_project.remote.model.Device;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {

    private TextView textViewWelcome;
    private TextView textViewPlanName;
    private TextView textViewSubStatus;
    private TextView textViewExpiry;
    private TextView textViewSpeedLimit;
    private LinearLayout deviceListContainer;
    private Button buttonLogout;
    private View backButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        apiService = RetrofitClient.getApiService(getApplicationContext());

        // Initialize Views
        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewPlanName = findViewById(R.id.textViewPlanName);
        textViewSubStatus = findViewById(R.id.textViewSubStatus);
        textViewExpiry = findViewById(R.id.textViewExpiry);
        textViewSpeedLimit = findViewById(R.id.textViewSpeedLimit);
        deviceListContainer = findViewById(R.id.deviceListContainer);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Handle back button
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
                Toast.makeText(UserProfileActivity.this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        if (textViewWelcome != null) {
            textViewWelcome.setText(getString(R.string.account_title));
            loadUserData();
        }
    }

    private void loadUserData() {
        // 1. Fetch Subscriptions
        apiService.getMySubscriptions().enqueue(new Callback<ApiResponse<List<Subscription>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Subscription>>> call, Response<ApiResponse<List<Subscription>>> response) {
                if (textViewWelcome == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Subscription> subs = response.body().getData();
                    Subscription activeSub = null;
                    for (Subscription s : subs) {
                        if (s.isActive()) {
                            activeSub = s;
                            break;
                        }
                    }

                    if (activeSub != null) {
                        String planName = activeSub.getPlan() != null ? activeSub.getPlan().getName() : "Premium";
                        textViewPlanName.setText(String.format(getString(R.string.subscription_plan), planName));

                        textViewSubStatus.setText(String.format(getString(R.string.subscription_status), getString(R.string.subscription_active)));
                        textViewSubStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));

                        textViewExpiry.setText(String.format(getString(R.string.subscription_expiry), activeSub.getEndDate()));

                        int speed = activeSub.getSpeedLimitMbps() != null ? activeSub.getSpeedLimitMbps() :
                                   (activeSub.getPlan() != null ? activeSub.getPlan().getSpeedLimitMbps() : 0);
                        textViewSpeedLimit.setText(String.format(getString(R.string.subscription_speed), String.valueOf(speed)));
                    } else {
                        textViewPlanName.setText(getString(R.string.no_active_subscription));
                        textViewSubStatus.setText(String.format(getString(R.string.subscription_status), getString(R.string.subscription_inactive)));
                        textViewSubStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        textViewExpiry.setText("");
                        textViewSpeedLimit.setText("");
                    }

                } else {
                     textViewPlanName.setText(getString(R.string.subscription_info_error));
                }
                // Continue to load devices
                loadDevices();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Subscription>>> call, Throwable t) {
                if (textViewPlanName != null) textViewPlanName.setText(getString(R.string.subscription_conn_error));
                loadDevices();
            }
        });
    }

    private void loadDevices() {
        apiService.getMyDevices().enqueue(new Callback<ApiResponse<List<Device>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Device>>> call, Response<ApiResponse<List<Device>>> response) {
                if (deviceListContainer == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Device> devices = response.body().getData();
                    deviceListContainer.removeAllViews();

                    LayoutInflater inflater = LayoutInflater.from(UserProfileActivity.this);

                    for (Device device : devices) {
                        View deviceView = inflater.inflate(R.layout.item_device, deviceListContainer, false);

                        TextView nameView = deviceView.findViewById(R.id.textViewDeviceName);
                        TextView statusView = deviceView.findViewById(R.id.textViewDeviceStatus);
                        TextView lastSeenView = deviceView.findViewById(R.id.textViewDeviceLastSeen);

                        nameView.setText(device.getDeviceName());

                        if (device.isActive()) {
                            statusView.setText(getString(R.string.device_status_active));
                            statusView.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                        } else {
                            statusView.setText(getString(R.string.device_status_inactive));
                            statusView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        }

                        lastSeenView.setText(String.format(getString(R.string.device_last_seen), device.getLastSeen()));

                        deviceListContainer.addView(deviceView);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Device>>> call, Throwable t) {
                // Silently fail or show error
            }
        });
    }
}
