-- Preserve existing branches as one organization; no customer records are removed.
CREATE TABLE salons (
 id bigserial PRIMARY KEY, name varchar(160) NOT NULL, active boolean NOT NULL DEFAULT true,
 plan varchar(16) NOT NULL CHECK(plan IN ('BASIC','STANDARD','ADVANCED','SMART')),
 expires_on date NOT NULL, point_unit numeric(16,0) NOT NULL DEFAULT 100000 CHECK(point_unit>0),
 risk_days int NOT NULL DEFAULT 90 CHECK(risk_days BETWEEN 1 AND 730),
 loyal_visits int NOT NULL DEFAULT 5 CHECK(loyal_visits BETWEEN 2 AND 100),
 created_at timestamptz NOT NULL DEFAULT now()
);
INSERT INTO salons(name,plan,expires_on,point_unit,risk_days,loyal_visits)
 SELECT organization,plan,coalesce(expires_on,(now() AT TIME ZONE 'Asia/Tehran')::date+30),point_unit,risk_days,loyal_visits FROM settings WHERE id=1;
ALTER TABLE branches ADD COLUMN salon_id bigint NOT NULL DEFAULT 1 REFERENCES salons;
ALTER TABLE branches ALTER COLUMN salon_id DROP DEFAULT;
ALTER TABLE branches ADD COLUMN phone varchar(20) NOT NULL DEFAULT '';
ALTER TABLE branches ADD COLUMN address varchar(500) NOT NULL DEFAULT '';
ALTER TABLE app_users ADD COLUMN salon_id bigint REFERENCES salons;
UPDATE app_users u SET salon_id=b.salon_id FROM branches b WHERE u.branch_id=b.id;
ALTER TABLE app_users ALTER COLUMN salon_id SET NOT NULL;
ALTER TABLE customers ADD COLUMN salon_id bigint REFERENCES salons;
UPDATE customers c SET salon_id=b.salon_id FROM branches b WHERE c.branch_id=b.id;
ALTER TABLE customers ALTER COLUMN salon_id SET NOT NULL;
ALTER TABLE customers DROP CONSTRAINT uq_customers_mobile_global;
ALTER TABLE customers DROP CONSTRAINT uq_customers_national_code;
ALTER TABLE customers ADD CONSTRAINT uq_customers_salon_mobile UNIQUE(salon_id,mobile);
ALTER TABLE customers ADD CONSTRAINT uq_customers_salon_national_code UNIQUE(salon_id,national_code);
ALTER TABLE auth_sessions ADD COLUMN active_branch_id bigint REFERENCES branches;
CREATE FUNCTION assign_salon() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 SELECT salon_id INTO NEW.salon_id FROM branches WHERE id=NEW.branch_id;
 RETURN NEW;
END $$;
CREATE TRIGGER customer_salon BEFORE INSERT OR UPDATE OF branch_id,salon_id ON customers FOR EACH ROW EXECUTE FUNCTION assign_salon();
CREATE TRIGGER user_salon BEFORE INSERT OR UPDATE OF branch_id,salon_id ON app_users FOR EACH ROW EXECUTE FUNCTION assign_salon();
CREATE INDEX ON branches(salon_id);
CREATE INDEX ON app_users(salon_id);
CREATE TABLE subscription_plans (
 code varchar(16) PRIMARY KEY CHECK(code IN ('BASIC','STANDARD','ADVANCED','SMART')),
 name varchar(100) NOT NULL, description varchar(500) NOT NULL, price numeric(16,0) NOT NULL CHECK(price>0),
 duration_days int NOT NULL CHECK(duration_days BETWEEN 1 AND 730), active boolean NOT NULL DEFAULT true
);
INSERT INTO subscription_plans VALUES
 ('BASIC','پایه','مشتریان، نوبت‌دهی و فروش',300000,30,true),
 ('STANDARD','استاندارد','امکانات پایه، باشگاه مشتریان و کمیسیون',500000,30,true),
 ('ADVANCED','پیشرفته','بازاریابی، رویدادها و تحلیل مشتریان',800000,30,true),
 ('SMART','هوشمند','همهٔ امکانات فعلی؛ هوش مصنوعی در نقشهٔ توسعه',1000000,30,true);
CREATE TABLE subscription_orders (
 id uuid PRIMARY KEY, salon_id bigint NOT NULL REFERENCES salons, plan_code varchar(16) NOT NULL REFERENCES subscription_plans,
 amount numeric(16,0) NOT NULL CHECK(amount>0), duration_days int NOT NULL CHECK(duration_days>0),
 status varchar(20) NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING','PAID','REJECTED')),
 payment_method varchar(20) NOT NULL CHECK(payment_method IN ('MANUAL','ZARINPAL')),
 reference varchar(150), authority varchar(100) UNIQUE, verified_reference varchar(150) UNIQUE,
 created_by bigint NOT NULL REFERENCES app_users, created_at timestamptz NOT NULL DEFAULT now(),
 reviewed_by bigint REFERENCES app_users, paid_at timestamptz, starts_on date, expires_on date,
 idempotency_key uuid NOT NULL, UNIQUE(salon_id,idempotency_key)
);
CREATE TABLE captcha_challenges(id uuid PRIMARY KEY, answer_hash varchar(64) NOT NULL, expires_at timestamptz NOT NULL);
CREATE TABLE auth_rate_limits(key_hash varchar(64) PRIMARY KEY, window_start timestamptz NOT NULL, attempts int NOT NULL);
