# Threat Trace — Syllabus Concepts Traceability & Viva QA Guide

This document serves as the **master traceability deliverable** for the Threat Trace cybersecurity forensic platform. It maps every Object-Oriented Programming (OOP), Data Structures, Algorithms, Java/C++/Python, and Database Management System (DBMS) syllabus concept to its **exact location in the codebase**, providing beginner-friendly explanations, actual code snippets, execution walkthroughs, trade-off analysis, and likely viva examination questions.

---

## 1. Traceability Summary Matrix

| Concept Name | Syllabus Unit | Implementation Location | Category / Status |
| :--- | :--- | :--- | :--- |
| **Classes & Objects** | OOP Unit 1 | `com.threattrace.domain.EventLog`, `InvestigationCase` | Core Java (Implemented) |
| **Encapsulation & Access Modifiers** | OOP Unit 1 | `com.threattrace.domain.User` | Core Java (Implemented) |
| **Constructors & Lifecycle** | OOP Unit 1 | `com.threattrace.domain.Asset`, `@PrePersist` | Core Java (Implemented) |
| **Method Overloading & Overriding** | OOP Unit 2 | `EventValidationService`, `User.toString()` | Core Java (Implemented) |
| **Inheritance & Polymorphism** | OOP Unit 2 | `MitigationRankingStrategy` & implementations | Core Java (Implemented) |
| **Abstract Classes & Interfaces** | OOP Unit 2 | `MitigationRankingStrategy` interface | Core Java (Implemented) |
| **Static & Final Keywords** | OOP Unit 2 | `ProcessLineageBuilder.MAX_TREE_DEPTH` | Core Java (Implemented) |
| **Packages & Namespaces** | OOP Unit 1 | `package com.threattrace.service;` | Core Java (Implemented) |
| **Custom Exception Handling** | OOP Unit 3 | `CustomExceptions.CycleDetectedException` | Core Java (Implemented) |
| **Recursion & Depth-First Search** | Data Structures | `ProcessLineageBuilder.buildTreeNodeRecursive()` | Core Java (Implemented) |
| **Collections & Generics** | Collections | `List<EventLog>`, `Map<String, String>`, `Set<Long>` | Core Java (Implemented) |
| **Date-Time API & String/StringBuilder** | Java Utilities | `LocalDateTime`, `ReportService.java` | Core Java (Implemented) |
| **Strategy Design Pattern** | Software Design | `MitigationAdvisor` & `MitigationRankingStrategy` | Core Java (Implemented) |
| **File I/O & Parsers** | I/O Streams | `EventImportService.importCsvLogs()` | Core Java (Implemented) |
| **Pointers & Double Pointers (`*`, `**`)** | Low-Level C++ | `concept-demos/cpp/pointer_tree_demo.cpp` | C++ Demo (Implemented) |
| **Manual Memory Allocation (`new`/`delete`)** | Low-Level C++ | `concept-demos/cpp/pointer_tree_demo.cpp` | C++ Demo (Implemented) |
| **RAII & Destructors (`~ProcessTreeNode`)** | Memory Mgmt | `concept-demos/cpp/pointer_tree_demo.cpp` | C++ Demo (Implemented) |
| **Virtual Functions & VTables** | C++ Polymorphism | `concept-demos/cpp/pointer_tree_demo.cpp` | C++ Demo (Implemented) |
| **Function Pointers** | Callbacks | `concept-demos/cpp/pointer_tree_demo.cpp` | C++ Demo (Implemented) |
| **Python Scripting & Data Generation** | Scripting | `concept-demos/python/log_generator.py` | Python Script (Implemented) |
| **Relational Model & 3NF/BCNF Normalization**| DBMS Unit 1 & 2 | `src/main/resources/db/schema.sql` (15 tables) | MySQL DB (Implemented) |
| **Primary, Foreign & Candidate Keys** | DBMS Constraints | `schema.sql` (`parent_event_id` self-join FK) | MySQL DB (Implemented) |
| **Recursive CTE Queries** | Advanced SQL | `src/main/resources/db/recursive_queries.sql` | MySQL DB (Implemented) |
| **Relational Algebra (Selection, Projection, Join)**| DBMS Algebra | `recursive_queries.sql`, `EventLogRepository` | MySQL DB (Implemented) |
| **Multivalued Dependencies & 4NF** | DBMS Normalization | `docs/DATABASE_DESIGN.md` | Teaching Example (Documented) |

---

## 2. Detailed Concept Breakdown

### Concept 1: Classes, Objects, and Encapsulation
- **Syllabus Unit:** OOP Unit 1 — Abstraction & Data Hiding.
- **Where it appears:** `d:\apdbmscapstone\src\main\java\com\threattrace\domain\User.java` (Lines 6–65).
- **Why it is used:** Protects sensitive security analyst data (password hashes, roles) from direct external mutation.
- **How it works:** All data fields are declared `private`. Access is strictly controlled through public getter and setter methods. Invariant rules can be enforced inside setters.
- **Actual Code Excerpt:**
  ```java
  @Entity
  @Table(name = "users")
  public class User {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      @Column(nullable = false, unique = true)
      private String username;

      @Column(name = "password_hash", nullable = false)
      private String passwordHash;

      public String getUsername() { return username; }
      public void setUsername(String username) { this.username = username; }
  }
  ```
- **Execution Walkthrough:**
  1. `EventImportService` instantiates a `User` instance.
  2. `user.setUsername("sarah_analyst")` sets the username safely.
  3. Attempting to directly write `user.username = "sarah"` from outside the class produces a compile error.
- **Alternatives & Trade-offs:** Public fields would save boilerplate getter/setter code, but break data integrity and security invariants.
- **Status:** Implemented in Core Java (`src/main/java/com/threattrace/domain/User.java`).
- **Viva Questions & Answers:**
  - *Q: What is the difference between encapsulation and data hiding in Java?*
  - *A: Encapsulation is wrapping data fields and methods into a single unit (class), while data hiding (using `private` access specifiers) restricts direct external access to those internal fields.*

---

### Concept 2: Recursion and Cycle Guarded Tree Traversal
- **Syllabus Unit:** Data Structures & Algorithms — Tree Traversal & Recursion.
- **Where it appears:** `d:\apdbmscapstone\src\main\java\com\threattrace\service\ProcessLineageBuilder.java` (Lines 25–65).
- **Why it is used:** Process execution in operating systems forms a parent-child hierarchy (e.g. `explorer.exe` -> `WINWORD.EXE` -> `cmd.exe` -> `powershell.exe`). Recursion allows clean tree traversal of arbitrary depth.
- **How it works:** The function `buildTreeNodeRecursive` calls itself for each child process ID. To prevent infinite loops caused by cyclic references or malformed log inputs, a `Set<Long> visitedInPath` tracks ancestors in the current recursion stack, throwing a `CycleDetectedException` if a loop is encountered.
- **Actual Code Excerpt:**
  ```java
  private ProcessNodeDTO buildTreeNodeRecursive(EventLog currentEvent, int currentDepth, Set<Long> visitedInPath) {
      if (currentDepth > MAX_TREE_DEPTH) {
          throw new CycleDetectedException("Maximum process tree depth exceeded");
      }
      if (visitedInPath.contains(currentEvent.getId())) {
          throw new CycleDetectedException("Cyclic parent-child reference at event ID: " + currentEvent.getId());
      }
      visitedInPath.add(currentEvent.getId());

      ProcessNodeDTO node = new ProcessNodeDTO(...);
      List<EventLog> children = eventLogRepository.findByParentEventId(currentEvent.getId());
      for (EventLog child : children) {
          Set<Long> pathCopy = new HashSet<>(visitedInPath);
          node.addChild(buildTreeNodeRecursive(child, currentDepth + 1, pathCopy));
      }
      return node;
  }
  ```
- **Execution Walkthrough:**
  1. Root event `EVT-1001` (`explorer.exe`) enters recursion at depth 0. `visitedInPath = {1}`.
  2. Child `EVT-1002` (`WINWORD.EXE`) enters recursion at depth 1. `visitedInPath = {1, 2}`.
  3. Child `EVT-1003` (`cmd.exe`) enters recursion at depth 2. `visitedInPath = {1, 2, 3}`.
  4. Returns constructed `ProcessNodeDTO` hierarchy tree to UI visualizer.
- **Alternatives & Trade-offs:** Iterative stack-based traversal avoids call-stack overflow risk, but recursive traversal yields significantly cleaner code for hierarchical tree structures when bounded by a depth guard.
- **Status:** Implemented in Core Java (`ProcessLineageBuilder.java`).
- **Viva Questions & Answers:**
  - *Q: How do you prevent a StackOverflowError in recursive process tree reconstruction?*
  - *A: By enforcing a maximum depth guard (`MAX_TREE_DEPTH`) and maintaining a path set (`visitedInPath`) to detect and break cyclic node references.*

---

### Concept 3: Strategy Design Pattern (Polymorphism & Interfaces)
- **Syllabus Unit:** Software Design Patterns / OOP Unit 2 — Interfaces & Dynamic Dispatch.
- **Where it appears:** `d:\apdbmscapstone\src\main\java\com\threattrace\service\strategy\` (`MitigationRankingStrategy.java`, `RiskReductionStrategy.java`, `LowEffortStrategy.java`, `BusinessImpactStrategy.java`).
- **Why it is used:** Security analysts require different criteria when prioritizing mitigations (e.g. emergency containment vs low IT effort vs minimal business disruption). The Strategy pattern decouples ranking algorithms from client code.
- **How it works:** The `MitigationRankingStrategy` interface defines `calculateScore()`. Three concrete classes implement distinct scoring algorithms. `MitigationAdvisor` dynamically selects the active implementation at runtime.
- **Actual Code Excerpt:**
  ```java
  public interface MitigationRankingStrategy {
      double calculateScore(MitigationStrategy strategy, double caseRiskScore);
      String getStrategyName();
  }

  @Component
  public class LowEffortStrategy implements MitigationRankingStrategy {
      @Override
      public double calculateScore(MitigationStrategy strategy, double caseRiskScore) {
          double effortWeight = switch (strategy.getEffortLevel().toUpperCase()) {
              case "LOW" -> 90.0;
              case "MEDIUM" -> 60.0;
              default -> 30.0;
          };
          return (effortWeight * 0.7) + (caseRiskScore * 0.3);
      }
  }
  ```
- **Execution Walkthrough:**
  1. Analyst selects "Lowest Analyst Effort" on dashboard UI.
  2. `MitigationAdvisor` retrieves `LowEffortStrategy` bean from strategy map.
  3. `strategy.calculateScore(ms, 82.5)` computes recommendation score prioritizing `LOW` effort actions.
- **Alternatives & Trade-offs:** Using `if/else` or `switch` statements inside a single method would violate the Open/Closed Principle (OCP), making it harder to add new ranking algorithms.
- **Status:** Implemented in Core Java (`com.threattrace.service.strategy`).
- **Viva Questions & Answers:**
  - *Q: Why use the Strategy pattern instead of a large switch block?*
  - *A: Strategy pattern adheres to the Open/Closed Principle. New ranking strategies can be added as new classes without modifying existing caller code.*

---

### Concept 4: Low-Level C++ Pointers, RAII Destructors & Memory Management
- **Syllabus Unit:** Low-Level Systems Programming — Pointers, Manual Memory, RAII, Virtual Functions.
- **Where it appears:** `d:\apdbmscapstone\concept-demos\cpp\pointer_tree_demo.cpp` (Lines 15–95).
- **Why it is used:** Java relies on garbage collection and hides pointers. This separate C++ module demonstrates low-level pointer arithmetic (`*`, `**`), manual memory management (`new`/`delete`), RAII resource allocation, and C++ vtable dynamic dispatch.
- **How it works:** `ProcessTreeNode` contains a single pointer `parentNode` and a double pointer `childrenNodes` (pointer to array of node pointers). The destructor `~ProcessTreeNode()` recursively calls `delete` to clean up heap memory upon destruction.
- **Actual Code Excerpt:**
  ```cpp
  class ProcessTreeNode {
  public:
      int eventId;
      std::string processName;
      ProcessTreeNode* parentNode;      // Single Pointer
      ProcessTreeNode** childrenNodes;  // Double Pointer
      int childCount;

      ProcessTreeNode(int id, const std::string& name) : eventId(id), processName(name) {
          childrenNodes = new ProcessTreeNode*[5]; // Manual Allocation
      }

      ~ProcessTreeNode() { // RAII Destructor
          for (int i = 0; i < childCount; ++i) {
              delete childrenNodes[i]; // Recursive Free
          }
          delete[] childrenNodes;
      }
  };
  ```
- **Execution Walkthrough:**
  1. Compile with `g++ -std=c++17 -o pointer_tree pointer_tree_demo.cpp`.
  2. Run `./pointer_tree`. Nodes allocated dynamically with `new`.
  3. Calling `delete root;` triggers `~ProcessTreeNode()`, releasing memory without memory leaks.
- **Status:** Implemented in Optional C++ Module (`concept-demos/cpp/pointer_tree_demo.cpp`).
- **Viva Questions & Answers:**
  - *Q: What is RAII in C++ and how does it prevent memory leaks?*
  - *A: Resource Acquisition Is Initialization (RAII) ties resource ownership to object lifetime. When an object goes out of scope, its destructor automatically frees allocated memory.*

---

### Concept 5: MySQL Database Design, Normalization, & Foreign Keys
- **Syllabus Unit:** DBMS Unit 1 & 2 — Relational Schema Design, Normalization (1NF–3NF/BCNF), Relational Constraints.
- **Where it appears:** `d:\apdbmscapstone\src\main\resources\db\schema.sql` (15 Tables).
- **Why it is used:** Storing forensic logs, assets, devices, kill chain mappings, and analyst notes in a structured, consistent, anomaly-free relational database.
- **How it works:**
  - **1NF:** All table attributes are atomic. Multi-valued fields are moved to separate tables.
  - **2NF:** Every non-key attribute is fully functionally dependent on the entire primary key.
  - **3NF:** No transitive functional dependencies exist (e.g. device hostname depends on `device_id`, not directly in `event_logs`).
  - **BCNF:** Every determinant is a candidate key.
- **Actual Code Excerpt:**
  ```sql
  CREATE TABLE event_logs (
      event_id INT AUTO_INCREMENT PRIMARY KEY,
      event_code VARCHAR(64) UNIQUE,
      case_id INT NOT NULL,
      device_id INT NOT NULL,
      event_time DATETIME NOT NULL,
      parent_event_id INT NULL, -- Self-referencing FK for process lineage
      FOREIGN KEY (case_id) REFERENCES investigation_cases(case_id) ON DELETE CASCADE,
      FOREIGN KEY (device_id) REFERENCES endpoint_devices(device_id) ON DELETE CASCADE,
      FOREIGN KEY (parent_event_id) REFERENCES event_logs(event_id) ON DELETE SET NULL
  ) ENGINE=InnoDB;
  ```
- **Status:** Implemented in MySQL Database (`src/main/resources/db/schema.sql`).
- **Viva Questions & Answers:**
  - *Q: Why is `parent_event_id` a self-referencing foreign key?*
  - *A: Because a child process event refers to another row in the same `event_logs` table representing its parent process event.*
