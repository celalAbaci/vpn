package com.abacicelal.supervpn_project.remote;

// Model sınıflarınızın tümünü import ediyoruz
import com.abacicelal.supervpn_project.remote.model.*;

import java.util.List;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.GET;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * PDF dökümanındaki tüm API endpoint'lerinin tanımlandığı arayüz.
 * Tüm model sınıfları (POJO) backend ile eşleşecek şekilde oluşturuldu.
 */
public interface ApiService {

    // --- 1. Kimlik Doğrulaması (Token GEREKMEZ) ---
    // NOT: AuthenticationController direkt nesne döndürüyor, ApiResponse KULLANMIYOR.

    @POST("api/v1/auth/register")
    Call<AuthResponse> registerUser(@Body RegisterRequest registerRequest);

    @POST("api/v1/auth/authenticate")
    Call<AuthResponse> loginUser(@Body AuthRequest authRequest);

    @POST("api/v1/auth/authenticate-guest")
    Call<AuthResponse> loginGuest(@Body GuestAuthRequest guestAuthRequest);

    @POST("api/v1/auth/refresh-token")
    Call<AuthResponse> refreshToken(@Body RefreshTokenRequest refreshTokenRequest);

    // --- 2. Kimlik Doğrulaması GEREKEN İstekler (USER Rolü) ---
    // NOT: Diğer Controller'lar ApiResponse wrapper kullanıyor.

    // Kullanıcı, Cihaz ve Durum İşlemleri
    @GET("api/v1/user/heartbeat")
    Call<ApiResponse<HeartbeatResponse>> checkHeartbeat();

    @GET("api/v1/devices/my")
    Call<ApiResponse<List<Device>>> getMyDevices();

    @POST("api/v1/devices/my")
    Call<ApiResponse<Device>> registerDevice(@Body DeviceRequest deviceRequest);

    @PUT("api/v1/devices/my/{id}")
    Call<ApiResponse<Device>> updateDevice(@Path("id") Long deviceId, @Body DeviceUpdateRequest updateRequest);

    @DELETE("api/v1/devices/my/{id}")
    Call<ApiResponse<Void>> deleteDevice(@Path("id") Long deviceId);

    // Sunucu (Server) ve Konfigürasyon İşlemleri
    @GET("api/v1/servers/active")
    Call<ApiResponse<List<Server>>> getActiveServers();

    @GET("api/v1/servers/{id}")
    Call<ApiResponse<Server>> getServerDetails(@Path("id") Long serverId);

    @GET("api/v1/countries")
    Call<ApiResponse<List<Country>>> getCountries();

    @GET("api/v1/countries/{id}")
    Call<ApiResponse<Country>> getCountryDetails(@Path("id") Long countryId);

    @POST("api/v1/config/generate")
    Call<ApiResponse<VpnConfigResponse>> generateConfig(@Body ConfigGenerationRequest configRequest);

    // Abonelik (Subscription) ve Ödeme İşlemleri
    @GET("api/v1/subscription-plans")
    Call<ApiResponse<List<SubscriptionPlan>>> getSubscriptionPlans();

    @GET("api/v1/subscription-plans/{id}")
    Call<ApiResponse<SubscriptionPlan>> getSubscriptionPlanDetails(@Path("id") Long planId);

    @GET("api/v1/subscriptions/my")
    Call<ApiResponse<List<Subscription>>> getMySubscriptions();

    @POST("api/v1/subscriptions")
    Call<ApiResponse<Subscription>> startManualSubscription(@Body ManualSubscriptionRequest manualRequest);

    @PUT("api/v1/subscriptions/{id}/cancel")
    Call<ApiResponse<Subscription>> cancelSubscription(@Path("id") Long subscriptionId);

    @POST("api/v1/payment/google/verify-subscription")
    Call<ApiResponse<Subscription>> verifyGooglePurchase(@Body GooglePurchaseRequest purchaseRequest);

    @GET("api/v1/payments/my")
    Call<ApiResponse<List<Payment>>> getMyPayments();

    // Diğer İşlemler (Loglar ve Duyurular)
    @GET("api/v1/announcements")
    Call<ApiResponse<List<Announcement>>> getAnnouncements(@retrofit2.http.Header("Accept-Language") String lang);

    @GET("api/v1/announcements/{id}")
    Call<ApiResponse<Announcement>> getAnnouncementDetails(@Path("id") Long announcementId);

    @POST("api/v1/announcements/{id}/dismiss")
    Call<ApiResponse<Void>> dismissAnnouncement(@Path("id") Long announcementId);

    @GET("api/v1/logs/my")
    Call<ApiResponse<List<ConnectionLog>>> getMyLogs();

    @POST("api/v1/logs/my")
    Call<ApiResponse<ConnectionLog>> createLog(@Body CreateLogRequest logRequest);

    @PATCH("api/v1/logs/my/{id}/disconnect")
    Call<ApiResponse<ConnectionLog>> updateLogDisconnect(@Path("id") Long logId, @Body java.util.Map<String, Object> body);

    @PATCH("api/v1/logs/my/active/disconnect")
    Call<ApiResponse<ConnectionLog>> updateActiveLogDisconnect(@Body java.util.Map<String, Object> body);
}
