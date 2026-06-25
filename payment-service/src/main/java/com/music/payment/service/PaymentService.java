package com.music.payment.service;

import com.music.payment.dto.response.CheckoutResponse;
import com.music.payment.dto.response.PaymentResponse;

import java.util.List;
import java.util.Map;

public interface PaymentService {

    // Creates a PENDING premium order for the current user via the chosen provider and returns the
    // redirect URL to its hosted payment page.
    CheckoutResponse checkout(PaymentProvider provider, String clientIp);

    // Verifies a VNPay return/IPN callback and finalizes the order (idempotent).
    PaymentConfirmResult confirmVnpay(Map<String, String> params);

    // Confirms a Stripe Checkout return by retrieving the session and finalizes the order (idempotent).
    PaymentConfirmResult confirmStripe(String sessionId);

    // Payment history of the current user.
    List<PaymentResponse> getMyPayments();
}
