-- Emergency Communication System Database Schema
-- Run this SQL script to create all necessary tables for the Emergency Communication System modules

-- Notification Logs Table (for Mass Notification System and Audit Trail)
CREATE TABLE IF NOT EXISTS notification_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    channel VARCHAR(50) NOT NULL COMMENT 'sms, email, pa',
    message TEXT NOT NULL,
    recipient VARCHAR(255) DEFAULT NULL,
    recipients TEXT DEFAULT NULL COMMENT 'Comma-separated list of recipients',
    priority VARCHAR(20) DEFAULT 'medium' COMMENT 'low, medium, high, critical',
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending, success, failed',
    sent_at DATETIME NOT NULL,
    sent_by VARCHAR(100) DEFAULT 'system',
    ip_address VARCHAR(45) DEFAULT NULL,
    response TEXT DEFAULT NULL,
    error_message TEXT DEFAULT NULL,
    INDEX idx_channel (channel),
    INDEX idx_status (status),
    INDEX idx_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Alert Categories Table (for Alert Categorization)
CREATE TABLE IF NOT EXISTS alert_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    icon VARCHAR(50) DEFAULT 'fa-exclamation-triangle',
    description TEXT DEFAULT NULL,
    color VARCHAR(7) DEFAULT '#4c8a89',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Alerts Table (if not exists)
CREATE TABLE IF NOT EXISTS alerts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT DEFAULT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    content TEXT DEFAULT NULL,
    status VARCHAR(20) DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES alert_categories(id) ON DELETE SET NULL,
    INDEX idx_category (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Conversations Table (for Two-Way Communication)
CREATE TABLE IF NOT EXISTS conversations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active, closed, archived',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Messages Table (for Two-Way Communication)
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    conversation_id INT NOT NULL,
    message TEXT NOT NULL,
    sender_type VARCHAR(20) NOT NULL COMMENT 'admin, citizen, system',
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    read_at DATETIME DEFAULT NULL,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    INDEX idx_conversation (conversation_id),
    INDEX idx_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Integration Settings Table (for Automated Warning Integration)
CREATE TABLE IF NOT EXISTS integration_settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    source VARCHAR(50) NOT NULL UNIQUE COMMENT 'pagasa, phivolcs',
    enabled TINYINT(1) DEFAULT 0,
    api_key VARCHAR(255) DEFAULT NULL,
    api_url VARCHAR(255) DEFAULT NULL,
    last_sync DATETIME DEFAULT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_source (source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Warning Settings Table (for Automated Warning Integration)
CREATE TABLE IF NOT EXISTS warning_settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sync_interval INT DEFAULT 15 COMMENT 'Minutes',
    auto_publish TINYINT(1) DEFAULT 0,
    notification_channels TEXT DEFAULT NULL COMMENT 'Comma-separated: sms,email,pa',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Automated Warnings Table (for Automated Warning Integration)
CREATE TABLE IF NOT EXISTS automated_warnings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    source VARCHAR(50) NOT NULL COMMENT 'pagasa, phivolcs',
    type VARCHAR(100) DEFAULT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    severity VARCHAR(20) DEFAULT 'medium' COMMENT 'low, medium, high, critical',
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending, published, archived',
    received_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    published_at DATETIME DEFAULT NULL,
    INDEX idx_source (source),
    INDEX idx_status (status),
    INDEX idx_received_at (received_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Alert Translations Table (for Multilingual Support)
CREATE TABLE IF NOT EXISTS alert_translations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    alert_id INT NOT NULL,
    target_language VARCHAR(10) NOT NULL COMMENT 'en, tl, ceb, etc.',
    translated_title VARCHAR(255) NOT NULL,
    translated_content TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'active',
    translated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (alert_id) REFERENCES alerts(id) ON DELETE CASCADE,
    UNIQUE KEY unique_alert_language (alert_id, target_language),
    INDEX idx_language (target_language),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Subscriptions Table (for Citizen Subscription and Alert Preferences)
CREATE TABLE IF NOT EXISTS subscriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    categories TEXT DEFAULT NULL COMMENT 'Comma-separated: weather,earthquake,bomb,fire,general',
    channels TEXT DEFAULT NULL COMMENT 'Comma-separated: sms,email,push',
    preferred_language VARCHAR(10) DEFAULT 'en',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active, inactive, suspended',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default alert categories
INSERT INTO alert_categories (name, icon, description, color) VALUES
('Weather', 'fa-cloud-rain', 'Weather-related alerts including storms, floods, and typhoons', '#3498db'),
('Earthquake', 'fa-mountain', 'Seismic activity and earthquake warnings', '#e74c3c'),
('Bomb Threat', 'fa-bomb', 'Security threats and bomb alerts', '#c0392b'),
('Fire', 'fa-fire', 'Fire emergencies and fire safety alerts', '#e67e22'),
('General', 'fa-exclamation-triangle', 'General emergency alerts and announcements', '#95a5a6')
ON DUPLICATE KEY UPDATE name=name;

-- Insert default integration settings
INSERT INTO integration_settings (source, enabled) VALUES
('pagasa', 0),
('phivolcs', 0)
ON DUPLICATE KEY UPDATE source=source;

-- Insert default warning settings
INSERT INTO warning_settings (sync_interval, auto_publish, notification_channels) VALUES
(15, 0, 'sms,email')
ON DUPLICATE KEY UPDATE id=id;

