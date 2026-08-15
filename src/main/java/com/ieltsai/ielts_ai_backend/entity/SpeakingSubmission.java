package com.ieltsai.ielts_ai_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "speaking_submissions")
@Getter
@Setter
public class SpeakingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private SpeakingSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpeakingPart part;

    private Double overallBandScore;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String evaluationJson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();
}
