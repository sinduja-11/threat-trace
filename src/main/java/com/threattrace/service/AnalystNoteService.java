package com.threattrace.service;

import com.threattrace.domain.*;
import com.threattrace.exception.CustomExceptions.ResourceNotFoundException;
import com.threattrace.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnalystNoteService {

    private final AnalystNoteRepository noteRepository;
    private final AuditLogRepository auditLogRepository;
    private final InvestigationCaseRepository caseRepository;
    private final UserRepository userRepository;

    public AnalystNoteService(AnalystNoteRepository noteRepository,
                              AuditLogRepository auditLogRepository,
                              InvestigationCaseRepository caseRepository,
                              UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.auditLogRepository = auditLogRepository;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AnalystNote addNote(Long caseId, Long analystId, String noteText, Boolean isFalsePositive) {
        InvestigationCase investigationCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));
        User analyst = userRepository.findById(analystId)
                .orElseThrow(() -> new ResourceNotFoundException("Analyst not found: " + analystId));

        AnalystNote note = new AnalystNote(investigationCase, analyst, noteText, isFalsePositive != null && isFalsePositive);
        AnalystNote savedNote = noteRepository.save(note);

        AuditLog audit = new AuditLog(analyst, "ADD_NOTE", "CASE-" + caseId, "Added analyst note (False Positive Flag: " + isFalsePositive + ")");
        auditLogRepository.save(audit);

        return savedNote;
    }

    public List<AnalystNote> getNotesForCase(Long caseId) {
        return noteRepository.findByInvestigationCaseIdOrderByCreatedAtDesc(caseId);
    }
}
