-- ============================================================================
-- Threat Trace: Defensive Cyber Forensics Database Schema
-- Database Target: MySQL 8.0+
-- Description: 15 Normalized tables (3NF/BCNF) with foreign key constraints,
--              indexes, and recursive process-ancestry support.
-- ============================================================================

CREATE DATABASE IF NOT EXISTS threat_trace_db;
USE threat_trace_db;

-- Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS analyst_notes;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS case_mitigations;
DROP TABLE IF EXISTS mitigation_strategies;
DROP TABLE IF EXISTS risk_assessments;
DROP TABLE IF EXISTS threat_actor_profiles;
DROP TABLE IF EXISTS attack_patterns;
DROP TABLE IF EXISTS event_stage_mappings;
DROP TABLE IF EXISTS kill_chain_stages;
DROP TABLE IF EXISTS event_logs;
DROP TABLE IF EXISTS investigation_cases;
DROP TABLE IF EXISTS endpoint_devices;
DROP TABLE IF EXISTS assets;
DROP TABLE IF EXISTS users;

-- ----------------------------------------------------------------------------
-- 1. USERS: Analyst identity, authentication, and role RBAC
-- ----------------------------------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'ANALYST',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 2. ASSETS: Critical infrastructure servers, workstations & criticality rating
-- ----------------------------------------------------------------------------
CREATE TABLE assets (
    asset_id INT AUTO_INCREMENT PRIMARY KEY,
    asset_name VARCHAR(100) NOT NULL,
    asset_type VARCHAR(50) NOT NULL, -- e.g., DOMAIN_CONTROLLER, DB_SERVER, WORKSTATION
    criticality_score INT NOT NULL CHECK (criticality_score BETWEEN 1 AND 10),
    owner_team VARCHAR(100) NOT NULL,
    ip_subnet VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 3. ENDPOINT_DEVICES: Hostnames & IP addresses bound to physical/virtual assets
-- ----------------------------------------------------------------------------
CREATE TABLE endpoint_devices (
    device_id INT AUTO_INCREMENT PRIMARY KEY,
    asset_id INT NOT NULL,
    hostname VARCHAR(100) NOT NULL UNIQUE,
    ip_address VARCHAR(45) NOT NULL,
    os_version VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (asset_id) REFERENCES assets(asset_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 4. INVESTIGATION_CASES: Incident cases managed by analysts
-- ----------------------------------------------------------------------------
CREATE TABLE investigation_cases (
    case_id INT AUTO_INCREMENT PRIMARY KEY,
    case_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, CLOSED
    severity_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, CRITICAL
    risk_score DOUBLE PRECISION DEFAULT 0.0,
    assigned_analyst_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_analyst_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 5. EVENT_LOGS: Endpoint security event logs & parent process lineage reference
-- ----------------------------------------------------------------------------
CREATE TABLE event_logs (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    event_code VARCHAR(64) UNIQUE, -- imported unique identifier e.g., EVT-1001
    case_id INT NOT NULL,
    device_id INT NOT NULL,
    event_time DATETIME NOT NULL,
    event_type VARCHAR(50) NOT NULL, -- LOGIN, PROCESS, FILE, NETWORK, USB
    severity VARCHAR(20) NOT NULL DEFAULT 'LOW', -- LOW, MEDIUM, HIGH, CRITICAL
    user_name VARCHAR(100),
    process_name VARCHAR(255),
    parent_event_id INT NULL, -- Self-referencing FK for process lineage tree
    target VARCHAR(255),
    source VARCHAR(255),
    description TEXT,
    raw_payload TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id) REFERENCES investigation_cases(case_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES endpoint_devices(device_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_event_id) REFERENCES event_logs(event_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_events_case_time ON event_logs(case_id, event_time);
CREATE INDEX idx_events_type ON event_logs(event_type);
CREATE INDEX idx_events_parent ON event_logs(parent_event_id);

-- ----------------------------------------------------------------------------
-- 6. KILL_CHAIN_STAGES: Standard Cyber Kill Chain stages
-- ----------------------------------------------------------------------------
CREATE TABLE kill_chain_stages (
    stage_id INT AUTO_INCREMENT PRIMARY KEY,
    stage_name VARCHAR(50) NOT NULL UNIQUE,
    stage_order INT NOT NULL UNIQUE,
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 7. EVENT_STAGE_MAPPINGS: Mapped Cyber Kill Chain stages per event
-- ----------------------------------------------------------------------------
CREATE TABLE event_stage_mappings (
    mapping_id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    stage_id INT NOT NULL,
    mapping_rule VARCHAR(100) NOT NULL,
    explanation TEXT NOT NULL,
    mapped_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES event_logs(event_id) ON DELETE CASCADE,
    FOREIGN KEY (stage_id) REFERENCES kill_chain_stages(stage_id) ON DELETE CASCADE,
    CONSTRAINT idx_event_stage UNIQUE (event_id, stage_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 8. ATTACK_PATTERNS: Detection signatures and rules for scoring
-- ----------------------------------------------------------------------------
CREATE TABLE attack_patterns (
    pattern_id INT AUTO_INCREMENT PRIMARY KEY,
    pattern_code VARCHAR(50) NOT NULL UNIQUE,
    pattern_name VARCHAR(100) NOT NULL,
    rule_expression TEXT NOT NULL,
    stage_id INT NOT NULL,
    severity_weight DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    FOREIGN KEY (stage_id) REFERENCES kill_chain_stages(stage_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 9. THREAT_ACTOR_PROFILES: Reference behavioral profiles (for lead similarity)
-- ----------------------------------------------------------------------------
CREATE TABLE threat_actor_profiles (
    profile_id INT AUTO_INCREMENT PRIMARY KEY,
    actor_codename VARCHAR(100) NOT NULL UNIQUE,
    tactics_summary TEXT NOT NULL,
    typical_kill_chain_signature VARCHAR(255) NOT NULL,
    risk_indicator_score INT NOT NULL DEFAULT 5
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 10. RISK_ASSESSMENTS: Explainable risk evaluation records
-- ----------------------------------------------------------------------------
CREATE TABLE risk_assessments (
    assessment_id INT AUTO_INCREMENT PRIMARY KEY,
    case_id INT NOT NULL,
    total_score DOUBLE PRECISION NOT NULL,
    assessment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    algorithm_version VARCHAR(20) NOT NULL DEFAULT 'v1.0.0',
    factor_breakdown_json TEXT NOT NULL,
    FOREIGN KEY (case_id) REFERENCES investigation_cases(case_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 11. MITIGATION_STRATEGIES: Master defensive mitigations catalog
-- ----------------------------------------------------------------------------
CREATE TABLE mitigation_strategies (
    mitigation_id INT AUTO_INCREMENT PRIMARY KEY,
    mitigation_code VARCHAR(50) NOT NULL UNIQUE,
    action_name VARCHAR(150) NOT NULL,
    effort_level VARCHAR(20) NOT NULL, -- LOW, MEDIUM, HIGH
    business_impact VARCHAR(20) NOT NULL, -- LOW, MEDIUM, HIGH
    category VARCHAR(50) NOT NULL, -- NETWORK, PROCESS, ACCOUNT, POLICY
    description TEXT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 12. CASE_MITIGATIONS: Case-specific mitigation recommendations & state
-- ----------------------------------------------------------------------------
CREATE TABLE case_mitigations (
    case_mitigation_id INT AUTO_INCREMENT PRIMARY KEY,
    case_id INT NOT NULL,
    mitigation_id INT NOT NULL,
    recommendation_score DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RECOMMENDED', -- RECOMMENDED, ACCEPTED, DISMISSED, APPLIED
    analyst_rationale TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id) REFERENCES investigation_cases(case_id) ON DELETE CASCADE,
    FOREIGN KEY (mitigation_id) REFERENCES mitigation_strategies(mitigation_id) ON DELETE CASCADE,
    CONSTRAINT idx_case_mitigation UNIQUE (case_id, mitigation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 13. REPORTS: Exported investigation case reports
-- ----------------------------------------------------------------------------
CREATE TABLE reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    case_id INT NOT NULL,
    generated_by INT NOT NULL,
    report_format VARCHAR(20) NOT NULL, -- JSON, CSV, TEXT
    file_path VARCHAR(255) NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id) REFERENCES investigation_cases(case_id) ON DELETE CASCADE,
    FOREIGN KEY (generated_by) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 14. ANALYST_NOTES: Case-linked investigator annotations & false-positive tags
-- ----------------------------------------------------------------------------
CREATE TABLE analyst_notes (
    note_id INT AUTO_INCREMENT PRIMARY KEY,
    case_id INT NOT NULL,
    analyst_id INT NOT NULL,
    note_text TEXT NOT NULL,
    is_false_positive_flag BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id) REFERENCES investigation_cases(case_id) ON DELETE CASCADE,
    FOREIGN KEY (analyst_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 15. AUDIT_LOGS: System audit trail of investigator actions
-- ----------------------------------------------------------------------------
CREATE TABLE audit_logs (
    audit_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action_type VARCHAR(50) NOT NULL,
    entity_affected VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
