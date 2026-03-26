-- ============================================================
-- FACULTY INSTRUCTIONAL LOAD BALANCE ANALYZER
-- PostgreSQL Database Schema
-- Run this entire script in pgAdmin Query Tool
-- ============================================================

-- Create database (run separately if needed)
-- CREATE DATABASE faculty_load_db;

-- ============================================================
-- 1. ROLES
-- ============================================================
CREATE TABLE IF NOT EXISTS roles (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(30) NOT NULL UNIQUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 2. USERS
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(150) UNIQUE,
    roll_number     VARCHAR(50) UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(150),
    role_id         BIGINT NOT NULL REFERENCES roles(id),
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 3. DEPARTMENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS departments (
    id              BIGSERIAL PRIMARY KEY,
    dept_code       VARCHAR(20) NOT NULL UNIQUE,
    dept_name       VARCHAR(100) NOT NULL,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 4. ADMINS
-- ============================================================
CREATE TABLE IF NOT EXISTS admins (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    admin_type      VARCHAR(30) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 5. FACULTIES
-- ============================================================
CREATE TABLE IF NOT EXISTS faculties (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    dept_id         BIGINT NOT NULL REFERENCES departments(id),
    name            VARCHAR(150) NOT NULL,
    roll_number     VARCHAR(50) NOT NULL UNIQUE,
    email           VARCHAR(150) NOT NULL UNIQUE,
    phone           VARCHAR(15),
    blood_group     VARCHAR(5),
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 6. COURSES
-- ============================================================
CREATE TABLE IF NOT EXISTS courses (
    id              BIGSERIAL PRIMARY KEY,
    course_code     VARCHAR(20) NOT NULL UNIQUE,
    course_name     VARCHAR(200) NOT NULL,
    credit          INTEGER,
    dept_id         BIGINT NOT NULL REFERENCES departments(id),
    semester        INTEGER NOT NULL,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 7. VENUES
-- ============================================================
CREATE TABLE IF NOT EXISTS venues (
    id              BIGSERIAL PRIMARY KEY,
    block           VARCHAR(50) NOT NULL,
    venue_name      VARCHAR(100) NOT NULL,
    venue_type      VARCHAR(15) NOT NULL CHECK (venue_type IN ('LAB', 'CLASSROOM')),
    dept_id         BIGINT REFERENCES departments(id),
    capacity        INTEGER,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 8. SECTIONS
-- ============================================================
CREATE TABLE IF NOT EXISTS sections (
    id              BIGSERIAL PRIMARY KEY,
    dept_id         BIGINT NOT NULL REFERENCES departments(id),
    year            INTEGER NOT NULL,
    semester        INTEGER NOT NULL,
    section_name    VARCHAR(10) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (dept_id, year, semester, section_name)
);

-- ============================================================
-- 9. TIMETABLES
-- ============================================================
CREATE TABLE IF NOT EXISTS timetables (
    id              BIGSERIAL PRIMARY KEY,
    section_id      BIGINT NOT NULL REFERENCES sections(id),
    status          VARCHAR(15) NOT NULL DEFAULT 'INACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    timetable_label VARCHAR(100),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 10. TIMETABLE SLOTS
-- ============================================================
CREATE TABLE IF NOT EXISTS timetable_slots (
    id                  BIGSERIAL PRIMARY KEY,
    timetable_id        BIGINT NOT NULL REFERENCES timetables(id) ON DELETE CASCADE,
    day_of_week         INTEGER NOT NULL CHECK (day_of_week BETWEEN 1 AND 6),
    hour                INTEGER NOT NULL CHECK (hour BETWEEN 1 AND 7),
    course_id           BIGINT REFERENCES courses(id),
    venue_id            BIGINT REFERENCES venues(id),
    default_faculty_id  BIGINT REFERENCES faculties(id),
    slot_type           VARCHAR(10) DEFAULT 'THEORY' CHECK (slot_type IN ('THEORY', 'LAB')),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 11. COURSE-FACULTY MAPPINGS
-- ============================================================
CREATE TABLE IF NOT EXISTS course_faculty_mappings (
    id              BIGSERIAL PRIMARY KEY,
    faculty_id      BIGINT NOT NULL REFERENCES faculties(id),
    course_id       BIGINT NOT NULL REFERENCES courses(id),
    type            VARCHAR(15) NOT NULL CHECK (type IN ('PRIMARY', 'ADDITIONAL')),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 12. LEAVE REQUESTS
-- ============================================================
CREATE TABLE IF NOT EXISTS leave_requests (
    id              BIGSERIAL PRIMARY KEY,
    faculty_id      BIGINT NOT NULL REFERENCES faculties(id),
    from_date       DATE NOT NULL,
    from_time       TIME,
    to_date         DATE NOT NULL,
    to_time         TIME,
    reason          VARCHAR(500),
    status          VARCHAR(15) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    temp_hod_id     BIGINT REFERENCES faculties(id),
    rejection_reason VARCHAR(300),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 13. WORK ASSIGNMENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS work_assignments (
    id                  BIGSERIAL PRIMARY KEY,
    faculty_id          BIGINT NOT NULL REFERENCES faculties(id),
    timetable_slot_id   BIGINT NOT NULL REFERENCES timetable_slots(id),
    assign_date         DATE NOT NULL,
    hour                INTEGER NOT NULL,
    venue_id            BIGINT REFERENCES venues(id),
    is_reassigned       BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (faculty_id, assign_date, hour)
);

-- ============================================================
-- 14. FACULTY MONTHLY LOAD
-- ============================================================
CREATE TABLE IF NOT EXISTS faculty_monthly_load (
    id              BIGSERIAL PRIMARY KEY,
    faculty_id      BIGINT NOT NULL REFERENCES faculties(id),
    year            INTEGER NOT NULL,
    month           INTEGER NOT NULL,
    total_hours     INTEGER DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 15. NOTIFICATIONS
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    title           VARCHAR(200) NOT NULL,
    message         VARCHAR(1000) NOT NULL,
    read_status     BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 16. PASSWORD RESET TOKENS
-- ============================================================
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id              BIGSERIAL PRIMARY KEY,
    token           VARCHAR(255) NOT NULL UNIQUE,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    expires_at      TIMESTAMP NOT NULL,
    used            BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- INDEXES (for query performance)
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_roll_number ON users(roll_number);
CREATE INDEX IF NOT EXISTS idx_faculties_dept_id ON faculties(dept_id);
CREATE INDEX IF NOT EXISTS idx_faculties_roll_number ON faculties(roll_number);
CREATE INDEX IF NOT EXISTS idx_courses_dept_semester ON courses(dept_id, semester);
CREATE INDEX IF NOT EXISTS idx_venues_dept_id ON venues(dept_id);
CREATE INDEX IF NOT EXISTS idx_timetable_slots_timetable ON timetable_slots(timetable_id);
CREATE INDEX IF NOT EXISTS idx_work_assignments_faculty_date ON work_assignments(faculty_id, assign_date);
CREATE INDEX IF NOT EXISTS idx_work_assignments_venue_date ON work_assignments(venue_id, assign_date, hour);
CREATE INDEX IF NOT EXISTS idx_leave_requests_faculty ON leave_requests(faculty_id);
CREATE INDEX IF NOT EXISTS idx_monthly_load_faculty ON faculty_monthly_load(faculty_id, year, month);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id, read_status);

-- ============================================================
-- SEED DATA: Insert default roles
-- ============================================================
INSERT INTO roles (name) VALUES
    ('SUPER_ADMIN'),
    ('FACULTY_ADMIN'),
    ('DEPARTMENT_ADMIN'),
    ('COURSE_ADMIN'),
    ('VENUE_ADMIN'),
    ('HOD'),
    ('FACULTY'),
    ('TEMP_HOD')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- SEED DATA: Insert Super Admin user
-- Password: admin123 (BCrypt hash)
-- ============================================================
INSERT INTO users (email, roll_number, password_hash, name, role_id, is_active)
SELECT 'admin@college.com', 'SA001',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
       'Super Admin',
       r.id,
       TRUE
FROM roles r WHERE r.name = 'SUPER_ADMIN'
AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@college.com');

-- ============================================================
-- DONE! All 16 tables created with indexes and seed data.
-- ============================================================
