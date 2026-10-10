# Threat Trace — Setup & Live Demonstration Guide

This guide provides step-by-step instructions for installing, configuring, running, and demonstrating the **Threat Trace** cybersecurity forensic platform.

---

## 1. System Requirements & Prerequisites

Verify the following software is installed on your operating system:

| Component | Minimum Version | Command to Check Version |
| :--- | :--- | :--- |
| **Java Development Kit (JDK)** | Java 17 LTS | `java -version` |
| **MySQL Community Server** | MySQL 8.0+ | `mysql --version` |
| **Apache Maven** | Maven 3.8+ (or `./mvnw`) | `mvn -v` |
| **C++ Compiler (G++ / MSVC)** | C++17 | `g++ --version` |
| **Python** | Python 3.10+ | `python --version` |

---

## 2. Database Setup Instructions (MySQL)

### Step 1: Start MySQL Server
Ensure your local MySQL service is running on port `3306`. Default credentials configured in `application.properties`:
- **Host:** `localhost:3306`
- **Username:** `root`
- **Password:** `root`

### Step 2: Initialize Database Schema & Seed Data
Open terminal or MySQL Workbench and run:
```bash
# Execute DDL Schema Creation (15 Tables)
mysql -u root -p < src/main/resources/db/schema.sql

# Execute Seed Data Import
mysql -u root -p threat_trace_db < src/main/resources/db/seed.sql
```

---

## 3. Running the Main Java Application

### Step 1: Build Application with Maven
Navigate to the root project directory `d:\apdbmscapstone\` and run:
```bash
mvn clean package -DskipTests
```

### Step 2: Launch Spring Boot Server
```bash
mvn spring-boot:run
```
Alternatively, launch the compiled JAR file:
```bash
java -jar target/threat-trace-1.0.0.jar
```

The server will start on **`http://localhost:8080`**.

### Step 3: Access the Web Dashboard
Open any modern web browser (Chrome / Edge / Firefox) and navigate to:
👉 **`http://localhost:8080`**

---

## 4. Running Syllabus Concept Demos

### 4.1 Running the C++ Pointers & RAII Demo
Navigate to the C++ concept demo folder:
```bash
cd concept-demos/cpp

# Compile using Makefile (or g++)
make

# Run C++ Concept Demonstration
./pointer_tree_demo
```

### 4.2 Running the Python Synthetic Log Generator Script
Navigate to the Python script folder:
```bash
cd concept-demos/python
python log_generator.py
```
This produces sample test files in `sample-data/`:
- `01_normal_events.csv`
- `02_suspicious_attack_chain.csv`
- `03_malformed_missing_parents.csv`
- `04_cyclic_parent_references.csv`
- `05_sample_logs.json`

---

## 5. Step-by-Step Live Demonstration Script

Follow this 5-minute presentation script for demonstration or viva evaluation:

1. **Open Dashboard (`http://localhost:8080`)**
   - Point out the active case selector showing `CASE-2026-001` and KPI summary cards (Total Events, Risk Score, Lead Threat Actor Match, Kill Chain Stages).
2. **Explore Event Timeline Tab**
   - Demonstrate multi-parameter filtering: Filter by Event Type `PROCESS` or Severity `CRITICAL`.
   - Show how ISO-8601 timestamps and process parameters are formatted.
3. **Inspect Process Lineage Tree Tab**
   - Click on the **Process Lineage Tree** tab.
   - Show the parent-child execution hierarchy reconstructed recursively from `parent_event_id` (`WINWORD.EXE` -> `cmd.exe` -> `powershell.exe` -> `rundll32.exe`).
4. **View Cyber Kill Chain & Attack DNA Tab**
   - Click **Cyber Kill Chain & DNA** tab.
   - Highlight the 7-stage pipeline visualizer showing 5 detected stages (Delivery, Exploitation, Installation, C2, Actions on Objectives).
   - Point out the Behavioral Attack DNA sequence and lead threat actor similarity match (`PHANTOM_SPIDER`).
5. **Review Explainable Risk Breakdown Tab**
   - Click **Explainable Risk Breakdown** tab.
   - Walk through the deterministic risk formula: Asset Criticality (+12.5 pts) + Max Severity (+25.0 pts) + Kill Chain Progression (+37.5 pts) x Process Multiplier (1.25x) = **82.5 / 100.0**.
6. **Evaluate Mitigation Recommendations Tab**
   - Click **Mitigation Recommendations** tab.
   - Switch ranking strategies in the dropdown selector (`Maximum Risk Reduction` vs `Lowest Analyst Effort` vs `Minimal Business Impact`) and observe dynamic re-ranking.
7. **Add Analyst Notes & Export Report**
   - Click **Analyst Notes & Export** tab.
   - Add a custom investigator note, check the "Flag as False Positive" option if desired, and click **Add Note**.
   - Click **Export Text Report** or **Export JSON Report** and download the evidence handoff file.
