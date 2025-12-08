package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * GET /api/v1/payments/my için model.
 * Backend'deki `com.celalabaci.dto.payment.PaymentDto` ile eşleşir.
 */
public class Payment {

    @SerializedName("id")
    private Long id;

    // 'user' alanı backend'de var
    // private UserDto user;

    @SerializedName("subscription")
    private Subscription subscription;

    @SerializedName("amount")
    private BigDecimal amount;

    @SerializedName("paymentDate")
    private String paymentDate; // Backend 'OffsetDateTime'

    @SerializedName("transaction")
    private String transaction;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt; // Backend 'OffsetDateTime'

    // Getter
    public Long getId() {
        return id;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public String getTransaction() {
        return transaction;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
