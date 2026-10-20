CREATE TABLE salon_resources (
 id bigserial PRIMARY KEY, branch_id bigint NOT NULL REFERENCES branches,
 name varchar(120) NOT NULL, kind varchar(20) NOT NULL CHECK(kind IN ('ROOM','CHAIR','EQUIPMENT')),
 active boolean NOT NULL DEFAULT true, UNIQUE(branch_id,name)
);
ALTER TABLE appointment_items ADD COLUMN resource_id bigint REFERENCES salon_resources;
ALTER TABLE appointment_items ADD CONSTRAINT resource_time_exclusion EXCLUDE USING gist
 (resource_id WITH =, tstzrange(starts_at,blocked_until,'[)') WITH &&) WHERE(active AND resource_id IS NOT NULL);

CREATE TABLE commission_rules (
 id bigserial PRIMARY KEY, branch_id bigint NOT NULL REFERENCES branches, staff_id bigint NOT NULL REFERENCES staff,
 service_id bigint NOT NULL REFERENCES services, name varchar(120) NOT NULL,
 kind varchar(16) NOT NULL CHECK(kind IN ('PERCENT','FIXED','TIERED')),
 rate numeric(5,2) NOT NULL CHECK(rate BETWEEN 0 AND 100), fixed_amount numeric(16,0) NOT NULL DEFAULT 0 CHECK(fixed_amount>=0),
 threshold numeric(16,0) NOT NULL DEFAULT 0 CHECK(threshold>=0), upper_rate numeric(5,2) NOT NULL DEFAULT 0 CHECK(upper_rate BETWEEN 0 AND 100),
 valid_from date NOT NULL, valid_to date NOT NULL, active boolean NOT NULL DEFAULT true,
 created_by bigint NOT NULL REFERENCES app_users, created_at timestamptz NOT NULL DEFAULT now(), CHECK(valid_to>=valid_from),
 EXCLUDE USING gist(staff_id WITH =,service_id WITH =,daterange(valid_from,valid_to,'[]') WITH &&) WHERE(active)
);
ALTER TABLE commissions ADD COLUMN rule_id bigint REFERENCES commission_rules;
ALTER TABLE commissions ADD COLUMN rule_snapshot varchar(500);

CREATE TABLE account_transfers (
 id bigserial PRIMARY KEY, branch_id bigint NOT NULL REFERENCES branches,
 from_account_id bigint NOT NULL REFERENCES accounts, to_account_id bigint NOT NULL REFERENCES accounts,
 amount numeric(16,0) NOT NULL CHECK(amount>0), reason varchar(300) NOT NULL,
 idempotency_key uuid UNIQUE NOT NULL, created_by bigint NOT NULL REFERENCES app_users,
 created_at timestamptz NOT NULL DEFAULT now(), CHECK(from_account_id<>to_account_id)
);
ALTER TABLE financial_entries ADD COLUMN transfer_id bigint REFERENCES account_transfers;
CREATE UNIQUE INDEX account_opening_once ON financial_entries(account_id) WHERE kind='OPENING';

CREATE TABLE message_templates (
 id bigserial PRIMARY KEY, branch_id bigint NOT NULL REFERENCES branches,
 kind varchar(20) NOT NULL CHECK(kind IN ('BOOKING','REMINDER','BIRTHDAY','FOLLOWUP')),
 body varchar(800) NOT NULL, active boolean NOT NULL DEFAULT false,
 delay_hours int NOT NULL DEFAULT 24 CHECK(delay_hours BETWEEN 1 AND 168),
 version bigint NOT NULL DEFAULT 0, UNIQUE(branch_id,kind)
);
ALTER TABLE messages ADD COLUMN dedupe_key varchar(180);
ALTER TABLE messages ADD COLUMN appointment_id bigint REFERENCES appointments;
ALTER TABLE messages ADD COLUMN appointment_version bigint;
ALTER TABLE messages ADD COLUMN invoice_id bigint REFERENCES invoices;
ALTER TABLE messages ADD COLUMN scheduled_at timestamptz NOT NULL DEFAULT now();
ALTER TABLE messages ADD COLUMN delivery_status varchar(20) NOT NULL DEFAULT 'UNKNOWN' CHECK(delivery_status IN ('UNKNOWN','DELIVERED','UNDELIVERABLE'));
ALTER TABLE messages ADD COLUMN delivered_at timestamptz;
CREATE UNIQUE INDEX messages_dedupe_idx ON messages(dedupe_key) WHERE dedupe_key IS NOT NULL;
CREATE INDEX messages_ready_idx ON messages(status,scheduled_at,id);

CREATE TABLE growth_attributions (
 id bigserial PRIMARY KEY, branch_id bigint NOT NULL REFERENCES branches,
 campaign_id bigint REFERENCES campaigns, event_id bigint REFERENCES events,
 invoice_id bigint NOT NULL UNIQUE REFERENCES invoices, evidence varchar(500) NOT NULL,
 created_by bigint NOT NULL REFERENCES app_users, created_at timestamptz NOT NULL DEFAULT now(),
 CHECK((campaign_id IS NULL)<>(event_id IS NULL))
);
CREATE TABLE growth_costs (
 id bigserial PRIMARY KEY, branch_id bigint NOT NULL REFERENCES branches,
 campaign_id bigint REFERENCES campaigns, event_id bigint REFERENCES events,
 expense_id bigint NOT NULL UNIQUE REFERENCES expenses,
 created_by bigint NOT NULL REFERENCES app_users, created_at timestamptz NOT NULL DEFAULT now(),
 CHECK((campaign_id IS NULL)<>(event_id IS NULL))
);

ALTER TABLE campaigns DROP CONSTRAINT campaigns_segment_check;
ALTER TABLE campaigns ADD CONSTRAINT campaigns_segment_check CHECK(segment IN ('ALL','LOYAL','GROWING','NEW','AT_RISK','LOST'));
