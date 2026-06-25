package com.music.payment.service;

// Outcome of confirming a Stripe Checkout return. Modeled as a value (not an exception) because the
// callback maps it to a browser redirect rather than an error response.
public record PaymentConfirmResult(Outcome outcome, String txnRef) {

    public enum Outcome {
        SUCCESS,
        ALREADY_CONFIRMED,
        PAYMENT_FAILED,
        NOT_FOUND
    }

    public boolean isSuccess() {
        return outcome == Outcome.SUCCESS || outcome == Outcome.ALREADY_CONFIRMED;
    }
}
