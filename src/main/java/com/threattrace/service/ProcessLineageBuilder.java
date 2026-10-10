package com.threattrace.service;

import com.threattrace.domain.EventLog;
import com.threattrace.dto.DTOs.ProcessNodeDTO;
import com.threattrace.exception.CustomExceptions.CycleDetectedException;
import com.threattrace.repository.EventLogRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProcessLineageBuilder {

    private final EventLogRepository eventLogRepository;
    private static final int MAX_TREE_DEPTH = 25;

    public ProcessLineageBuilder(EventLogRepository eventLogRepository) {
        this.eventLogRepository = eventLogRepository;
    }

    /**
     * Builds parent-child process tree for an investigation case using Depth-First Search (DFS).
     * Syllabus Concept Demonstrated: Recursion, Tree Traversal, DFS, Cycle Guards.
     */
    public List<ProcessNodeDTO> buildProcessLineageTree(Long caseId) {
        List<EventLog> rootEvents = eventLogRepository.findByInvestigationCaseIdAndParentEventIsNull(caseId);
        List<ProcessNodeDTO> forest = new ArrayList<>();

        for (EventLog root : rootEvents) {
            Set<Long> visitedInPath = new HashSet<>();
            ProcessNodeDTO rootNode = buildTreeNodeRecursive(root, 0, visitedInPath);
            forest.add(rootNode);
        }

        return forest;
    }

    /**
     * Recursive helper method for tree construction.
     */
    private ProcessNodeDTO buildTreeNodeRecursive(EventLog currentEvent, int currentDepth, Set<Long> visitedInPath) {
        if (currentDepth > MAX_TREE_DEPTH) {
            throw new CycleDetectedException("Maximum process tree depth exceeded (> " + MAX_TREE_DEPTH + ") for event ID: " + currentEvent.getId());
        }

        if (visitedInPath.contains(currentEvent.getId())) {
            throw new CycleDetectedException("Cyclic parent-child process reference detected at event ID: " + currentEvent.getId() + " (" + currentEvent.getProcessName() + ")");
        }

        visitedInPath.add(currentEvent.getId());

        ProcessNodeDTO node = new ProcessNodeDTO(
                currentEvent.getId(),
                currentEvent.getEventCode(),
                currentEvent.getProcessName() != null ? currentEvent.getProcessName() : currentEvent.getEventType(),
                currentEvent.getParentEvent() != null ? currentEvent.getParentEvent().getId() : null,
                currentEvent.getSeverity(),
                currentEvent.getEventType(),
                currentEvent.getEventTime(),
                currentEvent.getTarget(),
                currentDepth
        );

        List<EventLog> children = eventLogRepository.findByParentEventId(currentEvent.getId());
        for (EventLog child : children) {
            // Pass a copy of visitedInPath to allow branching siblings while detecting ancestor cycles
            Set<Long> pathCopy = new HashSet<>(visitedInPath);
            ProcessNodeDTO childNode = buildTreeNodeRecursive(child, currentDepth + 1, pathCopy);
            node.addChild(childNode);
        }

        return node;
    }
}
