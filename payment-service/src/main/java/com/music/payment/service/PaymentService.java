package com.music.payment.service;

import com.music.payment.dto.response.CheckoutResponse;
import com.music.payment.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    // Creates a PENDING premium order for the current user and returns the Stripe Checkout URL.
    CheckoutResponse checkout();

    // Confirms a Stripe Checkout return by retrieving the session and finalizes the order (idempotent).
    PaymentConfirmResult confirmStripe(String sessionId);

    // Payment history of the current user.
    List<PaymentResponse> getMyPayments();
}
