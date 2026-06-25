package com.music.payment.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckoutResponse {
    private String provider;   // VNPAY | STRIPE
    private String paymentUrl; // redirect the user here to pay
    private String txnRef;
    private long amount;
    private String currency;
}
