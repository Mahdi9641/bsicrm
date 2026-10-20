ALTER TABLE app_users ADD COLUMN customer_id bigint REFERENCES customers(id);
ALTER TABLE app_users DROP CONSTRAINT IF EXISTS app_users_role_check;
ALTER TABLE app_users ADD CONSTRAINT app_users_role_check CHECK(role IN ('ADMIN','MANAGER','RECEPTION','STYLIST','MARKETING','FINANCE','CUSTOMER'));
ALTER TABLE app_users ADD CONSTRAINT app_users_customer_role_check CHECK ((role='CUSTOMER' AND customer_id IS NOT NULL) OR (role<>'CUSTOMER' AND customer_id IS NULL));
CREATE UNIQUE INDEX uq_app_users_customer ON app_users(customer_id) WHERE customer_id IS NOT NULL;
CREATE INDEX ix_appointments_customer_time ON appointments(customer_id, starts_at DESC);
