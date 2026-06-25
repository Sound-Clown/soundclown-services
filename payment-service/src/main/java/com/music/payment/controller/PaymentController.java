package com.music.payment.controller;

import com.music.common.dto.ApiResponse;
import com.music.common.exception.AppException;
import com.music.common.exception.ErrorCode;
import com.music.payment.dto.response.CheckoutResponse;
import com.music.payment.dto.response.PaymentResponse;
import com.music.payment.service.PaymentConfirmResult;
import com.music.payment.service.PaymentProvider;
import com.music.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${payment.result-url}")
    private String resultUrl;

    // Authenticated: start a premium purchase via ?provider=vnpay|stripe (default vnpay).
    @PostMapping("/checkout")
    public ApiResponse<CheckoutResponse> checkout(
            @RequestParam(defaultValue = "vnpay") String provider,
            HttpServletRequest request) {
        return ApiResponse.<CheckoutResponse>builder()
                .result(paymentService.checkout(parseProvider(provider), clientIp(request)))
                .build();
    }

    // VNPay redirects the user's browser here. Verify, finalize, then 302 to the FE result page.
    @GetMapping("/vnpay-return")
    public ResponseEntity<Void> vnpayReturn(@RequestParam Map<String, String> params) {
        return redirectToResult(paymentService.confirmVnpay(params));
    }

    // VNPay server-to-server IPN. Returns VNPay's expected {RspCode, Message} acknowledgement.
    @GetMapping("/vnpay-ipn")
    public Map<String, String> vnpayIpn(@RequestParam Map<String, String> params) {
        PaymentConfirmResult result = paymentService.confirmVnpay(params);
        return switch (result.outcome()) {
            case SUCCESS, PAYMENT_FAILED -> Map.of("RspCode", "00", "Message", "Confirm Success");
            case ALREADY_CONFIRMED -> Map.of("RspCode", "02", "Message", "Order already confirmed");
            case INVALID_SIGNATURE -> Map.of("RspCode", "97", "Message", "Invalid signature");
            case NOT_FOUND -> Map.of("RspCode", "01", "Message", "Order not found");
        };
    }

    // Stripe redirects the user's browser here (success & cancel). Confirm, then 302 to the FE result page.
    @GetMapping("/stripe-return")
    public ResponseEntity<Void> stripeReturn(@RequestParam("session_id") String sessionId) {
        return redirectToResult(paymentService.confirmStripe(sessionId));
    }

    // Authenticated: current user's payment history.
    @GetMapping("/me")
    public ApiResponse<List<PaymentResponse>> myPayments() {
        return ApiResponse.<List<PaymentResponse>>builder()
                .result(paymentService.getMyPayments())
                .build();
    }

    private ResponseEntity<Void> redirectToResult(PaymentConfirmResult result) {
        String status = result.isSuccess() ? "success" : "failed";
        String location = resultUrl + "?status=" + status
                + "&txnRef=" + (result.txnRef() == null ? "" : result.txnRef());
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(location)).build();
    }

    private static PaymentProvider parseProvider(String provider) {
        try {
            return PaymentProvider.valueOf(provider.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
