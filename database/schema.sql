-- ============================================================
-- Ganesh Utsav Expense Tracker - MySQL Schema
-- Note: Spring Boot (spring.jpa.hibernate.ddl-auto=update) will
-- auto-create/update these tables on startup. This script is
-- provided for manual setup / reference / seeding sample data.
-- ============================================================

CREATE DATABASE IF NOT EXISTS ganesh_utsav_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ganesh_utsav_db;

-- ---------------- Members / committee & login accounts ----------------
CREATE TABLE IF NOT EXISTS members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    role ENUM('PRESIDENT','TREASURER','SECRETARY','VOLUNTEER') NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME
);

-- ---------------- Festival Years (President admin setup) ----------------
CREATE TABLE IF NOT EXISTS festival_years (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    label VARCHAR(255) NOT NULL UNIQUE,
    year INT NOT NULL,
    start_date DATE NOT NULL,
    duration_days INT NOT NULL,
    carry_forward_balance DECIMAL(12,2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_by_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (created_by_id) REFERENCES members(id) ON DELETE SET NULL
);

-- ---------------- Donations / Collections ----------------
CREATE TABLE IF NOT EXISTS donations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
    FOREIGN KEY (recorded_by_id) REFERENCES members(id) ON DELETE SET NULL,
    FOREIGN KEY (festival_year_id) REFERENCES festival_years(id) ON DELETE SET NULL,
    INDEX idx_donor_name (donor_name),
    INDEX idx_donation_date (donation_date)
);

-- ---------------- Expenses (day-wise, with note for Misc/Gift Distribution) ----------------
CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
    FOREIGN KEY (recorded_by_id) REFERENCES members(id) ON DELETE SET NULL,
    FOREIGN KEY (approved_by_id) REFERENCES members(id) ON DELETE SET NULL,
    FOREIGN KEY (festival_year_id) REFERENCES festival_years(id) ON DELETE SET NULL,
    INDEX idx_category (category),
    INDEX idx_expense_date (expense_date),
    INDEX idx_festival_day (festival_year_id, day_number)
);

-- ---------------- Auction Items (Velampata) ----------------
CREATE TABLE IF NOT EXISTS auction_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
    FOREIGN KEY (festival_year_id) REFERENCES festival_years(id) ON DELETE SET NULL,
    FOREIGN KEY (recorded_by_id) REFERENCES members(id) ON DELETE SET NULL
);

-- ---------------- Loans (post-festival micro-lending, President only) ----------------
CREATE TABLE IF NOT EXISTS loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
    FOREIGN KEY (festival_year_id) REFERENCES festival_years(id) ON DELETE SET NULL,
    FOREIGN KEY (recorded_by_id) REFERENCES members(id) ON DELETE SET NULL
);

-- ---------------- Sponsorship Categories (President-managed master data) ----------------
CREATE TABLE IF NOT EXISTS sponsorship_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (created_by_id) REFERENCES members(id) ON DELETE SET NULL
);

-- ---------------- General Sponsors ----------------
CREATE TABLE IF NOT EXISTS general_sponsors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sponsor_name VARCHAR(255) NOT NULL,
    contact_info VARCHAR(15),
    contribution_amount DECIMAL(12,2),
    contribution_details VARCHAR(500),
    category_id BIGINT NOT NULL,
    festival_year_id BIGINT,
    recorded_by_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (category_id) REFERENCES sponsorship_categories(id),
    FOREIGN KEY (festival_year_id) REFERENCES festival_years(id) ON DELETE SET NULL,
    FOREIGN KEY (recorded_by_id) REFERENCES members(id) ON DELETE SET NULL,
    INDEX idx_general_sponsor_category (category_id)
);

-- ---------------- Annadanam Sponsors (dedicated day-wise food sponsorship tracking) ----------------
CREATE TABLE IF NOT EXISTS annadanam_sponsors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
    FOREIGN KEY (festival_year_id) REFERENCES festival_years(id) ON DELETE SET NULL,
    FOREIGN KEY (recorded_by_id) REFERENCES members(id) ON DELETE SET NULL,
    INDEX idx_annadanam_day (festival_year_id, day_number)
);

-- ---------------- Loan Repayments (reducing-balance interest method) ----------------
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

-- ---------------- Seed: first committee login ----------------
-- username: admin / password: Admin@123  (BCrypt hash below)
-- Change this password immediately after first login in production.
INSERT INTO members (name, phone, email, role, username, password, active, created_at)
VALUES ('Committee Admin', '9999999999', 'admin@ganeshutsav.local', 'PRESIDENT',
        'admin', '$2b$10$YbE.G3AItuXu8/QpkapkVON6E98wuTyXE6edXlDAe4emB0oakx0UO',
        TRUE, NOW())
ON DUPLICATE KEY UPDATE username = username;
