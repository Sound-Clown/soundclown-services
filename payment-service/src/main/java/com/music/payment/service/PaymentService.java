package com.music.payment.service;

import com.music.payment.dto.response.CheckoutResponse;
import com.music.payment.dto.response.PaymentResponse;

import java.util.List;
import java.util.Map;

public interface PaymentService {

    // Creates a PENDING premium order for the current user and returns the VNPay redirect URL.
    CheckoutResponse checkout(String clientIp);

    // Verifies a VNPay return/IPN callback and finalizes the order (idempotent).
    VnpConfirmResult confirmVnpay(Map<String, String> params);

    // Payment history of the current user.
    List<PaymentResponse> getMyPayments();
}
