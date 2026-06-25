-- Stripe Checkout Session / PaymentIntent ids are long, so widen bank_txn_no to fit them.
ALTER TABLE payments
    MODIFY COLUMN bank_txn_no VARCHAR(255) DEFAULT NULL;
