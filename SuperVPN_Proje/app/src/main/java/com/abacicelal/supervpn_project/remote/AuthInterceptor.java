package com.abacicelal.supervpn_project.remote;

import android.content.Context;
import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Bu interceptor, SharedPreferences'dan token'ı okur ve
 * "Authorization" başlığını gerektiren isteklere ekler.
 */
public class AuthInterceptor implements Interceptor {

    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Token'ı SharedPreferences'dan al
        String token = RetrofitClient.getToken(context);

        // Eğer token varsa ve istek "auth" endpoint'i DEĞİLSE, başlığı ekle
        if (token != null && !originalRequest.url().encodedPath().contains("/api/v1/auth/")) {
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(newRequest);
        }

        // Token yoksa veya auth isteğiyse, orijinal isteği devam ettir
        return chain.proceed(originalRequest);
    }
}
