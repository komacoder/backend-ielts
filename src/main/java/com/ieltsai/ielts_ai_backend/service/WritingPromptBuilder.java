package com.ieltsai.ielts_ai_backend.service;

import com.ieltsai.ielts_ai_backend.entity.TaskType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Constructs optimized, token-efficient prompts for the Gemini API.
 *
 * All prompt text is externalized to application.yml and injected via @Value.
 * This class contains ZERO hardcoded prompt strings — changing the evaluation
 * instructions only requires a YAML edit, not a code change.
 */
@Service
public class WritingPromptBuilder {

    private final String systemInstruction;
    private final String userMessageTemplate;

    public WritingPromptBuilder(
            @Value("${gemini.api.prompt.system-instruction}") String systemInstruction,
            @Value("${gemini.api.prompt.user-message-template}") String userMessageTemplate
    ) {
        this.systemInstruction = systemInstruction;
        this.userMessageTemplate = userMessageTemplate;
    }

    /**
     * Returns the system-level instruction for Gemini.
     * This is static across all task types — the task context is conveyed
     * in the user message instead to keep the system prompt lean.
     */
    public String getSystemInstruction() {
        return systemInstruction;
    }

    /**
     * Builds the user-facing message by filling placeholders in the YAML template.
     *
     * @param essayText      The student's raw essay text.
     * @param topicPrompt    The IELTS topic/prompt text.
     * @param taskType       TASK_1 or TASK_2.
     * @return               The fully assembled user message string.
     */
    public String buildUserMessage(String essayText, String topicPrompt, TaskType taskType) {
        String taskLabel = taskType == TaskType.TASK_1 ? "Task 1" : "Task 2";
        return userMessageTemplate
                .replace("{taskType}", taskLabel)
                .replace("{questionPrompt}", topicPrompt)
                .replace("{essayText}", essayText);
    }
}
