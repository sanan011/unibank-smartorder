CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_payments_order_id UNIQUE (order_id)
);

CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL,
    attempt_number INT NOT NULL,
    gateway_reference VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    executed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_transactions_payment_id FOREIGN KEY (payment_id) REFERENCES payments(id)
);

CREATE INDEX idx_payments_customer_id ON payments(customer_id);
CREATE INDEX idx_payment_transactions_payment_id ON payment_transactions(payment_id);
