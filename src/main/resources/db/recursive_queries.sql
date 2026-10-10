-- ============================================================================
-- Threat Trace: SQL Demonstration Queries (Recursive CTE & Syllabus Requirements)
-- Description: Demonstrates recursive process tree reconstruction, multi-table
--              JOINs, aggregate analytics, subqueries, and relational algebra.
-- ============================================================================

USE threat_trace_db;

-- ----------------------------------------------------------------------------
-- 1. RECURSIVE CTE: Reconstruct Process Ancestry Lineage Tree for a Case
-- ----------------------------------------------------------------------------
-- Traces parent-to-child event hierarchy starting from root events (parent_event_id IS NULL)
WITH RECURSIVE process_lineage AS (
    -- Anchor Member: Root events in the case
    SELECT 
        event_id,
        event_code,
        case_id,
        process_name,
        parent_event_id,
        event_time,
        severity,
        0 AS depth_level,
        CAST(process_name AS CHAR(1000)) AS lineage_path
    FROM event_logs
    WHERE case_id = 1 AND parent_event_id IS NULL

    UNION ALL

    -- Recursive Member: Child events linking to parent_event_id
    SELECT 
        e.event_id,
        e.event_code,
        e.case_id,
        e.process_name,
        e.parent_event_id,
        e.event_time,
        e.severity,
        p.depth_level + 1 AS depth_level,
        CONCAT(p.lineage_path, ' -> ', e.process_name) AS lineage_path
    FROM event_logs e
    INNER JOIN process_lineage p ON e.parent_event_id = p.event_id
    WHERE p.depth_level < 20 -- Cycle Guard Depth Limit
)
SELECT 
    event_id,
    event_code,
    depth_level,
    CONCAT(REPEAT('  │ ', depth_level), '└── ', process_name) AS process_hierarchy_tree,
    event_time,
    severity,
    lineage_path
FROM process_lineage
ORDER BY depth_level, event_time;

-- ----------------------------------------------------------------------------
-- 2. MULTI-TABLE JOIN & GROUP BY/HAVING: Risk Distribution Across Assets
-- ----------------------------------------------------------------------------
SELECT 
    a.asset_name,
    a.asset_type,
    a.criticality_score,
    d.hostname,
    COUNT(e.event_id) AS total_events,
    SUM(CASE WHEN e.severity = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_events,
    MAX(e.event_time) AS latest_activity
FROM assets a
JOIN endpoint_devices d ON a.asset_id = d.asset_id
JOIN event_logs e ON d.device_id = e.device_id
GROUP BY a.asset_id, a.asset_name, a.asset_type, a.criticality_score, d.hostname
HAVING COUNT(e.event_id) > 0
ORDER BY critical_events DESC, a.criticality_score DESC;

-- ----------------------------------------------------------------------------
-- 3. SUBQUERY: Find Cases containing events mapped to 'Actions on Objectives'
-- ----------------------------------------------------------------------------
SELECT 
    c.case_number,
    c.title,
    c.severity_level,
    c.risk_score
FROM investigation_cases c
WHERE c.case_id IN (
    SELECT DISTINCT e.case_id
    FROM event_logs e
    JOIN event_stage_mappings m ON e.event_id = m.event_id
    JOIN kill_chain_stages k ON m.stage_id = k.stage_id
    WHERE k.stage_name = 'Actions on Objectives'
);

-- ----------------------------------------------------------------------------
-- 4. RELATIONAL SET OPERATION (UNION): Active High-Risk Events & Unassigned Cases
-- ----------------------------------------------------------------------------
SELECT 
    'HIGH_RISK_EVENT' AS record_type,
    e.event_code AS identifier,
    e.description AS details,
    e.created_at AS timestamp_info
FROM event_logs e
WHERE e.severity IN ('HIGH', 'CRITICAL')

UNION ALL

SELECT 
    'UNASSIGNED_CASE' AS record_type,
    c.case_number AS identifier,
    c.title AS details,
    c.created_at AS timestamp_info
FROM investigation_cases c
WHERE c.assigned_analyst_id IS NULL;
