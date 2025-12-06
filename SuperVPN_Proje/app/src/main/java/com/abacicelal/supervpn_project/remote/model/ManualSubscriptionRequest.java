package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * POST /api/v1/subscriptions için istek modeli.
 * Backend'deki `com.celalabaci.dto.subscription.SubscriptionCreateDto` ile eşleşir.
 */
public class ManualSubscriptionRequest {

    @SerializedName("planId")
    private Long planId;

    public ManualSubscriptionRequest(Long planId) {
        this.planId = planId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }
}
