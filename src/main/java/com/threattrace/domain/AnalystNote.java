package com.threattrace.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analyst_notes")
public class AnalystNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "case_id", nullable = false)
    private InvestigationCase investigationCase;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "analyst_id", nullable = false)
    private User analyst;

    @Column(name = "note_text", columnDefinition = "TEXT", nullable = false)
    private String noteText;

    @Column(name = "is_false_positive_flag")
    private Boolean isFalsePositiveFlag = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public AnalystNote() {}

    public AnalystNote(InvestigationCase investigationCase, User analyst, String noteText, Boolean isFalsePositiveFlag) {
        this.investigationCase = investigationCase;
        this.analyst = analyst;
        this.noteText = noteText;
        this.isFalsePositiveFlag = isFalsePositiveFlag;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InvestigationCase getInvestigationCase() { return investigationCase; }
    public void setInvestigationCase(InvestigationCase investigationCase) { this.investigationCase = investigationCase; }

    public User getAnalyst() { return analyst; }
    public void setAnalyst(User analyst) { this.analyst = analyst; }

    public String getNoteText() { return noteText; }
    public void setNoteText(String noteText) { this.noteText = noteText; }

    public Boolean getIsFalsePositiveFlag() { return isFalsePositiveFlag; }
    public void setIsFalsePositiveFlag(Boolean isFalsePositiveFlag) { this.isFalsePositiveFlag = isFalsePositiveFlag; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
