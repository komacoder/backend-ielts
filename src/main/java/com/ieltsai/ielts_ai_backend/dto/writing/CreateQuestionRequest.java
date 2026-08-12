package com.ieltsai.ielts_ai_backend.dto.writing;

import com.ieltsai.ielts_ai_backend.entity.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for admin to create a new writing question.
 */
public record CreateQuestionRequest(
        @NotBlank(message = "Title must not be blank")
        String title,

        @NotBlank(message = "Prompt text must not be blank")
        String promptText,

        @NotNull(message = "Task type must be specified (TASK_1 or TASK_2)")
        TaskType taskType
) {}
