package com.abacicelal.supervpn_project.remote;

import android.content.Context;

import com.abacicelal.supervpn_project.BuildConfig;
import com.abacicelal.supervpn_project.utils.SecurePrefsManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    // API base URL — BuildConfig üzerinden parçalı tanımlandı (tersine mühendisliğe karşı)
    private static final String BASE_URL = "https://" + BuildConfig.API_HOST + "/";

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    private static Retrofit getClient(Context context) {
        if (retrofit == null) {

            // Ağ isteklerini logcat'te görmek için logging interceptor (sadece debug'da aktif)
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(com.abacicelal.supervpn_project.BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE);

            // Token eklemek için AuthInterceptor'ı oluştur
            AuthInterceptor authInterceptor = new AuthInterceptor(context);

            // *** YENİ EKLENDİ: 401 hatalarını yakalamak için TokenAuthenticator ***
            TokenAuthenticator tokenAuthenticator = new TokenAuthenticator(context);
            // *** BİTTİ ***

            // Certificate Pinning — Let's Encrypt E8 intermediate + ISRG Root X1 yedek
            // Intermediate CA pin'lendi: leaf (90 günlük) yerine intermediate tercih edildi
            CertificatePinner certificatePinner = new CertificatePinner.Builder()
                    .add(BuildConfig.API_HOST, "sha256/jw0TovYuftFQbDMYOF1ZjiNykcocAiMNGJBiOkDPrm0=")  // Let's Encrypt E8
                    .add(BuildConfig.API_HOST, "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=")  // ISRG Root X1 (yedek)
                    .build();

            // OkHttpClient'ı yapılandır
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)       // 1. Önce: Giden isteğe token ekle
                    .addInterceptor(loggingInterceptor)    // (Opsiyonel) Giden isteği logla
                    .authenticator(tokenAuthenticator)     // 2. Sonra: 401 gelirse burayı çalıştır
                    .certificatePinner(certificatePinner)  // 3. Certificate pinning (MITM koruması)
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
        SecurePrefsManager.putString(context, "AUTH_TOKEN", token);
        SecurePrefsManager.putString(context, "REFRESH_TOKEN", refreshToken);
    }

    /**
     * Kayıtlı token'ı şifreli depodan okur.
     */
    public static String getToken(Context context) {
        return SecurePrefsManager.getString(context, "AUTH_TOKEN", null);
    }

    /**
     * Kayıtlı refresh token'ı şifreli depodan okur.
     */
    public static String getRefreshToken(Context context) {
        return SecurePrefsManager.getString(context, "REFRESH_TOKEN", null);
    }
}
