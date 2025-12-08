package com.abacicelal.supervpn_project.remote;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    // Kendi domain adresiniz (Doğru ayarlanmış)

//    https://www.dataguardvpn.com/
// Domain adresini buraya yazıyoruz
private static final String BASE_URL = "http://api.dataguardvpn.com:8080/";

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    private static Retrofit getClient(Context context) {
        if (retrofit == null) {

            // Ağ isteklerini logcat'te görmek için logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            // TODO: Uygulamayı yayınlarken bunu BODY yerine NONE yapın
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Token eklemek için AuthInterceptor'ı oluştur
            AuthInterceptor authInterceptor = new AuthInterceptor(context);

            // *** YENİ EKLENDİ: 401 hatalarını yakalamak için TokenAuthenticator ***
            TokenAuthenticator tokenAuthenticator = new TokenAuthenticator(context);
            // *** BİTTİ ***

            // OkHttpClient'ı yapılandır
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)       // 1. Önce: Giden isteğe token ekle
                    .addInterceptor(loggingInterceptor)    // (Opsiyonel) Giden isteği logla
                    .authenticator(tokenAuthenticator)   // 2. Sonra: 401 gelirse burayı çalıştır
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Gson ayarları (opsiyonel, tarih formatları vb. için)
            // Backend 'OffsetDateTime' kullandığı için tarih formatı gerekebilir,
            // Şimdilik Gson'un varsayılanını kullanıyoruz.
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            // Retrofit'i oluştur
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    /**
     * ApiService'in bir singleton örneğini döner.
     * @param context Application context
     * @return ApiService
     */
    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            apiService = getClient(context.getApplicationContext()).create(ApiService.class);
        }
        return apiService;
    }

    /**
     * Token'ı SharedPreferences'a kaydeder.
     * @param context Context
     * @param token Kaydedilecek JWT token
     * @param refreshToken Kaydedilecek Refresh token
     */
    public static void saveToken(Context context, String token, String refreshToken) {
        SharedPreferences prefs = context.getSharedPreferences("VPN_PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("AUTH_TOKEN", token);
        editor.putString("REFRESH_TOKEN", refreshToken);
        editor.apply();
    }

    /**
     * Kayıtlı token'ı SharedPreferences'dan okur.
     * @param context Context
     * @return Kayıtlı token veya token yoksa null
     */
    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("VPN_PREFS", Context.MODE_PRIVATE);
        return prefs.getString("AUTH_TOKEN", null);
    }

    /**
     * Kayıtlı refresh token'ı SharedPreferences'dan okur.
     * @param context Context
     * @return Kayıtlı refresh token veya token yoksa null
     */
    public static String getRefreshToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("VPN_PREFS", Context.MODE_PRIVATE);
        return prefs.getString("REFRESH_TOKEN", null);
    }
}
