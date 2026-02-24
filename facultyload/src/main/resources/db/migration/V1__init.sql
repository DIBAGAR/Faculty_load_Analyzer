-- Baseline schema for Faculty Instructional Load Balance Analyzer
-- PostgreSQL recommended.

CREATE TABLE IF NOT EXISTS roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    TEXT NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    role_id     BIGINT NOT NULL REFERENCES roles(id),
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);

CREATE TABLE IF NOT EXISTS departments (
    id                  BIGSERIAL PRIMARY KEY,
    name                TEXT NOT NULL UNIQUE,
    code                TEXT UNIQUE,
    hod_id              BIGINT NULL,
    temp_hod_id         BIGINT NULL,
    temp_hod_end_date   DATE NULL,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_departments_code ON departments(code);

CREATE TABLE IF NOT EXISTS faculty (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL UNIQUE REFERENCES users(id),
    department_id           BIGINT NOT NULL REFERENCES departments(id),
    name                    TEXT NOT NULL,
    designation             VARCHAR(60) NOT NULL,
    is_hod                  BOOLEAN NOT NULL DEFAULT FALSE,
    monthly_assigned_hours  INTEGER NOT NULL DEFAULT 0,
    total_assigned_hours    INTEGER NOT NULL DEFAULT 0,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_faculty_department_id ON faculty(department_id);
CREATE INDEX IF NOT EXISTS idx_faculty_user_id ON faculty(user_id);

ALTER TABLE departments
    ADD CONSTRAINT fk_departments_hod FOREIGN KEY (hod_id) REFERENCES faculty(id);
ALTER TABLE departments
    ADD CONSTRAINT fk_departments_temp_hod FOREIGN KEY (temp_hod_id) REFERENCES faculty(id);

CREATE TABLE IF NOT EXISTS courses (
    id          BIGSERIAL PRIMARY KEY,
    course_code VARCHAR(50) NOT NULL UNIQUE,
    name        TEXT NOT NULL,
    credit      INTEGER NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_courses_course_code ON courses(course_code);

CREATE TABLE IF NOT EXISTS course_departments (
    course_id        BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    department_id    BIGINT NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    PRIMARY KEY (course_id, department_id)
);

CREATE TABLE IF NOT EXISTS faculty_course (
    faculty_id      BIGINT NOT NULL REFERENCES faculty(id) ON DELETE CASCADE,
    course_id       BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    knowledge_type  VARCHAR(20) NOT NULL,
    PRIMARY KEY (faculty_id, course_id)
);

CREATE INDEX IF NOT EXISTS idx_faculty_course_faculty_id ON faculty_course(faculty_id);
CREATE INDEX IF NOT EXISTS idx_faculty_course_course_id ON faculty_course(course_id);
CREATE INDEX IF NOT EXISTS idx_faculty_course_knowledge_type ON faculty_course(knowledge_type);

CREATE TABLE IF NOT EXISTS venues (
    id            BIGSERIAL PRIMARY KEY,
    department_id BIGINT NOT NULL REFERENCES departments(id),
    name          VARCHAR(150) NOT NULL,
    code          VARCHAR(30) NOT NULL,
    type          VARCHAR(20) NOT NULL,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_venues_department_code UNIQUE (department_id, code)
);

CREATE INDEX IF NOT EXISTS idx_venues_department_id ON venues(department_id);
CREATE INDEX IF NOT EXISTS idx_venues_type ON venues(type);

CREATE TABLE IF NOT EXISTS timetables (
    id            BIGSERIAL PRIMARY KEY,
    department_id BIGINT NOT NULL REFERENCES departments(id),
    year_of_study INTEGER NOT NULL,
    semester      INTEGER NOT NULL,
    section       VARCHAR(10) NOT NULL,
    version_no    INTEGER,
    is_active     BOOLEAN NOT NULL DEFAULT FALSE,
    created_by    BIGINT NULL REFERENCES faculty(id),
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_timetables_department_id ON timetables(department_id);
CREATE INDEX IF NOT EXISTS idx_timetables_active ON timetables(is_active);

-- Only one ACTIVE timetable per (department, year_of_study, section)
CREATE UNIQUE INDEX IF NOT EXISTS uk_active_timetable_per_dept_year_section
    ON timetables(department_id, year_of_study, section)
    WHERE is_active;

CREATE TABLE IF NOT EXISTS timetable_entries (
    id                BIGSERIAL PRIMARY KEY,
    timetable_id      BIGINT NOT NULL REFERENCES timetables(id) ON DELETE CASCADE,

    -- denormalized fields (for active timetable conflict checks)
    department_id     BIGINT NOT NULL,
    year_of_study     INTEGER NOT NULL,
    semester          INTEGER NOT NULL,
    section           VARCHAR(10) NOT NULL,
    is_active         BOOLEAN NOT NULL DEFAULT FALSE,

    day_of_week       VARCHAR(10) NOT NULL,
    hour_number       INTEGER NOT NULL,
    course_id         BIGINT NOT NULL REFERENCES courses(id),
    venue_id          BIGINT NOT NULL REFERENCES venues(id),
    type              VARCHAR(20) NOT NULL,
    default_faculty_id BIGINT NULL REFERENCES faculty(id),

    CONSTRAINT uk_timetable_entries_timetable_slot UNIQUE (timetable_id, day_of_week, hour_number),
    CONSTRAINT chk_timetable_entries_hour CHECK (hour_number BETWEEN 1 AND 7),
    CONSTRAINT chk_timetable_entries_day CHECK (day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY'))
);

CREATE INDEX IF NOT EXISTS idx_timetable_entries_timetable_id ON timetable_entries(timetable_id);
CREATE INDEX IF NOT EXISTS idx_timetable_entries_course_id ON timetable_entries(course_id);
CREATE INDEX IF NOT EXISTS idx_timetable_entries_venue_id ON timetable_entries(venue_id);
CREATE INDEX IF NOT EXISTS idx_timetable_entries_default_faculty_id ON timetable_entries(default_faculty_id);
CREATE INDEX IF NOT EXISTS idx_timetable_entries_active_slot
    ON timetable_entries(department_id, year_of_study, section, semester, day_of_week, hour_number, is_active);
CREATE INDEX IF NOT EXISTS idx_timetable_entries_active_venue
    ON timetable_entries(venue_id, day_of_week, hour_number, is_active);

-- Prevent same section double booking for ACTIVE timetable
CREATE UNIQUE INDEX IF NOT EXISTS uk_active_section_slot
    ON timetable_entries(department_id, year_of_study, section, semester, day_of_week, hour_number)
    WHERE is_active;

-- Prevent venue double booking across ACTIVE timetables
CREATE UNIQUE INDEX IF NOT EXISTS uk_active_venue_slot
    ON timetable_entries(venue_id, day_of_week, hour_number)
    WHERE is_active;

CREATE TABLE IF NOT EXISTS leave_requests (
    id           BIGSERIAL PRIMARY KEY,
    faculty_id   BIGINT NOT NULL REFERENCES faculty(id),
    from_date    DATE,
    to_date      DATE,
    leave_type   VARCHAR(20) NOT NULL,
    hour_number  INTEGER NULL,
    status       VARCHAR(20) NOT NULL,
    is_emergency BOOLEAN NOT NULL DEFAULT FALSE,
    approved_by  BIGINT NULL REFERENCES faculty(id),
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_leave_requests_faculty_id ON leave_requests(faculty_id);
CREATE INDEX IF NOT EXISTS idx_leave_requests_status ON leave_requests(status);
CREATE INDEX IF NOT EXISTS idx_leave_requests_from_to ON leave_requests(from_date, to_date);

CREATE TABLE IF NOT EXISTS assigned_work (
    id                BIGSERIAL PRIMARY KEY,
    faculty_id         BIGINT NOT NULL REFERENCES faculty(id),
    timetable_entry_id BIGINT NOT NULL REFERENCES timetable_entries(id),
    work_date          DATE,
    week_start_date    DATE NOT NULL,
    status             VARCHAR(30) NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_assigned_work_faculty_id ON assigned_work(faculty_id);
CREATE INDEX IF NOT EXISTS idx_assigned_work_work_date ON assigned_work(work_date);
CREATE INDEX IF NOT EXISTS idx_assigned_work_week_start ON assigned_work(week_start_date);

CREATE TABLE IF NOT EXISTS notifications (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message    TEXT NOT NULL,
    is_read    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);

