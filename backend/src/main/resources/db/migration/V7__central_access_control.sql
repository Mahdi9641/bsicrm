-- Additive migration: preserve all existing users and tenant ownership.
CREATE TABLE access_roles (
 id bigserial PRIMARY KEY,
 salon_id bigint REFERENCES salons(id),
 code varchar(40), name varchar(120) NOT NULL,
 base_role varchar(20) NOT NULL CHECK(base_role IN ('ADMIN','MANAGER','RECEPTION','STYLIST','MARKETING','FINANCE','CUSTOMER')),
 built_in boolean NOT NULL DEFAULT false, active boolean NOT NULL DEFAULT true,
 version bigint NOT NULL DEFAULT 0, created_at timestamptz NOT NULL DEFAULT now(),
 CHECK ((built_in AND salon_id IS NULL AND code IS NOT NULL) OR (NOT built_in AND salon_id IS NOT NULL AND code IS NULL)),
 CHECK (built_in OR base_role<>'ADMIN')
);
CREATE UNIQUE INDEX uq_access_role_code ON access_roles(code) WHERE built_in;
CREATE UNIQUE INDEX uq_access_role_name ON access_roles(salon_id,lower(name)) WHERE NOT built_in;
CREATE TABLE access_role_permissions (
 role_id bigint NOT NULL REFERENCES access_roles(id) ON DELETE CASCADE,
 permission varchar(80) NOT NULL, PRIMARY KEY(role_id,permission)
);
ALTER TABLE app_users ADD COLUMN access_role_id bigint REFERENCES access_roles(id);
ALTER TABLE app_users ADD COLUMN access_version bigint NOT NULL DEFAULT 0;
CREATE TABLE user_permission_overrides (
 user_id bigint NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
 permission varchar(80) NOT NULL, allowed boolean NOT NULL,
 PRIMARY KEY(user_id,permission)
);
INSERT INTO access_roles(code,name,base_role,built_in) VALUES('ADMIN','مدیر سامانه','ADMIN',true);
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'appointments.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'appointments.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'audit.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'branches.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'branches.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'campaigns.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'campaigns.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'commission-rules.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'commission-rules.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'commissions.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'commissions.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'customers.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'customers.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'dashboard.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'events.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'events.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'finance.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'finance.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'invoices.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'invoices.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'loyalty.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'loyalty.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'messages.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'messages.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'products.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'products.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'reports.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'resources.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'resources.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'rfm.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'rfm.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'roles.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'roles.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'salons.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'salons.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'security.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'security.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'services.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'services.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'settings.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'settings.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'staff.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'staff.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'subscription.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'subscription.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'tasks.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'tasks.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'users.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'users.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'visits.execute' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'visits.read' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'visits.write' FROM access_roles WHERE code='ADMIN';
INSERT INTO access_roles(code,name,base_role,built_in) VALUES('MANAGER','مدیر سالن','MANAGER',true);
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'appointments.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'appointments.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'audit.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'branches.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'branches.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'campaigns.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'campaigns.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'commission-rules.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'commission-rules.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'commissions.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'commissions.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'customers.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'customers.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'dashboard.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'events.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'events.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'finance.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'finance.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'invoices.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'invoices.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'loyalty.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'loyalty.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'messages.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'messages.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'products.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'products.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'reports.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'resources.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'resources.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'rfm.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'rfm.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'roles.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'roles.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'security.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'security.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'services.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'services.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'staff.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'staff.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'subscription.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'subscription.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'tasks.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'tasks.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'users.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'users.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'visits.execute' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'visits.read' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'visits.write' FROM access_roles WHERE code='MANAGER';
INSERT INTO access_roles(code,name,base_role,built_in) VALUES('RECEPTION','پذیرش','RECEPTION',true);
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'appointments.read' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'appointments.write' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'customers.read' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'customers.write' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'dashboard.read' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'invoices.read' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'invoices.write' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'loyalty.read' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'products.read' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'security.read' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'security.write' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'services.read' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'tasks.read' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'tasks.write' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'visits.execute' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'visits.read' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'visits.write' FROM access_roles WHERE code='RECEPTION';
INSERT INTO access_roles(code,name,base_role,built_in) VALUES('STYLIST','زیباکار','STYLIST',true);
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'appointments.read' FROM access_roles WHERE code='STYLIST';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'commissions.read' FROM access_roles WHERE code='STYLIST';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'dashboard.read' FROM access_roles WHERE code='STYLIST';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'security.read' FROM access_roles WHERE code='STYLIST';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'security.write' FROM access_roles WHERE code='STYLIST';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'visits.execute' FROM access_roles WHERE code='STYLIST';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'visits.read' FROM access_roles WHERE code='STYLIST';
INSERT INTO access_roles(code,name,base_role,built_in) VALUES('MARKETING','بازاریابی','MARKETING',true);
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'campaigns.read' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'campaigns.write' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'customers.read' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'dashboard.read' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'events.read' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'events.write' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'loyalty.read' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'messages.read' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'messages.write' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'rfm.read' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'rfm.write' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'security.read' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'security.write' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'tasks.read' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'tasks.write' FROM access_roles WHERE code='MARKETING';
INSERT INTO access_roles(code,name,base_role,built_in) VALUES('FINANCE','امور مالی','FINANCE',true);
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'commission-rules.read' FROM access_roles WHERE code='FINANCE';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'commissions.read' FROM access_roles WHERE code='FINANCE';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'commissions.write' FROM access_roles WHERE code='FINANCE';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'customers.read' FROM access_roles WHERE code='FINANCE';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'dashboard.read' FROM access_roles WHERE code='FINANCE';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'finance.read' FROM access_roles WHERE code='FINANCE';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'finance.write' FROM access_roles WHERE code='FINANCE';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'invoices.read' FROM access_roles WHERE code='FINANCE';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'invoices.write' FROM access_roles WHERE code='FINANCE';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'reports.read' FROM access_roles WHERE code='FINANCE';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'security.read' FROM access_roles WHERE code='FINANCE';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'security.write' FROM access_roles WHERE code='FINANCE';
INSERT INTO access_roles(code,name,base_role,built_in) VALUES('CUSTOMER','مشتری','CUSTOMER',true);
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'portal.appointments.read' FROM access_roles WHERE code='CUSTOMER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'portal.appointments.write' FROM access_roles WHERE code='CUSTOMER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'portal.history.read' FROM access_roles WHERE code='CUSTOMER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'portal.profile.read' FROM access_roles WHERE code='CUSTOMER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'portal.profile.write' FROM access_roles WHERE code='CUSTOMER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'security.read' FROM access_roles WHERE code='CUSTOMER';
INSERT INTO access_role_permissions(role_id,permission) SELECT id,'security.write' FROM access_roles WHERE code='CUSTOMER';
UPDATE app_users u SET access_role_id=r.id FROM access_roles r WHERE r.built_in AND r.code=u.role;
-- Legacy creation paths and customer self-registration receive the matching built-in role.
CREATE FUNCTION assign_access_role() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE target access_roles%ROWTYPE;
BEGIN
 IF NEW.access_role_id IS NULL OR (TG_OP='UPDATE' AND NEW.role<>OLD.role AND NEW.access_role_id=OLD.access_role_id) THEN
   SELECT id INTO NEW.access_role_id FROM access_roles WHERE built_in AND code=NEW.role;
 END IF;
 SELECT * INTO target FROM access_roles WHERE id=NEW.access_role_id;
 IF target.id IS NULL OR target.base_role<>NEW.role OR (target.salon_id IS NOT NULL AND target.salon_id IS DISTINCT FROM (SELECT salon_id FROM branches WHERE id=NEW.branch_id)) THEN
   RAISE EXCEPTION 'نقش با نوع حساب یا سالن سازگار نیست' USING ERRCODE='23514';
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER user_access_role BEFORE INSERT OR UPDATE OF role,branch_id,access_role_id ON app_users FOR EACH ROW EXECUTE FUNCTION assign_access_role();
ALTER TABLE app_users ALTER COLUMN access_role_id SET NOT NULL;
CREATE INDEX ix_app_users_access_role ON app_users(access_role_id);
