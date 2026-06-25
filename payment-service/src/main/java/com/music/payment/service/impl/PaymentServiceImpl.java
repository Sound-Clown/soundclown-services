package com.music.payment.service.impl;

import com.music.common.security.AuthPrincipal;
import com.music.common.security.CurrentUserProvider;
import com.music.payment.dto.response.CheckoutResponse;
import com.music.payment.dto.response.PaymentResponse;
import com.music.payment.entity.Payment;
import com.music.payment.entity.PaymentStatus;
import com.music.payment.event.PremiumUpgradePublisher;
import com.music.payment.repository.PaymentRepository;
import com.music.payment.service.PaymentService;
import com.music.payment.service.VNPayService;
import com.music.payment.service.VnpConfirmResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final VNPayService vnPayService;
    private final PremiumUpgradePublisher premiumUpgradePublisher;
    private final CurrentUserProvider currentUserProvider;

    @Value("${premium.price-vnd}")
    private long premiumPriceVnd;

    @Value("${premium.duration-days}")
    private int premiumDurationDays;

    @Override
    @Transactional
    public CheckoutResponse checkout(String clientIp) {
        AuthPrincipal user = currentUserProvider.getCurrentUser();
        // Unique merchant reference: timestamp + userId (digits only, within VNPay's length limit).
        String txnRef = System.currentTimeMillis() + String.valueOf(user.userId());
        String orderInfo = "Nang cap tai khoan Premium " + premiumDurationDays + " ngay";

        Payment payment = Payment.builder()
                .userId(user.userId())
                .username(user.username())
                .amount(premiumPriceVnd)
                .currency("VND")
                .status(PaymentStatus.PENDING)
                .provider("VNPAY")
                .txnRef(txnRef)
                .orderInfo(orderInfo)
                .durationDays(premiumDurationDays)
                .build();
        paymentRepository.save(payment);

        String paymentUrl = vnPayService.buildPaymentUrl(txnRef, premiumPriceVnd, orderInfo, clientIp);
        log.info("Created premium checkout for user {} (txnRef {}, {} VND)",
                user.userId(), txnRef, premiumPriceVnd);

        return CheckoutResponse.builder()
                .paymentUrl(paymentUrl)
                .txnRef(txnRef)
                .amount(premiumPriceVnd)
                .currency("VND")
                .build();
    }

    @Override
    @Transactional
    public VnpConfirmResult confirmVnpay(Map<String, String> params) {
        String txnRef = params.get("vnp_TxnRef");

        if (!vnPayService.isValidSignature(params)) {
            log.warn("VNPay callback with invalid signature (txnRef {})", txnRef);
            return new VnpConfirmResult(VnpConfirmResult.Outcome.INVALID_SIGNATURE, txnRef);
        }

        Payment payment = paymentRepository.findByTxnRef(txnRef).orElse(null);
        if (payment == null) {
            return new VnpConfirmResult(VnpConfirmResult.Outcome.NOT_FOUND, txnRef);
        }
        // Return URL and IPN can both fire — finalize only once.
        if (payment.getStatus() == PaymentStatus.PAID) {
            return new VnpConfirmResult(VnpConfirmResult.Outcome.ALREADY_CONFIRMED, txnRef);
        }

        boolean success = "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));
        if (!success) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.info("Payment {} FAILED (responseCode {})", txnRef, params.get("vnp_ResponseCode"));
            return new VnpConfirmResult(VnpConfirmResult.Outcome.PAYMENT_FAILED, txnRef);
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setBankTxnNo(params.get("vnp_TransactionNo"));
        paymentRepository.save(payment);
        log.info("Payment {} PAID — granting premium to user {}", txnRef, payment.getUserId());

        premiumUpgradePublisher.publish(payment.getUserId(), payment.getDurationDays(), txnRef);
        return new VnpConfirmResult(VnpConfirmResult.Outcome.SUCCESS, txnRef);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments() {
        Long userId = currentUserProvider.getCurrentUserId();
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .txnRef(p.getTxnRef())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus().name())
                .orderInfo(p.getOrderInfo())
                .durationDays(p.getDurationDays())
                .createdAt(p.getCreatedAt())
                .paidAt(p.getPaidAt())
                .build();
    }
}
