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
import com.music.payment.service.PaymentService;
import com.music.payment.service.StripeService;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String PROVIDER = "STRIPE";

    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;
    private final PremiumUpgradePublisher premiumUpgradePublisher;
    private final CurrentUserProvider currentUserProvider;

    @Value("${premium.duration-days}")
    private int premiumDurationDays;

    @Override
    @Transactional
    public CheckoutResponse checkout() {
        AuthPrincipal user = currentUserProvider.getCurrentUser();
        // Unique merchant reference: timestamp + userId.
        String txnRef = System.currentTimeMillis() + String.valueOf(user.userId());
        String orderInfo = "Nang cap tai khoan Premium " + premiumDurationDays + " ngay";

        // Create the Stripe session first (throws if misconfigured) so we never persist an orphan order.
        Session session = stripeService.createCheckoutSession(txnRef, orderInfo);
        long amount = stripeService.getPriceCents();
        String currency = stripeService.getCurrency().toUpperCase();

        Payment payment = Payment.builder()
                .userId(user.userId())
                .username(user.username())
                .amount(amount)
                .currency(currency)
                .status(PaymentStatus.PENDING)
                .provider(PROVIDER)
                .txnRef(txnRef)
                .bankTxnNo(session.getId()) // Stripe Checkout Session id, for traceability
                .orderInfo(orderInfo)
                .durationDays(premiumDurationDays)
                .build();
        paymentRepository.save(payment);

        log.info("Created premium checkout for user {} (txnRef {}, session {})",
                user.userId(), txnRef, session.getId());
        return CheckoutResponse.builder()
                .provider(PROVIDER)
                .paymentUrl(session.getUrl())
                .txnRef(txnRef)
                .amount(amount)
                .currency(currency)
                .build();
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
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.info("Payment {} FAILED (stripe payment_status {})", txnRef, session.getPaymentStatus());
            return new PaymentConfirmResult(PaymentConfirmResult.Outcome.PAYMENT_FAILED, txnRef);
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setBankTxnNo(session.getPaymentIntent());
        paymentRepository.save(payment);
        log.info("Payment {} PAID — granting premium to user {}", txnRef, payment.getUserId());

        premiumUpgradePublisher.publish(payment.getUserId(), payment.getDurationDays(), txnRef);
        return new PaymentConfirmResult(PaymentConfirmResult.Outcome.SUCCESS, txnRef);
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
