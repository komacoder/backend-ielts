package com.ieltsai.ielts_ai_backend.dto.writing;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for submitting an IELTS writing essay for AI evaluation.
 */
public record SubmitEssayRequest(
        @NotNull(message = "Question ID must be provided")
        Long questionId,

        @NotBlank(message = "Essay text must not be blank")
        String essayText
) {}
