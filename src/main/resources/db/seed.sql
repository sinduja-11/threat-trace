-- ============================================================================
-- Threat Trace: MySQL Seed Data Script
-- Description: Standard seed data for Kill Chain stages, initial analysts,
--              assets, devices, mitigations, patterns, and sample incident cases.
-- ============================================================================

USE threat_trace_db;

-- Disable Foreign Key checks temporarily for clean insertion
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE analyst_notes;
TRUNCATE TABLE reports;
TRUNCATE TABLE case_mitigations;
TRUNCATE TABLE mitigation_strategies;
TRUNCATE TABLE risk_assessments;
TRUNCATE TABLE threat_actor_profiles;
TRUNCATE TABLE attack_patterns;
TRUNCATE TABLE event_stage_mappings;
TRUNCATE TABLE kill_chain_stages;
TRUNCATE TABLE event_logs;
TRUNCATE TABLE investigation_cases;
TRUNCATE TABLE endpoint_devices;
TRUNCATE TABLE assets;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. USERS
INSERT INTO users (user_id, username, password_hash, full_name, email, role) VALUES
(1, 'admin_sec', '$2a$10$wE8w.6uY3H0Kx4W4a...hash', 'Lead Analyst Sarah', 'sarah.analyst@threattrace.org', 'ADMIN'),
(2, 'johndoe', '$2a$10$xL9x.7vZ4I1Ly5X5b...hash', 'John Doe', 'john.doe@threattrace.org', 'ANALYST');

-- 2. ASSETS
INSERT INTO assets (asset_id, asset_name, asset_type, criticality_score, owner_team, ip_subnet) VALUES
(1, 'DC-CORP-01', 'DOMAIN_CONTROLLER', 10, 'IT Infrastructure Security', '192.168.1.0/24'),
(2, 'DB-PROD-SQL', 'DATABASE_SERVER', 9, 'Database Administration', '10.0.5.0/24'),
(3, 'WKSTN-FIN-04', 'WORKSTATION', 5, 'Finance Division', '172.16.10.0/24');

-- 3. ENDPOINT_DEVICES
INSERT INTO endpoint_devices (device_id, asset_id, hostname, ip_address, os_version, status) VALUES
(1, 1, 'dc01.corp.internal', '192.168.1.10', 'Windows Server 2022', 'ACTIVE'),
(2, 2, 'db-sql-01.corp.internal', '10.0.5.50', 'Ubuntu 22.04 LTS', 'ACTIVE'),
(3, 3, 'fin-desk-04.corp.internal', '172.16.10.45', 'Windows 11 Enterprise', 'ACTIVE');

-- 4. KILL_CHAIN_STAGES
INSERT INTO kill_chain_stages (stage_id, stage_name, stage_order, description) VALUES
(1, 'Reconnaissance', 1, 'Research, identification, and selection of targets.'),
(2, 'Weaponization', 2, 'Coupling executable payload with exploit into a deliverable asset.'),
(3, 'Delivery', 3, 'Transmission of payload to the targeted environment.'),
(4, 'Exploitation', 4, 'Triggering malicious code execution on target system.'),
(5, 'Installation', 5, 'Installing malware / establishing persistence on asset.'),
(6, 'Command & Control (C2)', 6, 'Establishing covert communication channel with attacker.'),
(7, 'Actions on Objectives', 7, 'Exfiltrating data, lateral movement, or causing operational impact.');

-- 5. ATTACK_PATTERNS
INSERT INTO attack_patterns (pattern_id, pattern_code, pattern_name, rule_expression, stage_id, severity_weight) VALUES
(1, 'PAT-001', 'Phishing Attachment Launch', 'event_type = PROCESS AND process_name IN (cmd.exe, powershell.exe) AND parent_process = WINWORD.EXE', 3, 2.5),
(2, 'PAT-002', 'Suspicious PowerShell Execution', 'process_name = powershell.exe AND raw_payload LIKE %-EncodedCommand%', 4, 3.0),
(3, 'PAT-003', 'Persistence Registry Run Key', 'target LIKE %SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run%', 5, 2.8),
(4, 'PAT-004', 'Covert Outbound Beaconing', 'event_type = NETWORK AND destination_port IN (4444, 8080, 8443) AND severity = HIGH', 6, 3.5),
(5, 'PAT-005', 'Privilege Escalation / LSASS Dump', 'process_name = rundll32.exe AND target LIKE %lsass.dmp%', 7, 4.0);

-- 6. THREAT_ACTOR_PROFILES
INSERT INTO threat_actor_profiles (profile_id, actor_codename, tactics_summary, typical_kill_chain_signature, risk_indicator_score) VALUES
(1, 'PHANTOM_SPIDER', 'Uses spear-phishing with malicious macro documents, encoded PowerShell, and C2 over port 4444.', 'Delivery -> Exploitation -> C2', 8),
(2, 'SHADOW_BEAR', 'Targeted enterprise lateral movement, credential dumping via LSASS, and RDP abuse.', 'Exploitation -> Installation -> Actions on Objectives', 9);

-- 7. MITIGATION_STRATEGIES
INSERT INTO mitigation_strategies (mitigation_id, mitigation_code, action_name, effort_level, business_impact, category, description) VALUES
(1, 'MIT-001', 'Isolate Endpoint Device from Network', 'LOW', 'MEDIUM', 'NETWORK', 'Immediately sever network connectivity of compromised host to contain lateral movement.'),
(2, 'MIT-002', 'Revoke Compromised User Session & Reset Password', 'LOW', 'LOW', 'ACCOUNT', 'Invalidate active auth tokens and force password reset for affected account.'),
(3, 'MIT-003', 'Block Outbound IP / Port on Firewall', 'LOW', 'LOW', 'NETWORK', 'Add firewall rules blocking communication to malicious C2 IP address.'),
(4, 'MIT-004', 'Kill Suspicious Parent & Child Process Tree', 'LOW', 'LOW', 'PROCESS', 'Terminate malicious powershell/cmd processes across affected device.'),
(5, 'MIT-005', 'Deploy EDR Rule for LSASS Memory Protection', 'MEDIUM', 'LOW', 'POLICY', 'Enable Credential Guard and block process memory access to lsass.exe.');

-- 8. INVESTIGATION_CASES
INSERT INTO investigation_cases (case_id, case_number, title, description, status, severity_level, risk_score, assigned_analyst_id) VALUES
(1, 'CASE-2026-001', 'Suspicious Macro Execution & C2 Connection on WKSTN-FIN-04', 'Investigation into phishing email attachment launching Powershell encoded payload.', 'OPEN', 'HIGH', 82.5, 1),
(2, 'CASE-2026-002', 'Routine Failed Admin Logins on DC-CORP-01', 'Multiple failed authentication events on Domain Controller.', 'CLOSED', 'LOW', 15.0, 2);

-- 9. SAMPLE EVENT LOGS (Case 1 Incident Timeline with Process Lineage)
INSERT INTO event_logs (event_id, event_code, case_id, device_id, event_time, event_type, severity, user_name, process_name, parent_event_id, target, source, description, raw_payload) VALUES
(1, 'EVT-1001', 1, 3, '2026-10-09 09:15:00', 'LOGIN', 'LOW', 'alice_finance', 'explorer.exe', NULL, 'WKSTN-FIN-04', 'LOCAL', 'User session logon initiated', 'User session started normally.'),
(2, 'EVT-1002', 1, 3, '2026-10-09 09:16:30', 'PROCESS', 'LOW', 'alice_finance', 'WINWORD.EXE', 1, 'Invoice_Q3.docm', 'C:\\Users\\alice\\Downloads', 'Microsoft Word launched document', 'WINWORD.EXE C:\\Users\\alice\\Downloads\\Invoice_Q3.docm'),
(3, 'EVT-1003', 1, 3, '2026-10-09 09:16:32', 'PROCESS', 'HIGH', 'alice_finance', 'cmd.exe', 2, 'cmd.exe /c powershell.exe', 'WINWORD.EXE', 'Word macro launched Windows Command Prompt', 'cmd.exe /c powershell.exe -e aW52b2tl...'),
(4, 'EVT-1004', 1, 3, '2026-10-09 09:16:35', 'PROCESS', 'CRITICAL', 'alice_finance', 'powershell.exe', 3, 'powershell.exe -EncodedCommand', 'cmd.exe', 'PowerShell executed obfuscated script payload', 'powershell.exe -ExecutionPolicy Bypass -e aW52b2tlLWV4cHJlc3Npb24...'),
(5, 'EVT-1005', 1, 3, '2026-10-09 09:17:01', 'NETWORK', 'CRITICAL', 'alice_finance', 'powershell.exe', 4, '185.220.101.5:4444', '172.16.10.45:49152', 'Outbound connection established to suspicious IP on port 4444', 'TCP 172.16.10.45:49152 -> 185.220.101.5:4444 ESTABLISHED'),
(6, 'EVT-1006', 1, 3, '2026-10-09 09:18:20', 'FILE', 'HIGH', 'SYSTEM', 'rundll32.exe', 4, 'C:\\Windows\\Temp\\lsass.dmp', 'lsass.exe', 'LSASS memory dump file created in Temp directory', 'rundll32.exe comsvcs.dll, MiniDump 672 C:\\Windows\\Temp\\lsass.dmp full');

-- 10. EVENT_STAGE_MAPPINGS
INSERT INTO event_stage_mappings (event_id, stage_id, mapping_rule, explanation) VALUES
(2, 3, 'RUL-DEL-01', 'Phishing document opened by user (Delivery stage)'),
(3, 4, 'RUL-EXP-01', 'Macro launched shell execution (Exploitation stage)'),
(4, 5, 'RUL-INS-01', 'Encoded PowerShell execution for persistence/scripting (Installation stage)'),
(5, 6, 'RUL-C2-01', 'Outbound connection to untrusted IP on non-standard port 4444 (C2 stage)'),
(6, 7, 'RUL-ACT-01', 'Credential harvesting via LSASS dump (Actions on Objectives stage)');

-- 11. RISK_ASSESSMENT
INSERT INTO risk_assessments (assessment_id, case_id, total_score, algorithm_version, factor_breakdown_json) VALUES
(1, 1, 82.5, 'v1.0.0', '{"assetCriticality": 5.0, "maxSeverityWeight": 4.0, "killChainProgression": 7.0, "suspiciousChainMultiplier": 1.5, "eventCount": 6}');

-- 12. CASE_MITIGATIONS
INSERT INTO case_mitigations (case_id, mitigation_id, recommendation_score, status, analyst_rationale) VALUES
(1, 1, 95.0, 'RECOMMENDED', 'Host WKSTN-FIN-04 is actively communicating with C2 server.'),
(1, 4, 90.0, 'RECOMMENDED', 'Terminate powershell.exe (EVT-1004) and cmd.exe (EVT-1003).'),
(1, 3, 85.0, 'RECOMMENDED', 'Block C2 IP 185.220.101.5 at perimeter firewall.');

-- 13. ANALYST_NOTES
INSERT INTO analyst_notes (case_id, analyst_id, note_text, is_false_positive_flag) VALUES
(1, 1, 'Initial triage confirmed malicious VBA macro in Invoice_Q3.docm. Isolated host via MIT-001.', FALSE);
