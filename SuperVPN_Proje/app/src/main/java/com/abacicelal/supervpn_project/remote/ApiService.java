package com.abacicelal.supervpn_project.remote;

import com.abacicelal.supervpn_project.remote.model.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<UserDto> register(@Body RegisterRequest request);

    // NEW GUEST LOGIN
    @POST("auth/guest-login")
    Call<AuthResponse> guestLogin(@Body GuestLoginRequest request);

    @GET("device/my-devices")
    Call<ApiResponse<List<Device>>> getMyDevices();

    @POST("device/register")
    Call<ApiResponse<Device>> registerDevice(@Body DeviceRequest request);

    @GET("subscription/my-subscriptions")
    Call<ApiResponse<List<Subscription>>> getMySubscriptions();

    @POST("vpn-config/generate")
    Call<ApiResponse<VpnConfigResponse>> generateConfig(@Body ConfigGenerationRequest request);
}
