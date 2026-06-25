package com.music.payment.service;

// Outcome of confirming a VNPay return/IPN callback. Modeled as a value (not an exception)
// because both callbacks need to map it to a redirect / RspCode rather than an error response.
public record VnpConfirmResult(Outcome outcome, String txnRef) {

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
