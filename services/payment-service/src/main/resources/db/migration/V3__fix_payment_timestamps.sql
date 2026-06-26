ALTER TABLE payments ALTER COLUMN created_at TYPE TIMESTAMPTZ;
ALTER TABLE payments ALTER COLUMN updated_at TYPE TIMESTAMPTZ;

CREATE INDEX idx_payments_status ON payments(status);
