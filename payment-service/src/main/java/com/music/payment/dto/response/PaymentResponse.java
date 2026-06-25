package com.music.payment.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {
    private Long id;
    private String txnRef;
    private long amount;
    private String currency;
    private String status;
    private String orderInfo;
    private int durationDays;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
