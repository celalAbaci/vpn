package com.abacicelal.supervpn_project.remote;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abacicelal.supervpn_project.AccountActivity;
import com.abacicelal.supervpn_project.remote.model.AuthResponse;
import com.abacicelal.supervpn_project.remote.model.RefreshTokenRequest;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

/**
 * Bu sınıf, OkHttp istemcisinden 401 (Yetkisiz) yanıtı alındığında otomatik olarak tetiklenir.
 * Refresh Token kullanarak yeni bir Access Token almaya çalışır.
 *
 * YENİ DOSYA
 */
public class TokenAuthenticator implements Authenticator {
    private final Context context;

    public TokenAuthenticator(Context context) {
        this.context = context.getApplicationContext();
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        // 1. Mevcut (eski) refresh token'ı al
        String refreshToken = RetrofitClient.getRefreshToken(context);

        if (refreshToken == null) {
            // Refresh token yoksa, kullanıcıyı giriş ekranına yönlendirip işlemi sonlandır.
            redirectToLogin();
            return null; // Yenileme başarısız
        }

        // 2. Token yenileme isteğini *senkron* (synchronous) olarak yap
        // Not: Bu işlem Authenticator'ın kendi çalışma thread'inde (arka plan) çalışır, UI'ı kilitlemez.
        ApiService apiService = RetrofitClient.getApiService(context);
        Call<AuthResponse> refreshCall = apiService.refreshToken(new RefreshTokenRequest(refreshToken));

        try {
            // .execute() senkron çağrıdır
            retrofit2.Response<AuthResponse> refreshResponse = refreshCall.execute();

            if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                // 3. Başarılı! Yeni token'ları al
                AuthResponse newTokens = refreshResponse.body();

                // 4. Yeni token'ları kaydet
                RetrofitClient.saveToken(
                        context,
                        newTokens.getAccessToken(),
                        newTokens.getRefreshToken()
                );

                // 5. Başarısız olan orijinal isteği *yeni access token* ile tekrar oluştur
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + newTokens.getAccessToken())
                        .build();
            } else {
                // 6. Refresh token da geçersiz (süresi dolmuş veya hatalı)
                redirectToLogin();
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Token yenileme işlemi başarısız olduğunda (kullanıcının refresh token'ı da geçersizse),
     * kullanıcıyı uygulamadan atıp tekrar giriş yapması için giriş ekranına yönlendirir.
     */
    private void redirectToLogin() {
        // Tüm token'ları temizle
        RetrofitClient.saveToken(context, null, null);

        // Giriş ekranını aç
        Intent intent = new Intent(context, AccountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
