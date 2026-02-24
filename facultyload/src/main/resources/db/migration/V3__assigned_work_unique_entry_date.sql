DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM   pg_constraint
        WHERE  conname = 'uk_assigned_work_entry_date'
    ) THEN
        ALTER TABLE assigned_work
            ADD CONSTRAINT uk_assigned_work_entry_date UNIQUE (timetable_entry_id, work_date);
    END IF;
END
$$;

