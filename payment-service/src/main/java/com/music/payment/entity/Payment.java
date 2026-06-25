package com.music.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments",
        uniqueConstraints = @UniqueConstraint(name = "uk_payments_txn_ref", columnNames = "txn_ref"),
        indexes = @Index(name = "idx_payments_user_id", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // From auth_db (JWT claim) — no FK, lives in another service's database.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    // Amount in VND, integer (no minor units in VND).
    @Column(nullable = false)
    private long amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, length = 20)
    private String provider;

    // Our merchant reference sent to VNPay as vnp_TxnRef; unique.
    @Column(name = "txn_ref", nullable = false, length = 64)
    private String txnRef;

    // VNPay's own transaction number (vnp_TransactionNo), set on success.
    @Column(name = "bank_txn_no", length = 64)
    private String bankTxnNo;

    @Column(name = "order_info", length = 255)
    private String orderInfo;

    // Days of premium this purchase grants.
    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
