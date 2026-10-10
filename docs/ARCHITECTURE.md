# Threat Trace — System Architecture & Data Flow Specification

## 1. High-Level Architecture

Threat Trace is implemented using a modular **3-Tier Layered Architecture**:

```
 ┌────────────────────────────────────────────────────────┐
 │           1. PRESENTATION LAYER (Web SPA UI)           │
 │ - Vanilla HTML5 / CSS3 / ES6 JavaScript                │
 │ - Sleek Dark Cybersecurity Glassmorphism Interface    │
 │ - Interactive Process Lineage Tree Visualizer          │
 │ - Event Timeline Filters & Cyber Kill Chain Pipeline   │
 └──────────────────────────┬─────────────────────────────┘
                            │ REST API (JSON / HTTP)
 ┌──────────────────────────▼─────────────────────────────┐
 │         2. APPLICATION / BUSINESS LAYER (Java 17)      │
 │ - Spring Boot 3 REST Controllers & DTOs                │
 │ - Log Ingestion & Error Validation Service             │
 │ - Process Lineage Reconstruction (DFS + Cycle Guard)   │
 │ - Cyber Kill Chain Classifier Engine                   │
 │ - Explainable Risk Score Calculator                    │
 │ - Mitigation Ranking Advisor (Strategy Pattern)        │
 │ - Report Exporter (Text / JSON / CSV)                  │
 └──────────────────────────┬─────────────────────────────┘
                            │ JDBC / Spring Data JPA
 ┌──────────────────────────▼─────────────────────────────┐
 │         3. DATA STORAGE LAYER (MySQL 8.0+)            │
 │ - 15 Normalized Tables (3NF/BCNF)                      │
 │ - Foreign Key Constraints & Triggers                   │
 │ - Recursive CTE Queries for Process Ancestry Tree      │
 └────────────────────────────────────────────────────────┘

 ┌────────────────────────────────────────────────────────┐
 │              4. ISOLATED CONCEPT DEMOS                 │
 │ - C++ Pointer, Memory Allocation & RAII Demo           │
 │ - Python Synthetic Log Dataset Generator               │
 └────────────────────────────────────────────────────────┘
```

---

## 2. Component Responsibilities

### Presentation Layer (`src/main/resources/static/`)
- **`index.html`**: Single-Page Application (SPA) layout housing the dashboard, KPI metric cards, event timeline, process lineage visualizer, kill chain pipeline, risk breakdown panel, mitigation strategy selector, analyst notes, and report export triggers.
- **`css/styles.css`**: Modern dark-mode cybersecurity theme featuring glassmorphism containers, JetBrains Mono typography, glowing status badges, and responsive CSS grid/flexbox layouts.
- **`js/app.js`**: Client-side controller handling REST API communication (`fetch`), rendering interactive nested process tree nodes, dynamic table filtering, modal state management, and report downloads.

### Application Layer (`src/main/java/com/threattrace/`)
- **Controllers (`controller/`)**:
  - `CaseController`: REST endpoints for creating and viewing investigation cases.
  - `EventController`: File upload ingestion (`/import/csv`, `/import/json`), filtered timeline retrieval, and process tree building.
  - `AnalysisController`: Triggers Kill Chain mapping, Attack DNA summary, risk calculation, and mitigation ranking.
  - `ReportController`: Generates and serves exported investigation report files.
- **Services (`service/`)**:
  - `EventValidationService`: Validates mandatory fields, parses ISO-8601 timestamps, checks duplicate event codes, and verifies parent references.
  - `EventImportService`: Parses CSV and JSON log files, correlates events into cases, and persists valid records.
  - `ProcessLineageBuilder`: Executes Depth-First Search (DFS) recursive tree traversal with depth guards and cycle detection algorithms.
  - `KillChainMapper`: Applies rule-based stage classification and records evidence explanations.
  - `AttackDNAAnalyzer`: Extracts behavioral feature vectors and computes similarity against reference threat actor profiles.
  - `RiskCalculator`: Evaluates deterministic, explainable risk score formula and returns factor breakdown.
  - `MitigationAdvisor`: Evaluates mitigation catalog strategies using the **Strategy Pattern** (`RiskReductionStrategy`, `LowEffortStrategy`, `BusinessImpactStrategy`).
  - `ReportService`: Exports comprehensive case reports in TEXT, JSON, or CSV formats.
  - `AnalystNoteService`: Records investigator annotations, false-positive tags, and audit trails.

### Data Storage Layer (`src/main/resources/db/`)
- **`schema.sql`**: MySQL DDL defining 15 normalized tables, foreign keys, unique constraints, and indexes.
- **`seed.sql`**: Default seed data for users, assets, devices, kill chain stages, attack patterns, mitigations, and incident cases.
- **`recursive_queries.sql`**: SQL Recursive CTE queries for server-side process lineage tree generation.

---

## 3. Core Data Flow Workflows

### 3.1 Log Ingestion & Validation Workflow
```
[User Uploads CSV/JSON] ──> [EventController] ──> [EventImportService]
                                                        │
                                                        ▼
                                           [EventValidationService]
                                           - Validate required fields
                                           - Parse ISO-8601 Timestamp
                                           - Check Batch & DB Duplicates
                                                        │
                                    ┌───────────────────┴───────────────────┐
                                    ▼                                       ▼
                             [Valid Records]                        [Malformed Records]
                                    │                                       │
                         [Save to MySQL DB]                    [Collect Error Messages]
                                    │                                       │
                                    └───────────────────┬───────────────────┘
                                                        ▼
                                             [Return ImportResultDTO]
```

### 3.2 Process Lineage Reconstruction Workflow
```
[Client Requests Tree] ──> [ProcessLineageBuilder]
                                  │
                                  ▼
                   [Find Root Events (parent_event_id IS NULL)]
                                  │
                                  ▼
                 [Recursive DFS Traversal (buildTreeNodeRecursive)]
                 - Add event ID to visitedInPath stack
                 - IF depth > 25 OR ID in stack ──> [Throw CycleDetectedException]
                 - Fetch child events where parent_event_id = current.id
                 - Recurse for each child node
                                  │
                                  ▼
                     [Return Tree DTO to UI]
```

### 3.3 Explainable Risk Calculation Workflow
```
[Trigger Risk Evaluation] ──> [RiskCalculator]
                                    │
                                    ▼
                [1. Calculate Max Asset Criticality (Score x 2.5)]
                [2. Determine Max Event Severity Weight (Up to 25.0)]
                [3. Count Observed Kill Chain Stages (Count x 7.5)]
                [4. Check Suspicious Process Ancestry (x1.25 Multiplier)]
                                    │
                                    ▼
                 [Raw Score = (Asset + Sev + KillChain) * Multiplier]
                 [Final Score = Min(100.0, Raw Score)]
                                    │
                                    ▼
                   [Persist RiskAssessment & Update Case]
```
