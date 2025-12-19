package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * GET /api/v1/subscription-plans için model.
 * Backend'deki `com.celalabaci.dto.subscriptionplan.SubscriptionPlanDto` ile eşleşir.
 */
public class SubscriptionPlan {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private BigDecimal price;

    @SerializedName("durationDays")
    private Integer durationDays;

    @SerializedName("speedLimitMbps")
    private Integer speedLimitMbps;

    @SerializedName("deviceLimit")
    private Integer deviceLimit;

    @SerializedName("dataLimitGb")
    private Integer dataLimitGb;

    // Getter
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public Integer getSpeedLimitMbps() {
        return speedLimitMbps;
    }

    public Integer getDeviceLimit() {
        return deviceLimit;
    }

    public Integer getDataLimitGb() {
        return dataLimitGb;
    }
}
