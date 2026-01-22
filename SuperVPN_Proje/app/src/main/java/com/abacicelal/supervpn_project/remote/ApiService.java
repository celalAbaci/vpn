package com.abacicelal.supervpn_project.remote;

import com.abacicelal.supervpn_project.remote.model.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // --- AUTH ---
    @POST("api/v1/auth/register")
    Call<AuthResponse> registerUser(@Body RegisterRequest request);

    @POST("api/v1/auth/authenticate")
    Call<AuthResponse> loginUser(@Body AuthRequest request);

    @POST("api/v1/auth/guest-login")
    Call<AuthResponse> guestLogin(@Body GuestLoginRequest request);

    @POST("api/v1/auth/refresh-token")
    Call<AuthResponse> refreshToken(@Body RefreshTokenRequest request);

    // --- USER STATUS ---
    @GET("api/v1/user/heartbeat")
    Call<ApiResponse<HeartbeatResponse>> checkHeartbeat();

    // --- SERVERS ---
    // Note: getActiveServers is used by ServerSelectionActivity.
    // Ideally we should use standard REST naming (getServers), but preserving existing calls.
    @GET("api/v1/servers/active")
    Call<ApiResponse<List<Server>>> getActiveServers();

    // Alias for getActiveServers if standardized
    @GET("api/v1/vpn-servers")
    Call<ApiResponse<List<VpnServer>>> getServers();

    @GET("api/v1/servers/{id}")
    Call<ApiResponse<Server>> getServerDetails(@Path("id") Long serverId);

    // --- COUNTRIES ---
    @GET("api/v1/countries")
    Call<ApiResponse<List<Country>>> getCountries();

    @GET("api/v1/countries/{id}")
    Call<ApiResponse<Country>> getCountryDetails(@Path("id") Long countryId);

    // --- CONFIG ---
    @POST("api/v1/config/generate")
    Call<ApiResponse<VpnConfigResponse>> generateConfig(@Body ConfigGenerationRequest request);

    // --- DEVICES ---
    @GET("api/v1/devices/my") // Old path
    Call<ApiResponse<List<Device>>> getMyDevices();

    // Alternative path if standardized
    @GET("api/v1/devices/my-devices")
    Call<ApiResponse<List<Device>>> getMyDevicesStandard();

    @POST("api/v1/devices/my") // Old path
    Call<ApiResponse<Device>> registerDevice(@Body DeviceRequest request);

    @PUT("api/v1/devices/my/{id}")
    Call<ApiResponse<Device>> updateDevice(@Path("id") Long deviceId, @Body DeviceUpdateRequest request);

    @DELETE("api/v1/devices/my/{id}")
    Call<ApiResponse<Void>> deleteDevice(@Path("id") Long deviceId);

    // --- SUBSCRIPTIONS ---
    @GET("api/v1/subscription-plans") // Used by PremiumActivity
    Call<ApiResponse<List<SubscriptionPlan>>> getSubscriptionPlans();

    @GET("api/v1/subscription-plans/{id}")
    Call<ApiResponse<SubscriptionPlan>> getSubscriptionPlanDetails(@Path("id") Long planId);

    @GET("api/v1/subscriptions/my") // Used by PremiumActivity/MainActivity
    Call<ApiResponse<List<Subscription>>> getMySubscriptions();

    @GET("api/v1/subscriptions/my-subscriptions") // Alternative
    Call<ApiResponse<List<Subscription>>> getMySubscriptionsStandard();

    @POST("api/v1/subscriptions")
    Call<ApiResponse<Subscription>> startManualSubscription(@Body ManualSubscriptionRequest request);

    @PUT("api/v1/subscriptions/{id}/cancel")
    Call<ApiResponse<Subscription>> cancelSubscription(@Path("id") Long subscriptionId);

    @POST("api/v1/payment/google/verify-subscription")
    Call<ApiResponse<Subscription>> verifyGooglePurchase(@Body GooglePurchaseRequest request);

    @GET("api/v1/payments/my")
    Call<ApiResponse<List<Payment>>> getMyPayments();

    @POST("api/v1/payment/purchase")
    Call<ApiResponse<Payment>> purchaseSubscription(@Body PurchaseRequest request);

    // --- LOGS & ANNOUNCEMENTS ---
    @GET("api/v1/announcements")
    Call<ApiResponse<List<Announcement>>> getAnnouncements();

    @GET("api/v1/announcements/{id}")
    Call<ApiResponse<Announcement>> getAnnouncementDetails(@Path("id") Long announcementId);

    @GET("api/v1/logs/my")
    Call<ApiResponse<List<ConnectionLog>>> getMyLogs();

    @POST("api/v1/logs/my")
    Call<ApiResponse<ConnectionLog>> createLog(@Body CreateLogRequest request);
}
