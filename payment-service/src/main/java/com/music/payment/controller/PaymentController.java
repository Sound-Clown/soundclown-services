package com.music.payment.controller;

import com.music.common.dto.ApiResponse;
import com.music.payment.dto.response.CheckoutResponse;
import com.music.payment.dto.response.PaymentResponse;
import com.music.payment.service.PaymentConfirmResult;
import com.music.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${payment.result-url}")
    private String resultUrl;

    // Authenticated: start a premium purchase, returns the Stripe Checkout URL to redirect to.
    @PostMapping("/checkout")
    public ApiResponse<CheckoutResponse> checkout() {
        return ApiResponse.<CheckoutResponse>builder()
                .result(paymentService.checkout())
                .build();
    }

    // Stripe redirects the user's browser here (success & cancel). Confirm, then 302 to the FE result page.
    @GetMapping("/stripe-return")
    public ResponseEntity<Void> stripeReturn(@RequestParam("session_id") String sessionId) {
        PaymentConfirmResult result = paymentService.confirmStripe(sessionId);
        String status = result.isSuccess() ? "success" : "failed";
        String location = resultUrl + "?status=" + status
                + "&txnRef=" + (result.txnRef() == null ? "" : result.txnRef());
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(location)).build();
    }

    // Authenticated: current user's payment history.
    @GetMapping("/me")
    public ApiResponse<List<PaymentResponse>> myPayments() {
        return ApiResponse.<List<PaymentResponse>>builder()
                .result(paymentService.getMyPayments())
                .build();
    }
}
