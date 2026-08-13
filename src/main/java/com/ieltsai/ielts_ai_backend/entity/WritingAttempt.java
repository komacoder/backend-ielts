package com.ieltsai.ielts_ai_backend.entity;

import com.ieltsai.ielts_ai_backend.dto.writing.CriteriaScoresDto;
import com.ieltsai.ielts_ai_backend.dto.writing.WritingFeedbackData;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

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
     * Stores the four IELTS scoring criteria as a JSONB object, e.g.:
     * { "taskResponseOrAchievement": 7.0, "coherenceAndCohesion": 6.5, ... }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private CriteriaScoresDto criteriaScores;

    /**
     * Stores the full structured feedback from Gemini as a JSONB object,
     * including summary, strengths, weaknesses, detected errors, and recommendations.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private WritingFeedbackData feedbackDetails;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;
}
