package com.ieltsai.ielts_ai_backend.entity;

import com.ieltsai.ielts_ai_backend.dto.writing.GeminiEvaluationResult;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Stores a user's essay submission and the full AI-generated evaluation result.
 *
 * criteriaScores and feedbackDetails are persisted as native PostgreSQL JSONB columns
 * using Hibernate 6's built-in @JdbcTypeCode(SqlTypes.JSON) support, which means
 * no additional library (e.g. hibernate-types) is required.
 */
@Entity
@Table(name = "writing_attempts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WritingAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionBank question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String essayText;

    @Column(nullable = false)
    private int wordCount;

    @Column(nullable = false)
    private double overallBand;

    /**
     * Stores the four IELTS scoring criteria as a JSON map, e.g.:
     * { "Task Response": 7.0, "Coherence & Cohesion": 6.5, ... }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Double> criteriaScores;

    /**
     * Stores the full structured feedback from Gemini, including detected errors,
     * correction suggestions, and overall recommendations.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private GeminiEvaluationResult.FeedbackDetails feedbackDetails;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;
}
