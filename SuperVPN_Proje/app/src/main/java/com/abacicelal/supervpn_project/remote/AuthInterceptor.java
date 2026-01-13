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
        Request.Builder requestBuilder = originalRequest.newBuilder();

        if (token != null && !originalRequest.url().encodedPath().contains("/api/v1/auth/")) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        // Add Device ID header if available
        String deviceId = com.abacicelal.supervpn_project.utils.DeviceIdManager.getDeviceId(context);
        if (deviceId != null) {
            requestBuilder.header("X-Device-ID", deviceId);
        }

        return chain.proceed(requestBuilder.build());
    }
}
