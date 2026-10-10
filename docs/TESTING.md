# Threat Trace — Testing & Quality Assurance Documentation

This document describes the automated unit testing, edge case handling, and integration verification suite implemented for **Threat Trace**.

---

## 1. Test Suite Coverage Summary

| Test Class | Test Target | Key Edge Cases Verified |
| :--- | :--- | :--- |
| `EventValidationServiceTest` | Log validation engine | Invalid timestamps, missing mandatory fields, batch duplicate detection |
| `ProcessLineageBuilderTest` | Process tree builder | Root node discovery, parent-child linking, **cyclic parent reference detection** |
| `RiskCalculatorTest` | Risk scoring engine | Single event risk, multi-stage kill chain progression, 100.0 score capping |
| `MitigationAdvisorTest` | Strategy pattern | Strategy dynamic switching (Risk Reduction vs Low Effort vs Impact) |

---

## 2. Key Edge Cases & Safeguards

### 2.1 Cyclic Parent References
- **Risk:** Malformed or malicious log files containing circular `parent_event_id` references (e.g. Event A -> Event B -> Event A) can cause infinite loops or stack overflow during recursive tree traversal.
- **Safeguard:** `ProcessLineageBuilder` maintains a call-stack path set `visitedInPath` and a maximum depth guard (`MAX_TREE_DEPTH = 25`). If an ancestor event ID is re-encountered, it throws a `CycleDetectedException` and safely aborts loop traversal.

### 2.2 Missing Parent Event IDs
- **Risk:** Log streams where parent events were dropped or logged out of sequence.
- **Safeguard:** `EventImportService` gracefully converts unresolvable parent references into root nodes without failing the entire log ingestion batch.

### 2.3 Malformed Dates & Timestamps
- **Risk:** Inputs containing non-ISO dates or garbage text (e.g. `INVALID_DATE_TIME`).
- **Safeguard:** `EventValidationService` tests input strings against multiple ISO-8601 formatters, capturing parse failures into `validationErrors` list without crashing the application.

---

## 3. Running Automated JUnit 5 Tests

Execute the full automated test suite using Maven:

```bash
mvn test
```

### Sample Expected Output:
```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.threattrace.service.EventValidationServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.threattrace.service.ProcessLineageBuilderTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.threattrace.service.RiskCalculatorTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] RESULTS: SUCCESS (All automated unit tests passed)
```
