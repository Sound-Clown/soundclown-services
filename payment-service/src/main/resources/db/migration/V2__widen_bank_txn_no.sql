-- Stripe Checkout Session / PaymentIntent ids are longer than VNPay's transaction number,
-- so widen bank_txn_no to fit them.
ALTER TABLE payments
    MODIFY COLUMN bank_txn_no VARCHAR(255) DEFAULT NULL;
