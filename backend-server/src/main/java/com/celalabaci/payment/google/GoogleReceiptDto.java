package com.celalabaci.payment.google;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Android istemcisinden Google Play satın alma bilgilerini
 * (token ve ürün kimliği) almak için kullanılacak DTO.
 */
@Data
@NoArgsConstructor
public class GoogleReceiptDto {

    /**
     * Google Play tarafından sağlanan, satın almaya özel token.
     */
    @NotEmpty(message = "Purchase token cannot be empty.")
    private String purchaseToken;

    /**
     * Google Play Console'da tanımladığınız ürün (abonelik) kimliği.
     * Örn: "vpn.monthly.plan", "premium_subscription_1"
     */
    @NotEmpty(message = "Product ID (Subscription ID) cannot be empty.")
    private String productId;
}
