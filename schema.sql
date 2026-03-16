-- ─────────────────────────────────────────────────────────────────────────────
-- SpendIQ Pro — MySQL Schema
-- Run this once before starting the application (or let JPA auto-create it)
-- ─────────────────────────────────────────────────────────────────────────────

CREATE DATABASE IF NOT EXISTS spendiq_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE spendiq_db;

-- ── Users ─────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(150)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    avatar      VARCHAR(5)    NOT NULL,
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP
);

-- ── Expenses ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS expenses (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    title       VARCHAR(200)  NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,
    date        DATE          NOT NULL,
    category    ENUM('Food','Entertainment','Utilities','Health','Transport','Shopping','Income','Other') NOT NULL,
    type        ENUM('expense','income') NOT NULL,
    recurring   TINYINT(1)    NOT NULL DEFAULT 0,
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_expenses_user_date (user_id, date),
    INDEX idx_expenses_user_type (user_id, type)
);

-- ── Group Splits ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS group_splits (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(200)  NOT NULL,
    total_amount    DECIMAL(12,2) NOT NULL,
    per_head        DECIMAL(12,2) NOT NULL,
    date            DATE          NOT NULL,
    note            VARCHAR(500),
    paid_by_user_id BIGINT        NOT NULL,
    qr_image_path   VARCHAR(500),
    created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (paid_by_user_id) REFERENCES users(id)
);

-- ── Group Split Members (many-to-many) ────────────────────────────────────────
CREATE TABLE IF NOT EXISTS group_split_members (
    split_id    BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    PRIMARY KEY (split_id, user_id),
    FOREIGN KEY (split_id) REFERENCES group_splits(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)  REFERENCES users(id) ON DELETE CASCADE
);

-- ── Split Owes ────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS split_owes (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    split_id    BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,
    paid        TINYINT(1)    NOT NULL DEFAULT 0,
    paid_at     DATETIME,
    FOREIGN KEY (split_id) REFERENCES group_splits(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)  REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_split_user (split_id, user_id)
);

-- ── Notifications ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS notifications (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    split_id     BIGINT       NOT NULL,
    message      VARCHAR(500) NOT NULL,
    dismissed    TINYINT(1)   NOT NULL DEFAULT 0,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    dismissed_at DATETIME,
    FOREIGN KEY (user_id)  REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (split_id) REFERENCES group_splits(id) ON DELETE CASCADE,
    INDEX idx_notif_user_dismissed (user_id, dismissed)
);

-- ── Seed Demo Users ───────────────────────────────────────────────────────────
-- Passwords are BCrypt hashes of: arjun123, priya123, rahul123
INSERT IGNORE INTO users (name, email, password, avatar) VALUES
('Arjun Sharma', 'arjun@email.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'AS'),
('Priya Patel',  'priya@email.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'PP'),
('Rahul Mehta',  'rahul@email.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'RM');
-- NOTE: The hash above is for 'password' — replace with real BCrypt hashes for actual passwords
