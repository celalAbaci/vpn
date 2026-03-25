package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * POST /api/v1/payment/google/verify-subscription için istek modeli.
 * Backend'deki `com.celalabaci.payment.google.GoogleReceiptDto` ile eşleşir.
 */
public class GooglePurchaseRequest {

    @SerializedName("purchaseToken")
    private String purchaseToken;

    @SerializedName("productId")
    private String productId;

    public GooglePurchaseRequest(String purchaseToken, String productId) {
        this.purchaseToken = purchaseToken;
        this.productId = productId;
    }

    // Getter
    public String getPurchaseToken() {
        return purchaseToken;
    }

    public String getProductId() {
        return productId;
    }
}
