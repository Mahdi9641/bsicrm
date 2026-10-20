-- Additive upgrade: preserve all V1 records and Flyway checksums.
ALTER TABLE appointments ADD COLUMN version bigint NOT NULL DEFAULT 0;
ALTER TABLE appointments ADD COLUMN updated_at timestamptz NOT NULL DEFAULT now();
CREATE TABLE appointment_changes (
    id bigserial PRIMARY KEY,
    appointment_id bigint NOT NULL REFERENCES appointments,
    changed_by bigint NOT NULL REFERENCES app_users,
    old_start timestamptz NOT NULL,
    new_start timestamptz NOT NULL,
    reason varchar(500) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE follow_up_tasks (
    id bigserial PRIMARY KEY,
    branch_id bigint NOT NULL REFERENCES branches,
    customer_id bigint REFERENCES customers,
    assigned_to bigint NOT NULL REFERENCES app_users,
    created_by bigint NOT NULL REFERENCES app_users,
    title varchar(160) NOT NULL,
    description varchar(2000) NOT NULL DEFAULT '',
    due_at timestamptz NOT NULL,
    priority varchar(10) NOT NULL CHECK (priority IN ('LOW','NORMAL','HIGH','URGENT')),
    status varchar(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','IN_PROGRESS','DONE','CANCELLED')),
    version bigint NOT NULL DEFAULT 0,
    completed_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE task_events (
    id bigserial PRIMARY KEY,
    task_id bigint NOT NULL REFERENCES follow_up_tasks,
    user_id bigint NOT NULL REFERENCES app_users,
    kind varchar(20) NOT NULL,
    body varchar(2000) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);
ALTER TABLE auth_sessions ADD COLUMN user_agent varchar(300) NOT NULL DEFAULT '';
ALTER TABLE auth_sessions ADD COLUMN remote_address varchar(64) NOT NULL DEFAULT '';
ALTER TABLE auth_sessions ADD COLUMN refreshed_at timestamptz NOT NULL DEFAULT now();
CREATE INDEX tasks_due_idx ON follow_up_tasks(branch_id,status,due_at,id);
CREATE INDEX tasks_assignee_idx ON follow_up_tasks(assigned_to,status,due_at);
CREATE INDEX tasks_customer_idx ON follow_up_tasks(customer_id,created_at DESC);
CREATE INDEX task_events_idx ON task_events(task_id,created_at,id);
CREATE INDEX appointment_changes_idx ON appointment_changes(appointment_id,created_at DESC);
CREATE INDEX invoices_branch_created_idx ON invoices(branch_id,created_at DESC);
CREATE INDEX visits_branch_status_idx ON visits(branch_id,status,check_in_at DESC);
CREATE INDEX audit_branch_created_idx ON audit_logs(branch_id,created_at DESC);
CREATE INDEX auth_sessions_user_idx ON auth_sessions(user_id,expires_at) WHERE NOT revoked;
