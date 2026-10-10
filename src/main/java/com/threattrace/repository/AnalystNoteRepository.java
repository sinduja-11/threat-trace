package com.threattrace.repository;

import com.threattrace.domain.AnalystNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalystNoteRepository extends JpaRepository<AnalystNote, Long> {
    List<AnalystNote> findByInvestigationCaseIdOrderByCreatedAtDesc(Long caseId);
}
