# Threat Trace --- Project Idea & Implementation Blueprint

## 1. Project title

**Threat Trace: Forensic Investigation, Attack-Timeline Reconstruction,
Risk Scoring, and Evidence Handoff**

## 2. Project summary

Threat Trace is a defensive cybersecurity investigation platform that
turns endpoint/security event logs into a readable incident timeline. It
correlates events, reconstructs process ancestry, maps events to Cyber
Kill Chain stages, creates a behavioral "Attack DNA" fingerprint,
calculates an explainable risk score, recommends mitigations, and
exports an investigation report for further analysis.

The implementation should be realistic for a student team and
demonstrable with **synthetic/sample logs**. Do not collect data from
real systems without permission, perform offensive actions, or claim
that behavioral similarity proves the identity of an attacker.
Attribution output must be framed as a confidence-based lead, not a
definitive identification.

## 3. Problem statement

Security logs often live in separate sources and are difficult to
interpret as one incident. Analysts may have to manually order events,
infer process relationships, classify attack stages, prioritize risk,
and prepare a report. Threat Trace provides one workflow for these tasks
and preserves the evidence and reasoning behind each conclusion.

## 4. Core workflow

1.  **Collect/import:** Upload a CSV or JSON file containing sample
    endpoint events.
2.  **Validate and normalize:** Check required fields, timestamps, event
    types, host/device IDs, and parent-event references.
3.  **Correlate:** Group related events into an investigation case using
    case/device/user/time fields.
4.  **Reconstruct:** Sort the timeline and build parent-child process
    lineage from `parent_event_id`.
5.  **Classify:** Map events to Cyber Kill Chain stages using
    transparent, rule-based mappings.
6.  **Fingerprint:** Generate an explainable Attack DNA summary from
    sequence, timing, targets, event categories, and spread/progression.
7.  **Score risk:** Calculate a risk score from documented factors such
    as asset criticality, event severity, suspicious sequence, and
    spread. Show each factor's contribution.
8.  **Recommend:** Rank defensive mitigations by expected risk
    reduction, effort, and business impact. Explain why each
    recommendation was selected.
9.  **Review and report:** Let an analyst add notes, mark false
    positives, record decisions, and export a case report as
    JSON/CSV/PDF if supported.
10. **Audit:** Record important analyst actions and changes to case
    status.

## 5. Scope and priorities

### MVP --- must implement first

-   Login or a clearly documented demo analyst mode.
-   Create, view, update, and close investigation cases.
-   Import sample CSV/JSON event logs with validation and helpful error
    messages.
-   Timeline view with filtering by time, event type, host/device, and
    severity.
-   Process-lineage tree with cycle/missing-parent detection.
-   Cyber Kill Chain classification with a visible explanation for each
    mapping.
-   Explainable risk score and risk-factor breakdown.
-   Mitigation recommendations from a maintainable rules/catalog table.
-   Search, sorting, and case details.
-   Relational database with constraints and normalized schema.
-   Report export and a small set of sample datasets.
-   Unit tests and setup instructions.

### Stretch goals --- only after MVP works

-   Compare an incident's behavioral fingerprint with
    fictional/reference profiles and show feature-by-feature similarity.
-   Strategy selection for ranking mitigations (risk reduction, effort,
    or business impact).
-   Recalculate risk when new events are imported.
-   Analyst feedback/false-positive tracking.
-   Multi-threaded import or analysis for a large synthetic dataset.
-   Optional local/offline language-model summary. The application must
    remain usable without an API key or paid service; any generated text
    must be labelled as a draft and grounded in the underlying events.

Do not start with AI. First make the ingestion, database, timeline,
rules, risk score, and tests correct.

## 6. Recommended technology choices

-   **Primary implementation language:** Java.
-   **Java backend:** Java 17 or newer, using a simple layered
    architecture. Spring Boot is acceptable if the project can be built
    and explained confidently; otherwise use a smaller Java application
    and REST API.
-   **Frontend:** HTML, CSS, and JavaScript, or a Java-supported
    frontend approach. Keep the interface clear and practical.
-   **Database:** MySQL as the main relational database. SQLite may be
    used only for a quick local demo if MySQL setup becomes a blocker;
    document the difference.
-   **Build/test:** Maven or Gradle, JUnit, and Git.
-   **Optional C++ module:** A separate small command-line utility for
    demonstrating pointer-based process-tree reconstruction, recursive
    traversal, or STL containers. It must not duplicate the entire Java
    application or be required to run the core product.
-   **Optional Python:** Only for generating synthetic log data, a
    one-off analysis helper, or testing/visualization when it materially
    saves time. Java remains the main implementation.
-   **Security:** Store password hashes if real login is implemented;
    validate uploads; use parameterized SQL; apply role checks; avoid
    logging secrets.

Choose the simplest stack that the team can build and explain. Do not
introduce microservices, Kubernetes, or unnecessary frameworks.

## 7. Suggested architecture

-   `domain`: `User`, `Asset`, `EndpointDevice`, `InvestigationCase`,
    `EventLog`, `KillChainStage`, `AttackPattern`, `ThreatActorProfile`,
    `RiskAssessment`, `MitigationStrategy`, `Report`, `AnalystNote`.
-   `service`: `EventImportService`, `EventValidationService`,
    `EventCorrelationService`, `TimelineBuilder`,
    `ProcessLineageBuilder`, `KillChainMapper`, `AttackDNAAnalyzer`,
    `RiskCalculator`, `MitigationAdvisor`, `ReportService`.
-   `repository`: database access and query classes.
-   `controller` or `api`: endpoints/input handling.
-   `ui`: dashboard, case list, case details, timeline, process tree,
    risk panel, and report page.
-   `tests`: unit tests, database integration tests, import-validation
    tests, and sample-data tests.

Keep business rules outside controllers and UI code. Use interfaces for
interchangeable strategies where the design genuinely benefits from
them. Avoid creating an interface for every class without a reason.

## 8. Database design requirements

Use MySQL and design an ER diagram before writing the schema. Include
primary keys, foreign keys, suitable unique constraints, NOT NULL
constraints, indexes, and transactions where appropriate.

Suggested tables: 1. `users` --- analyst identity and role. 2. `assets`
--- asset name, type, criticality, owner/team. 3. `endpoint_devices` ---
hostname and associated asset. 4. `investigation_cases` --- title,
status, created/updated times, assigned analyst. 5. `event_logs` ---
case, device, timestamp, event type, severity, user/process/file/network
metadata, `parent_event_id`. 6. `kill_chain_stages` --- stage
definitions. 7. `event_stage_mappings` --- event-to-stage mapping and
explanation; prefer a mapping table if an event may map to more than one
stage. 8. `attack_patterns` --- rule/pattern definitions and detected
matches. 9. `threat_actor_profiles` --- fictional/reference behavioral
profiles, not asserted real-world identities. 10. `risk_assessments` ---
score, timestamp, factors, and algorithm version. 11.
`mitigation_strategies` --- action, effort, impact, category. 12.
`case_mitigations` --- recommended/accepted/dismissed mitigation and
rationale. 13. `reports` --- report metadata and export location/content
reference. 14. `analyst_notes` --- case-linked notes and review
decisions. 15. `audit_logs` --- important changes and actions, without
storing secrets.

### Required relational-design work

-   Produce an ER diagram and explain every relationship/cardinality.
-   Map the ER model to relational schemas.
-   Identify candidate keys, primary keys, foreign keys, and important
    functional dependencies.
-   Explain atomic attributes and 1NF.
-   Demonstrate decomposition reasoning for 2NF, 3NF, and BCNF where
    applicable; do not claim a schema is normalized merely because the
    term is mentioned.
-   Discuss multivalued dependencies and 4NF only if the actual schema
    contains a relevant independent multivalued fact. If it does not,
    provide a small separate teaching example rather than inventing a
    dependency in the production schema.
-   Explain why each table exists and what redundancy or update
    anomalies it avoids.
-   Include SQL DDL and realistic seed data.
-   Demonstrate joins, grouping/aggregation, subqueries, views if
    useful, transactions, and indexes.
-   Include recursive process ancestry using a recursive CTE if
    supported by the chosen MySQL version. Also implement a safe Java
    traversal as a fallback.
-   Handle cycles, missing parent IDs, invalid timestamps, duplicate
    imports, and orphan references gracefully.

### Important schema note

A single `event_logs.parent_event_id` self-referencing foreign key is
useful for representing parent-child event/process relationships, but a
process tree should not be treated as a proven attacker identity. Decide
and document whether `parent_event_id` points to a parent event row or a
stable process entity; do not mix the two meanings.

## 9. Programming concepts to demonstrate

Use the course syllabus as a traceability checklist. Only include
concepts where they have a genuine purpose. For concepts that are
language-specific, create a small isolated demonstration module rather
than forcing the concept into the main application.

### Object-Oriented Programming / Java and C++

-   **Classes and objects:** domain entities such as
    `InvestigationCase`, `EventLog`, and `Asset`.
-   **Encapsulation/access specifiers:** private fields, validated
    constructors, and public methods.
-   **Accessor/mutator methods:** controlled getters/setters where
    appropriate; avoid unrestricted setters for invariant-sensitive
    fields.
-   **Constructors/destructors:** Java constructors for valid object
    creation; explain Java garbage collection instead of pretending Java
    has deterministic destructors. If demonstrating C++, show RAII and
    destructors in the optional C++ module.
-   **`new`/`delete`:** C++ demonstration only; Java uses `new` but not
    manual `delete`.
-   **Overloading:** useful constructor or method overloads where they
    improve usability.
-   **Inheritance and overriding:** event subtypes such as `LoginEvent`,
    `ProcessEvent`, `FileEvent`, and `NetworkEvent` only if they share
    meaningful behavior. Prefer composition when types do not need
    inheritance.
-   **Polymorphism / virtual functions:** Java overridden methods or
    interfaces for event-specific interpretation; C++ `virtual`/abstract
    base class in the optional demo.
-   **Abstract classes and interfaces:**
    e.g. `MitigationRankingStrategy` with implementations such as
    `RiskReductionStrategy` and `LowEffortStrategy`.
-   **Static/final methods and fields:** constants, stateless helpers,
    and immutable configuration where appropriate; explain Java method
    hiding vs overriding if a demonstration is needed.
-   **Packages/namespaces:** Java package structure and C++ namespaces.
-   **Exceptions:** typed validation/import errors and clear error
    handling.
-   **Recursion:** process ancestry/tree traversal with a depth/cycle
    guard; compare with iterative traversal.
-   **Pointers and pointers-to-pointers:** C++ educational module for
    tree/linked structures, not Java core code.
-   **Function pointers:** optional C++ callback demonstration only if
    it adds value.
-   **STL / collections:** Java Collections Framework (`List`, `Set`,
    `Map`, `Queue`, `Deque`, `ArrayList`, `HashMap`, `HashSet`,
    `Comparator`, `Comparable`, iterators, generics); optional C++
    `vector`, `map`/`unordered_map`, `stack`, `queue`, and linked list
    demo.
-   **Wrapper classes / String / StringBuilder / Math / Random /
    Date-Time:** use wrapper types for nullable imported values where
    justified; `StringBuilder` for reports; `Math` for score
    calculations; `Random` only for synthetic test data; Java Time API
    for event timestamps.
-   **Files and I/O / serialization:** parse CSV/JSON, export report
    data, and document serialization format. Do not use unsafe native
    Java object deserialization for untrusted uploads.
-   **Inner classes:** use only for a small helper/listener if it is
    naturally scoped; do not add artificially.
-   **Concurrency / threads / Runnable / coordination / interruption:**
    optional background import or analysis job with progress and
    cancellation. Include tests for thread safety and clean interruption
    if this is implemented.
-   **Annotations:** explain actual annotations used (for example, test
    or framework annotations) and what they do.
-   **Object class / final class and methods:** demonstrate through
    meaningful `toString`, `equals`, `hashCode`, or an immutable value
    object; use final only when justified.
-   **Interfaces and evolving contracts:** explain how an interface can
    gain new implementations without changing the calling service;
    document compatibility considerations.

### Do not force these concepts

Virtual function tables, every possible interface rule, every
inner-class type, annotations, Java object serialization,
pointers-to-pointers, function pointers, or multi-thread coordination
need not be embedded in the core application. If required by the
syllabus, place them in a `concept-demos` folder with a small runnable
example and connect each example to the project use case.

## 10. Required explanation document

Create `docs/CONCEPTS_USED_WHERE.md` as a detailed, beginner-friendly
explanation document. It must be a real deliverable, not just a list of
buzzwords.

For every concept, include: 1. **Concept name and syllabus unit/topic.**
2. **Where it appears:** exact package/class/method/file path, or
clearly mark "separate demo". 3. **Why it is used:** the actual design
need. 4. **How it works:** beginner-level explanation without
unexplained jargon. 5. **Small code excerpt:** use code from the actual
implementation, not imaginary placeholder code. 6. **Execution
walkthrough:** a small input → steps → output example. 7.
**Alternatives/trade-offs:** why this approach was selected and when
another approach would fit. 8. **Evidence/status:** implemented,
partially implemented, or not implemented, with a link to a test or file
proving it. 9. **Viva questions:** at least one likely question and a
short answer per major concept group.

The document must distinguish: - implemented in the core Java
application; - demonstrated in optional C++ code; - demonstrated in
optional Python tooling; - explained only / not implemented.

Do not claim a concept is implemented unless the source code actually
demonstrates it. Update the document after implementation changes.

Also create: - `docs/ARCHITECTURE.md` --- architecture, data flow, and
module responsibilities. - `docs/DATABASE_DESIGN.md` --- ERD, relational
mapping, keys, functional dependencies, normalization, SQL features, and
design trade-offs. - `docs/SETUP_AND_DEMO.md` --- exact install/run
steps, sample credentials if demo mode exists, sample import steps, and
a short demo script. - `docs/TESTING.md` --- tests, commands, edge
cases, and known limitations.

## 11. Required DBMS evidence

The project and its documentation should visibly demonstrate: -
relational schema and ER model; - primary/candidate/foreign keys and
constraints; - relational algebra concepts through documented examples
of selection, projection, joins, and set operations where relevant; -
SQL DDL/DML and intermediate SQL; - joins across case, asset/device,
events, risk, and mitigation tables; - grouping, aggregate functions,
`HAVING`, subqueries, and ordering; - functional dependencies and
normalization rationale; - transactions and rollback on failed
multi-step operations; - indexes and query-plan discussion where
practical; - recursive CTE or Java recursion for process ancestry; -
sample queries with expected results.

## 12. Sample event format

Use a documented CSV/JSON schema such as: - `event_id` - `case_id` (or
assigned by the import workflow) - `device_id` / `hostname` -
`event_time` (ISO 8601 with timezone) - `event_type` (`LOGIN`,
`PROCESS`, `FILE`, `NETWORK`, `USB`) - `severity` - `user_name` -
`process_name` - `parent_event_id` - `target` - `source` - `description`

Include safe, fictional sample events. Provide examples with a normal
login sequence, suspicious process chain, missing parent, duplicate row,
invalid timestamp, and a cyclic parent reference so validation behavior
can be demonstrated.

## 13. Risk score and Attack DNA requirements

-   Document the scoring formula, input features, weights, and score
    range.
-   Show a breakdown for each score rather than returning only a number.
-   Make the formula configurable and version it in stored assessments.
-   Explain that risk scoring is a prioritization aid, not a guarantee
    that an event is malicious.
-   For Attack DNA, use explicit, reproducible features: event-type
    sequence, time gaps, affected assets, event categories, and observed
    progression.
-   If similarity scores are used, document the distance/similarity
    method and its limitations. Avoid asserting that a similarity score
    proves attribution to a real person or group.

## 14. UI requirements

-   Dashboard: open cases, recent activity, risk distribution, and quick
    actions.
-   Case list: search, status filter, sort, and create case.
-   Case detail: event timeline, filters, event details, and analyst
    notes.
-   Process lineage: parent-child visualization plus a readable fallback
    table.
-   Analysis panel: Kill Chain stages, Attack DNA features, risk
    breakdown, and explanation.
-   Mitigation panel: ranked recommendations, effort/impact, rationale,
    and analyst decision.
-   Report page: preview and export.
-   Provide empty states, loading/error states, validation messages, and
    accessible labels. Avoid decorative metrics that are not calculated
    from real data.

## 15. Quality and acceptance criteria

The project is complete only when: - A new developer can follow the
setup guide and run it. - A sample dataset imports successfully and
invalid rows produce understandable errors. - Imported events persist in
MySQL and appear in the correct case timeline. - Process ancestry is
reconstructed accurately and handles missing links/cycles. - Kill Chain
mappings show their rule/reason. - Risk scoring is deterministic,
tested, and explainable. - Mitigation rankings can be compared using at
least two strategies if the stretch goal is selected. - A report can be
exported and matches the case data. - DB schema, ERD, SQL examples, and
normalization reasoning are documented. - `docs/CONCEPTS_USED_WHERE.md`
maps syllabus concepts to actual files and runnable examples. -
Automated tests cover core rules and common malformed input. - No
secrets or real personal data are included in sample data.

## 16. Suggested build sequence

1.  Inspect the existing repository and make a plan before changing
    files.
2.  Finalize the ERD and relational schema.
3.  Implement schema, migrations/SQL scripts, and seed data.
4.  Implement Java domain classes and validation tests.
5.  Implement import, correlation, timeline, and process-lineage
    services.
6.  Implement Kill Chain mapping, Attack DNA features, and risk scoring.
7.  Implement mitigation strategy and analyst review workflow.
8.  Implement API/UI and report export.
9.  Add sample datasets and edge cases.
10. Write and verify documentation against the real code.
11. Run tests, fix failures, and provide a final status report with
    implemented/partial/not implemented concepts.

## 17. Suggested repository structure

``` text
threat-trace/
  README.md
  pom.xml or build.gradle
  src/main/java/.../
    domain/
    service/
    repository/
    api/
    config/
  src/main/resources/
    application.properties
    db/
      schema.sql
      seed.sql
  src/test/java/.../
  frontend/                     # if using a separate frontend
  concept-demos/
    cpp/
    python/                      # only if needed
  sample-data/
    normal-events.csv
    suspicious-process-chain.csv
    malformed-events.csv
  docs/
    CONCEPTS_USED_WHERE.md
    ARCHITECTURE.md
    DATABASE_DESIGN.md
    SETUP_AND_DEMO.md
    TESTING.md
```

## 18. Important instructions for the coding agent

-   Work in small, verifiable phases. First inspect the repository and
    report what already exists.
-   Do not overwrite or delete existing work without explaining the
    impact and getting approval if destructive changes are necessary.
-   Before each phase, state the files to be created/changed and the
    acceptance checks.
-   Build a working vertical slice before adding stretch goals.
-   Use Java as the main language; C++ is optional for syllabus-specific
    demonstrations; Python is optional and must not become the main
    implementation.
-   Never fabricate test results, code paths, screenshots, features, or
    concept coverage.
-   Run the relevant tests and build commands after changes. If
    something cannot be run, say why.
-   Keep a checklist of requirements with statuses: `implemented`,
    `partial`, `not implemented`, `blocked`.
-   At the end, provide exact run commands, demo steps, tests executed
    and results, database setup, known limitations, and concept-to-code
    map.
