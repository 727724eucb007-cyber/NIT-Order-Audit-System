-- Safe migration: widen status column from ENUM to VARCHAR(20)
-- Preserves all existing rows and their current status values.
-- ALTER COLUMN is idempotent on VARCHAR; safe to re-run.
ALTER TABLE sales_orders
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
