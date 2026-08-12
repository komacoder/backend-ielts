package com.ieltsai.ielts_ai_backend.dto.writing;

import com.ieltsai.ielts_ai_backend.entity.TaskType;

import java.util.List;
import java.util.Map;

/**
 * Represents the raw structured evaluation result returned by the Gemini AI model.
 * This is an intermediate object parsed from the Gemini JSON response, before
 * being mapped to the final EssayFeedbackResponse and persisted.
 */
public record GeminiEvaluationResult(
        double overallBand,
        Map<String, Double> criteriaScores,
        FeedbackDetails feedbackDetails
) {
    /**
     * Top-level feedback container returned by the AI.
     */
    public record FeedbackDetails(
            List<DetectedError> errors,
            List<String> recommendations
    ) {}

    /**
     * Represents a single identified error in the essay.
     */
    public record DetectedError(
            String originalText,
            String correction,
            String explanation,
            String feedbackType   // e.g. "GRAMMAR", "LEXICAL", "COHERENCE", "TASK_RESPONSE"
    ) {}
}
