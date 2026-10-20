-- Never merge or remove existing customer records automatically.
DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM customers GROUP BY mobile HAVING count(*) > 1) THEN
    RAISE EXCEPTION 'Customer mobile duplicates exist across branches. Reconcile them before V4; no records were merged or deleted.';
  END IF;
END $$;
ALTER TABLE customers ADD COLUMN national_code varchar(10);
ALTER TABLE customers ADD COLUMN customer_code varchar(40) GENERATED ALWAYS AS ('CUS-' || id::text) STORED;
ALTER TABLE customers ADD COLUMN status varchar(20) NOT NULL DEFAULT 'ACTIVE';
UPDATE customers SET status=CASE WHEN active THEN 'ACTIVE' ELSE 'INACTIVE' END;
ALTER TABLE customers ADD CONSTRAINT uq_customers_mobile_global UNIQUE(mobile);
ALTER TABLE customers ADD CONSTRAINT uq_customers_national_code UNIQUE(national_code);
ALTER TABLE customers ADD CONSTRAINT uq_customers_code UNIQUE(customer_code);
ALTER TABLE customers ADD CONSTRAINT ck_customers_mobile CHECK(mobile ~ '^09[0-9]{9}$');
ALTER TABLE customers ADD CONSTRAINT ck_customers_national_code CHECK(national_code IS NULL OR national_code ~ '^[0-9]{10}$');
ALTER TABLE customers ADD CONSTRAINT ck_customers_status CHECK(status IN ('ACTIVE','INACTIVE','BLOCKED') AND active=(status='ACTIVE'));
ALTER TABLE customers ADD CONSTRAINT ck_customers_contact CHECK(contact_channel IN ('SMS','PHONE','WHATSAPP'));
CREATE TABLE customer_relations (
 id bigserial PRIMARY KEY,
 customer_id bigint NOT NULL REFERENCES customers(id),
 relationship varchar(20) NOT NULL CHECK(relationship IN ('SPOUSE','CHILD','OTHER')),
 full_name varchar(160) NOT NULL,
 birth_date date,
 notes varchar(500),
 created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_customer_relations_customer ON customer_relations(customer_id);
