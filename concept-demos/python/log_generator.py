"""
Threat Trace - Synthetic Log Generator Script
Generates sample CSV and JSON security logs for testing validation, 
timeline reconstruction, process lineage, and edge cases.
"""

import csv
import json
import os

def create_sample_datasets():
    output_dir = os.path.join(os.path.dirname(__file__), "../../sample-data")
    os.makedirs(output_dir, exist_ok=True)

    # Dataset 1: Normal Events CSV
    normal_events = [
        ["event_code", "event_time", "event_type", "severity", "hostname", "user_name", "process_name", "parent_event_id", "target", "description"],
        ["EVT-NORM-01", "2026-10-09 08:00:00", "LOGIN", "LOW", "fin-desk-04.corp.internal", "bob_finance", "explorer.exe", "", "fin-desk-04", "User logon initiated"],
        ["EVT-NORM-02", "2026-10-09 08:05:10", "PROCESS", "LOW", "fin-desk-04.corp.internal", "bob_finance", "chrome.exe", "EVT-NORM-01", "https://portal.corp.internal", "Browser opened internal intranet"],
        ["EVT-NORM-03", "2026-10-09 08:12:00", "PROCESS", "LOW", "fin-desk-04.corp.internal", "bob_finance", "excel.exe", "EVT-NORM-01", "Q3_Report.xlsx", "User opened spreadsheet"]
    ]
    with open(os.path.join(output_dir, "01_normal_events.csv"), "w", newline="", encoding="utf-8") as f:
        csv.writer(f).writerows(normal_events)

    # Dataset 2: Suspicious Attack Chain CSV
    attack_chain = [
        ["event_code", "event_time", "event_type", "severity", "hostname", "user_name", "process_name", "parent_event_id", "target", "description", "raw_payload"],
        ["EVT-ATTACK-01", "2026-10-09 09:15:00", "LOGIN", "LOW", "fin-desk-04.corp.internal", "alice_finance", "explorer.exe", "", "WKSTN-FIN-04", "User session logon", "LogonType 2"],
        ["EVT-ATTACK-02", "2026-10-09 09:16:30", "PROCESS", "LOW", "alice_finance", "WINWORD.EXE", "EVT-ATTACK-01", "Invoice_Q3.docm", "Word document opened", "WINWORD.EXE Invoice_Q3.docm"],
        ["EVT-ATTACK-03", "2026-10-09 09:16:32", "PROCESS", "HIGH", "alice_finance", "cmd.exe", "EVT-ATTACK-02", "cmd.exe /c powershell.exe", "Word spawned cmd shell", "cmd.exe /c powershell.exe -e aW52b2tl..."],
        ["EVT-ATTACK-04", "2026-10-09 09:16:35", "PROCESS", "CRITICAL", "alice_finance", "powershell.exe", "EVT-ATTACK-03", "powershell.exe -EncodedCommand", "Encoded PowerShell executed", "powershell.exe -e aW52b2tl..."],
        ["EVT-ATTACK-05", "2026-10-09 09:17:01", "NETWORK", "CRITICAL", "alice_finance", "powershell.exe", "EVT-ATTACK-04", "185.220.101.5:4444", "Outbound C2 connection", "TCP 172.16.10.45:49152 -> 185.220.101.5:4444"],
        ["EVT-ATTACK-06", "2026-10-09 09:18:20", "FILE", "HIGH", "SYSTEM", "rundll32.exe", "EVT-ATTACK-04", "C:\\Windows\\Temp\\lsass.dmp", "LSASS dump file created", "rundll32 comsvcs.dll, MiniDump 672 C:\\Windows\\Temp\\lsass.dmp"]
    ]
    with open(os.path.join(output_dir, "02_suspicious_attack_chain.csv"), "w", newline="", encoding="utf-8") as f:
        csv.writer(f).writerows(attack_chain)

    # Dataset 3: Malformed / Missing Parents CSV
    malformed = [
        ["event_code", "event_time", "event_type", "severity", "hostname", "user_name", "process_name", "parent_event_id", "target", "description"],
        ["EVT-BAD-01", "INVALID_DATE_TIME", "PROCESS", "LOW", "fin-desk-04.corp.internal", "alice", "notepad.exe", "", "test.txt", "Invalid timestamp test"],
        ["EVT-BAD-02", "2026-10-09 10:00:00", "PROCESS", "HIGH", "fin-desk-04.corp.internal", "alice", "cmd.exe", "EVT-NON-EXISTENT-999", "dir", "Missing parent ID reference test"],
        ["", "2026-10-09 10:05:00", "LOGIN", "LOW", "", "alice", "explorer.exe", "", "", "Missing mandatory event_code & hostname"]
    ]
    with open(os.path.join(output_dir, "03_malformed_missing_parents.csv"), "w", newline="", encoding="utf-8") as f:
        csv.writer(f).writerows(malformed)

    # Dataset 4: Cyclic Parent References CSV
    cyclic = [
        ["event_code", "event_time", "event_type", "severity", "hostname", "user_name", "process_name", "parent_event_id", "target", "description"],
        ["EVT-CYCLE-01", "2026-10-09 11:00:00", "PROCESS", "HIGH", "dc01.corp.internal", "admin", "processA.exe", "EVT-CYCLE-02", "targetA", "Cycle Node 1 (points to Node 2)"],
        ["EVT-CYCLE-02", "2026-10-09 11:00:05", "PROCESS", "HIGH", "dc01.corp.internal", "admin", "processB.exe", "EVT-CYCLE-01", "targetB", "Cycle Node 2 (points to Node 1)"]
    ]
    with open(os.path.join(output_dir, "04_cyclic_parent_references.csv"), "w", newline="", encoding="utf-8") as f:
        csv.writer(f).writerows(cyclic)

    # Dataset 5: JSON Format Sample Logs
    json_logs = [
        {
            "event_code": "EVT-JSON-01",
            "event_time": "2026-10-09T12:00:00.000Z",
            "event_type": "LOGIN",
            "severity": "LOW",
            "hostname": "db-sql-01.corp.internal",
            "user_name": "db_admin",
            "process_name": "sshd",
            "parent_event_id": None,
            "target": "db-sql-01",
            "description": "SSH Remote Admin Login"
        },
        {
            "event_code": "EVT-JSON-02",
            "event_time": "2026-10-09T12:01:15.000Z",
            "event_type": "PROCESS",
            "severity": "HIGH",
            "hostname": "db-sql-01.corp.internal",
            "user_name": "db_admin",
            "process_name": "mysqldump",
            "parent_event_id": "EVT-JSON-01",
            "target": "/tmp/customer_db_backup.sql",
            "description": "Full database dump initiated"
        }
    ]
    with open(os.path.join(output_dir, "05_sample_logs.json"), "w", encoding="utf-8") as f:
        json.dump(json_logs, f, indent=2)

    print("Successfully generated all 5 sample datasets in sample-data/")

if __name__ == "__main__":
    create_sample_datasets()

