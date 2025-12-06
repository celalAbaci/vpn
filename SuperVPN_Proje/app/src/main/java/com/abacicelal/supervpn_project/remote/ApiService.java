package com.abacicelal.supervpn_project.remote;

// Model sınıflarınızın tümünü import ediyoruz
import com.abacicelal.supervpn_project.remote.model.*;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * PDF dökümanındaki tüm API endpoint'lerinin tanımlandığı arayüz.
 * Tüm model sınıfları (POJO) backend ile eşleşecek şekilde oluşturuldu.
 */
public interface ApiService {

    // --- 1. Kimlik Doğrulaması (Token GEREKMEZ) ---

    @POST("api/v1/auth/register")
    Call<AuthResponse> registerUser(@Body RegisterRequest registerRequest);

    @POST("api/v1/auth/authenticate")
    Call<AuthResponse> loginUser(@Body AuthRequest authRequest);

    @POST("api/v1/auth/refresh-token")
    Call<AuthResponse> refreshToken(@Body RefreshTokenRequest refreshTokenRequest);

    // --- 2. Kimlik Doğrulaması GEREKEN İstekler (USER Rolü) ---

    // Kullanıcı, Cihaz ve Durum İşlemleri
    @GET("api/v1/user/heartbeat")
    Call<HeartbeatResponse> checkHeartbeat();

    @GET("api/v1/devices/my")
    Call<List<Device>> getMyDevices();

    @POST("api/v1/devices/my")
    Call<Device> registerDevice(@Body DeviceRequest deviceRequest);

    @PUT("api/v1/devices/my/{id}")
    Call<Device> updateDevice(@Path("id") Long deviceId, @Body DeviceUpdateRequest updateRequest);

    @DELETE("api/v1/devices/my/{id}")
    Call<Void> deleteDevice(@Path("id") Long deviceId);

    // Sunucu (Server) ve Konfigürasyon İşlemleri
    @GET("api/v1/servers/active")
    Call<List<Server>> getActiveServers();

    @GET("api/v1/servers/{id}")
    Call<Server> getServerDetails(@Path("id") Long serverId);

    @GET("api/v1/countries")
    Call<List<Country>> getCountries();

    @GET("api/v1/countries/{id}")
    Call<Country> getCountryDetails(@Path("id") Long countryId);

    @POST("api/v1/config/generate")
    Call<VpnConfigResponse> generateConfig(@Body ConfigGenerationRequest configRequest);

    // Abonelik (Subscription) ve Ödeme İşlemleri
    @GET("api/v1/subscription-plans")
    Call<List<SubscriptionPlan>> getSubscriptionPlans();

    @GET("api/v1/subscription-plans/{id}")
    Call<SubscriptionPlan> getSubscriptionPlanDetails(@Path("id") Long planId);

    @GET("api/v1/subscriptions/my")
    Call<List<Subscription>> getMySubscriptions();

    @POST("api/v1/subscriptions")
    Call<Subscription> startManualSubscription(@Body ManualSubscriptionRequest manualRequest);

    @PUT("api/v1/subscriptions/{id}/cancel")
    Call<Subscription> cancelSubscription(@Path("id") Long subscriptionId);

    @POST("api/v1/payment/google/verify-subscription")
    Call<Subscription> verifyGooglePurchase(@Body GooglePurchaseRequest purchaseRequest);

    @GET("api/v1/payments/my")
    Call<List<Payment>> getMyPayments();

    // Diğer İşlemler (Loglar ve Duyurular)
    @GET("api/v1/announcements")
    Call<List<Announcement>> getAnnouncements();

    @GET("api/v1/announcements/{id}")
    Call<Announcement> getAnnouncementDetails(@Path("id") Long announcementId);

    @GET("api/v1/logs/my")
    Call<List<ConnectionLog>> getMyLogs();

    @POST("api/v1/logs/my")
    Call<ConnectionLog> createLog(@Body CreateLogRequest logRequest);
}
