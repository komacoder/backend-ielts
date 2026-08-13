package com.ieltsai.ielts_ai_backend.dto.writing;

import com.ieltsai.ielts_ai_backend.entity.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Response DTO that represents a topic from the topic bank shown to users and admins.
 */
public record TopicResponse(
        Long id,
        String title,
        String promptText,
        TaskType taskType,
        boolean isActive,
        LocalDateTime createdAt
) {}
