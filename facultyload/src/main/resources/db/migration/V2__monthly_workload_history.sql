CREATE TABLE IF NOT EXISTS monthly_workload_history (
    id                    BIGSERIAL PRIMARY KEY,
    faculty_id            BIGINT NOT NULL REFERENCES faculty(id) ON DELETE CASCADE,
    month_start_date      DATE NOT NULL,
    monthly_assigned_hours INTEGER NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_monthly_workload_history_faculty_month UNIQUE (faculty_id, month_start_date)
);

CREATE INDEX IF NOT EXISTS idx_monthly_workload_history_faculty_id ON monthly_workload_history(faculty_id);
CREATE INDEX IF NOT EXISTS idx_monthly_workload_history_month ON monthly_workload_history(month_start_date);

