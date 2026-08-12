package com.ieltsai.ielts_ai_backend.dto.writing;

import com.ieltsai.ielts_ai_backend.entity.TaskType;

import java.time.LocalDateTime;

/**
 * Response DTO that represents a question from the question bank shown to users.
 */
public record QuestionResponse(
        Long id,
        String title,
        String promptText,
        TaskType taskType,
        LocalDateTime createdAt
) {}
