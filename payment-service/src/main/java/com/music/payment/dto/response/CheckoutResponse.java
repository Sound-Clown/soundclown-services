package com.music.payment.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckoutResponse {
    private String provider;   // STRIPE
    private String paymentUrl; // redirect the user here to pay (Stripe Checkout)
    private String txnRef;
    private long amount;
    private String currency;
}
