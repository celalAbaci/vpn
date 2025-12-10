package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.abacicelal.supervpn_project.remote.ApiService;
import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.ApiResponse;
import com.abacicelal.supervpn_project.remote.model.SubscriptionPlan;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PremiumActivity extends AppCompatActivity {
    private static final String TAG = "PremiumActivity";
    private RelativeLayout weeklyPlanLayout;
    private RelativeLayout monthlyPlanLayout;
    private RelativeLayout yearlyPlanLayout;
    private TextView tvWeeklyPlanName, tvWeeklyPrice, tvWeeklyDetails;
    private TextView tvMonthlyPlanName, tvMonthlyPrice, tvMonthlyDetails;
    private TextView tvYearlyPlanName, tvYearlyPrice, tvYearlyDetails;
    private Button btnBuyWeekly;
    private Button btnBuyMonthly;
    private Button btnBuyYearly;
    private ProgressBar loadingBar;
    private ApiService apiService;
    private PlanType selectedPlanType = PlanType.MONTHLY;
    private Long weeklyPlanId = null;
    private Long monthlyPlanId = null;
    private Long yearlyPlanId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);

        // --- ÇÖZÜM: TOKEN KONTROLÜ EKLENDİ ---
        // Kullanıcı giriş yapmamışsa, bu ekranı açabilir ancak API isteği göndermeden
        // önce (örn. satın alma butonuna basınca) veya en başta kontrol etmeliyiz.
        // Şimdilik planları görmek için token'a gerek olmadığını varsayalım,
        // ancak "Zaten Premium'unuz var mı?" linki için bu kontrol mantıklı.
        //
        // NOT: Sizin backend kodunuzda (SecurityConfig) planları listelemek
        // (getSubscriptionPlans) token gerektiriyor. Bu yüzden buraya da
        // token kontrolü eklemek ZORUNDAYIZ.
        if (RetrofitClient.getToken(this) == null) {
            Toast.makeText(this, "Planları görmek için lütfen önce giriş yapın", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, AccountActivity.class);
            startActivity(intent);
            finish(); // Bu Activity'yi kapat
            return; // onCreate metodunun geri kalanını çalıştırma
        }
        // --- ÇÖZÜM BİTTİ ---

        apiService = RetrofitClient.getApiService(getApplicationContext());

        // UI elementlerini bağlama
        loadingBar = findViewById(R.id.loadingBar);
        weeklyPlanLayout = findViewById(R.id.weeklyPlanLayout);
        monthlyPlanLayout = findViewById(R.id.monthlyPlanLayout);
        yearlyPlanLayout = findViewById(R.id.yearlyPlanLayout);
        tvWeeklyPlanName = findViewById(R.id.tvWeeklyPlanName);
        tvMonthlyPlanName = findViewById(R.id.tvMonthlyPlanName);
        tvYearlyPlanName = findViewById(R.id.tvYearlyPlanName);
        tvWeeklyPrice = findViewById(R.id.tvWeeklyPrice);
        tvMonthlyPrice = findViewById(R.id.tvMonthlyPrice);
        tvYearlyPrice = findViewById(R.id.tvYearlyPrice);
        tvWeeklyDetails = findViewById(R.id.tvWeeklyDetails);
        tvMonthlyDetails = findViewById(R.id.tvMonthlyDetails);
        tvYearlyDetails = findViewById(R.id.tvYearlyDetails);
        btnBuyWeekly = findViewById(R.id.btnBuyWeekly);
        btnBuyMonthly = findViewById(R.id.btnBuyMonthly);
        btnBuyYearly = findViewById(R.id.btnBuyYearly);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        weeklyPlanLayout.setOnClickListener(v -> selectPlan(PlanType.WEEKLY));
        monthlyPlanLayout.setOnClickListener(v -> selectPlan(PlanType.MONTHLY));
        yearlyPlanLayout.setOnClickListener(v -> selectPlan(PlanType.YEARLY));
        btnBuyWeekly.setOnClickListener(v -> startPurchaseFlow(weeklyPlanId, "Haftalık Plan"));
        btnBuyMonthly.setOnClickListener(v -> startPurchaseFlow(monthlyPlanId, "Aylık Plan"));
        btnBuyYearly.setOnClickListener(v -> startPurchaseFlow(yearlyPlanId, "Yıllık Plan"));

        findViewById(R.id.tvAlreadyPremium).setOnClickListener(v -> {
            Intent intent = new Intent(PremiumActivity.this, AccountActivity.class);
            startActivity(intent);
        });

        selectPlan(PlanType.MONTHLY);
        fetchSubscriptionPlans();
    }

    private void fetchSubscriptionPlans() {
        Log.d(TAG, "Abonelik planları çekiliyor...");
        loadingBar.setVisibility(View.VISIBLE);
        setAllLayoutsEnabled(false);

        apiService.getSubscriptionPlans().enqueue(new Callback<ApiResponse<List<SubscriptionPlan>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SubscriptionPlan>>> call, Response<ApiResponse<List<SubscriptionPlan>>> response) {
                loadingBar.setVisibility(View.GONE);
                setAllLayoutsEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, response.body().getData().size() + " adet plan çekildi.");
                    populatePlans(response.body().getData());
                } else {
                    Log.e(TAG, "Planlar çekilemedi. Hata: " + response.code());
                    String errorMsg = (response.body() != null && response.body().getMessage() != null)
                            ? response.body().getMessage()
                            : "Abonelik planları yüklenemedi";
                    Toast.makeText(PremiumActivity.this, errorMsg + " (Hata: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SubscriptionPlan>>> call, Throwable t) {
                loadingBar.setVisibility(View.GONE);
                setAllLayoutsEnabled(true);
                Log.e(TAG, "Plan çekme hatası (onFailure): ", t);
                Toast.makeText(PremiumActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populatePlans(List<SubscriptionPlan> plans) {
        // Planları görünmez yap (API'den gelmezse diye)
        weeklyPlanLayout.setVisibility(View.GONE);
        monthlyPlanLayout.setVisibility(View.GONE);
        yearlyPlanLayout.setVisibility(View.GONE);

        for (SubscriptionPlan plan : plans) {
            // Planları sürelerine göre ayır
            if (plan.getDurationDays() != null && plan.getDurationDays() <= 10) { // Haftalık (örn: 7 gün)
                weeklyPlanId = plan.getId();
                tvWeeklyPlanName.setText(plan.getName());
                tvWeeklyPrice.setText(formatPrice(plan.getPrice(), "hafta"));
                tvWeeklyDetails.setText(String.format(Locale.getDefault(), "%d Cihaz / %dGB Limit", plan.getDeviceLimit(), plan.getDataLimitGb()));
                weeklyPlanLayout.setVisibility(View.VISIBLE);
            } else if (plan.getDurationDays() != null && plan.getDurationDays() <= 35) { // Aylık (örn: 30 gün)
                monthlyPlanId = plan.getId();
                tvMonthlyPlanName.setText(plan.getName());
                tvMonthlyPrice.setText(formatPrice(plan.getPrice(), "ay"));
                tvMonthlyDetails.setText(String.format(Locale.getDefault(), "%d Cihaz / %dGB Limit", plan.getDeviceLimit(), plan.getDataLimitGb()));
                monthlyPlanLayout.setVisibility(View.VISIBLE);
            } else if (plan.getDurationDays() != null) { // Yıllık (örn: 365 gün)
                yearlyPlanId = plan.getId();
                tvYearlyPlanName.setText(plan.getName());
                tvYearlyPrice.setText(formatPrice(plan.getPrice(), "yıl"));
                tvYearlyDetails.setText(String.format(Locale.getDefault(), "%d Cihaz / %dGB Limit", plan.getDeviceLimit(), plan.getDataLimitGb()));
                yearlyPlanLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private String formatPrice(BigDecimal price, String duration) {
        if (price == null) price = BigDecimal.ZERO;
        // TODO: Para birimini (örn: TL) backend'den veya sabit olarak ekleyin
        return String.format(Locale.getDefault(), "%.2f TL / %s", price, duration);
    }

    private void setAllLayoutsEnabled(boolean enabled) {
        weeklyPlanLayout.setEnabled(enabled);
        monthlyPlanLayout.setEnabled(enabled);
        yearlyPlanLayout.setEnabled(enabled);
    }

    private void selectPlan(PlanType planType) {
        this.selectedPlanType = planType;
        Log.d(TAG, "Plan seçildi: " + planType.name());

        weeklyPlanLayout.setBackgroundResource(R.drawable.bg_rounded_dark_gray_2);
        monthlyPlanLayout.setBackgroundResource(R.drawable.bg_rounded_dark_gray_2);
        yearlyPlanLayout.setBackgroundResource(R.drawable.bg_rounded_dark_gray_2);

        btnBuyWeekly.setBackgroundResource(R.drawable.bg_rounded_gray_button);
        btnBuyWeekly.setTextColor(getResources().getColor(R.color.gray_button_text));
        btnBuyMonthly.setBackgroundResource(R.drawable.bg_rounded_gray_button);
        btnBuyMonthly.setTextColor(getResources().getColor(R.color.gray_button_text));
        btnBuyYearly.setBackgroundResource(R.drawable.bg_rounded_gray_button);
        btnBuyYearly.setTextColor(getResources().getColor(R.color.gray_button_text));

        switch (planType) {
            case WEEKLY:
                weeklyPlanLayout.setBackgroundResource(R.drawable.bg_rounded_yellow_active);
                btnBuyWeekly.setBackgroundResource(R.drawable.bg_rounded_yellow_button);
                btnBuyWeekly.setTextColor(getResources().getColor(R.color.button_text_color));
                break;
            case MONTHLY:
                monthlyPlanLayout.setBackgroundResource(R.drawable.bg_rounded_yellow_active);
                btnBuyMonthly.setBackgroundResource(R.drawable.bg_rounded_yellow_button);
                btnBuyMonthly.setTextColor(getResources().getColor(R.color.button_text_color));
                break;
            case YEARLY:
                yearlyPlanLayout.setBackgroundResource(R.drawable.bg_rounded_yellow_active);
                btnBuyYearly.setBackgroundResource(R.drawable.bg_rounded_yellow_button);
                btnBuyYearly.setTextColor(getResources().getColor(R.color.button_text_color));
                break;
        }
    }

    private void startPurchaseFlow(Long planId, String planName) {
        if (planId == null) {
            Toast.makeText(this, "Bu plan şu anda mevcut değil.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, planName + " için satın alma denendi ancak planId null.");
            return;
        }

        // Burası Google Play Billing kütüphanesinin (BillingClient)
        // çağrılacağı yerdir.
        Log.d(TAG, "Satın alma akışı başlatılıyor... Plan ID: " + planId + " (" + planName + ")");
        Toast.makeText(this, planName + " için Google Play akışı başlatılıyor... (Simülasyon)", Toast.LENGTH_LONG).show();

    }

    private enum PlanType {
        WEEKLY, MONTHLY, YEARLY
    }
}