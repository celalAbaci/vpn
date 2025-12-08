package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * GET /api/v1/subscriptions/my için model.
 * Backend'deki `com.celalabaci.dto.subscription.SubscriptionDto` ile eşleşir.
 */
public class Subscription {

    @SerializedName("id")
    private Long id;

    // 'user' alanı backend'de var, ancak genellikle gerekmez
    // private UserDto user;

    @SerializedName("plan")
    private SubscriptionPlan plan;

    @SerializedName("startDate")
    private String startDate; // Backend 'LocalDate' gönderir

    @SerializedName("endDate")
    private String endDate; // Backend 'LocalDate' gönderir

    @SerializedName("speedLimitMbps")
    private Integer speedLimitMbps;

    @SerializedName("active")
    private boolean isActive;

    // Getter
    public Long getId() {
        return id;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public Integer getSpeedLimitMbps() {
        return speedLimitMbps;
    }

    public boolean isActive() {
        return isActive;
    }
}
