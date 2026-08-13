package com.ieltsai.ielts_ai_backend.dto.writing;

import com.ieltsai.ielts_ai_backend.entity.TaskType;

/**
 * Request DTO for admin to update an existing writing topic.
 */
public record UpdateTopicRequest(
        String title,
        String promptText,
        TaskType taskType,
        Boolean isActive
) {}
