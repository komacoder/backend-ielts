package com.ieltsai.ielts_ai_backend.dto.writing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * The full evaluation response returned to the client after an essay submission.
 * The refreshToken field intentionally excludes the raw essay text to keep the
 * response lean.
 */
public record EssayFeedbackResponse(
        Long attemptId,
        Long questionId,
        String questionTitle,
        int wordCount,
        double overallBand,
        Map<String, Double> criteriaScores,
        FeedbackDetails feedbackDetails,
        LocalDateTime submittedAt
) {
    /**
     * Mirrors GeminiEvaluationResult.FeedbackDetails for the API response contract.
     */
    public record FeedbackDetails(
            List<DetectedError> errors,
            List<String> recommendations
    ) {}

    public record DetectedError(
            String originalText,
            String correction,
            String explanation,
            String feedbackType
    ) {}
}
