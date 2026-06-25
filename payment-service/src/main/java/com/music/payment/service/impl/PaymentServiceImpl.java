package com.music.payment.service.impl;

import com.music.common.security.AuthPrincipal;
import com.music.common.security.CurrentUserProvider;
import com.music.payment.dto.response.CheckoutResponse;
import com.music.payment.dto.response.PaymentResponse;
import com.music.payment.entity.Payment;
import com.music.payment.entity.PaymentStatus;
import com.music.payment.event.PremiumUpgradePublisher;
import com.music.payment.repository.PaymentRepository;
import com.music.payment.service.PaymentConfirmResult;
import com.music.payment.service.PaymentProvider;
import com.music.payment.service.PaymentService;
import com.music.payment.service.StripeService;
import com.music.payment.service.VNPayService;
import com.stripe.model.checkout.Session;
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
    private final StripeService stripeService;
    private final PremiumUpgradePublisher premiumUpgradePublisher;
    private final CurrentUserProvider currentUserProvider;

    @Value("${premium.price-vnd}")
    private long premiumPriceVnd;

    @Value("${premium.duration-days}")
    private int premiumDurationDays;

    @Override
    @Transactional
    public CheckoutResponse checkout(PaymentProvider provider, String clientIp) {
        AuthPrincipal user = currentUserProvider.getCurrentUser();
        // Unique merchant reference: timestamp + userId (digits only, within provider length limits).
        String txnRef = System.currentTimeMillis() + String.valueOf(user.userId());
        String orderInfo = "Nang cap tai khoan Premium " + premiumDurationDays + " ngay";

        return switch (provider) {
            case VNPAY -> checkoutVnpay(user, txnRef, orderInfo, clientIp);
            case STRIPE -> checkoutStripe(user, txnRef, orderInfo);
        };
    }

    private CheckoutResponse checkoutVnpay(AuthPrincipal user, String txnRef, String orderInfo, String clientIp) {
        vnPayService.ensureConfigured(); // fail fast with a clear error before creating an order
        Payment payment = newPayment(user, txnRef, orderInfo, premiumPriceVnd, "VND", PaymentProvider.VNPAY);
        paymentRepository.save(payment);

        String paymentUrl = vnPayService.buildPaymentUrl(txnRef, premiumPriceVnd, orderInfo, clientIp);
        log.info("Created VNPAY premium checkout for user {} (txnRef {}, {} VND)",
                user.userId(), txnRef, premiumPriceVnd);
        return CheckoutResponse.builder()
                .provider("VNPAY").paymentUrl(paymentUrl).txnRef(txnRef)
                .amount(premiumPriceVnd).currency("VND").build();
    }

    private CheckoutResponse checkoutStripe(AuthPrincipal user, String txnRef, String orderInfo) {
        // Create the Stripe session first (throws if misconfigured) so we never persist an orphan order.
        Session session = stripeService.createCheckoutSession(txnRef, orderInfo);
        long amount = stripeService.getPriceCents();
        String currency = stripeService.getCurrency().toUpperCase();

        Payment payment = newPayment(user, txnRef, orderInfo, amount, currency, PaymentProvider.STRIPE);
        payment.setBankTxnNo(session.getId()); // Stripe Checkout Session id, for traceability
        paymentRepository.save(payment);

        log.info("Created STRIPE premium checkout for user {} (txnRef {}, session {})",
                user.userId(), txnRef, session.getId());
        return CheckoutResponse.builder()
                .provider("STRIPE").paymentUrl(session.getUrl()).txnRef(txnRef)
                .amount(amount).currency(currency).build();
    }

    private Payment newPayment(AuthPrincipal user, String txnRef, String orderInfo,
            long amount, String currency, PaymentProvider provider) {
        return Payment.builder()
                .userId(user.userId())
                .username(user.username())
                .amount(amount)
                .currency(currency)
                .status(PaymentStatus.PENDING)
                .provider(provider.name())
                .txnRef(txnRef)
                .orderInfo(orderInfo)
                .durationDays(premiumDurationDays)
                .build();
    }

    @Override
    @Transactional
    public PaymentConfirmResult confirmVnpay(Map<String, String> params) {
        String txnRef = params.get("vnp_TxnRef");

        if (!vnPayService.isValidSignature(params)) {
            log.warn("VNPay callback with invalid signature (txnRef {})", txnRef);
            return new PaymentConfirmResult(PaymentConfirmResult.Outcome.INVALID_SIGNATURE, txnRef);
        }

        Payment payment = paymentRepository.findByTxnRef(txnRef).orElse(null);
        if (payment == null) {
            return new PaymentConfirmResult(PaymentConfirmResult.Outcome.NOT_FOUND, txnRef);
        }
        if (payment.getStatus() == PaymentStatus.PAID) {
            return new PaymentConfirmResult(PaymentConfirmResult.Outcome.ALREADY_CONFIRMED, txnRef);
        }

        boolean success = "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));
        if (!success) {
            return markFailed(payment, txnRef, "responseCode " + params.get("vnp_ResponseCode"));
        }
        return markPaid(payment, txnRef, params.get("vnp_TransactionNo"));
    }

    @Override
    @Transactional
    public PaymentConfirmResult confirmStripe(String sessionId) {
        Session session;
        try {
            session = stripeService.retrieveSession(sessionId);
        } catch (Exception ex) {
            log.warn("Could not retrieve Stripe session {}", sessionId, ex);
            return new PaymentConfirmResult(PaymentConfirmResult.Outcome.PAYMENT_FAILED, null);
        }

        String txnRef = session.getClientReferenceId();
        Payment payment = txnRef == null ? null : paymentRepository.findByTxnRef(txnRef).orElse(null);
        if (payment == null) {
            return new PaymentConfirmResult(PaymentConfirmResult.Outcome.NOT_FOUND, txnRef);
        }
        if (payment.getStatus() == PaymentStatus.PAID) {
            return new PaymentConfirmResult(PaymentConfirmResult.Outcome.ALREADY_CONFIRMED, txnRef);
        }

        if (!"paid".equals(session.getPaymentStatus())) {
            return markFailed(payment, txnRef, "stripe payment_status " + session.getPaymentStatus());
        }
        return markPaid(payment, txnRef, session.getPaymentIntent());
    }

    private PaymentConfirmResult markPaid(Payment payment, String txnRef, String providerTxnNo) {
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setBankTxnNo(providerTxnNo);
        paymentRepository.save(payment);
        log.info("Payment {} PAID — granting premium to user {}", txnRef, payment.getUserId());

        premiumUpgradePublisher.publish(payment.getUserId(), payment.getDurationDays(), txnRef);
        return new PaymentConfirmResult(PaymentConfirmResult.Outcome.SUCCESS, txnRef);
    }

    private PaymentConfirmResult markFailed(Payment payment, String txnRef, String reason) {
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        log.info("Payment {} FAILED ({})", txnRef, reason);
        return new PaymentConfirmResult(PaymentConfirmResult.Outcome.PAYMENT_FAILED, txnRef);
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
