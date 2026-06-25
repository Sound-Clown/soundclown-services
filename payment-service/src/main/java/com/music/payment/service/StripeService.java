package com.music.payment.service;

import com.music.common.exception.AppException;
import com.music.common.exception.ErrorCode;
import com.music.payment.config.StripeProperties;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Stripe Checkout integration. checkout creates a hosted Checkout Session and returns its URL;
 * after payment Stripe redirects to returnUrl?session_id=..., and we confirm by retrieving the
 * session server-side (secret key) and reading its payment_status — no public webhook required.
 */
@Service
@RequiredArgsConstructor
public class StripeService {

    private final StripeProperties props;

    /** Fails with a clear error if the Stripe secret key isn't configured. */
    public void ensureConfigured() {
        if (!StringUtils.hasText(props.getSecretKey())) {
            throw new AppException(ErrorCode.PAYMENT_NOT_CONFIGURED);
        }
    }

    public long getPriceCents() {
        return props.getPriceCents();
    }

    public String getCurrency() {
        return props.getCurrency();
    }

    /** Creates a Checkout Session for an order; clientReferenceId carries our txnRef back. */
    public Session createCheckoutSession(String txnRef, String description) {
        ensureConfigured();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(props.getReturnUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(props.getReturnUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                .setClientReferenceId(txnRef)
                .putMetadata("txnRef", txnRef)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(props.getCurrency())
                                .setUnitAmount(props.getPriceCents())
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(props.getProductName())
                                        .setDescription(description)
                                        .build())
                                .build())
                        .build())
                .build();
        try {
            return Session.create(params, requestOptions());
        } catch (StripeException ex) {
            throw new IllegalStateException("Failed to create Stripe checkout session", ex);
        }
    }

    public Session retrieveSession(String sessionId) {
        ensureConfigured();
        try {
            return Session.retrieve(sessionId, requestOptions());
        } catch (StripeException ex) {
            throw new IllegalStateException("Failed to retrieve Stripe session " + sessionId, ex);
        }
    }

    private RequestOptions requestOptions() {
        return RequestOptions.builder().setApiKey(props.getSecretKey()).build();
    }
}
