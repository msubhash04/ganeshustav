-- ============================================================
-- Ganesh Utsav Management Application - MySQL Schema
-- Multi-Tenant (Multi-Committee) architecture.
--
-- Note: Spring Boot (spring.jpa.hibernate.ddl-auto=update) will
-- auto-create/update these tables on startup. This script is
-- provided for manual setup / reference / seeding the Developer
-- (Super Admin) account, which cannot be created via any API
-- endpoint by design - it only ever exists via direct DB seeding.
-- ============================================================

CREATE DATABASE IF NOT EXISTS ganesh_utsav_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ganesh_utsav_db;

-- ============================================================
-- TENANT ROOT: one row per registered Ganesh Committee.
-- Every other tenant-scoped table below joins back to this table's
-- immutable `id` (never to tenant_code, which is regenerable).
-- ============================================================
CREATE TABLE IF NOT EXISTS committees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_code VARCHAR(20) NOT NULL UNIQUE,   -- "Ganesh Unique Code", e.g. GU-MH-PUN-0001
    name VARCHAR(255) NOT NULL,                -- e.g. "Shivaji Nagar Ganesh Mandal"
    city VARCHAR(100),
    state VARCHAR(100),
    address VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,       -- Developer can lock a committee post-festival
    created_by_developer_id BIGINT,             -- which Developer registered this committee
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_committee_city (city),
    INDEX idx_committee_state (state)
);

-- ---------------- Members / login accounts ----------------
-- committee_id is NULL only for DEVELOPER (Super Admin) accounts, which
-- are global and not scoped to any single committee. Every other role
-- (PRESIDENT, TREASURER, SECRETARY, VOLUNTEER) must have a committee -
-- enforced in the service layer (TenantContext), not as a DB constraint,
-- so this one table can hold both kinds of account.
CREATE TABLE IF NOT EXISTS members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    role ENUM('DEVELOPER','PRESIDENT','TREASURER','SECRETARY','VOLUNTEER') NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    committee_id BIGINT NULL,
    created_at DATETIME,
    FOREIGN KEY (committee_id) REFERENCES committees(id) ON DELETE SET NULL,
    INDEX idx_member_committee (committee_id)
);

ALTER TABLE committees
    ADD CONSTRAINT fk_committee_created_by_developer
    FOREIGN KEY (created_by_developer_id) REFERENCES members(id) ON DELETE SET NULL;

-- ---------------- Festival Years (President admin setup, per committee) ----------------
CREATE TABLE IF NOT EXISTS festival_years (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    committee_id BIGINT NOT NULL,
    label VARCHAR(255) NOT NULL,
    year INT NOT NULL,
    start_date DATE NOT NULL,
    duration_days INT NOT NULL,
    carry_forward_balance DECIMAL(12,2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_by_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (committee_id) REFERENCES committees(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_id) REFERENCES members(id) ON DELETE SET NULL,
    UNIQUE KEY uq_committee_label (committee_id, label),
    INDEX idx_festival_year_committee (committee_id)
);

-- ---------------- Donations / Collections ----------------
CREATE TABLE IF NOT EXISTS donations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    committee_id BIGINT NOT NULL,
    receipt_number VARCHAR(30) NOT NULL UNIQUE,
    donor_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    address VARCHAR(255),
    amount DECIMAL(12,2) NOT NULL,
    payment_mode ENUM('CASH','UPI','BANK_TRANSFER','CHEQUE') NOT NULL,
    donation_date DATE NOT NULL,
    recorded_by_id BIGINT,
    festival_year_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (committee_id) REFERENCES committees(id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by_id) REFERENCES members(id) ON DELETE SET NULL,
    FOREIGN KEY (festival_year_id) REFERENCES festival_years(id) ON DELETE SET NULL,
    INDEX idx_donation_committee (committee_id),
    INDEX idx_donor_name (donor_name),
    INDEX idx_donation_date (donation_date)
);

-- ---------------- Expenses (day-wise, with note for Misc/Gift Distribution) ----------------
CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    committee_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    category ENUM('IDOL_MURTI','PANDAL_DECORATION','ELECTRICITY_LIGHTING','SOUND_SYSTEM',
                  'PRIEST_POOJA_MATERIALS','FOOD_PRASAD','IMMERSION_VISARJAN','SECURITY',
                  'CULTURAL_PROGRAMS','MISCELLANEOUS') NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    paid_to VARCHAR(255) NOT NULL,
    expense_date DATE NOT NULL,
    payment_mode ENUM('CASH','UPI','BANK_TRANSFER','CHEQUE') NOT NULL,
    bill_file_path VARCHAR(500),
    festival_year_id BIGINT,
    day_number INT,
    note VARCHAR(500),
    recorded_by_id BIGINT,
    approved_by_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (committee_id) REFERENCES committees(id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by_id) REFERENCES members(id) ON DELETE SET NULL,
    FOREIGN KEY (approved_by_id) REFERENCES members(id) ON DELETE SET NULL,
    FOREIGN KEY (festival_year_id) REFERENCES festival_years(id) ON DELETE SET NULL,
    INDEX idx_expense_committee (committee_id),
    INDEX idx_category (category),
    INDEX idx_expense_date (expense_date),
    INDEX idx_festival_day (festival_year_id, day_number)
);

-- ---------------- Auction Items (Velampata) ----------------
CREATE TABLE IF NOT EXISTS auction_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    committee_id BIGINT NOT NULL,
    festival_year_id BIGINT,
    day_number INT,
    item_name VARCHAR(255) NOT NULL,
    winner_name VARCHAR(255) NOT NULL,
    bid_amount DECIMAL(12,2) NOT NULL,
    payment_status ENUM('PAID','PENDING') NOT NULL,
    payment_mode ENUM('CASH','UPI','BANK_TRANSFER','CHEQUE'),
    recorded_by_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (committee_id) REFERENCES committees(id) ON DELETE CASCADE,
    FOREIGN KEY (festival_year_id) REFERENCES festival_years(id) ON DELETE SET NULL,
    FOREIGN KEY (recorded_by_id) REFERENCES members(id) ON DELETE SET NULL,
    INDEX idx_auction_committee (committee_id)
);

-- ---------------- Loans (post-festival micro-lending, President only) ----------------
CREATE TABLE IF NOT EXISTS loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    committee_id BIGINT NOT NULL,
    festival_year_id BIGINT,
    borrower_name VARCHAR(255) NOT NULL,
    borrower_phone VARCHAR(15),
    original_principal DECIMAL(12,2) NOT NULL,
    current_principal DECIMAL(12,2) NOT NULL,
    monthly_interest_rate_percent DECIMAL(5,2) NOT NULL,
    loan_date DATE NOT NULL,
    last_interest_date DATE NOT NULL,
    status ENUM('ACTIVE','CLOSED') NOT NULL DEFAULT 'ACTIVE',
    recorded_by_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (committee_id) REFERENCES committees(id) ON DELETE CASCADE,
    FOREIGN KEY (festival_year_id) REFERENCES festival_years(id) ON DELETE SET NULL,
    FOREIGN KEY (recorded_by_id) REFERENCES members(id) ON DELETE SET NULL,
    INDEX idx_loan_committee (committee_id)
);

-- ---------------- Loan Repayments (reducing-balance interest method) ----------------
-- No direct committee_id here by design - a repayment is only ever
-- accessed through its parent Loan, which is already committee-scoped.
CREATE TABLE IF NOT EXISTS loan_repayments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    payment_date DATE NOT NULL,
    payment_amount DECIMAL(12,2) NOT NULL,
    interest_portion DECIMAL(12,2) NOT NULL,
    principal_portion DECIMAL(12,2) NOT NULL,
    remaining_principal_after DECIMAL(12,2) NOT NULL,
    recorded_by_id BIGINT,
    created_at DATETIME,
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by_id) REFERENCES members(id) ON DELETE SET NULL
);

-- ---------------- Sponsorship Categories (President-managed master data, per committee) ----------------
CREATE TABLE IF NOT EXISTS sponsorship_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    committee_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,       -- unique PER COMMITTEE only (see constraint below)
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (committee_id) REFERENCES committees(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_id) REFERENCES members(id) ON DELETE SET NULL,
    UNIQUE KEY uq_committee_category_name (committee_id, name)
);

-- ---------------- General Sponsors ----------------
CREATE TABLE IF NOT EXISTS general_sponsors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    committee_id BIGINT NOT NULL,
    sponsor_name VARCHAR(255) NOT NULL,
    contact_info VARCHAR(15),
    contribution_amount DECIMAL(12,2),
    contribution_details VARCHAR(500),
    category_id BIGINT NOT NULL,
    festival_year_id BIGINT,
    recorded_by_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (committee_id) REFERENCES committees(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES sponsorship_categories(id),
    FOREIGN KEY (festival_year_id) REFERENCES festival_years(id) ON DELETE SET NULL,
    FOREIGN KEY (recorded_by_id) REFERENCES members(id) ON DELETE SET NULL,
    INDEX idx_general_sponsor_committee (committee_id),
    INDEX idx_general_sponsor_category (category_id)
);

-- ---------------- Annadanam Sponsors (dedicated day-wise food sponsorship tracking) ----------------
CREATE TABLE IF NOT EXISTS annadanam_sponsors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    committee_id BIGINT NOT NULL,
    sponsor_name VARCHAR(255) NOT NULL,
    contact_info VARCHAR(15),
    day_number INT NOT NULL,
    meal_slot VARCHAR(50),
    contribution_amount DECIMAL(12,2),
    contribution_details VARCHAR(500),
    festival_year_id BIGINT,
    recorded_by_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (committee_id) REFERENCES committees(id) ON DELETE CASCADE,
    FOREIGN KEY (festival_year_id) REFERENCES festival_years(id) ON DELETE SET NULL,
    FOREIGN KEY (recorded_by_id) REFERENCES members(id) ON DELETE SET NULL,
    INDEX idx_annadanam_committee (committee_id),
    INDEX idx_annadanam_day (festival_year_id, day_number)
);

-- ============================================================
-- SEED: the ONE Developer (Super Admin) account.
--
-- SECURITY NOTE: this is the only way a DEVELOPER account is ever
-- created - there is intentionally no API endpoint for it (see
-- AuthController.register, which explicitly rejects role=DEVELOPER).
-- This account has committee_id = NULL (global access) and its
-- password below is a real, verified BCrypt hash - not a placeholder.
--
--   username: ganeshdev
--   password: GaneshDev@2026
--
-- CHANGE THIS PASSWORD after first login in any real deployment -
-- there is currently no in-app "change password" flow, so do it via
-- a direct UPDATE with a freshly generated BCrypt hash.
-- ============================================================
INSERT INTO members (name, phone, email, role, username, password, active, committee_id, created_at)
VALUES ('Platform Developer', '9000000000', 'developer@ganeshutsav.platform', 'DEVELOPER',
        'ganeshdev', '$2b$10$mcc.N.iOom7XYUDCJ7yZIuN1F38V9suhENHYrrAFb4whToyyR.f5a',
        TRUE, NULL, NOW())
ON DUPLICATE KEY UPDATE username = username;
