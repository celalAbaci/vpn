package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.abacicelal.supervpn_project.remote.ApiService;
import com.abacicelal.supervpn_project.remote.RetrofitClient;
import com.abacicelal.supervpn_project.remote.model.Announcement;
import com.abacicelal.supervpn_project.remote.model.ApiResponse;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementsActivity extends BaseActivity {

    private LinearLayout container;
    private TextView emptyText;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        container = findViewById(R.id.announcementsContainer);
        emptyText = findViewById(R.id.emptyText);
        api = RetrofitClient.getApiService(this);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Görüldü olarak işaretle (rozeti gizlemek için)
        getSharedPreferences("vpn_prefs", MODE_PRIVATE)
                .edit()
                .putLong("last_seen_announcement_time", System.currentTimeMillis())
                .apply();

        loadAnnouncements();
    }

    private void loadAnnouncements() {
        String lang = Locale.getDefault().getLanguage(); // "tr", "en", vb.
        api.getAnnouncements(lang).enqueue(new Callback<ApiResponse<List<Announcement>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Announcement>>> call,
                                   Response<ApiResponse<List<Announcement>>> response) {
                container.removeAllViews();
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    List<Announcement> list = response.body().getData();
                    if (list.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                    } else {
                        for (int i = 0; i < list.size(); i++) {
                            container.addView(createCard(list.get(i), i + 1));
                        }
                    }
                } else {
                    emptyText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Announcement>>> call, Throwable t) {
                emptyText.setVisibility(View.VISIBLE);
                emptyText.setText(R.string.error_load_announcements);
            }
        });
    }

    private String resolveForLang(String lang, Announcement ann, boolean isTitle) {
        String tr = isTitle ? ann.getTitleTr()  : ann.getMessageTr();
        String en = isTitle ? ann.getTitleEn()  : ann.getMessageEn();
        String de = isTitle ? ann.getTitleDe()  : ann.getMessageDe();
        String fr = isTitle ? ann.getTitleFr()  : ann.getMessageFr();
        String ru = isTitle ? ann.getTitleRu()  : ann.getMessageRu();
        String fb = isTitle ? ann.getTitle()    : ann.getMessage();
        String v;
        switch (lang) {
            case "tr": v = tr; break;
            case "en": v = en; break;
            case "de": v = de; break;
            case "fr": v = fr; break;
            case "ru": v = ru; break;
            default:   v = null; break;
        }
        if (v != null && !v.isEmpty()) return v;
        // Fallback sırası: tr → en → fb
        if (tr != null && !tr.isEmpty()) return tr;
        if (en != null && !en.isEmpty()) return en;
        return fb != null ? fb : "";
    }

    private View createCard(Announcement ann, int number) {
        String lang = Locale.getDefault().getLanguage();
        String displayTitle   = resolveForLang(lang, ann, true);
        String displayMessage = resolveForLang(lang, ann, false);

        float dp = getResources().getDisplayMetrics().density;
        int pad = (int) (16 * dp);
        int pad8 = (int) (8 * dp);
        int pad6 = (int) (6 * dp);

        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, (int) (12 * dp));
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(Color.parseColor("#1E293B"));
        card.setRadius(16f * dp);
        card.setCardElevation(4f * dp);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(pad, pad, pad, pad);

        // Üst satır: numara + başlık + kapat butonu
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        // Numara rozeti
        TextView numBadge = new TextView(this);
        numBadge.setText(String.valueOf(number));
        numBadge.setTextColor(Color.WHITE);
        numBadge.setTextSize(11f);
        numBadge.setTypeface(null, Typeface.BOLD);
        numBadge.setBackgroundColor(Color.parseColor("#0284C7"));
        numBadge.setPadding(pad8, (int)(3*dp), pad8, (int)(3*dp));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        badgeParams.setMarginEnd(pad8);
        numBadge.setLayoutParams(badgeParams);
        numBadge.setGravity(Gravity.CENTER);

        // Başlık
        TextView title = new TextView(this);
        title.setText(displayTitle);
        title.setTextColor(Color.parseColor("#38BDF8"));
        title.setTextSize(14f);
        title.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(titleParams);

        // Kapat (X) butonu
        TextView closeBtn = new TextView(this);
        closeBtn.setText("✕");
        closeBtn.setTextColor(Color.parseColor("#64748B"));
        closeBtn.setTextSize(16f);
        closeBtn.setPadding(pad8, 0, 0, 0);
        closeBtn.setClickable(true);
        closeBtn.setFocusable(true);
        closeBtn.setOnClickListener(v -> dismissAnnouncement(ann.getId(), card));

        topRow.addView(numBadge);
        topRow.addView(title);
        topRow.addView(closeBtn);

        // Mesaj
        TextView message = new TextView(this);
        message.setText(displayMessage);
        message.setTextColor(Color.parseColor("#CBD5E1"));
        message.setTextSize(13f);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        msgParams.topMargin = pad8;
        message.setLayoutParams(msgParams);
        message.setLineSpacing(0, 1.3f);

        // Tarih
        TextView date = new TextView(this);
        if (ann.getCreatedAt() != null && ann.getCreatedAt().length() >= 10) {
            date.setText(ann.getCreatedAt().substring(0, 10));
        }
        date.setTextColor(Color.parseColor("#475569"));
        date.setTextSize(11f);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dateParams.topMargin = pad6;
        date.setLayoutParams(dateParams);

        inner.addView(topRow);
        inner.addView(message);
        inner.addView(date);

        // Hedef URL varsa link butonu ekle
        String targetUrl = ann.getTargetUrl();
        if (targetUrl != null && !targetUrl.trim().isEmpty()) {
            TextView linkBtn = new TextView(this);
            linkBtn.setText(getString(R.string.ann_read_more));
            linkBtn.setTextColor(Color.parseColor("#38BDF8"));
            linkBtn.setTextSize(12f);
            linkBtn.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams linkParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            linkParams.topMargin = pad8;
            linkBtn.setLayoutParams(linkParams);
            linkBtn.setClickable(true);
            linkBtn.setFocusable(true);
            linkBtn.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)));
                } catch (Exception ignored) {}
            });
            inner.addView(linkBtn);
        }

        card.addView(inner);
        return card;
    }

    private void dismissAnnouncement(Long id, View card) {
        // Kart anında gizlenir, sonra API çağrısı yapılır
        card.setVisibility(View.GONE);

        api.dismissAnnouncement(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                // Tüm kartlar gizlendiyse boş mesaj göster
                runOnUiThread(() -> {
                    boolean anyVisible = false;
                    for (int i = 0; i < container.getChildCount(); i++) {
                        if (container.getChildAt(i).getVisibility() == View.VISIBLE) {
                            anyVisible = true;
                            break;
                        }
                    }
                    if (!anyVisible) emptyText.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                // Hata olursa kartı tekrar göster
                runOnUiThread(() -> card.setVisibility(View.VISIBLE));
            }
        });
    }
}
