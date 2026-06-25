package com.music.payment.service;

// Outcome of confirming a payment callback (VNPay return/IPN or Stripe return). Modeled as a value
// (not an exception) because callbacks map it to a redirect / RspCode rather than an error response.
public record PaymentConfirmResult(Outcome outcome, String txnRef) {

    public enum Outcome {
        SUCCESS,
        ALREADY_CONFIRMED,
        PAYMENT_FAILED,
        INVALID_SIGNATURE,
        NOT_FOUND
    }

    public boolean isSuccess() {
        return outcome == Outcome.SUCCESS || outcome == Outcome.ALREADY_CONFIRMED;
    }
}
