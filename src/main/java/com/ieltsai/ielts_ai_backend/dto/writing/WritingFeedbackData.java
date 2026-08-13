package com.ieltsai.ielts_ai_backend.dto.writing;

import lombok.*;

import java.util.List;

/**
 * JSONB storage structure for the feedbackDetails column in the writing_attempts table.
 * Holds the complete AI evaluation feedback.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingFeedbackData {
    private String summary;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<WritingErrorDto> errors;
    private List<String> recommendations;
}
