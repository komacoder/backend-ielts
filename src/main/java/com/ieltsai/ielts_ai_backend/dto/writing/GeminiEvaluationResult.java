package com.ieltsai.ielts_ai_backend.dto.writing;

import java.util.List;

/**
 * Intermediate object parsed directly from Gemini's JSON response.
 * Field names match exactly what Gemini outputs so Jackson can deserialize it.
 */
public record GeminiEvaluationResult(
        double overallBand,
        CriteriaScoresDto criteriaScores,
        String summary,
        List<String> strengths,
        List<String> weaknesses,
        List<WritingErrorDto> errors,
        List<String> recommendations
) {}
