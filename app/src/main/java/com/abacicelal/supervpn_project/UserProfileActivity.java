package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.abacicelal.supervpn_project.remote.model.ConnectionLog;
import com.abacicelal.supervpn_project.remote.model.Device;
import com.abacicelal.supervpn_project.remote.model.Subscription;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends BaseActivity {

    private ApiService apiService;

    private TextView tvPlanBadge, tvPlanName, tvActiveStatus;
    private TextView tvStartDate, tvEndDate, tvRemainingDays, tvSpeedLimit;
    private Button btnUpgrade, buttonLogout;
    private TextView tvDeviceCount;
    private LinearLayout llDevices, llLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        apiService = RetrofitClient.getApiService(getApplicationContext());

        tvPlanBadge      = findViewById(R.id.tvPlanBadge);
        tvPlanName       = findViewById(R.id.tvPlanName);
        tvActiveStatus   = findViewById(R.id.tvActiveStatus);
        tvStartDate      = findViewById(R.id.tvStartDate);
        tvEndDate        = findViewById(R.id.tvEndDate);
        tvRemainingDays  = findViewById(R.id.tvRemainingDays);
        tvSpeedLimit     = findViewById(R.id.tvSpeedLimit);
        btnUpgrade       = findViewById(R.id.btnUpgrade);
        buttonLogout     = findViewById(R.id.buttonLogout);
        tvDeviceCount    = findViewById(R.id.tvDeviceCount);
        llDevices        = findViewById(R.id.llDevices);
        llLogs           = findViewById(R.id.llLogs);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        buttonLogout.setOnClickListener(v -> {
            RetrofitClient.saveToken(this, null, null);
            Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnUpgrade.setOnClickListener(v ->
                startActivity(new Intent(this, PremiumActivity.class)));

        loadSubscription();
        loadDevices();
        loadLogs();
    }

    // ── Abonelik ─────────────────────────────────────────────────────────────

    private void loadSubscription() {
        apiService.getMySubscriptions().enqueue(new Callback<ApiResponse<List<Subscription>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Subscription>>> call,
                                   Response<ApiResponse<List<Subscription>>> response) {
                if (!response.isSuccessful() || response.body() == null
                        || response.body().getData() == null) {
                    showFreeState();
                    return;
                }

                Subscription active = null;
                for (Subscription s : response.body().getData()) {
                    if (s.isActive()) { active = s; break; }
                }

                if (active != null) {
                    showPremiumState(active);
                } else {
                    showFreeState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Subscription>>> call, Throwable t) {
                showFreeState();
            }
        });
    }

    private void showPremiumState(Subscription sub) {
        tvPlanBadge.setText(getString(R.string.plan_premium_badge));
        tvPlanBadge.setTextColor(Color.parseColor("#FFC107"));
        tvActiveStatus.setText(getString(R.string.status_active));
        tvActiveStatus.setTextColor(Color.parseColor("#4CAF50"));

        String planName = (sub.getPlan() != null && sub.getPlan().getName() != null)
                ? sub.getPlan().getName() : "Premium";
        tvPlanName.setText(planName);

        tvStartDate.setText(formatDate(sub.getStartDate()));
        tvEndDate.setText(formatDate(sub.getEndDate()));

        long remaining = remainingDays(sub.getEndDate());
        if (remaining >= 0) {
            tvRemainingDays.setText(String.valueOf(remaining));
            tvRemainingDays.setTextColor(remaining <= 7
                    ? Color.parseColor("#FF5555")
                    : Color.parseColor("#FFC107"));
        } else {
            tvRemainingDays.setText("—");
        }

        Integer speed = sub.getSpeedLimitMbps();
        tvSpeedLimit.setText(speed != null ? speed + " Mbps" : getString(R.string.unlimited));

        btnUpgrade.setVisibility(View.GONE);
    }

    private void showFreeState() {
        tvPlanBadge.setText(getString(R.string.plan_free_badge));
        tvPlanBadge.setTextColor(Color.parseColor("#AAAAAA"));
        tvActiveStatus.setText(getString(R.string.subscription_inactive));
        tvActiveStatus.setTextColor(Color.parseColor("#AAAAAA"));
        tvPlanName.setText(getString(R.string.plan_free_badge));
        tvStartDate.setText("—");
        tvEndDate.setText("—");
        tvRemainingDays.setText("—");
        tvSpeedLimit.setText("—");
        btnUpgrade.setVisibility(View.VISIBLE);
    }

    // ── Cihazlar ─────────────────────────────────────────────────────────────

    private void loadDevices() {
        apiService.getMyDevices().enqueue(new Callback<ApiResponse<List<Device>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Device>>> call,
                                   Response<ApiResponse<List<Device>>> response) {
                if (!response.isSuccessful() || response.body() == null
                        || response.body().getData() == null) return;

                List<Device> devices = response.body().getData();
                tvDeviceCount.setText(getString(R.string.devices_count, devices.size()));

                llDevices.removeAllViews();
                for (Device d : devices) {
                    llDevices.addView(buildDeviceRow(d));
                }

                if (devices.isEmpty()) {
                    llDevices.addView(buildEmptyLabel(getString(R.string.no_devices_found)));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Device>>> call, Throwable t) { }
        });
    }

    private View buildDeviceRow(Device device) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, dpToPx(10));

        // Cihaz adı
        TextView name = new TextView(this);
        name.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        name.setText(device.getDeviceName() != null ? device.getDeviceName() : getString(R.string.device_unknown));
        name.setTextColor(Color.WHITE);
        name.setTextSize(13f);

        // Son görülme
        TextView lastSeen = new TextView(this);
        lastSeen.setText(formatDateTime(device.getLastSeen()));
        lastSeen.setTextColor(Color.parseColor("#AAAAAA"));
        lastSeen.setTextSize(12f);

        // Aktif nokta
        if (device.isActive()) {
            name.setCompoundDrawablePadding(dpToPx(4));
            lastSeen.setText(getString(R.string.status_active));
            lastSeen.setTextColor(Color.parseColor("#4CAF50"));
        }

        row.addView(name);
        row.addView(lastSeen);
        return row;
    }

    // ── Bağlantı Logları ────────────────────────────────────────────────────

    private void loadLogs() {
        apiService.getMyLogs().enqueue(new Callback<ApiResponse<List<ConnectionLog>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ConnectionLog>>> call,
                                   Response<ApiResponse<List<ConnectionLog>>> response) {
                if (!response.isSuccessful() || response.body() == null
                        || response.body().getData() == null) return;

                List<ConnectionLog> logs = response.body().getData();
                llLogs.removeAllViews();

                // En son 5 kaydı göster
                int start = Math.max(0, logs.size() - 5);
                List<ConnectionLog> recent = logs.subList(start, logs.size());

                if (recent.isEmpty()) {
                    llLogs.addView(buildEmptyLabel(getString(R.string.no_connection_logs)));
                    return;
                }

                // Tersten göster (en yeni üstte)
                for (int i = recent.size() - 1; i >= 0; i--) {
                    llLogs.addView(buildLogRow(recent.get(i)));
                    if (i > 0) llLogs.addView(buildDivider());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ConnectionLog>>> call, Throwable t) { }
        });
    }

    private View buildLogRow(ConnectionLog log) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(0, 0, 0, dpToPx(6));

        // Tarih satırı
        LinearLayout dateRow = new LinearLayout(this);
        dateRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView dateLabel = new TextView(this);
        dateLabel.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        dateLabel.setText(formatDateTime(log.getConnectTime()));
        dateLabel.setTextColor(Color.WHITE);
        dateLabel.setTextSize(13f);

        TextView dataUsed = new TextView(this);
        String data = (log.getDataUsedMb() != null)
                ? String.format(Locale.US, "%.1f MB", log.getDataUsedMb().floatValue())
                : "";
        dataUsed.setText(data);
        dataUsed.setTextColor(Color.parseColor("#BB86FC"));
        dataUsed.setTextSize(12f);

        dateRow.addView(dateLabel);
        dateRow.addView(dataUsed);

        // Süre satırı
        TextView durationLabel = new TextView(this);
        durationLabel.setText(getString(R.string.label_duration) + " " + calcDuration(log.getConnectTime(), log.getDisconnectTime()));
        durationLabel.setTextColor(Color.parseColor("#AAAAAA"));
        durationLabel.setTextSize(12f);
        durationLabel.setPadding(0, dpToPx(2), 0, 0);

        col.addView(dateRow);
        col.addView(durationLabel);
        return col;
    }

    // ── Yardımcılar ──────────────────────────────────────────────────────────

    /** "2025-06-01" → "01 Haz 2025" */
    private String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return "—";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy", new Locale("tr"));
            Date d = in.parse(raw);
            return d != null ? out.format(d) : raw;
        } catch (ParseException e) {
            return raw.length() >= 10 ? raw.substring(0, 10) : raw;
        }
    }

    /** OffsetDateTime string → "01 Haz 2025 14:30" */
    private String formatDateTime(String raw) {
        if (raw == null || raw.isEmpty()) return "—";
        try {
            // OffsetDateTime örn: "2025-06-01T14:30:00.000+03:00"
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("dd MMM HH:mm", new Locale("tr"));
            Date d = in.parse(raw);
            if (d != null) return out.format(d);
        } catch (ParseException e) {
            // Farklı format dene
            try {
                SimpleDateFormat in2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
                SimpleDateFormat out = new SimpleDateFormat("dd MMM HH:mm", new Locale("tr"));
                Date d = in2.parse(raw);
                if (d != null) return out.format(d);
            } catch (ParseException ignored) { }
        }
        return raw.length() >= 10 ? raw.substring(0, 10) : raw;
    }

    /** Bitiş tarihine kaç gün kaldığını hesapla */
    private long remainingDays(String endDateStr) {
        if (endDateStr == null || endDateStr.isEmpty()) return -1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date end = sdf.parse(endDateStr);
            if (end == null) return -1;
            long diff = end.getTime() - System.currentTimeMillis();
            return TimeUnit.MILLISECONDS.toDays(diff);
        } catch (ParseException e) {
            return -1;
        }
    }

    /** İki OffsetDateTime string'inden süreyi "2s 35dk" biçiminde döndür */
    private String calcDuration(String connectStr, String disconnectStr) {
        if (connectStr == null || disconnectStr == null) return "—";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
            Date c = sdf.parse(connectStr);
            Date d = sdf.parse(disconnectStr);
            if (c == null || d == null) return "—";
            long diffMs = d.getTime() - c.getTime();
            if (diffMs < 0) return "—";
            long hours = TimeUnit.MILLISECONDS.toHours(diffMs);
            long mins  = TimeUnit.MILLISECONDS.toMinutes(diffMs) % 60;
            if (hours > 0) return hours + "s " + mins + "dk";
            return mins + " dk";
        } catch (ParseException e) {
            return "—";
        }
    }

    private View buildDivider() {
        View v = new View(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
        lp.topMargin    = dpToPx(6);
        lp.bottomMargin = dpToPx(6);
        v.setLayoutParams(lp);
        v.setBackgroundColor(Color.parseColor("#3E3E3E"));
        return v;
    }

    private TextView buildEmptyLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#777777"));
        tv.setTextSize(13f);
        tv.setPadding(0, dpToPx(4), 0, dpToPx(4));
        return tv;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
