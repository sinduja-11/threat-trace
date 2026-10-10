package com.threattrace.service;

import com.threattrace.domain.EventLog;
import com.threattrace.dto.DTOs.ProcessNodeDTO;
import com.threattrace.exception.CustomExceptions.CycleDetectedException;
import com.threattrace.repository.EventLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ProcessLineageBuilderTest {

    private ProcessLineageBuilder lineageBuilder;
    private EventLogRepository eventLogRepository;

    @BeforeEach
    void setUp() {
        eventLogRepository = Mockito.mock(EventLogRepository.class);
        lineageBuilder = new ProcessLineageBuilder(eventLogRepository);
    }

    @Test
    void testBuildProcessLineageTree_ValidHierarchy() {
        EventLog root = new EventLog();
        root.setId(1L);
        root.setEventCode("EVT-1");
        root.setProcessName("WINWORD.EXE");
        root.setEventType("PROCESS");
        root.setSeverity("LOW");
        root.setEventTime(LocalDateTime.now());

        EventLog child = new EventLog();
        child.setId(2L);
        child.setEventCode("EVT-2");
        child.setProcessName("cmd.exe");
        child.setParentEvent(root);
        child.setEventType("PROCESS");
        child.setSeverity("HIGH");
        child.setEventTime(LocalDateTime.now().plusSeconds(2));

        when(eventLogRepository.findByInvestigationCaseIdAndParentEventIsNull(1L)).thenReturn(List.of(root));
        when(eventLogRepository.findByParentEventId(1L)).thenReturn(List.of(child));
        when(eventLogRepository.findByParentEventId(2L)).thenReturn(Collections.emptyList());

        List<ProcessNodeDTO> tree = lineageBuilder.buildProcessLineageTree(1L);

        assertEquals(1, tree.size());
        assertEquals("WINWORD.EXE", tree.get(0).getProcessName());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("cmd.exe", tree.get(0).getChildren().get(0).getProcessName());
    }

    @Test
    void testBuildProcessLineageTree_CycleDetected_ThrowsException() {
        EventLog nodeA = new EventLog();
        nodeA.setId(10L);
        nodeA.setProcessName("processA.exe");
        nodeA.setEventType("PROCESS");

        EventLog nodeB = new EventLog();
        nodeB.setId(20L);
        nodeB.setProcessName("processB.exe");
        nodeB.setParentEvent(nodeA);
        nodeB.setEventType("PROCESS");

        // Cycle reference: nodeA child is nodeB, nodeB child is nodeA
        when(eventLogRepository.findByInvestigationCaseIdAndParentEventIsNull(2L)).thenReturn(List.of(nodeA));
        when(eventLogRepository.findByParentEventId(10L)).thenReturn(List.of(nodeB));
        when(eventLogRepository.findByParentEventId(20L)).thenReturn(List.of(nodeA));

        assertThrows(CycleDetectedException.class, () -> {
            lineageBuilder.buildProcessLineageTree(2L);
        });
    }
}
